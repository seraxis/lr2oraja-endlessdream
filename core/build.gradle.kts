import java.nio.file.FileSystems

plugins {
    id("java-library")
    id("application")
    id("com.gradleup.shadow") version "8.3.9"
    id("org.endlessdream.extra.multiplatform-convention")
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    flatDir{
        dirs("../lib")
    }
    maven(url = "https://jitpack.io" )
}

version = libs.versions.beatoraja.get()

sourceSets {
    main {
        java.srcDirs(listOf("src/", "dependencies/jbms-parser/", "dependencies/jbmstable-parser"))
        resources.srcDirs(listOf("src/"))
    }
}

application {
    mainClass.set("bms.player.beatoraja.MainLoader")
}

tasks {
    // fat/uber-jar task provided by https://github.com/GradleUp/shadow
    shadowJar {
        val platformProp = System.getProperty("platform")
        val archProp = System.getProperty("arch")
        val archVariant = when(archProp != null) {
            true -> archProp.plus("-")
            false -> ""
        }
        val classifierPlatform = when(platformProp != null)  {
            true -> platformProp.plus("-").plus(archVariant).plus(libs.versions.endlessdream.get())
            false -> "".plus(libs.versions.endlessdream.get())
        }
        destinationDirectory.set(projectDir.resolveSibling("dist"))
        archiveBaseName.set("lr2oraja")
        archiveClassifier.set(classifierPlatform)
	    mergeServiceFiles()
    }

    // shadow task that extends java `application` plugin JavaExec to cover fatjars
    // used to test builds, does not contain portaudio natives.
    runShadow {
        val runDirProp = System.getProperty("runDir")
        val runDir = when(runDirProp != null)  {
            true -> FileSystems.getDefault().getPath(runDirProp).normalize().toAbsolutePath().toFile()
            false -> projectDir.resolve("../assets")
        }
        workingDir = runDir
    }
}

// versions and bundles defined in ../gradle/libs.versions.toml
dependencies {
    implementation(libs.bundles.libgdx)
    
    implementation(libs.gdx.platform)  {
        artifact {
            classifier = "natives-desktop"
        }
    }
    implementation(libs.gdx.freetype.platform) {
        artifact {
            classifier = "natives-desktop"
        }
    }

    implementation(libs.bundles.imgui)

    implementation(libs.bundles.ffmpeg)

    implementation(libs.bundles.codecs)
    implementation(libs.bundles.jackson)

    implementation(libs.sqlite)
    implementation(libs.commons.compress)
    implementation(libs.commons.dbutils)
    implementation(libs.xz)

    implementation(libs.javadiscord)
    implementation(libs.twitter4j)

    implementation(libs.shapedrawer)
    implementation(libs.guacamole)

    implementation(libs.ebur128java)
    implementation(libs.jna)

    // non-gradle managed file dependencies. jportaudio not on maven. "custom" scares me.
    implementation(":jportaudio")
    implementation(":luaj-jse:3.0.2-custom")
}

plugins {
    id("java-library")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.endlessdream.extra.multiplatform-convention")
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    flatDir{
        dirs("../lib")
    }
}

version = "0.8.6-endlessdream"

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
    // fat/uber-jar task provided by https://github.com/johnrengelman/shadow
    shadowJar {
        destinationDirectory.set(projectDir.resolveSibling("dist"))
        archiveBaseName.set("lr2oraja")
	    mergeServiceFiles()
    }

    // shadow task that extends java `application` plugin JavaExec to cover fatjars
    // used to test builds, does not contain portaudio natives.
    runShadow {
        workingDir = projectDir.resolve("../assets")
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

    implementation(libs.javadiscord)
    implementation(libs.twitter4j)

    // non-gradle managed file dependencies. jportaudio not on maven. "custom" scares me.
    implementation(":jportaudio")
    implementation(":luaj-jse:3.0.2-custom")
}

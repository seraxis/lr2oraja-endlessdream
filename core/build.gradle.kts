import java.io.ByteArrayOutputStream
import java.nio.file.FileSystems
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    id("java-library")
    id("application")
    id("com.gradleup.shadow") version "8.3.9"
    id("org.endlessdream.extra.multiplatform-convention")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
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
        dependsOn("generateBuildMetaInfo")
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

        // Include IR JAR in uberjar
        val runDirProp = System.getProperty("runDir")
        val useIRProp = System.getProperty("useIR")
        when(runDirProp != null
                && gradle.startParameter.taskNames.any() { it.contains("runShadow") }
                && useIRProp.toBoolean()
        ) {
            true -> {
                println("Including IR jars")
                val runDir = FileSystems.getDefault().getPath(runDirProp).normalize().toAbsolutePath().toFile()
                val irJars : FileTree = fileTree(runDir.resolve("./ir")) {
                    include("*.jar")
                }
                from(irJars)
            }
            false -> { println("Skipping IR jar inclusion") }
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

// Generate current build's meta info: git commit hash, commit time, etc
tasks.register("generateBuildMetaInfo") {
    val gitHash: String by lazy {
        try {
            val stdout = ByteArrayOutputStream()
            exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = stdout
            }
            stdout.toString().trim()
        } catch (e: Exception) {
            "unknown"
        }
    }

    val buildTime: String by lazy {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.format(Date())
    }

    var output = file("src/resources/build.properties")
    output.writeText(
        """
            git_commit=${gitHash}
            build_time=${buildTime} 
        """.trimIndent()
    )
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

    /* After version 1.86.11 imgui-java updated their lwjgl3 dependency to 3.3.4
     * this introduced an lwjgl3 bug that causes crashes on Nvidia under Wayland
     * #82. Until such a point that libgdx and imgui-java update their lwjgl3 to
     * 3.3.7 we stay on 3.3.3 to avoid the crash and related issues.
     *
     * See also:
     * https://github.com/libgdx/libgdx/issues/7495
     * https://github.com/libgdx/libgdx/pull/7555
     */
    implementation(libs.bundles.imgui) {
        exclude(group = "org.lwjgl")
    }

    implementation(libs.bundles.ffmpeg)

    implementation(libs.bundles.codecs)
    implementation(libs.bundles.jackson)

    implementation(libs.bundles.jna)

    implementation(libs.sqlite)
    implementation(libs.commons.compress)
    implementation(libs.commons.csv)
    implementation(libs.commons.dbutils)
    implementation(libs.xz)

    implementation(libs.twitter4j)

    implementation(libs.shapedrawer)
    implementation(libs.guacamole)

    implementation(libs.ebur128java)

    implementation(libs.javawebsocket)
    implementation(libs.bundles.slf4j)

    // non-gradle managed file dependencies. jportaudio not on maven. "custom" scares me.
    implementation(":jportaudio")
    implementation(":luaj-jse:3.0.2-custom")
}

import java.nio.file.FileSystems
import java.security.MessageDigest
import java.util.HexFormat

plugins {
    id("java-library")
    id("application")
    id("com.gradleup.shadow") version "9.3.2"
    id("org.endlessdream.extra.multiplatform-convention")
}

val backbeatSdkVersion = "1.0.0"
val backbeatReleaseUrl = "https://github.com/zkldi/backbeat/releases/download/v$backbeatSdkVersion"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    flatDir {
        dirs("../lib")
    }
    maven(url = "https://jitpack.io")
    exclusiveContent {
        forRepository {
            ivy {
                url = uri(backbeatReleaseUrl)
                patternLayout {
                    artifact("[artifact]-[revision](-[classifier]).[ext]")
                }
                metadataSources { artifact() }
            }
        }
        filter { includeGroup("ac.backbeat.release") }
    }
}

version = libs.versions.beatoraja.get()

sourceSets {
    main {
        java.srcDirs("src/", "dependencies/jbms-parser/", "dependencies/jbmstable-parser")
        resources.srcDirs("src/")
    }
    test {
        java.srcDirs("test/")
        resources.srcDirs("test/resources")
    }
}

application {
    mainClass.set("bms.player.beatoraja.MainLoader")
}

tasks {
    jar {
        dependsOn("generateBuildMetaInfo")
    }
    compileTestJava {
        dependsOn("generateBuildMetaInfo")
    }

    // fat/uber-jar task provided by https://github.com/GradleUp/shadow
    shadowJar {
        dependsOn("generateBuildMetaInfo", "test")

        val archProp = System.getProperty("arch")
        val archVariant = archProp?.let { "$it-" } ?: ""
        val platformProp = System.getProperty("platform")
        val endlessDreamVersion = libs.versions.endlessdream.get()
        val classifierPlatform = platformProp?.let { "$it-$archVariant$endlessDreamVersion"} ?: endlessDreamVersion

        destinationDirectory.set(projectDir.resolveSibling("dist"))
        archiveBaseName.set("lr2oraja")
        archiveClassifier.set(classifierPlatform)
        mergeServiceFiles()
    }

    // shadow task that extends java `application` plugin JavaExec to cover fatjars
    // used to test builds, does not contain portaudio natives.
    runShadow {
        val runDirProp = System.getProperty("runDir")
        val runDir = when (runDirProp != null) {
            true -> FileSystems.getDefault().getPath(runDirProp).normalize().toAbsolutePath().toFile()
            false -> projectDir.resolve("../assets")
        }
        val useIRProp = System.getProperty("useIR")
        if (runDirProp != null && useIRProp.toBoolean()) {
            application.applicationDefaultJvmArgs += "-DcustomIRDirectory=$runDir/ir"
        }
        workingDir = runDir
    }
}

tasks.test {
    useJUnitPlatform()
}

val gitHashProvider = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.map { it.trim() }.orElse("unknown")

// Generate current build's meta info: git commit hash, build time, etc
tasks.register("generateBuildMetaInfo") {
    inputs.property("gitHash", gitHashProvider)

    val output = layout.buildDirectory.file("resources/main/resources/build.properties")
    outputs.file(output)

    doLast {
        val outputFile = output.get().asFile
        outputFile.parentFile.mkdirs()
        val gitHash = gitHashProvider.get()
        outputFile.writeText(
            """
            git_commit=${gitHash}
            """.trimIndent()
        )
    }
}

val backbeatClassifier = "${System.getProperty("platform") ?: "windows"}-${System.getProperty("arch") ?: "x86-64"}"
val backbeatNativeSha256 = mapOf(
    "windows-x86-64" to "bde08c0f68ebdf551f32653987d63e1266453b5f756a39f27eb9353db71d37e1",
    "macos-x86-64" to "7c32663530c472e416c1937dc904596802e7324b7c7e27d1d962e7249b5677c1",
    "macos-aarch64" to "cc7297a59330945b41dbeed1dd35189459b906ffbe45afa95b814ad213b6aa9b",
    "linux-x86-64" to "a5822e3c2ab65f2c41553ed921df20384a9dde3c04746dd5ca67a7843f648cdb",
    "linux-aarch64" to "d2184a4cec795e6db36eb9cdf9537a91c349c1ecea02b6fbced31a328a0dbb7f",
)[backbeatClassifier] ?: throw GradleException("Backbeat SDK $backbeatSdkVersion does not support $backbeatClassifier")

// versions and bundles defined in ../gradle/libs.versions.toml
dependencies {
    implementation(libs.bundles.libgdx)

    implementation(libs.gdx.platform) {
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

    implementation("ac.backbeat.release:backbeat-java-sdk:$backbeatSdkVersion")
    runtimeOnly("ac.backbeat.release:backbeat-java-sdk:$backbeatSdkVersion:$backbeatClassifier")

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

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

val backbeatJavaSha256 = "cc7dd59ce53f9107974726ace5e6dce4d3f054cd0d2fbcae2be89d662119a113"

val verifyBackbeatSdk by tasks.registering {
    val expected = mapOf(
        "backbeat-java-sdk-$backbeatSdkVersion.jar" to
            backbeatJavaSha256,
        "backbeat-java-sdk-$backbeatSdkVersion-$backbeatClassifier.jar" to backbeatNativeSha256,
    )
    inputs.files(configurations.named("runtimeClasspath"))

    doLast {
        val artifacts = configurations.getByName("runtimeClasspath").files.associateBy { it.name }
        expected.forEach { (name, expectedSha256) ->
            val artifact = artifacts[name] ?: throw GradleException("Backbeat SDK artifact is missing: $name")
            val actualSha256 = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(artifact.readBytes())
            )
            if (actualSha256 != expectedSha256) {
                throw GradleException(
                    "SHA-256 mismatch for $name: expected $expectedSha256, got $actualSha256"
                )
            }
        }
    }
}

tasks.compileJava {
    dependsOn(verifyBackbeatSdk)
}

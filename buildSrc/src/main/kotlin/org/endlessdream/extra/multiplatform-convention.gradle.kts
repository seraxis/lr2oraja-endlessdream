package org.endlessdream.extra

plugins {
    `java-library`
}

// use `-Pplatform=[platform]` or change this value to set the target platform for the jar
// Available platforms:
//    "windows", "linux", "macos"
var platform = when(hasProperty("platform"))  {
    true -> property("platform").toString()
    false -> "windows"
}

tasks {
    register<Copy>("resolveRuntimeClasspath") {
        from(configurations.runtimeClasspath)
        into(layout.buildDirectory.dir("runtimeClasspath"))
    }
}

configurations.matching {
    it.name.endsWith("runtimeClasspath", ignoreCase = true)
}.configureEach {
    attributes {
        attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(platform))
        attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named("x86-64"))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

configurations.all {
    afterEvaluate {
        if (isCanBeResolved) {
            attributes.attribute(platformModified, true)
        }
    }
}

val artifactType = Attribute.of("artifactType", String::class.java)
val platformModified = Attribute.of("platformModified", Boolean::class.javaObjectType)

dependencies {
    attributesSchema {
        attribute(platformModified)
    }
    artifactTypes.getByName("jar") {
        attributes.attribute(platformModified, false)
    }
    registerTransform(PlatformFilter::class) {
        from.attribute(platformModified, false).attribute(artifactType, "jar")
        to.attribute(platformModified, true).attribute(artifactType, "jar")

        parameters {
            platformString = platform
        }
    }
    components {
        // javacv does not make it easy to exclude these items in gradle, so a componentmetadata rule is as good
        // as it's going to get.
        withModule("org.bytedeco:javacv") {
            allVariants {
                withDependencies {
                    removeAll {
                        it.module.name in listOf(
                                "artoolkitplus",
                                "ffmpeg",
                                "flandmark",
                                "flycapture",
                                "ibfreenect2",
                                "leptonica",
                                "libdc1394",
                                "libfreenect",
                                "libfreenect2",
                                "librealsense",
                                "librealsense2",
                                "openblas",
                                "opencv",
                                "tesseract",
                                "videoinput",
                                "artoolkitplus-platform",
                                "ffmpeg-platform",
                                "flandmark-platform",
                                "flycapture-platform",
                                "ibfreenect2-platform",
                                "leptonica-platform",
                                "libdc1394-platform",
                                "libfreenect-platform",
                                "libfreenect2-platform",
                                "librealsense-platform",
                                "librealsense2-platform",
                                "openblas-platform",
                                "opencv-platform",
                                "tesseract-platform",
                                "videoinput-platform"
                        )
                    }
                }
            }
        }
        withModule<FFmpegRule>("org.bytedeco:javacpp")
        withModule<FFmpegRule>("org.bytedeco:ffmpeg")
    }
    components {

    }
}
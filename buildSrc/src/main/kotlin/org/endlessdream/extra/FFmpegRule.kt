package org.endlessdream.extra

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import javax.inject.Inject


data class JvmNativeVariant(val targetName: String, val os: String, val arch: String)

@CacheableRule
abstract class FFmpegRule : ComponentMetadataRule {
    val jvmNativeVariants: List<JvmNativeVariant> = listOf(
            JvmNativeVariant("linux-arm64", OperatingSystemFamily.LINUX, "aarch64"),
            JvmNativeVariant("linux-arm64", OperatingSystemFamily.LINUX, "arm64-v8"),
            JvmNativeVariant("linux-x86_64", OperatingSystemFamily.LINUX, "x86-64"),
            JvmNativeVariant("macosx-arm64", OperatingSystemFamily.MACOS, "aarch64"),
            JvmNativeVariant("macosx-arm64", OperatingSystemFamily.MACOS, "arm64-v8"),
            JvmNativeVariant("macosx-x86_64", OperatingSystemFamily.MACOS, "x86-64"),
            JvmNativeVariant("windows-x86_64", OperatingSystemFamily.WINDOWS, "x86-64")
    )

    @get:Inject
    abstract val objects: ObjectFactory

    override fun execute(context: ComponentMetadataContext) = context.details.run {
        withVariant("runtime") {
            attributes {
                attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named("none"))
                attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named("none"))
            }
        }
        for ((targetName, os, arch) in jvmNativeVariants) {
            addVariant("$targetName-$arch-runtime", "runtime") {
                attributes {
                    attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(os))
                    attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(arch))
                }
                withFiles {
                    addFile("${id.name}-${id.version}-$targetName.jar")
                }
            }
        }
    }
}
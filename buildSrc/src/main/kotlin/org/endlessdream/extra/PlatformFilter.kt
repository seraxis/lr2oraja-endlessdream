package org.endlessdream.extra

import org.gradle.api.artifacts.transform.*
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import javax.inject.Inject

/*
 * Platform specific Artifact Transform
 *
 * As beatoraja is distributed as a fatjar this artifact transformer tries to remove some of the unnecessary
 * platform specific dependencies to reduce file size. We exclude direct and transitive dependencies after they
 * have been resolved as ComponentMetaData rules can't target transitive dependencies and libgdx upstream has
 * no interest in producing platform variants. By no means comprehensive or idiomatic.
 *
 */

@CacheableTransform
abstract class PlatformFilter : TransformAction<PlatformFilter.Parameters> {

    interface Parameters : TransformParameters {
        @get:Input
        var platformString: String
        @get:Input
        var archString: String
    }

    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputArtifact
    abstract val primaryInput: Provider<FileSystemLocation>

    override
    fun transform(outputs: TransformOutputs) {
        val fileName = primaryInput.get().asFile.name
        // Remove all platform natives for platforms other than the current one
        // hack for macos-arm64
        val lwjglSuffixName = when (parameters.platformString) {
            "macos" -> if (parameters.archString == "aarch64") "macos-arm64" else "macos"
            else -> parameters.platformString
        }
        if (fileName.contains("lwjgl") && fileName.contains("natives")) {
            val nameWithoutExtension = fileName.substring(0, fileName.length - 4)
            if (!nameWithoutExtension.endsWith(lwjglSuffixName)) {
                return
            }
        }
        if (fileName.contains("imgui") && fileName.contains("natives")) {
            if (!fileName.contains(parameters.platformString)) {
                return
            }
        }
        if (fileName.contains("javacpp-1.5.9-windows-x86_64")) return
        if (fileName.contains("javacpp-1.5.9-macos-arm64")) return

        outputs.file(primaryInput)
    }

}
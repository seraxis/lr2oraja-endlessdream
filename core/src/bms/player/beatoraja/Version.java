package bms.player.beatoraja;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Version {
    private static Logger logger = LoggerFactory.getLogger(Version.class);
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 3;
    public static final int VERSION_PATCH = 2;

    public static final BuildType BUILD_TYPE;
    public static final String version;
    public static final String unqualifiedVersion;
    public static final String versionLong;

    public static String getVersion() { return version; }
    public static String getLongVersion() { return versionLong; }

    private static Properties buildMetaInfo = new Properties();

    static {
        BUILD_TYPE = BuildType.PRERELEASE;
        unqualifiedVersion = String.valueOf(VERSION_MAJOR) + '.' + VERSION_MINOR + '.' + VERSION_PATCH;
        version = BUILD_TYPE.prefix + unqualifiedVersion;
        versionLong = "LR2oraja Endless Dream " + (BUILD_TYPE.prefix.isBlank() ? "" : "pre-release ") + unqualifiedVersion;
        tryLoadingBuildMetaInfo();
    }

    private static int[] versionStringToIntArray(String versionString) {
        return Arrays.stream(versionString.split("\\."))
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue)
                .toArray();
    }


    public static int compareToString(String other) {
        if (other == null) { return 1; }
        if (other.isBlank()) { return 1; }
        // Defend against version string that is malformed but long enough to pass the null and blank check
        if (other.length() < 3) { return 1; }

        boolean otherPrerelease;
        int otherMajor;
        int otherMinor;
        int otherPatch;

        // check for pre-release, startsWith also returns true for empty string for some reason
        if (other.substring(0,3).equals("pre")) {
            int[] versionParts = versionStringToIntArray(other.substring(3));
            // If the other version string is malformed (too few parts), this static version trumps it
            if (versionParts.length != 3) { return 1; }
            otherPrerelease = true;
            otherMajor = versionParts[0];
            otherMinor = versionParts[1];
            otherPatch = versionParts[2];
        } else {
            int[] versionParts = versionStringToIntArray(other);
            otherPrerelease = false;
            otherMajor = versionParts[0];
            otherMinor = versionParts[1];
            otherPatch = versionParts[2];
        }

        if (VERSION_MAJOR != otherMajor) { return Integer.compare(VERSION_MAJOR, otherMajor); }
        if (VERSION_MINOR != otherMinor) { return Integer.compare(VERSION_MINOR, otherMinor); }
        if (VERSION_PATCH != otherPatch) { return Integer.compare(VERSION_PATCH, otherPatch); }

        boolean thisPrerelease = !BUILD_TYPE.prefix.isBlank();
        if (thisPrerelease && !otherPrerelease) { return -1; }
        if (!thisPrerelease && otherPrerelease) { return 1; }

        return 0;
    }

    /**
     * Get current build's git commit hash
     * @return commit hash or "unknown" if anything went wrong
     */
    public static String getGitCommitHash() {
        return buildMetaInfo.getProperty("git_commit");
    }

    /**
     * Get the build time of the current build
     * @return a date represents when it's being built, or null if anything went wrong
     */
    public static Date getBuildDate() {
        try {
            String buildDate = buildMetaInfo.getProperty("build_time");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(buildDate);
        } catch (Exception e) {
			logger.error("Failed to parse build time: {}", e.getMessage());
            return null;
        }
    }

    public enum BuildType {
        PRERELEASE("pre"),
        STABLE("");

        public final String prefix;

        BuildType(String prefix) {
            this.prefix = prefix;
        }
    }

    /**
     * Try loading the build meta data, no exceptions would be thrown
     */
    private static void tryLoadingBuildMetaInfo() {
        try {
            buildMetaInfo.load(Version.class.getClassLoader().getResourceAsStream("resources/build.properties"));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to load build meta info");
        }
    }
}

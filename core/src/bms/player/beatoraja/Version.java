package bms.player.beatoraja;

import java.util.Arrays;

public class Version {
    // TODO: Parse current commit from ~somewhere~. Have to either check manifest of JAR or inject at gradle fatJar stage
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 3;
    public static final int VERSION_PATCH = 1;

    public static final BuildType BUILD_TYPE;
    public static final String version;
    public static final String unqualifiedVersion;
    public static final String versionLong;

    public static String getVersion() { return version; }
    public static String getLongVersion() { return versionLong; }

    static {
        BUILD_TYPE = BuildType.PRERELEASE;
        unqualifiedVersion = String.valueOf(VERSION_MAJOR) + '.' + VERSION_MINOR + '.' + VERSION_PATCH;
        version = BUILD_TYPE.prefix + unqualifiedVersion;
        versionLong = "LR2oraja Endless Dream " + (BUILD_TYPE.prefix.isBlank() ? "" : "pre-release ") + unqualifiedVersion;
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

    public enum BuildType {
        PRERELEASE("pre"),
        STABLE("");

        public final String prefix;

        BuildType(String prefix) {
            this.prefix = prefix;
        }
    }
}

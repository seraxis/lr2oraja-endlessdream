package bms.player.beatoraja.modmenu.fm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * NOTE: FolderManager can only manage folders which sql is based on favorite field
 * Expect some low-level functions, you should never use FOLDER_DEFINITIONS directly
 * or try to modify the result from getFolderDefinitions()
 */
public class FolderManager {
    private static final String FILE_LOCATION = "folder/default.json";
    private static List<FolderDefinition> FOLDER_DEFINITIONS = new ArrayList<>();

    //@formatter:off
    static {
        try {
            ObjectMapper om = new ObjectMapper();
            List<FolderDefinition> fds = om.readValue(new BufferedInputStream(Files.newInputStream(Paths.get(FILE_LOCATION))), new TypeReference<>(){});
            // Hack: extract the bits field from sql for not breaking the compatibility
            fds.forEach(fd -> {
                fd.tryExtractBitsFromSql();
                FOLDER_DEFINITIONS.add(fd);
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //@formatter:on

    // NOTE: Here's a limitation from 'favorite' length (26+4=30 <= 31)
    private static final int MAXIMUM_FOLDER_COUNT = 5;

    public static List<FolderDefinition> getFolderDefinitions() {
        return FOLDER_DEFINITIONS.stream().filter(fd -> fd.getBits() != null).toList();
    }

    /**
     * Persist fds to folder/default.json
     */
    public static void persist(List<FolderDefinition> fds) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.writeValue(new BufferedOutputStream(Files.newOutputStream(Paths.get(FILE_LOCATION))), fds);
    }

    /**
     * Save one folder definition<br>
     * <li>Update FOLDER_DEFINITIONS</li>
     * <li>Persist current data to disk</li>
     *
     * @implSpec If persist failed, FOLDER_DEFINITIONS must keep the same
     */
    private static void save(FolderDefinition folderDefinition) throws IOException {
        try {
            // Copy entire list for persisting
            List<FolderDefinition> copy = new ArrayList<>(FOLDER_DEFINITIONS);
            copy.add(folderDefinition);
            persist(copy);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        FOLDER_DEFINITIONS.add(folderDefinition);
    }

    private static void remove(int bits) throws IOException {
        Predicate<FolderDefinition> equalsOnBits = fd -> fd.getBits() != null && fd.getBits().equals(bits);
        try {
            List<FolderDefinition> copy = new ArrayList<>(FOLDER_DEFINITIONS);
            if (!copy.removeIf(equalsOnBits)) {
                return ; // Okay dokey
            }
            persist(copy);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        FOLDER_DEFINITIONS.removeIf(equalsOnBits);
    }

    /**
     * Create a new folder and persist state to disk
     *
     * @param name folder name
     * @throws IllegalStateException if there is no room for folder or something bad happens
     */
    public static void newFolder(String name) throws IllegalStateException {
        if (getFolderDefinitions().size() >= MAXIMUM_FOLDER_COUNT) {
            throw new IllegalStateException("Failed to save changes: folder count cannot exceed to " + MAXIMUM_FOLDER_COUNT);
        }
        List<Integer> usedBits = getFolderDefinitions().stream().map(FolderDefinition::getBits).toList();
        int nextBit = findMex(usedBits);
        FolderDefinition fd = new FolderDefinition(generateSql(nextBit), name, false, nextBit);
        try {
            save(fd);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save changes: " + e.getMessage());
        }
    }

    /**
     * Remove specified folder and persist current state to disk
     *
     * @throws IllegalStateException if the folder is not exist or something bad happens
     */
    public static void removeFolder(int bits) throws IllegalStateException {
        try {
            remove(bits);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save changes: " + e.getMessage());
        }
    }

    /**
     * Get the mex from an array of integers
     * MEX: The minimum integer x satisfy "x >= 0 and x doesn't appear in arr"
     *
     * @return MEX of arr
     */
    private static int findMex(List<Integer> arr) {
        int mex = 0;
        // No need for sort
        while (true) {
            boolean noProgress = true;
            for (Integer x : arr) {
                if (x.equals(mex)) {
                    noProgress = false;
                    mex++;
                    break;
                }
            }
            if (noProgress) {
                break;
            }
        }
        return mex;
    }

    /**
     * @return `favorite & {bits} != 0`
     */
    private static String generateSql(Integer bits) {
        return String.format("favorite & %d != 0", 1 << bits);
    }
}

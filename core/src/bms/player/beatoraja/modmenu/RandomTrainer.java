package bms.player.beatoraja.modmenu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomTrainer {
    private static final Logger logger = LoggerFactory.getLogger(RandomTrainer.class);
    private static String laneOrder = "1234567";
    private static ArrayList<Character> lanesToRandom = new ArrayList<>();

    private static boolean blackWhitePermute;
    private static boolean active;

    private static final ArrayList<Boolean> laneMask = new ArrayList<>(Arrays.asList(false, false, false, false, false, false, false));

    private static HashMap<Integer, Long> randomSeedMap;

    private static final ArrayDeque<RandomHistoryEntry> laneOrderHistory = new ArrayDeque<RandomHistoryEntry>();

    public RandomTrainer() {
        if (randomSeedMap == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                ObjectInputStream ois = new ObjectInputStream(cl.getResourceAsStream("resources/randomtrainer.dat"));

                randomSeedMap = (HashMap<Integer, Long>) ois.readObject();
                ois.close();
            } catch (ClassNotFoundException | NullPointerException | IOException ex) {
                logger.error("RandomTrainer: randomtrainer.dat corrupted or missing from jar");
                ex.printStackTrace();
            }
        }
    }

    public static String getLaneOrder() {
        if (blackWhitePermute) {
            List<Character> black = new ArrayList<Character>();
            List<Character> white = new ArrayList<Character>();
            laneOrder.chars()
                    .mapToObj(Character::toChars)
                    .map(cs -> cs[0])
                    .forEach(c -> {
                        if ((Character.getNumericValue(c) % 2) == 0) {
                            black.add(c);
                        } else {
                            white.add(c);
                        }
                    });
            Collections.shuffle(black);
            Collections.shuffle(white);

            StringBuilder newLaneOrder = new StringBuilder(laneOrder);
            for (int i = 0; i < laneOrder.length(); i++) {
                char current = laneOrder.charAt(i);
                if ((Character.getNumericValue(current) % 2) == 0) {
                    newLaneOrder.setCharAt(i, black.remove(0));
                } else {
                    newLaneOrder.setCharAt(i, white.remove(0));
                }
            }
            laneOrder = newLaneOrder.toString();
        }

        ArrayList<Character> shuffledLanes = new ArrayList<>(lanesToRandom);
        Collections.shuffle(shuffledLanes);
        StringBuilder newLaneOrder = new StringBuilder(laneOrder);
        for (int i = 0; i < laneOrder.length(); i++) {
            if (lanesToRandom.contains(laneOrder.charAt(i))) {
                newLaneOrder.setCharAt(i, shuffledLanes.remove(0));
            }
        }
        laneOrder = newLaneOrder.toString();
        return laneOrder;
    }

    public static boolean isLaneToRandom(char lane) {return lanesToRandom.contains(lane);}

    public static void setLaneToRandom(char lane) {lanesToRandom.add(lane);}

    public static void removeLaneToRandom(char lane) {lanesToRandom.remove(lanesToRandom.indexOf(lane));}

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean active) {
        RandomTrainer.active = active;
    }

    public static HashMap<Integer, Long> getRandomSeedMap() {
        return randomSeedMap;
    }

    public static void setBlackWhitePermute(boolean blackWhitePermute) {
        RandomTrainer.blackWhitePermute = blackWhitePermute;
    }


    public static void setLaneOrder(String number) {
        laneOrder = number;
    }

    public static ArrayDeque<RandomHistoryEntry> getRandomHistory() {
        return laneOrderHistory;
    }

    public static void addRandomHistory(RandomHistoryEntry histEntry) {
        laneOrderHistory.addFirst(histEntry);
    }


    public class RandomHistoryEntry {
        private String title;

        private String random;

        public RandomHistoryEntry(String title, String random) {
            this.title = title;
            this.random = random;
        }

        public String getTitle() {
            return title;
        }

        public String getRandom() {
            return random;
        }
    }
}

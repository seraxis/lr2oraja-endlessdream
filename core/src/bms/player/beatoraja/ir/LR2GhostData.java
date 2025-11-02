package bms.player.beatoraja.ir;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import bms.player.beatoraja.pattern.Random;
import bms.player.beatoraja.pattern.LR2Random;
import bms.player.beatoraja.modmenu.ImGuiNotify;

// external CSV library for parsing LR2IR responses; do not use for LR2 skin code
import org.apache.commons.csv.*;

public class LR2GhostData {
    private Random random;
    private int seed;
    private int laneOrder;
    private int[] judgements;
    private int pgreat;
    private int great;
    private int good;
    private int bad;
    private int poor;

    private LR2GhostData(Random random, int seed, int lanes, int[] judgements,
                         int pgreat, int great, int good, int bad, int poor) {
        this.random = random;
        this.seed = seed;
        this.laneOrder = lanes;
        this.judgements = judgements;
        this.pgreat = pgreat;
        this.great = great;
        this.good = good;
        this.bad = bad;
        this.poor = poor;
    }

    public Random getRandom() { return random; }
    public int getLaneOrder() { return laneOrder; }
    public int getSeed() { return seed; }
    public int[] getJudgements() { return judgements; }
    public int getPgreat() { return pgreat; }
    public int getGreat() { return great; }
    public int getGood() { return good; }
    public int getBad() { return bad; }
    public int getPoor() { return poor; }

    public static LR2GhostData parse(String ghostCsv) {
        CSVFormat format =
            CSVFormat.DEFAULT.builder().setHeader("name", "options", "seed", "ghost").get();
        CSVParser parser;
        try {
            parser = CSVParser.parse(ghostCsv, format);
        }
        catch (Exception e) {
            ImGuiNotify.error(String.format("Could not parse ghost data response from LR2IR."));
            return null;
        }
        List<CSVRecord> list = parser.getRecords();
        if (list.isEmpty()) {
            ImGuiNotify.error(String.format("LR2IR returned empty response."));
            return null;
        }
        CSVRecord data = list.get(0);

        // option field is a 4-digit decimal that encodes options
        // starting with least significant digit: gauge, random 1, random 2, dpflip
        int options = Integer.parseInt(data.get("options"));
        // random: 0 nonrand, 1 mirror, 2 random, 3 sran, 4 hran, 5 converge
        int random = (options / 10) % 10;
        // for now, we only support mirror and random, and only SP
        if (3 <= random) {
            ImGuiNotify.warning("Unsupported random option: " + random);
            return null;
        }

        Random randomOption = switch (random) {
            default -> Random.IDENTITY;
            case 1 -> Random.MIRROR;
            case 2 -> Random.RANDOM;
        };

        // generate a proper lane ordering from the given seed
        int seed = Integer.parseInt(data.get("seed"));
        LR2Random rng = new LR2Random(seed);
        int targets[] = {0, 1, 2, 3, 4, 5, 6, 7};
        for (int lane = 1; lane < 7; ++lane) {
            int swap = lane + rng.nextInt(7 - lane + 1);
            int tmp = targets[lane];
            targets[lane] = targets[swap];
            targets[swap] = tmp;
        }
        int lanes[] = {0, 1, 2, 3, 4, 5, 6, 7};
        for (int i = 1; i < 8; ++i) { lanes[targets[i]] = i; }
        // we store the lane order as a decimal where the digits
        // encode the lanes left-to-right, and most to least significant
        int encodedLanes = 0;
        for (int i = 1; i < 8; ++i) { encodedLanes = encodedLanes * 10 + lanes[i]; }

        int[] judgements = decodePlayGhost(data.get("ghost"));

        int pgreat = 0;
        int great = 0;
        int good = 0;
        int bad = 0;
        int poor = 0;
        for (int judge : judgements) {
            switch (judge) {
            case 0 -> pgreat++;
            case 1 -> great++;
            case 2 -> good++;
            case 3 -> bad++;
            default -> poor++;
            };
        }

        return new LR2GhostData(randomOption, seed, encodedLanes, judgements,
                                pgreat, great, good, bad, poor);
    }

    public static int[] decodePlayGhost(String data) {
        data = data.replace("q", "XX");
        data = data.replace("r", "X1");
        data = data.replace("s", "X2");
        data = data.replace("t", "X3");
        data = data.replace("u", "X4");
        data = data.replace("v", "X5");
        data = data.replace("w", "X6");
        data = data.replace("x", "X7");
        data = data.replace("y", "X8");
        data = data.replace("z", "X9");

        data = data.replace("F", "E1");
        data = data.replace("G", "E2");
        data = data.replace("H", "E3");
        data = data.replace("I", "E4");
        data = data.replace("J", "E5");
        data = data.replace("K", "E6");
        data = data.replace("L", "E7");
        data = data.replace("M", "E8");
        data = data.replace("N", "E9");
        data = data.replace("P", "EC");
        data = data.replace("Q", "EB");
        data = data.replace("R", "EA");
        data = data.replace("S", "D2");
        data = data.replace("T", "D3");
        data = data.replace("U", "D4");
        data = data.replace("V", "D5");
        data = data.replace("W", "D6");
        data = data.replace("X", "DE");
        data = data.replace("Y", "DC");
        data = data.replace("a", "DB");
        data = data.replace("b", "DA");
        data = data.replace("c", "C2");
        data = data.replace("d", "C3");
        data = data.replace("e", "C4");
        data = data.replace("f", "C5");
        data = data.replace("g", "CE");
        data = data.replace("h", "CD");
        data = data.replace("i", "CB");
        data = data.replace("j", "CA");
        data = data.replace("k", "AB");
        data = data.replace("l", "AC");
        data = data.replace("m", "AD");
        data = data.replace("n", "AE");
        data = data.replace("o", "A2");
        data = data.replace("p", "A3");

        // guard character to slightly simplify the loop
        // (the ghost data already seems to have guards,
        //  so this is just to avoid relying on them)
        data = data + "?";

        // after all the substitutions in the first part of this function,
        // the ghost description is now a simple run-length encoded sequence
        // of judgements - for example, ED3CE2 translates to EDDDCEE
        // the following loop performs this decoding
        List<Character> notes = new ArrayList<>();
        int runLength = 0;
        char currentCharacter = 0;
        for (char next : data.toCharArray()) {
            if (next == '?') {
                if (currentCharacter != 0) {
                    if (runLength == 0) { runLength = 1; }
                    List<Character> run = Collections.nCopies(runLength, currentCharacter);
                    notes.addAll(run);
                }
                break;
            }
            else if ('0' <= next && next <= '9') { runLength = runLength * 10 + (next - '0'); }
            else if ('@' <= next && next <= 'E') {
                if (currentCharacter == 0) {
                    currentCharacter = next;
                    runLength = 0;
                }
                else {
                    if (runLength == 0) { runLength = 1; }
                    List<Character> run = Collections.nCopies(runLength, currentCharacter);
                    notes.addAll(run);
                    currentCharacter = next;
                    runLength = 0;
                }
            }
            else {
                // we do ignore some characters
            }
        }

        int extra = 0;
        for (int i = 0; i < notes.size(); ++i) {
            if ('@' == notes.get(i)) extra++;
        }

        int noteCount = notes.size() - extra;
        int[] ghost = new int[noteCount];
        int n = 0;
        for (int i = 0; i < notes.size(); ++i) {
            switch (notes.get(i)) {
            case 'E' -> ghost[n++] = 0; // pgreat
            case 'D' -> ghost[n++] = 1; // great
            case 'C' -> ghost[n++] = 2; // good
            case 'B' -> ghost[n++] = 3; // bad
            case 'A' -> ghost[n++] = 4; // poor
            // mash poors
            // case '@' -> ghost[n++] = 5;
            // default -> ghost[n++] = 6;
            default -> { continue; }
            }
        }

        return ghost;
    }
}

package bms.player.beatoraja.arena.lobby;

import bms.model.Mode;
import bms.player.beatoraja.arena.client.Client;
import bms.player.beatoraja.arena.enums.Gauge;
import bms.player.beatoraja.arena.network.Peer;
import bms.player.beatoraja.pattern.Random;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.flag.*;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GraphMenu {
    enum SortType {
        Score,
        BP,
        MaxCombo
    }

    private static final float graphWidth = 150;
    private static final float graphHeight = 400;
    private static SortType sortType = SortType.Score;

    public static void show(ImBoolean showGraphMenu) {
        ImGui.begin("Graph", showGraphMenu, ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoBringToFrontOnFocus);
        {
            // TODO: items::Render();
            if (Client.connected.get() && !Client.state.getPeers().isEmpty()) {
                List<Peer> sortedPeers = Client.state.getPeers().values().stream().sorted((lhs, rhs) -> switch (sortType) {
                    case Score -> rhs.getExScore() - lhs.getExScore();
                    case BP -> rhs.getBP() - lhs.getBP();
                    case MaxCombo -> rhs.getMaxCombo() - lhs.getMaxCombo();
                }).toList();

                ImGui.setNextWindowSizeConstraints(0, 0, graphWidth * 2.0f, graphHeight * 1.5f);
                // TODO: We don't have auto resize here
                ImGui.beginChild("GraphDisp", 0, graphHeight, true, ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.HorizontalScrollbar);
                {
                    List<Integer> values = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    List<Double> positions = new ArrayList<>();
                    if (ImGui.beginTabBar("##TabsGraph")) {
                        if (ImGui.beginTabItem("Score")) {
                            sortType = SortType.Score;
                            prepareData(values, labels, positions, sortedPeers, sortType);
                            displayGraph(values, labels, positions, sortedPeers, sortType);
                            ImGui.endTabItem();
                        }
                        if (ImGui.beginTabItem("BP")) {
                            sortType = SortType.BP;
                            prepareData(values, labels, positions, sortedPeers, sortType);
                            displayGraph(values, labels, positions, sortedPeers, sortType);
                            ImGui.endTabItem();
                        }
                        if (ImGui.beginTabItem("Max Combo")) {
                            sortType = SortType.MaxCombo;
                            prepareData(values, labels, positions, sortedPeers, sortType);
                            displayGraph(values, labels, positions, sortedPeers, sortType);
                            ImGui.endTabItem();
                        }
                        ImGui.endTabBar();
                    }
                    ImGui.endChild();
                }

                ImGui.sameLine();

                ImGui.beginChild("ScoreDetails", graphWidth + 40, graphHeight, true, ImGuiWindowFlags.AlwaysAutoResize);
                {
                    int entryIdx = 0;
                    for (Peer value : sortedPeers) {
                        ImGui.bulletText(String.format("%s: ", value.getUserName()));
                        ImGui.sameLine();
                        ImVec2 oldCursorPos = ImGui.getCursorPos();
                        ImVec2 textStand = ImGui.calcTextSize("9999 (100.00%)");
                        ImGui.dummy(textStand.x, textStand.y);
                        ImGui.setCursorPos(oldCursorPos.x, oldCursorPos.y);
                        ImGui.text(String.format("%d (%.2f%%)", value.getExScore(), value.getRate()));

                        String opt = Random.getRandom(value.getOption(), Mode.BEAT_7K).name();
                        String gauge = Gauge.from(value.getGauge()).getName();

                        String detail = String.format("[%s][%s]", gauge, opt);

                        float windowWidth = ImGui.getWindowSizeX();
                        float textWidth = ImGui.calcTextSize(detail).x;
                        ImGui.setCursorPosX((windowWidth - textWidth) * 0.5f);
                        ImGui.textDisabled(detail);

                        if (ImGui.beginTable(String.format("ScoreTable##%d", entryIdx), 5)) {
                            ImGui.tableNextRow();
                            ImGui.tableNextColumn();
                            ImGui.text("PG");
                            ImGui.tableNextColumn();
                            ImGui.text("GR");
                            ImGui.tableNextColumn();
                            ImGui.text("GD");
                            ImGui.tableNextColumn();
                            ImGui.text("BD");
                            ImGui.tableNextColumn();
                            ImGui.text("PR  ");

                            ImVec2 countSize = ImGui.calcTextSize("9999");
                            ImGui.tableNextRow();
                            ImGui.tableNextColumn();
                            oldCursorPos = ImGui.getCursorPos();
                            ImGui.dummy(countSize.x, countSize.y);
                            ImGui.setCursorPos(oldCursorPos.x, oldCursorPos.y);
                            ImGui.textColored(0.765f, 0.976f, 0.824f, 1.0f, String.valueOf(value.getScore().getpGreat()));
                            ImGui.tableNextColumn();
                            oldCursorPos = ImGui.getCursorPos();
                            ImGui.dummy(countSize.x, countSize.y);
                            ImGui.setCursorPos(oldCursorPos.x, oldCursorPos.y);
                            ImGui.textColored(1, 0.824f, 0, 1.0f, String.valueOf(value.getScore().getGreat()));
                            ImGui.tableNextColumn();
                            oldCursorPos = ImGui.getCursorPos();
                            ImGui.dummy(countSize.x, countSize.y);
                            ImGui.setCursorPos(oldCursorPos.x, oldCursorPos.y);
                            ImGui.textColored(1, 0.659f, 0, 1.0f, String.valueOf(value.getScore().getGood()));
                            ImGui.tableNextColumn();
                            oldCursorPos = ImGui.getCursorPos();
                            ImGui.dummy(countSize.x, countSize.y);
                            ImGui.setCursorPos(oldCursorPos.x, oldCursorPos.y);
                            ImGui.textColored(1, 0.412f, 0, 1.0f, String.valueOf(value.getScore().getBad()));
                            ImGui.tableNextColumn();
                            oldCursorPos = ImGui.getCursorPos();
                            ImGui.dummy(countSize.x, countSize.y);
                            ImGui.setCursorPos(oldCursorPos.x, oldCursorPos.y);
                            ImGui.textColored(1, 0.129f, 0, 1.0f, String.valueOf(value.getScore().getPoor()));

                            ImGui.endTable();
                        }
                        entryIdx++;
                    }
                    ImGui.endChild();
                }
            } else{
                ImGui.text("Not connected to any server...");
            }
            ImGui.end();
        }
    }

    private static void prepareData(List<Integer> values, List<String> labels, List<Double> positions, List<Peer> peers, SortType sortType) {
        int i = 0;

        for (Peer peer : peers) {
            for (int j = 0; j < i; ++j) {
                values.add(0);
            }
            switch (sortType) {
                case Score -> values.add(peer.getExScore());
                case BP -> values.add(peer.getBP());
                case MaxCombo -> values.add(peer.getMaxCombo());
            }
            for (int j = i + 1; j < peers.size(); ++j) {
                values.add(0);
            }
            labels.add(peer.getUserName());
            positions.add(i * 1.0);
            i++;
        }
    }

    private static void displayGraph(List<Integer> values, List<String> labels, List<Double> positions, List<Peer> peers, SortType sortType) {
        float adjGraphWidth = graphWidth;
        float adjGraphHeight = graphHeight - 40; // TODO: This probably could be replace with child window flags, but we don't have it currently
        if (Client.state.getPeers().size() > 2) {
            adjGraphWidth *= (float) (Client.state.getPeers().size() * 0.5);
        }

        String yLabel = switch (sortType) {
            case Score -> "Score";
            case BP -> "BP";
            case MaxCombo -> "Max Combo";
        };

        if (ImPlot.beginPlot("##GraphPlot", new ImVec2(adjGraphWidth, adjGraphHeight), ImPlotFlags.NoFrame | ImPlotFlags.NoInputs | ImPlotFlags.NoTitle | ImPlotFlags.NoLegend)) {
            ImPlot.setupAxes("Players", yLabel, ImPlotAxisFlags.AutoFit | ImPlotAxisFlags.NoLabel, ImPlotAxisFlags.AutoFit | ImPlotAxisFlags.NoLabel);
            double[] positionsArray = new double[positions.size()];
            for (int i = 0; i < positions.size(); ++i) {
                positionsArray[i] = positions.get(i);
            }
            String[] labelsArray = labels.toArray(new String[0]);
            ImPlot.setupAxisTicks(ImPlotAxis.X1, positionsArray, labels.size(), labelsArray);
            if (sortType == SortType.Score) {
                double[] rankPos = new double[]{
                        1, 2, 3, 4
                };
                Optional<Integer> maxScore = Client.state.getMaxScore();
                maxScore.ifPresent(limit -> {
                    rankPos[0] = Math.ceil(limit * 0.666);
                    rankPos[1] = Math.ceil(limit * 0.777);
                    rankPos[2] = Math.ceil(limit * 0.888);
                    rankPos[3] = limit;
                });
                String[] rankLabels = new String[]{"A", "AA", "AAA", "MAX"};
                ImPlot.setupAxisTicks(ImPlotAxis.Y1, rankPos, rankPos.length, rankLabels);
                ImPlot.setupAxisLimits(ImPlotAxis.Y1, 0, maxScore.orElse(4), ImPlotCond.Always);
            } else if (sortType == SortType.MaxCombo) {
                int maxCombo = Client.state.getMaxCombo().orElse(1);
                ImPlot.setupAxisLimits(ImPlotAxis.Y1, 0, maxCombo, ImPlotCond.Always);
            }
            int[] valuesArray = new int[values.size()];
            for (int i = 0; i < valuesArray.length; ++i) {
                valuesArray[i] = values.get(i);
            }
            // TODO: plotBarGroups is crashing everytime, really no idea
            ImPlot.plotBarGroupsV(labelsArray, valuesArray, labels.size(), labels.size(), 0.67f, 0, ImPlotBarGroupsFlags.Stacked);
            ImPlot.endPlot();
        }
    }
}

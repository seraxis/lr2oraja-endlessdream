package bms.player.beatoraja.modmenu;

import bms.tool.mdprocessor.DownloadTask;
import bms.tool.mdprocessor.HttpDownloadProcessor;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class DownloadTaskState {
    public static Map<Integer, DownloadTask> runningDownloadTasks =
        new HashMap<Integer, DownloadTask>();
    public static final Set<Integer> expiredTasks = new HashSet<Integer>();

    private static HttpDownloadProcessor httpDownloadProcessor;

    public static void initialize(HttpDownloadProcessor httpDownloadProcessor) {
        DownloadTaskState.httpDownloadProcessor = httpDownloadProcessor;
        lastSnapshot = System.nanoTime();
    }

    private static long lastSnapshot = 0;

    public static void update() {
        long now = System.nanoTime();
        // no reason to check very often (1s)
        if ((now - lastSnapshot) < 1000000000L) {
            return;
        }
        lastSnapshot = now;

        Map<Integer, DownloadTask> tasks = httpDownloadProcessor.getAllTasks();
        if (tasks.size() == expiredTasks.size()) {
            return;
        }

        for (var taskEntry : tasks.entrySet()) {
            int id = taskEntry.getKey();
            DownloadTask task = taskEntry.getValue();
            if (expiredTasks.contains(id)) {
                continue;
            }


            DownloadTask.DownloadTaskStatus state = task.getDownloadTaskStatus();
            boolean finished = task.getDownloadTaskStatus().getValue() >=
                               DownloadTask.DownloadTaskStatus.Extracted.getValue();
            boolean expired = finished && (5000000000L < now - task.getTimeFinished());

            if (expired) {
                if (runningDownloadTasks.containsKey(id)) {
                    runningDownloadTasks.remove(id);
                }
                expiredTasks.add(id);
            }
            else {
                runningDownloadTasks.put(id, task);
            }
        }
    }
}

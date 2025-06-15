package com.ictu.pushnotificationapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class SchedulerService {
    private static final Logger LOGGER = Logger.getLogger(SchedulerService.class.getName());
    private static SchedulerService instance;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private SchedulerService() {}

    public static synchronized SchedulerService getInstance() {
        if (instance == null) instance = new SchedulerService();
        return instance;
    }

    public void startService() {
        executorService.scheduleAtFixedRate(this::checkAndDispatchTasks, 10, 60, TimeUnit.SECONDS);
    }
    
    private void checkAndDispatchTasks() {
        List<JSONObject> tasks = DatabaseManager.getPendingScheduledSends();
        if (tasks.isEmpty()) return;

        for (JSONObject task : tasks) {
            int taskId = task.getInt("schedule_id");
            try {
                UserSession.startSession(task.getInt("user_id"), "SchedulerTask");
                
                String recipientType = task.getString("recipient_type");
                String recipientName = task.getString("recipient_name");
                List<JSONObject> recipientList;

                if ("Group".equals(recipientType)) {
                    recipientList = DatabaseManager.getContactsByGroup(recipientName);
                } else {
                    recipientList = new ArrayList<>();
                    // Ce cas est plus simple si on stocke le JSON des contacts
                    // Pour l'instant, on suppose que l'envoi programmé est par groupe
                }
                
                new NotificationWorker(
                    task.getString("send_type"),
                    recipientList,
                    recipientName,
                    task.getString("subject"),
                    task.getString("message"),
                    task.getString("attachment_path"),
                    null
                ).execute();
                
                DatabaseManager.updateScheduledSendStatus(taskId, "Déclenché");
            } catch (Exception e) {
                DatabaseManager.updateScheduledSendStatus(taskId, "Échec");
            } finally {
                UserSession.endSession();
            }
        }
    }

    public void stopService() {
        executorService.shutdown();
    }
}

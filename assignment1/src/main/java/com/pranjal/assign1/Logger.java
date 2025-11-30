package com.pranjal.assign1;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe file logger for persistent event recording.
 * 
 * Writes all events to execution_history.log with timestamps.
 * Format: [HH:mm:ss.SSS] [Component] : Message
 * 
 * @author Pranjal
 * @version 2.1
 */
public class Logger {

    private static PrintWriter writer;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /**
     * Initializes logger and creates log file in overwrite mode.
     */
    public static void init() {
        try {
            // Check if we're already in assignment1 directory
            String currentDir = System.getProperty("user.dir");
            String logPath = currentDir.endsWith("assignment1") ? 
                "execution_history.log" : "assignment1/execution_history.log";
            
            writer = new PrintWriter(new FileWriter(logPath, false));
            log("SYSTEM", "Logger Initialized. Recording events...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs a message with timestamp and component name.
     * Thread-safe via synchronized keyword.
     * Flushes immediately to prevent data loss on crash.
     * 
     * @param component name of component generating log
     * @param message log message
     */
    public static synchronized void log(String component, String message) {
        if (writer != null) {
            String timestamp = dtf.format(LocalDateTime.now());
            writer.printf("[%s] [%-15s] : %s%n", timestamp, component, message);
            writer.flush(); // Immediate flush for crash resistance
        }
    }

    /**
     * Closes logger and releases file resources.
     * Should be called in finally block at shutdown.
     */
    public static void close() {
        if (writer != null) {
            log("SYSTEM", "System Shutdown. Closing logs.");
            writer.close();
        }
    }
}

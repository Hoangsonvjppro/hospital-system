package com.hospital.util;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AsyncTask {

    private static final Logger LOGGER = Logger.getLogger(AsyncTask.class.getName());

    private AsyncTask() {}


    public static <T> void run(Supplier<T> supplier, Consumer<T> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return supplier.get();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    onSuccess.accept(result);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (onError != null) {
                        onError.accept(cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
                    } else {
                        LOGGER.log(Level.SEVERE, "AsyncTask failed", cause);
                    }
                }
            }
        }.execute();
    }

    public static <T> void run(Supplier<T> supplier, Consumer<T> onSuccess) {
        run(supplier, onSuccess, null);
    }


    public static void runVoid(Runnable task, Runnable onDone, Consumer<Exception> onError) {
        run(() -> { task.run(); return null; },
            ignored -> { if (onDone != null) onDone.run(); },
            onError);
    }
}

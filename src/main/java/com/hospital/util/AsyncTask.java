package com.hospital.util;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiện ích chạy tác vụ nền (DB query, business logic nặng) ngoài EDT,
 * rồi callback kết quả trên EDT — tránh đóng băng giao diện.
 *
 * <pre>
 * AsyncTask.run(
 *     () -> invoiceBUS.findAll(),    // background
 *     list -> refreshTable(list),    // on EDT with result
 *     ex -> showError(ex)            // on EDT with error
 * );
 * </pre>
 */
public final class AsyncTask {

    private static final Logger LOGGER = Logger.getLogger(AsyncTask.class.getName());

    private AsyncTask() {}

    /**
     * Chạy supplier trên background thread, gọi onSuccess trên EDT khi xong,
     * hoặc onError trên EDT nếu có exception.
     *
     * @param <T>       Kiểu kết quả
     * @param supplier  Tác vụ nền (không gọi trên EDT)
     * @param onSuccess Callback nhận kết quả, chạy trên EDT
     * @param onError   Callback nhận exception, chạy trên EDT (nullable — sẽ log nếu null)
     */
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

    /**
     * Overload without error handler — errors are logged.
     */
    public static <T> void run(Supplier<T> supplier, Consumer<T> onSuccess) {
        run(supplier, onSuccess, null);
    }

    /**
     * Chạy tác vụ void (không trả kết quả) trên background thread,
     * gọi onDone trên EDT khi hoàn thành.
     */
    public static void runVoid(Runnable task, Runnable onDone, Consumer<Exception> onError) {
        run(() -> { task.run(); return null; },
            ignored -> { if (onDone != null) onDone.run(); },
            onError);
    }
}

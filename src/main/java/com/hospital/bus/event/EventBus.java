package com.hospital.bus.event;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Event Bus — Publish/Subscribe pattern cho giao tiếp giữa các module.
 * <p>
 * Tất cả event được publish trên EDT (Swing Event Dispatch Thread)
 * để đảm bảo an toàn khi cập nhật GUI.
 * <p>
 * Sử dụng:
 * <pre>
 *   // Subscribe
 *   EventBus.getInstance().subscribe(PatientRegisteredEvent.class, event -> {
 *       System.out.println("BN mới: " + event.getPatientId());
 *   });
 *
 *   // Publish
 *   EventBus.getInstance().publish(new PatientRegisteredEvent(patientId));
 * </pre>
 */
public class EventBus {

    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
    private static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    /**
     * Đăng ký lắng nghe một loại event.
     *
     * @param eventType class của event cần lắng nghe
     * @param listener  callback xử lý event
     * @param <T>       kiểu event
     */
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Hủy đăng ký lắng nghe.
     *
     * @param eventType class của event
     * @param listener  callback cần hủy
     * @param <T>       kiểu event
     */
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    /**
     * Phát event tới tất cả listener đã đăng ký.
     * Listener được gọi trên EDT (SwingUtilities.invokeLater).
     *
     * @param event đối tượng event
     * @param <T>   kiểu event
     */
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        List<Consumer<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer<?> listener : eventListeners) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        ((Consumer<T>) listener).accept(event);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE,
                                "Lỗi xử lý event " + event.getClass().getSimpleName(), e);
                    }
                });
            }
        }
    }

    /**
     * Xóa toàn bộ listener (dùng cho testing hoặc reset).
     */
    public void clearAll() {
        listeners.clear();
    }
}

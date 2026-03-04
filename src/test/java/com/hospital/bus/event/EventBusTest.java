package com.hospital.bus.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho EventBus.
 * <p>
 * Lưu ý: EventBus publish qua SwingUtilities.invokeLater,
 * nên test dùng CountDownLatch để đợi kết quả.
 */
class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = EventBus.getInstance();
        eventBus.clearAll();
    }

    @Test
    @DisplayName("Subscribe và publish event — listener nhận được event")
    void subscribeAndPublish() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<Long> captured = new ArrayList<>();

        eventBus.subscribe(PatientRegisteredEvent.class, event -> {
            captured.add(event.getPatientId());
            latch.countDown();
        });

        eventBus.publish(new PatientRegisteredEvent(42L));

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Listener phải được gọi trong 2 giây");
        assertEquals(1, captured.size());
        assertEquals(42L, captured.getFirst());
    }

    @Test
    @DisplayName("Nhiều listener cho cùng event type")
    void multipleListeners() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        List<String> logs = new ArrayList<>();

        eventBus.subscribe(QueueUpdatedEvent.class, event -> {
            logs.add("L1:" + event.getQueueStatus());
            latch.countDown();
        });
        eventBus.subscribe(QueueUpdatedEvent.class, event -> {
            logs.add("L2:" + event.getQueueStatus());
            latch.countDown();
        });
        eventBus.subscribe(QueueUpdatedEvent.class, event -> {
            logs.add("L3:" + event.getQueueStatus());
            latch.countDown();
        });

        eventBus.publish(new QueueUpdatedEvent(1L, "EXAMINING"));

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(3, logs.size());
    }

    @Test
    @DisplayName("Publish event không có listener — không lỗi")
    void publishWithoutListeners() {
        // Không subscribe, chỉ publish → không crash
        assertDoesNotThrow(() ->
                eventBus.publish(new ExaminationCompletedEvent(99L)));
    }

    @Test
    @DisplayName("Các event type khác nhau — listener được phân loại đúng")
    void differentEventTypes() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        List<String> results = new ArrayList<>();

        eventBus.subscribe(PatientRegisteredEvent.class, event -> {
            results.add("patient:" + event.getPatientId());
            latch.countDown();
        });
        eventBus.subscribe(PaymentCompletedEvent.class, event -> {
            results.add("payment:" + event.getInvoiceId());
            latch.countDown();
        });

        eventBus.publish(new PatientRegisteredEvent(1L));
        eventBus.publish(new PaymentCompletedEvent(100L));

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(2, results.size());
        assertTrue(results.contains("patient:1"));
        assertTrue(results.contains("payment:100"));
    }

    @Test
    @DisplayName("Unsubscribe — listener không nhận event nữa")
    void unsubscribe() throws InterruptedException {
        List<Long> captured = new ArrayList<>();
        var listener = new java.util.function.Consumer<PatientRegisteredEvent>() {
            @Override
            public void accept(PatientRegisteredEvent event) {
                captured.add(event.getPatientId());
            }
        };

        eventBus.subscribe(PatientRegisteredEvent.class, listener);
        eventBus.unsubscribe(PatientRegisteredEvent.class, listener);

        eventBus.publish(new PatientRegisteredEvent(42L));

        // Đợi một chút để chắc chắn event đã có cơ hội được dispatch
        Thread.sleep(500);
        assertTrue(captured.isEmpty(), "Listener đã unsubscribe nên không nhận event");
    }

    @Test
    @DisplayName("clearAll — xóa toàn bộ listener")
    void clearAll() throws InterruptedException {
        List<Long> captured = new ArrayList<>();

        eventBus.subscribe(PatientRegisteredEvent.class, event ->
                captured.add(event.getPatientId()));

        eventBus.clearAll();
        eventBus.publish(new PatientRegisteredEvent(42L));

        Thread.sleep(500);
        assertTrue(captured.isEmpty(), "Sau clearAll không listener nào nhận event");
    }
}

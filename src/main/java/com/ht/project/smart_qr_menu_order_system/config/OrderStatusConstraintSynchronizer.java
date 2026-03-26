//package com.ht.project.smart_qr_menu_order_system.config;
//
//import com.ht.project.smart_qr_menu_order_system.enums.OrderStatus;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.stream.Collectors;
//
////@Slf4j
////@Component
//@RequiredArgsConstructor
//public class OrderStatusConstraintSynchronizer {
//
//    private static final String CONSTRAINT_NAME = "orders_status_check";
//
//    private final JdbcTemplate jdbcTemplate;
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void synchronize() {
//        String allowedStatuses = Arrays.stream(OrderStatus.values())
//                .map(OrderStatus::name)
//                .map(status -> "'" + status + "'")
//                .collect(Collectors.joining(", "));
//
//        try {
//            jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS " + CONSTRAINT_NAME);
//            jdbcTemplate.execute("""
//                    ALTER TABLE orders
//                    ADD CONSTRAINT %s
//                    CHECK (status IN (%s))
//                    """.formatted(CONSTRAINT_NAME, allowedStatuses));
//            log.info("Synchronized {} with statuses [{}]", CONSTRAINT_NAME, allowedStatuses);
//        } catch (Exception exception) {
//            log.error("Failed to synchronize {}", CONSTRAINT_NAME, exception);
//        }
//    }
//}

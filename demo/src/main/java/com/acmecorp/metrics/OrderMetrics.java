package com.acmecorp.metrics;

public class OrderMetrics {

    // Missing "_total" suffix on a counter.
    static final Counter ordersCreated = Counter.build("orders_created", "Orders created").register();

    // camelCase, should be snake_case.
    static final Gauge activeSessions = Gauge.build("activeSessions", "Active sessions").register();

    // Correct: snake_case + _total suffix.
    static final Counter paymentsProcessedTotal = Counter.build("payments_processed_total", "Payments processed").register();
}

# Demo project

`OrderMetrics.java` — `ordersCreated` is missing the `_total` suffix,
`activeSessions` is camelCase, `paymentsProcessedTotal` is correct.

## Try it

1. `./gradlew runIde` from `prometheus-metric-naming-companion`, open
   this `demo/` folder as the project.
2. Open `OrderMetrics.java` — warning icons appear on the first two
   metric names but not the third.

The GIFs in `docs/media/` show the same checks, plus Micrometer's own
naming convention: lowercase, dot-separated names, where a missing
`_total` is correct (the Prometheus registry adds it) and only an
uppercase/camelCase name or a manual `_bucket`/`_count`/`_sum` is
reported.

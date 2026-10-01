# Demo project

`OrderMetrics.java` — `ordersCreated` is missing the `_total` suffix,
`activeSessions` is camelCase, `paymentsProcessedTotal` is correct.

## Try it

1. `./gradlew runIde` from `prometheus-metric-naming-companion`, open
   this `demo/` folder as the project.
2. Open `OrderMetrics.java` — warning icons appear on the first two
   metric names but not the third.

The GIFs in `docs/media/` show the same checks, plus Micrometer's own
naming convention (dot-separated lowercase names, no unit suffixes).

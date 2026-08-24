# Demo data for screenshots

`OrderMetrics.java` — `ordersCreated` is missing the `_total` suffix,
`activeSessions` is camelCase, `paymentsProcessedTotal` is correct.

## How to get the screenshot

1. `./gradlew runIde` from `prometheus-metric-naming-companion`, open
   this `demo/` folder as the project.
2. Full Screen, open `OrderMetrics.java` — warning icons should appear
   on the first two metric names but not the third.
3. Screenshot with all 3 metric declarations visible, save into
   `prometheus-metric-naming-companion/docs/screenshots/`. Close the
   sandbox.

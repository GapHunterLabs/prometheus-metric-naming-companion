<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Prometheus Metric Naming Companion Changelog

## [Unreleased]

## [0.2.3]

### Fixed

- Micrometer meters are checked against Micrometer's naming
  convention, not Prometheus's. `Counter.builder("orders.created")`
  (lowercase words separated by dots, the style Micrometer documents)
  was flagged as not snake_case, and a Micrometer counter was told to
  end in `_total`, which its Prometheus registry leaves to the
  Prometheus client (it appends `_total` itself and rejects a name that
  already has it). Now only an uppercase/camelCase Micrometer name, or
  a manual `_bucket`/`_count`/`_sum`, is reported. The Prometheus Java
  client's `Counter.build("name", "help")` keeps its three rules.

## [0.2.2]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.2.1]

### Fixed

- Marketplace listing (`plugin.xml`) still only mentioned 2 of the 3
  real checks ("lowercase snake_case" and the counter `_total` suffix)
  -- stale since 0.2.0 added the Histogram/Summary
  `_bucket`/`_count`/`_sum` rule. README already listed all 3;
  `plugin.xml` now matches.

## [0.2.0]

### Added

- New rule: a Histogram/Summary metric name manually carrying
  `_bucket`/`_count`/`_sum` is now flagged -- the client library
  appends these suffixes itself when exposing the metric, so a
  manually-added one produces a broken/duplicated name at scrape time.

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct real findings, a one-time
  notification asks whether to rate the plugin on Marketplace, with a
  permanent "Don't ask again" option. Standard mechanism used
  catalog-wide since 2026-08-24, rolled out
  to this plugin now.

## [0.1.0]

### Added

- Warning icon on any Java/Kotlin metric name literal that violates
  Prometheus's own naming conventions: not lowercase snake_case, or a
  counter missing its `_total` suffix.
- Recognizes the Prometheus Java client and Micrometer registration
  shapes.
- 100% static PSI analysis, Java and Kotlin, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/compare/0.2.3...HEAD
[0.2.3]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/compare/0.2.2...0.2.3
[0.2.2]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/compare/0.1.1...0.2.0
[0.1.1]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/commits/0.1.0

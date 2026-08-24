<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Prometheus Metric Naming Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning icon on any Java/Kotlin metric name literal that violates
  Prometheus's own naming conventions: not lowercase snake_case, or a
  counter missing its `_total` suffix.
- Recognizes the Prometheus Java client and Micrometer registration
  shapes.
- 100% static PSI analysis, Java and Kotlin, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/prometheus-metric-naming-companion/commits/0.1.0

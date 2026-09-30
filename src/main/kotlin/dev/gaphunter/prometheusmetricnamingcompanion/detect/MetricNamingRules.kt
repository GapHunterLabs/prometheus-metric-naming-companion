package dev.gaphunter.prometheusmetricnamingcompanion.detect

import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricApi
import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricKind
import dev.gaphunter.prometheusmetricnamingcompanion.model.NamingProblem

/**
 * The 3 real Prometheus naming rules this plugin checks (from
 * prometheus.io/docs/practices/naming/, confirmed 2026-08-23, not
 * guessed): a metric name should be lowercase snake_case, a counter's
 * name should end in `_total`, and a Histogram/Summary's base name must
 * NOT manually carry `_bucket`/`_count`/`_sum` -- the client library
 * appends those itself when exposing the metric, so a manually-added
 * one produces a broken/duplicated name at scrape time (e.g. a
 * Histogram named "request_duration_seconds_bucket" is actually
 * exposed as "request_duration_seconds_bucket_bucket"). Checked in
 * this priority order -- a malformed name is reported before the
 * suffix checks, since fixing the name shape first usually also fixes
 * the suffix.
 *
 * Micrometer names are different: its documented convention is
 * lowercase words separated by dots ("http.server.requests"), and its
 * Prometheus registry turns them into snake_case and leaves "_total" to
 * the Prometheus client, which appends it itself (and rejects a name that
 * already carries it). So for `Counter.builder("...")` a dotted name is
 * right and a missing "_total" is right; only an uppercase/camelCase name
 * and a manual `_bucket`/`_count`/`_sum` are reported.
 */
object MetricNamingRules {

    private val SNAKE_CASE = Regex("^[a-z][a-z0-9_]*$")
    private val MICROMETER_NAME = Regex("^[a-z][a-z0-9]*([._][a-z0-9]+)*$")
    private val RESERVED_HISTOGRAM_SUMMARY_SUFFIXES = setOf("_bucket", "_count", "_sum")

    fun firstProblem(kind: MetricKind, name: String, api: MetricApi = MetricApi.PROMETHEUS_CLIENT): NamingProblem? =
        when (api) {
            MetricApi.PROMETHEUS_CLIENT -> prometheusClientProblem(kind, name)
            MetricApi.MICROMETER -> micrometerProblem(kind, name)
        }

    private fun prometheusClientProblem(kind: MetricKind, name: String): NamingProblem? {
        if (!SNAKE_CASE.matches(name)) return NamingProblem.NOT_SNAKE_CASE
        if (kind.isCounter && !name.endsWith("_total")) return NamingProblem.COUNTER_MISSING_TOTAL_SUFFIX
        if (hasReservedSuffix(kind, name)) return NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX
        return null
    }

    private fun micrometerProblem(kind: MetricKind, name: String): NamingProblem? {
        if (!MICROMETER_NAME.matches(name)) return NamingProblem.MICROMETER_NOT_LOWERCASE
        if (hasReservedSuffix(kind, name.replace('.', '_'))) return NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX
        return null
    }

    private fun hasReservedSuffix(kind: MetricKind, snakeName: String): Boolean =
        kind.isHistogramOrSummary && RESERVED_HISTOGRAM_SUMMARY_SUFFIXES.any { snakeName.endsWith(it) }
}

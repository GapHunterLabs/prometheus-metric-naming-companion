package dev.gaphunter.prometheusmetricnamingcompanion.detect

import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricKind
import dev.gaphunter.prometheusmetricnamingcompanion.model.NamingProblem

/**
 * The 2 real Prometheus naming rules this plugin checks (from
 * prometheus.io/docs/practices/naming/, confirmed 2026-08-23, not
 * guessed): a metric name should be lowercase snake_case, and a
 * counter's name should end in `_total`. Checked in this priority
 * order -- a malformed name is reported before the suffix check, since
 * fixing the name shape first usually also fixes the suffix.
 */
object MetricNamingRules {

    private val SNAKE_CASE = Regex("^[a-z][a-z0-9_]*$")

    fun firstProblem(kind: MetricKind, name: String): NamingProblem? {
        if (!SNAKE_CASE.matches(name)) return NamingProblem.NOT_SNAKE_CASE
        if (kind.isCounter && !name.endsWith("_total")) return NamingProblem.COUNTER_MISSING_TOTAL_SUFFIX
        return null
    }
}

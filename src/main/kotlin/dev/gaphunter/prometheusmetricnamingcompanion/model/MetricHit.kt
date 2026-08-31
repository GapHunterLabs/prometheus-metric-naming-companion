package dev.gaphunter.prometheusmetricnamingcompanion.model

import com.intellij.psi.PsiElement

enum class MetricKind(val builderNames: Set<String>, val isCounter: Boolean, val isHistogramOrSummary: Boolean = false) {
    COUNTER(setOf("Counter"), isCounter = true),
    GAUGE(setOf("Gauge"), isCounter = false),
    HISTOGRAM(setOf("Histogram"), isCounter = false, isHistogramOrSummary = true),
    SUMMARY(setOf("Summary"), isCounter = false, isHistogramOrSummary = true),
    TIMER(setOf("Timer"), isCounter = false);

    companion object {
        fun byBuilderName(name: String): MetricKind? = entries.firstOrNull { name in it.builderNames }
    }
}

enum class NamingProblem {
    NOT_SNAKE_CASE,
    COUNTER_MISSING_TOTAL_SUFFIX,
    HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX,
}

/** One metric-name literal found registered via a Prometheus client/Micrometer builder call, plus the real naming problem it has. */
data class MetricHit(val nameLiteralElement: PsiElement, val metricName: String, val problem: NamingProblem)

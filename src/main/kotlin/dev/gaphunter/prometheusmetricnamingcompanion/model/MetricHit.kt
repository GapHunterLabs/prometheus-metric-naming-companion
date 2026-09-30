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

/**
 * Which library registers the metric, told apart by the call shape: the Prometheus Java client's
 * `Counter.build("name", "help")` versus Micrometer's `Counter.builder("name")`. They follow different naming
 * conventions, so the same name can be right in one and wrong in the other.
 */
enum class MetricApi {
    PROMETHEUS_CLIENT,
    MICROMETER;

    companion object {
        fun byConstructorName(methodName: String): MetricApi? = when (methodName) {
            "build" -> PROMETHEUS_CLIENT
            "builder" -> MICROMETER
            else -> null
        }
    }
}

enum class NamingProblem {
    NOT_SNAKE_CASE,
    COUNTER_MISSING_TOTAL_SUFFIX,
    HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX,
    MICROMETER_NOT_LOWERCASE,
}

/** One metric-name literal found registered via a Prometheus client/Micrometer builder call, plus the real naming problem it has. */
data class MetricHit(val nameLiteralElement: PsiElement, val metricName: String, val problem: NamingProblem)

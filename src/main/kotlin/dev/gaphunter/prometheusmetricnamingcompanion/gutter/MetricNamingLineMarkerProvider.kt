package dev.gaphunter.prometheusmetricnamingcompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.prometheusmetricnamingcompanion.detect.JavaMetricFinder
import dev.gaphunter.prometheusmetricnamingcompanion.detect.KotlinMetricFinder
import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricHit
import dev.gaphunter.prometheusmetricnamingcompanion.model.NamingProblem
import dev.gaphunter.prometheusmetricnamingcompanion.review.ReviewPrompt

class MetricNamingLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "Prometheus metric naming"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaMetricFinder.findAll(file)
            "kotlin" -> KotlinMetricFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val leafByHit = hits.associateBy { leafOf(it.nameLiteralElement) }
        for (element in elements) {
            val hit = leafByHit[element] ?: continue
            result.add(buildMarker(element, hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(leaf: PsiElement, hit: MetricHit): LineMarkerInfo<PsiElement> {
        val tooltip = tooltipFor(hit)
        return LineMarkerInfo(
            leaf,
            leaf.textRange,
            MetricNamingIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }

    private fun tooltipFor(hit: MetricHit): String = when (hit.problem) {
        NamingProblem.NOT_SNAKE_CASE ->
            "Metric name \"${hit.metricName}\" should be lowercase snake_case per Prometheus naming conventions (e.g. \"http_requests_total\")"
        NamingProblem.COUNTER_MISSING_TOTAL_SUFFIX ->
            "Counter metric name \"${hit.metricName}\" should end in \"_total\" per Prometheus naming conventions"
        NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX ->
            "Metric name \"${hit.metricName}\" must not manually carry a \"_bucket\"/\"_count\"/\"_sum\" suffix -- the client library appends these itself for a Histogram/Summary, so this name would be exposed with a broken/duplicated suffix at scrape time"
    }

    /** Leaf-anchored, never a composite node. */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}

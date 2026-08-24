package dev.gaphunter.prometheusmetricnamingcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricHit
import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricKind

/**
 * Finds Java metric-registration calls of the two most common real
 * shapes -- Prometheus Java client's `Counter.build("name", "help")`
 * and Micrometer's `Counter.builder("name")` (same for
 * Gauge/Histogram/Summary/Timer) -- and checks the metric name literal
 * against [MetricNamingRules]. Matches by simple class/method name
 * only, so it works whether the real Prometheus/Micrometer jar is on
 * the classpath or not.
 *
 * **v0.1 scope, stated honestly:** only the metric name passed directly
 * to `build(...)`/`builder(...)` is checked -- the Prometheus client's
 * alternate chained form (`Counter.build().name("x").help("y")`) isn't
 * covered, a real, documented limitation.
 */
object JavaMetricFinder {

    private val CONSTRUCTOR_METHODS = setOf("build", "builder")

    fun findAll(file: PsiFile): List<MetricHit> {
        val hits = mutableListOf<MetricHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(call: PsiMethodCallExpression): MetricHit? {
        val methodName = call.methodExpression.referenceName ?: return null
        if (methodName !in CONSTRUCTOR_METHODS) return null

        val qualifierName = (call.methodExpression.qualifierExpression as? PsiReferenceExpression)?.referenceName ?: return null
        val kind = MetricKind.byBuilderName(qualifierName) ?: return null

        val firstArg = call.argumentList.expressions.firstOrNull() as? PsiLiteralExpression ?: return null
        val name = firstArg.value as? String ?: return null

        val problem = MetricNamingRules.firstProblem(kind, name) ?: return null
        return MetricHit(firstArg, name, problem)
    }
}

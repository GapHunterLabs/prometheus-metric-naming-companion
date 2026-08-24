package dev.gaphunter.prometheusmetricnamingcompanion.detect

import com.intellij.psi.PsiFile
import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricHit
import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricKind
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaMetricFinder]. */
object KotlinMetricFinder {

    private val CONSTRUCTOR_METHODS = setOf("build", "builder")

    fun findAll(file: PsiFile): List<MetricHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<MetricHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(expression: KtDotQualifiedExpression): MetricHit? {
        val receiverName = (expression.receiverExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return null
        val kind = MetricKind.byBuilderName(receiverName) ?: return null

        val call = expression.selectorExpression as? KtCallExpression ?: return null
        val methodName = (call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return null
        if (methodName !in CONSTRUCTOR_METHODS) return null

        val firstArgExpr = call.valueArguments.firstOrNull()?.getArgumentExpression() as? KtStringTemplateExpression ?: return null
        val name = plainLiteralTextOf(firstArgExpr) ?: return null

        val problem = MetricNamingRules.firstProblem(kind, name) ?: return null
        return MetricHit(firstArgExpr, name, problem)
    }

    /** Same "exact literal, no interpolation" contract as `feature-flag-reference-companion`'s `KotlinFlagCheckFinder.plainLiteralTextOf`. */
    private fun plainLiteralTextOf(template: KtStringTemplateExpression): String? {
        val entries = template.entries
        if (entries.size != 1) return null
        val entry = entries[0]
        if (entry.javaClass.simpleName != "KtLiteralStringTemplateEntry") return null
        return entry.text
    }
}

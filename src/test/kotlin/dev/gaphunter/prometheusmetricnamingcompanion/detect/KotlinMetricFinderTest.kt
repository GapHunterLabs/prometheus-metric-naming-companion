package dev.gaphunter.prometheusmetricnamingcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.gaphunter.prometheusmetricnamingcompanion.model.NamingProblem

class KotlinMetricFinderTest : BasePlatformTestCase() {

    fun `test a well-formed counter name is not flagged`() {
        val file = myFixture.configureByText(
            "Metrics.kt",
            """
            class Metrics {
                fun init() {
                    Counter.builder("http_requests_total").register(registry)
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinMetricFinder.findAll(file).isEmpty())
    }

    fun `test a counter missing the _total suffix is flagged`() {
        val file = myFixture.configureByText(
            "Metrics.kt",
            """
            class Metrics {
                fun init() {
                    Counter.builder("http_requests").register(registry)
                }
            }
            """.trimIndent(),
        )
        val hits = KotlinMetricFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(NamingProblem.COUNTER_MISSING_TOTAL_SUFFIX, hits[0].problem)
    }

    fun `test a camelCase name is flagged as not snake_case`() {
        val file = myFixture.configureByText(
            "Metrics.kt",
            """
            class Metrics {
                fun init() {
                    Gauge.builder("activeConnections") { 0.0 }.register(registry)
                }
            }
            """.trimIndent(),
        )
        val hits = KotlinMetricFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(NamingProblem.NOT_SNAKE_CASE, hits[0].problem)
    }

    fun `test an interpolated metric name is never checked -- v0-1 documented scope limit`() {
        val file = myFixture.configureByText(
            "Metrics.kt",
            """
            class Metrics {
                fun init(prefix: String) {
                    Counter.builder("${'$'}prefix_requests").register(registry)
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinMetricFinder.findAll(file).isEmpty())
    }

    fun `test a Histogram name manually carrying the _bucket suffix is flagged`() {
        val file = myFixture.configureByText(
            "Metrics.kt",
            """
            class Metrics {
                fun init() {
                    Histogram.builder("request_duration_seconds_bucket").register(registry)
                }
            }
            """.trimIndent(),
        )
        val hits = KotlinMetricFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX, hits[0].problem)
    }

    fun `test a well-formed Histogram name is not flagged`() {
        val file = myFixture.configureByText(
            "Metrics.kt",
            """
            class Metrics {
                fun init() {
                    Histogram.builder("request_duration_seconds").register(registry)
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinMetricFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated call is never flagged`() {
        val file = myFixture.configureByText(
            "Metrics.kt",
            """
            class Metrics {
                fun init() {
                    StringBuilder().build("not_a_metric")
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinMetricFinder.findAll(file).isEmpty())
    }
}

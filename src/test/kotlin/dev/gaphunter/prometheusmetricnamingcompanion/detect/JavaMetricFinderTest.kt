package dev.gaphunter.prometheusmetricnamingcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.gaphunter.prometheusmetricnamingcompanion.model.NamingProblem

class JavaMetricFinderTest : BasePlatformTestCase() {

    fun `test Prometheus client Counter-build with a well-formed _total name is not flagged`() {
        val file = myFixture.configureByText(
            "Metrics.java",
            """
            class Metrics {
                void init() {
                    Counter.build("http_requests_total", "help").register();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaMetricFinder.findAll(file).isEmpty())
    }

    fun `test a Counter missing the _total suffix is flagged`() {
        val file = myFixture.configureByText(
            "Metrics.java",
            """
            class Metrics {
                void init() {
                    Counter.build("http_requests", "help").register();
                }
            }
            """.trimIndent(),
        )
        val hits = JavaMetricFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(NamingProblem.COUNTER_MISSING_TOTAL_SUFFIX, hits[0].problem)
    }

    fun `test Micrometer Counter-builder is also recognized`() {
        val file = myFixture.configureByText(
            "Metrics.java",
            """
            class Metrics {
                void init() {
                    Counter.builder("http_requests").register(registry);
                }
            }
            """.trimIndent(),
        )
        val hits = JavaMetricFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(NamingProblem.COUNTER_MISSING_TOTAL_SUFFIX, hits[0].problem)
    }

    fun `test a camelCase name is flagged as not snake_case`() {
        val file = myFixture.configureByText(
            "Metrics.java",
            """
            class Metrics {
                void init() {
                    Gauge.build("activeConnections", "help").register();
                }
            }
            """.trimIndent(),
        )
        val hits = JavaMetricFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(NamingProblem.NOT_SNAKE_CASE, hits[0].problem)
    }

    fun `test a well-formed Gauge name (no _total requirement) is not flagged`() {
        val file = myFixture.configureByText(
            "Metrics.java",
            """
            class Metrics {
                void init() {
                    Gauge.build("active_connections", "help").register();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaMetricFinder.findAll(file).isEmpty())
    }

    fun `test a call unrelated to metric registration is never flagged`() {
        val file = myFixture.configureByText(
            "Metrics.java",
            """
            class Metrics {
                void init() {
                    StringBuilder.build("not_a_metric");
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaMetricFinder.findAll(file).isEmpty())
    }

    fun `test a Histogram name manually carrying the _bucket suffix is flagged`() {
        val file = myFixture.configureByText(
            "Metrics.java",
            """
            class Metrics {
                void init() {
                    Histogram.build("request_duration_seconds_bucket", "help").register();
                }
            }
            """.trimIndent(),
        )
        val hits = JavaMetricFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX, hits[0].problem)
    }

    fun `test a well-formed Histogram name is not flagged`() {
        val file = myFixture.configureByText(
            "Metrics.java",
            """
            class Metrics {
                void init() {
                    Histogram.build("request_duration_seconds", "help").register();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaMetricFinder.findAll(file).isEmpty())
    }
}

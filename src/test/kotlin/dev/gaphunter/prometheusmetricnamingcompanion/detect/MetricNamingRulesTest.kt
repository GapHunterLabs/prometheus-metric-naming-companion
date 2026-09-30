package dev.gaphunter.prometheusmetricnamingcompanion.detect

import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricApi
import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricKind
import dev.gaphunter.prometheusmetricnamingcompanion.model.NamingProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetricNamingRulesTest {

    // Micrometer: lowercase dot notation is its documented convention, and its Prometheus registry leaves "_total"
    // to the Prometheus client, which appends it itself.

    @Test
    fun `a Micrometer dotted name has no problem`() {
        assertNull(MetricNamingRules.firstProblem(MetricKind.COUNTER, "http.server.requests", MetricApi.MICROMETER))
        assertNull(MetricNamingRules.firstProblem(MetricKind.TIMER, "checkout.duration", MetricApi.MICROMETER))
    }

    @Test
    fun `a Micrometer counter is never asked for the _total suffix`() {
        assertNull(MetricNamingRules.firstProblem(MetricKind.COUNTER, "orders_created", MetricApi.MICROMETER))
    }

    @Test
    fun `a camelCase or uppercase Micrometer name is flagged with the Micrometer rule`() {
        assertEquals(NamingProblem.MICROMETER_NOT_LOWERCASE,
            MetricNamingRules.firstProblem(MetricKind.GAUGE, "activeSessions", MetricApi.MICROMETER))
        assertEquals(NamingProblem.MICROMETER_NOT_LOWERCASE,
            MetricNamingRules.firstProblem(MetricKind.COUNTER, "HTTP.requests", MetricApi.MICROMETER))
        assertEquals(NamingProblem.MICROMETER_NOT_LOWERCASE,
            MetricNamingRules.firstProblem(MetricKind.COUNTER, "orders..created", MetricApi.MICROMETER))
    }

    @Test
    fun `a Micrometer histogram name carrying a reserved suffix is still flagged, dots included`() {
        assertEquals(NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX,
            MetricNamingRules.firstProblem(MetricKind.HISTOGRAM, "request.duration.bucket", MetricApi.MICROMETER))
    }

    @Test
    fun `the Prometheus client keeps its three rules`() {
        assertEquals(NamingProblem.NOT_SNAKE_CASE,
            MetricNamingRules.firstProblem(MetricKind.COUNTER, "http.server.requests", MetricApi.PROMETHEUS_CLIENT))
        assertEquals(NamingProblem.COUNTER_MISSING_TOTAL_SUFFIX,
            MetricNamingRules.firstProblem(MetricKind.COUNTER, "orders_created", MetricApi.PROMETHEUS_CLIENT))
    }

    @Test
    fun `the call shape tells the two APIs apart`() {
        assertEquals(MetricApi.PROMETHEUS_CLIENT, MetricApi.byConstructorName("build"))
        assertEquals(MetricApi.MICROMETER, MetricApi.byConstructorName("builder"))
        assertNull(MetricApi.byConstructorName("register"))
    }

    @Test
    fun `a well-formed counter name has no problem`() {
        assertNull(MetricNamingRules.firstProblem(MetricKind.COUNTER, "http_requests_total"))
    }

    @Test
    fun `a well-formed gauge name (no _total requirement) has no problem`() {
        assertNull(MetricNamingRules.firstProblem(MetricKind.GAUGE, "active_connections"))
    }

    @Test
    fun `a counter missing the _total suffix is flagged`() {
        assertEquals(NamingProblem.COUNTER_MISSING_TOTAL_SUFFIX, MetricNamingRules.firstProblem(MetricKind.COUNTER, "http_requests"))
    }

    @Test
    fun `a well-formed histogram name has no problem`() {
        assertNull(MetricNamingRules.firstProblem(MetricKind.HISTOGRAM, "request_duration_seconds"))
    }

    @Test
    fun `a histogram name manually carrying the _bucket suffix is flagged`() {
        assertEquals(
            NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX,
            MetricNamingRules.firstProblem(MetricKind.HISTOGRAM, "request_duration_seconds_bucket"),
        )
    }

    @Test
    fun `a summary name manually carrying the _sum suffix is flagged`() {
        assertEquals(
            NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX,
            MetricNamingRules.firstProblem(MetricKind.SUMMARY, "request_duration_seconds_sum"),
        )
    }

    @Test
    fun `a summary name manually carrying the _count suffix is flagged`() {
        assertEquals(
            NamingProblem.HISTOGRAM_OR_SUMMARY_RESERVED_SUFFIX,
            MetricNamingRules.firstProblem(MetricKind.SUMMARY, "request_duration_seconds_count"),
        )
    }

    @Test
    fun `a counter is never flagged for the histogram-summary reserved suffix rule`() {
        // "_count" happens to end a valid counter-shaped name too -- the
        // reserved-suffix rule only ever applies to Histogram/Summary.
        assertNull(MetricNamingRules.firstProblem(MetricKind.COUNTER, "requests_count_total"))
    }

    @Test
    fun `a camelCase name is flagged as not snake_case before the suffix check`() {
        assertEquals(NamingProblem.NOT_SNAKE_CASE, MetricNamingRules.firstProblem(MetricKind.COUNTER, "httpRequests"))
    }

    @Test
    fun `an uppercase name is flagged as not snake_case`() {
        assertEquals(NamingProblem.NOT_SNAKE_CASE, MetricNamingRules.firstProblem(MetricKind.GAUGE, "HTTP_Requests"))
    }

    @Test
    fun `a hyphenated name is flagged as not snake_case`() {
        assertEquals(NamingProblem.NOT_SNAKE_CASE, MetricNamingRules.firstProblem(MetricKind.GAUGE, "active-connections"))
    }
}

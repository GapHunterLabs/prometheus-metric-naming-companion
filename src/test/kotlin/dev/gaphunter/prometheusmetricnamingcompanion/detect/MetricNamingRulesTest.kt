package dev.gaphunter.prometheusmetricnamingcompanion.detect

import dev.gaphunter.prometheusmetricnamingcompanion.model.MetricKind
import dev.gaphunter.prometheusmetricnamingcompanion.model.NamingProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetricNamingRulesTest {

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

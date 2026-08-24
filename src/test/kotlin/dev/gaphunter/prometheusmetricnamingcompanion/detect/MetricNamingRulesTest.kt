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

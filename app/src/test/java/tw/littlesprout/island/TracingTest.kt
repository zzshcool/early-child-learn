package tw.littlesprout.island

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.*
import org.junit.Test

class TracingTest {
    private val line = listOf(Offset(.5f, .1f), Offset(.5f, .9f))
    private val full = (1..9).map { Offset(.5f, it / 10f) }
    @Test fun acceptsFullStrokeWithSmallMotorDeviation() { assertTrue(Tracing.matches(full.map { it + Offset(.03f, 0f) }, line)) }
    @Test fun rejectsEndpointOnlyShortcut() { assertFalse(Tracing.matches(listOf(line.first(), line.first(), line.last()), line)) }
    @Test fun rejectsReversedStroke() { assertFalse(Tracing.matches(full.reversed(), line)) }
    @Test fun rejectsStrokeOutsideGuide() { assertFalse(Tracing.matches(full.map { it + Offset(.3f, 0f) }, line)) }
    @Test fun acceptsClosedLoopsAndCrossingPaths() {
        listOf("0", "8", "A", "ㄅ").forEach { symbol -> Tracing.guide(symbol)!!.forEach { guide ->
            val input = guide.zipWithNext().flatMap { (a, b) -> (0..40).map { a + (b - a) * (it / 40f) } }
            assertTrue("Guide $symbol must be traceable", Tracing.matches(input, guide))
        } }
    }
}

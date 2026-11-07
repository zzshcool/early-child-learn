package tw.littlesprout.island

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun IslandSea(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawRect(Color(0xFFE4F3F4))
        val ocean = Path().apply {
            moveTo(0f, h * .28f)
            cubicTo(w * .25f, h * .24f, w * .65f, h * .35f, w, h * .28f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(ocean, Color(0xFFC6E5E8))
        drawOval(Color(0xFFEDDDAF), Offset(-w * .3f, h * .47f), Size(w * .48f, h * .13f))
        drawOval(Color(0xFFD5E4B4), Offset(-w * .28f, h * .47f), Size(w * .42f, h * .09f))
        drawOval(Color(0xFFEDDDAF), Offset(w * .83f, h * .77f), Size(w * .42f, h * .13f))
        drawOval(Color(0xFFD5E4B4), Offset(w * .87f, h * .77f), Size(w * .34f, h * .08f))
        repeat(9) { row ->
            val y = h * (.35f + row * .075f)
            val x = w * (if (row % 2 == 0) .06f else .7f)
            val wave = Path().apply { moveTo(x, y); cubicTo(x + w * .04f, y - 5.dp.toPx(), x + w * .08f, y + 5.dp.toPx(), x + w * .14f, y) }
            drawPath(wave, Color.White.copy(alpha = .55f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
fun BoatJourney(s: IslandSession, destination: String) {
    val progress = remember(destination) { Animatable(0f) }
    LaunchedEffect(destination) {
        progress.animateTo(1f, tween(durationMillis = 1100, easing = FastOutSlowInEasing))
        s.arrive()
    }
    val names = mapOf("cognition" to ("顏色與形狀" to "Colors & shapes"), "numbers" to ("數字" to "Numbers"), "english" to ("英文" to "English"), "zhuyin" to ("注音" to "Zhuyin"), "clock" to ("車站" to "Station"), "writing" to ("沙灘畫畫" to "Sand drawing"), "weather" to ("小屋" to "House"))
    val sceneNames = mapOf("beach" to ("海灘" to "Beach"), "bridge" to ("小橋" to "Bridge"), "fruit" to ("水果攤" to "Fruit stand"), "delivery" to ("碼頭" to "Harbor"))
    val name = (sceneNames[destination] ?: names[destination])?.let { s.text(it.first, it.second) } ?: destination
    Box(Modifier.fillMaxSize().background(Color(0xFFE4F3F4)).clickable(enabled = true, onClick = {}).semantics { contentDescription = s.text("搭小船前往$name", "Sailing to $name") }) {
        IslandSea(Modifier.fillMaxSize())
        Column(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 64.dp, start = 24.dp, end = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(s.text("出發！", "Let's go!"), fontSize = 30.sp, color = Color(0xFF283B36), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(name, fontSize = 22.sp, color = Color(0xFF43856B))
        }
        Canvas(Modifier.fillMaxWidth().height(240.dp).align(Alignment.Center)) {
            val unit = size.width.coerceAtMost(600.dp.toPx()) * .28f
            val p = progress.value
            val x = -unit + (size.width + unit) * p
            val y = size.height * .58f + sin(p * PI * 4).toFloat() * 5.dp.toPx()
            drawOval(Color(0xFF7EB9C4).copy(alpha = .35f), Offset(x - unit * .25f, y + unit * .11f), Size(unit * 1.15f, unit * .16f))
            translate(x, y) {
                val hull = Path().apply { moveTo(0f, 0f); lineTo(unit, 0f); lineTo(unit * .79f, unit * .2f); lineTo(unit * .22f, unit * .2f); close() }
                drawPath(hull, Color(0xFFD59A58))
                drawLine(Color(0xFF775537), Offset(unit * .47f, 0f), Offset(unit * .47f, -unit * .75f), 4.dp.toPx(), StrokeCap.Round)
                val sail = Path().apply { moveTo(unit * .5f, -unit * .72f); lineTo(unit * .92f, -unit * .08f); lineTo(unit * .5f, -unit * .08f); close() }
                drawPath(sail, Color(0xFFFFFAE9))
                val flag = Path().apply { moveTo(unit * .47f, -unit * .75f); lineTo(unit * .2f, -unit * .66f); lineTo(unit * .47f, -unit * .57f); close() }
                drawPath(flag, Color(0xFF43856B))
                drawLine(Color(0xFFB47C43), Offset(unit * .55f, unit * .09f), Offset(unit * .78f, unit * .38f), 4.dp.toPx(), StrokeCap.Round)
            }
        }
    }
}

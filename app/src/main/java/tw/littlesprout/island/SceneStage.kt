package tw.littlesprout.island

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable fun SceneStage(scene: String, done: Boolean, modifier: Modifier = Modifier, color: Color = Color(0xFFE58075), plank: Int = -1, gap: Int = 0, count: Int = 0, secondCount: Int = 0, cargo: String = "apple", weather: Int = 0, equipment: Int = -1) {
    val progress by animateFloatAsState(if(done) 1f else 0f, tween(1100), label="scene-result")
    Canvas(modifier.widthIn(max = 500.dp).fillMaxWidth().heightIn(min = 110.dp, max = 200.dp).aspectRatio(2.5f).clipToBounds()) {
        val unit=size.width/330f
        scale(unit,unit,Offset.Zero) {
            val height=size.height/unit
            drawRoundRect(Color(0xFFD9EFF0),size=Size(330f,height),cornerRadius=androidx.compose.ui.geometry.CornerRadius(20f))
            drawCircle(Color(0xFFF1D689),18f,Offset(282f,30f))
            repeat(3){i->drawOval(Color.White.copy(alpha=.65f),Offset(25f+i*100,23f+(i%2)*11),Size(43f,14f))}
            drawOval(Color(0xFFEDDDAF),Offset(-25f,height*.62f),Size(380f,height*.6f))
            fun objectAt(id:String,x:Float,y:Float,extent:Float) { translate(x,y){scale(extent/100,extent/100,Offset.Zero){art(id)}} }
            fun rabbit(x:Float,y:Float) {
                val step=sin(progress*22)*3
                drawOval(Color(0xFFF1E2BF),Offset(x+16,y+5),Size(34f,35f))
                drawOval(Color(0xFFFFF7DF),Offset(x+23,y+12),Size(20f,23f))
                drawLine(Color(0xFFE0C79A),Offset(x+20,y+35),Offset(x+16+step,y+47),7f,StrokeCap.Round)
                drawLine(Color(0xFFE0C79A),Offset(x+43,y+35),Offset(x+47-step,y+47),7f,StrokeCap.Round)
                drawLine(Color(0xFFE0C79A),Offset(x+17,y+14),Offset(x+8,y+23+step),6f,StrokeCap.Round)
                drawLine(Color(0xFFE0C79A),Offset(x+49,y+14),Offset(x+58,y+23-step),6f,StrokeCap.Round)
                objectAt("rabbit",x+7,y-30,52f)
            }
            when(scene) {
                "bridge"->{
                    val length=75f+gap*25f; val left=(330-length)/2
                    drawRect(Color(0xFF84BFCA),Offset(left,height*.5f),Size(length,height*.5f))
                    if(plank>=0)drawLine(Color(0xFFBA8450),Offset(left-6,height*.78f),Offset(left+75+plank*25+6,height*.78f),16f,StrokeCap.Butt)
                    rabbit(30+progress*230,height*.48f-sin(progress*18)*3)
                    objectAt("tree",275f,height*.3f,55f)
                }
                "beach"->{
                    if(done){drawArc(color,180f,180f,true,Offset(133f,height*.22f),Size(130f,125f));drawLine(Color(0xFF775537),Offset(198f,height*.53f),Offset(198f,height*.89f),4f)}
                    rabbit(42+progress*115,height*.52f-sin(progress*12)*3)
                    repeat(3){i->drawOval(Color.White,Offset(20f+i*98,height*.93f),Size(28f,3f))}
                }
                "fruit"->{
                    objectAt("tree",252f,height*.22f,72f)
                    rabbit(16f+progress*150,height*.48f-sin(progress*15)*3)
                    val basketX=157f+(progress-.5f).coerceAtLeast(0f)*160
                    drawRoundRect(Color(0xFFBA8450),Offset(basketX,height*.69f),Size(90f,32f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(8f))
                    repeat((count+secondCount).coerceAtMost(24)){i->objectAt(if(i<count)"apple" else "pear",basketX+2+(i%6)*14,height*.62f-(i/6)*13,21f)}
                    if(done)drawLine(Color(0xFF775537),Offset(basketX+14,height*.7f),Offset(basketX+73,height*.7f),4f)
                }
                "delivery"->{
                    drawRect(Color(0xFF84BFCA),Offset(0f,height*.73f),Size(330f,height*.27f))
                    drawRoundRect(Color(0xFFEDDDAF),Offset(225f,height*.65f),Size(105f,height*.35f))
                    objectAt("house",242f,height*.17f,75f)
                    val x=16f+progress*216
                    val hull=Path().apply{moveTo(x,height*.8f);lineTo(x+75,height*.8f);lineTo(x+61,height*.92f);lineTo(x+15,height*.92f);close()}
                    drawPath(hull,Color(0xFFBA8450));objectAt(cargo,x+18,height*.47f,42f)
                    if(done)objectAt(cargo,272f,height*.56f,28f)
                }
                "clock"->{
                    objectAt("house",18f,height*.24f,80f)
                    drawLine(Color(0xFF775537),Offset(0f,height*.86f),Offset(330f,height*.86f),3f)
                    repeat(16){i->drawLine(Color(0xFF775537),Offset(i*23f,height*.83f),Offset(i*23f,height*.9f),3f)}
                    objectAt("train",108f+progress*250,height*.32f,75f)
                }
                "weather"->{
                    objectAt("house",215f,height*.24f,100f)
                    if(weather==1||weather==2){repeat(3){i->drawOval(Color(0xFF97AFB8),Offset(45f+i*40,20f+(i%2)*10),Size(100f,35f))}}
                    if(weather==2)repeat(15){i->drawLine(Color(0xFF72ACCA),Offset(25f+i*19,62f+(i%3)*12),Offset(19f+i*19,77f+(i%3)*12),2f)}
                    if(weather==3)repeat(3){i->drawLine(Color(0xFF72ACCA),Offset(10f,40f+i*20),Offset(180f-i*20,40f+i*20),3f,StrokeCap.Round)}
                    rabbit(85f+progress*40,height*.5f)
                    when(equipment){0->objectAt("umbrella",50f+progress*40,height*.2f,80f);1->objectAt("hat",91f+progress*40,height*.27f,48f);2->objectAt("jacket",89f+progress*40,height*.61f,52f)}
                }
                "writing"->{drawRoundRect(Color(0xFFD2BB8A),Offset(100f,height*.55f),Size(120f,60f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(8f));drawLine(Color(0xFF775537),Offset(145f,height*.8f),Offset(165f,height*.6f),3f);drawLine(Color(0xFF775537),Offset(165f,height*.6f),Offset(185f,height*.8f),3f);objectAt("pencil",190f,height*.45f,60f);rabbit(35f,height*.52f)}
                else->{objectAt("house",220f,height*.25f,90f);rabbit(50f,height*.52f)}
            }
        }
    }
}

@Composable fun SceneThumbnail(id:String, modifier:Modifier) {
    SceneStage(id,false,modifier)
}

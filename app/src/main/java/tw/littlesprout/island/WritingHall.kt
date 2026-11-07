package tw.littlesprout.island

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable fun WritingHall(s:IslandSession,availablePaper:androidx.compose.ui.unit.Dp=480.dp){BoxWithConstraints(Modifier.fillMaxWidth()){val paperMax=minOf(availablePaper,if(maxWidth<400.dp)220.dp else 480.dp);Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){WritingContents(s,paperMax)}}}

@Composable private fun WritingContents(s:IslandSession,paperMax:androidx.compose.ui.unit.Dp){
    var gallery by remember{mutableStateOf(false)}
    val categories=listOf("123","ㄅㄆㄇ","ABC","abc")
    val categoryIndex=s.worldValue("writing.category").coerceIn(0,3)
    val category=categories[categoryIndex]
    val symbols=when(categoryIndex){0->"0123456789";1->Lessons.zhuyin;2->"ABCDEFGHIJKLMNOPQRSTUVWXYZ";else->"abcdefghijklmnopqrstuvwxyz"}
    val index=s.worldValue("writing.index")%symbols.length
    val symbol=symbols[index].toString()
    val guided=s.worldValue("writing.guided")==1
    val guide=s.guide(symbol)!!
    val draftKey="writing.draft.$symbol.$guided"
    var strokes by remember(draftKey){mutableStateOf(s.decodeDrawing(s.worldText(draftKey)))}
    var current by remember(draftKey){mutableStateOf(emptyList<Offset>())}
    var message by remember(draftKey){mutableStateOf("")}
    var saved by remember(draftKey,strokes){mutableStateOf(false)}
    var demonstration by remember(draftKey){mutableStateOf(false)}
    var demoStroke by remember{mutableIntStateOf(0)}
    var demoProgress by remember{mutableFloatStateOf(0f)}
    var washing by remember{mutableStateOf(false)}
    val wave=remember{Animatable(0f)}
    LaunchedEffect(s.muted,s.audioEnglish){s.play("writing_instruction")}
    fun persist(lines:List<List<Offset>>){strokes=lines;message="";s.saveWorldText(draftKey,s.encodeDrawing(lines))}
    LaunchedEffect(demonstration,draftKey){if(demonstration){guide.indices.forEach{step->demoStroke=step;repeat(41){demoProgress=it/40f;delay(24)}};demonstration=false}}
    LaunchedEffect(washing){if(washing){wave.snapTo(0f);wave.animateTo(1f,tween(1000));persist(emptyList());washing=false}}
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(s.text(if(gallery)"作品簿" else "沙灘畫畫",if(gallery)"Sketchbook" else "Sand drawing"),Modifier.weight(1f),fontSize=24.sp);TextButton(onClick={gallery=!gallery}){Text(s.text(if(gallery)"回沙灘" else "作品簿",if(gallery)"Back" else "Sketchbook"))}}
    if(gallery){
        if(s.drawings.isEmpty())Text(s.text("畫一張，再放進作品簿。","Draw something and save it here."))
        s.drawings.forEach{drawing->Card{Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(drawing.symbol,fontSize=24.sp);Canvas(Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFEFDEB2))){drawLines(drawing.strokes,Color(0xFF775537))}}}}
        return
    }
    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){categories.forEachIndexed{i,name->OutlinedButton(onClick={s.saveWorld("writing.category",i);s.saveWorld("writing.index",0)},modifier=Modifier.weight(1f),contentPadding=PaddingValues(2.dp)){Text(name)}}}
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)){symbols.forEachIndexed{i,char->OutlinedButton(onClick={s.saveWorld("writing.index",i);if(categoryIndex==1)s.play("zhuyin_${char.code.toString(16)}","zh")else if(categoryIndex>=2)s.play("letter_${char.uppercaseChar()}","en")},modifier=Modifier.sizeIn(minWidth=56.dp,minHeight=56.dp),contentPadding=PaddingValues(5.dp)){Text(char.toString(),fontSize=22.sp)}}}
    Row(verticalAlignment=Alignment.CenterVertically){Text(s.text("跟著畫","Trace"));Switch(guided,{s.saveWorld("writing.guided",if(it)1 else 0)});TextButton(onClick={demonstration=true}){Text(s.text("看示範","Watch"))};if(categoryIndex==1)TextButton(onClick={s.play("zhuyin_${symbol[0].code.toString(16)}","zh")}){Text(s.text("發音","Sound"))}}
    Box(Modifier.widthIn(max=paperMax).fillMaxWidth().aspectRatio(1f).background(Color(0xFFEFDEB2),RoundedCornerShape(22.dp)),contentAlignment=Alignment.Center){
        if(!guided){if(categoryIndex==1)OfficialZhuyin(s,symbol,Modifier.fillMaxSize().padding(25.dp),Color(0xFFD2BB8A))else Text(symbol,color=Color(0xFFD2BB8A),fontSize=(paperMax.value*.6f).sp)}
        Canvas(Modifier.fillMaxSize().pointerInput(draftKey,demonstration,washing){
            awaitEachGesture{
                val down=awaitFirstDown(requireUnconsumed=false)
                if(!demonstration&&!washing){
                    down.consume();current=listOf(Offset(down.position.x/size.width,down.position.y/size.height))
                    var cancelled=false
                    do{
                        val event=awaitPointerEvent();val change=event.changes.firstOrNull{it.id==down.id}
                        if(change==null){cancelled=true;break}
                        change.consume();current=current+Offset((change.position.x/size.width).coerceIn(0f,1f),(change.position.y/size.height).coerceIn(0f,1f))
                    }while(change.pressed)
                    if(!cancelled){
                        val input=if(current.size<3)current+current.last()else current
                        if(!guided)persist(strokes+listOf(input))
                        else if(strokes.size<guide.size&&Tracing.matches(input,guide[strokes.size])){
                            persist(strokes+listOf(input));message=s.text("這一筆好了。","Stroke done.")
                            if(strokes.size==guide.size){s.mark("writing.$symbol.guided");message=s.text("完成了！","All done!")}
                        }else message=s.text("從起點慢慢畫。","Start at the dot and go slowly.")
                    }
                    current=emptyList();if(s.due)s.beginRest()
                }
            }
        }){
            if(guided||demonstration){
                drawLines(guide,Color(0xFFD2BB8A),14f)
                if(guided&&strokes.size<guide.size){val p=guide[strokes.size][0];drawCircle(Color(0xFF43856B),8.dp.toPx(),Offset(p.x*size.width,p.y*size.height))}
                if(demonstration){val points=guide[demoStroke.coerceAtMost(guide.lastIndex)];val position=demoProgress*(points.size-1);val segment=position.toInt().coerceAtMost(points.lastIndex);val a=points[segment];val b=points[(segment+1).coerceAtMost(points.lastIndex)];val p=a+(b-a)*(position-segment);drawCircle(Color(0xFF43856B),10.dp.toPx(),Offset(p.x*size.width,p.y*size.height))}
            }
            drawLines(strokes+listOf(current),Color(0xFF775537))
            if(washing){val x=-size.width+wave.value*size.width*2;val path=Path().apply{moveTo(x,0f);for(i in 0..20){val y=size.height*i/20;lineTo(x+sin(i.toFloat()*.6f+wave.value*6)*15.dp.toPx(),y)};lineTo(x+size.width,size.height);lineTo(x+size.width,0f);close()};drawPath(path,Color(0xFF9BCDD7))}
        }
    }
    if(message.isNotBlank())Text(message)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){
        OutlinedButton(onClick={if(strokes.isNotEmpty())persist(strokes.dropLast(1))},enabled=!washing,modifier=Modifier.weight(1f).heightIn(min=56.dp),contentPadding=PaddingValues(3.dp)){Text(s.text("撤銷","Undo"),fontSize=13.sp)}
        OutlinedButton(onClick={washing=true},enabled=!washing,modifier=Modifier.weight(1f).heightIn(min=56.dp),contentPadding=PaddingValues(3.dp)){Text(s.text("洗平","Wash"),fontSize=13.sp)}
        Button(onClick={s.saveDrawing(symbol,strokes);saved=true},enabled=strokes.isNotEmpty()&&!saved&&!washing,modifier=Modifier.weight(1f).heightIn(min=56.dp),contentPadding=PaddingValues(3.dp)){Text(s.text(if(saved)"已保存" else "保存",if(saved)"Saved" else "Save"),fontSize=13.sp)}
        Button(onClick={if(strokes.isNotEmpty()&&!guided)s.mark("writing.$symbol.free");s.saveWorld("writing.index",(index+1)%symbols.length);if(s.due)s.beginRest()},modifier=Modifier.weight(1f).heightIn(min=56.dp),contentPadding=PaddingValues(3.dp)){Text(s.text("下一個","Next"),fontSize=13.sp)}
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLines(lines:List<List<Offset>>,color:Color,width:Float=7f){
    lines.forEach{points->if(points.isNotEmpty()){
        if(points.size<2||(points.first()-points.last()).getDistance()<.005f&&points.size<4)drawCircle(color,width.dp.toPx()/2,Offset(points.first().x*size.width,points.first().y*size.height))
        else{val path=Path().apply{moveTo(points.first().x*size.width,points.first().y*size.height);points.drop(1).forEach{lineTo(it.x*size.width,it.y*size.height)}};drawPath(path,color,style=Stroke(width.dp.toPx(),cap=StrokeCap.Round,join=StrokeJoin.Round))}
    }}
}

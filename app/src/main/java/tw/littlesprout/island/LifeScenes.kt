package tw.littlesprout.island

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable fun StationGame(s:IslandSession){Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){StationContents(s)}}
@Composable private fun StationContents(s:IslandSession){
    val round=s.worldValue("clock.round");val done=s.worldValue("clock.done")==1
    val mode = s.getSceneMode("clock")
    val tasks = GameplayRegistry.getTasks("clock", mode)
    val taskIndex = s.getTaskIndex("clock").coerceIn(0, maxOf(0, tasks.lastIndex))
    val currentTask = tasks.getOrNull(taskIndex) ?: tasks.firstOrNull() ?: IslandTask("clock_task", "clock", mode, "車站", "Station", "把時鐘調好，讓火車出發。", "Set the clock.", "station_instruction")
    val grade = when(mode) { GameMode.FREE -> 0; GameMode.STEP -> 2; GameMode.COMBO -> 3; GameMode.CHALLENGE -> 4 }
    val target = GameRules.clockTarget(round, grade); val time = s.worldValue("clock.time") % 720
    LaunchedEffect(round, s.muted, s.audioEnglish) {
        if (!done) {
            s.currentAudioPrompt = currentTask.audioId
            s.play(currentTask.audioId)
        }
    }
    var hand by remember { mutableIntStateOf(0) }
    ModeSelectorBar(s, "clock", mode)
    TaskPromptBar(s, currentTask)
    Text(GameRules.clockLabel(target), fontSize = 28.sp, color = Color(0xFF43856B))
    SceneStage("clock",done)
    val clockModifier=Modifier.widthIn(max=240.dp).fillMaxWidth().aspectRatio(1f).pointerInput(done){
        if(!done)detectDragGestures(onDragStart={point->
            val current=s.worldValue("clock.time")
            val a=atan2(point.y-size.height/2,point.x-size.width/2)*180/PI+90
            val angle=((a+360)%360).toFloat()
            fun difference(x:Float,y:Float)=min(abs(x-y),360-abs(x-y))
            hand=if(difference(angle,GameRules.hourAngle(current))<difference(angle,GameRules.minuteAngle(current)))0 else 1
        },onDrag={change,_->
            change.consume();val angle=((atan2(change.position.y-size.height/2,change.position.x-size.width/2)*180/PI+450)%360)
            val current=s.worldValue("clock.time")
            val next=if(hand==0)((angle/30).roundToInt()%12)*60+current%60 else{
                val step=if(grade>=4)5 else if(grade>=3)30 else 60
                val m=(((angle/6)/step).roundToInt()*step)%60
                var h=current/60;if(current%60>45&&m<15)h++ else if(current%60<15&&m>45)h--
                ((h+12)%12)*60+m
            }
            s.saveWorld("clock.time",next)
        })
    }
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
        Box(Modifier.weight(1f)){ClockFace(if(time/60==0)12 else time/60,time%60,clockModifier)}
        Column(Modifier.width(105.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text(GameRules.clockLabel(time),fontSize=24.sp)
            if(!done){
                Row{OutlinedButton(onClick={s.saveWorld("clock.time",(time+660)%720)},modifier=Modifier.weight(1f),contentPadding=PaddingValues(2.dp)){Text(s.text("時−","H−"))};OutlinedButton(onClick={s.saveWorld("clock.time",(time+60)%720)},modifier=Modifier.weight(1f),contentPadding=PaddingValues(2.dp)){Text(s.text("時＋","H+"))}}
                val step=if(grade>=4)5 else 30
                Row{OutlinedButton(onClick={s.saveWorld("clock.time",(time+720-step)%720)},modifier=Modifier.weight(1f),contentPadding=PaddingValues(2.dp)){Text(s.text("分−","M−"))};OutlinedButton(onClick={s.saveWorld("clock.time",(time+step)%720)},modifier=Modifier.weight(1f),contentPadding=PaddingValues(2.dp)){Text(s.text("分＋","M+"))}}
            }
        }
    }
    if(!done){
        Button(onClick={s.completeGame("clock")},enabled=time==target,modifier=Modifier.heightIn(min=64.dp)){Text(s.text("火車出發","Go, train!"),fontSize=22.sp)}
    }else{
        LaunchedEffect(round){delay(1200);if(s.due)s.beginRest()}
        Button(onClick={s.nextGame("clock")},modifier=Modifier.heightIn(min=64.dp)){Text(s.text("下一班","Next train"))}
    }
}

@Composable fun WeatherHouse(s:IslandSession){Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){WeatherContents(s)}}
@Composable private fun WeatherContents(s:IslandSession){
    val round=s.worldValue("weather.round")
    val mode = s.getSceneMode("weather")
    val tasks = GameplayRegistry.getTasks("weather", mode)
    val taskIndex = s.getTaskIndex("weather").coerceIn(0, maxOf(0, tasks.lastIndex))
    val currentTask = tasks.getOrNull(taskIndex) ?: tasks.firstOrNull() ?: IslandTask("weather_task", "weather", mode, "小屋", "House", "觀察天氣並準備出門。", "Observe weather.", "weather_question")
    val difficulty = when(mode) { GameMode.FREE -> 0; GameMode.STEP -> 1; GameMode.COMBO -> 2; GameMode.CHALLENGE -> 3 }
    val kind = s.worldValue("weather.kind").coerceIn(0, 3)
    val equipment = s.worldValue("weather.equipment") - 1
    val done = s.worldValue("weather.done") == 1
    val target = round % Lessons.weather.size
    val listening = difficulty == 3 && !s.muted
    val needsWeather = difficulty >= 2
    val observed = !needsWeather || s.worldValue("weather.observed") == 1
    var hint by remember(round, difficulty, kind) { mutableStateOf("") }
    LaunchedEffect(round, difficulty, s.muted) {
        if (!done) {
            s.currentAudioPrompt = currentTask.audioId
            s.play(if (needsWeather) Lessons.weather[target].id else currentTask.audioId)
        }
    }
    ModeSelectorBar(s, "weather", mode)
    TaskPromptBar(s, currentTask)
    Text("How is the weather today?", fontSize = 24.sp)
    if(needsWeather&&!observed){
        if(listening)TextButton(onClick={s.play(Lessons.weather[target].id,"en")}){Text(s.text("再聽一次","Listen again"))}
        else if(difficulty==2&&!s.muted)Text("It's ${Lessons.weather[target].id}!",fontSize=24.sp)
        else WeatherArt(target,Modifier.size(64.dp))
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
        Lessons.weather.forEachIndexed{index,item->
            OutlinedButton(onClick={
                if(needsWeather&&index!=target){hint=s.text("再看看天氣。","Look at the weather again.")}
                else{
                    hint="";s.saveWorld("weather.kind",index);s.saveWorld("weather.equipment",0);s.saveWorld("weather.done",0);s.saveWorld("weather.observed",1);s.selected=item.id;s.play(item.id,"en");s.mark("weather.${item.id}.${if(needsWeather&&listening)"listening" else "explored"}")
                }
            },enabled=!done,modifier=Modifier.weight(1f),contentPadding=PaddingValues(vertical=10.dp,horizontal=2.dp)){
                Column(horizontalAlignment=Alignment.CenterHorizontally){WeatherArt(index,Modifier.size(42.dp));Text(s.text(item.zh,item.en),fontSize=14.sp)}
            }
        }
    }
    if(observed){
        Text("It's ${Lessons.weather[kind].id}!",fontSize=24.sp,color=Color(0xFF43856B))
        SceneStage("weather",done,weather=kind,equipment=equipment)
        Text(s.text("幫小兔子準備出門。","Help Rabbit get ready."))
        if(!done)Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
            listOf("umbrella","hat","jacket").forEachIndexed{index,id->ObjectButton(id,{
                s.saveWorld("weather.equipment",index+1)
                if(difficulty==0||GameRules.weatherAccepts(kind,index))s.completeGame("weather")
                else hint=s.text(if(kind==2)"下雨需要能擋雨的東西。" else "風大，試試帽子或外套。",if(kind==2)"Choose something to keep the rain off." else "Try a hat or jacket for the wind.")
            },Modifier.weight(1f))}
        }else{
            LaunchedEffect(kind){delay(1200);if(s.due)s.beginRest()}
            Button(onClick={s.saveWorld("weather.equipment",0);s.saveWorld("weather.observed",0);s.nextGame("weather")},modifier=Modifier.heightIn(min=64.dp)){Text(s.text("再玩一次","Play again"))}
        }
    }
    if(hint.isNotBlank()&&!done)Text(hint)
}

@Composable fun WeatherArt(kind:Int,modifier:Modifier){
    Canvas(modifier){val u=size.minDimension/100;scale(u,u,Offset.Zero){
        if(kind==0)art("sun")else if(kind==3){repeat(3){i->drawLine(Color(0xFF72ACCA),Offset(10f,25f+i*23),Offset(85f-i*8,25f+i*23),6f,androidx.compose.ui.graphics.StrokeCap.Round)}}else{
            drawOval(Color(0xFF9AB7C0),Offset(10f,30f),Size(80f,38f));drawCircle(Color(0xFF9AB7C0),25f,Offset(45f,32f))
            if(kind==2)repeat(4){i->drawLine(Color(0xFF72ACCA),Offset(25f+i*17,74f),Offset(19f+i*17,89f),4f)}
        }
    }}
}

@Composable fun OfficialZhuyin(s:IslandSession,symbol:String,modifier:Modifier,color:Color=Color(0xFF43856B)){
    if(!s.zhuyinData.has(symbol))return
    val strokes=s.zhuyinData.getJSONObject(symbol).getJSONArray("outlines")
    Canvas(modifier){val u=size.minDimension/2048;scale(u,u,Offset.Zero){
        repeat(strokes.length()){i->
            val commands=strokes.getJSONArray(i);val path=Path()
            fun org.json.JSONObject.point(key:String):Offset{val p=getJSONObject(key);return Offset(p.getDouble("x").toFloat(),p.getDouble("y").toFloat())}
            repeat(commands.length()){j->val c=commands.getJSONObject(j);when(c.getString("type")){
                "M"->path.moveTo(c.getDouble("x").toFloat(),c.getDouble("y").toFloat())
                "L"->path.lineTo(c.getDouble("x").toFloat(),c.getDouble("y").toFloat())
                "Q"->{val b=c.point("begin");val e=c.point("end");path.quadraticTo(b.x,b.y,e.x,e.y)}
                "C"->{val b=c.point("begin");val m=c.point("mid");val e=c.point("end");path.cubicTo(b.x,b.y,m.x,m.y,e.x,e.y)}
                "Z"->path.close()
            }}
            path.close();drawPath(path,color)
        }
    }}
}

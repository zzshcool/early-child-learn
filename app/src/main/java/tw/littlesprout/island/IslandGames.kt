package tw.littlesprout.island

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val SeaGreen=Color(0xFF43856B)

@Composable fun IslandMap(s:IslandSession){
    val preferred=when(s.topic){"fruit"->"fruit";"animals"->"delivery";"weather"->"weather";else->""}
    val destinations=listOf("beach" to s.text("海灘","Beach"),"bridge" to s.text("小橋","Bridge"),"fruit" to s.text("水果攤","Fruit stand"),"delivery" to s.text("碼頭","Harbor"),"writing" to s.text("沙灘畫畫","Sand drawing"),"clock" to s.text("車站","Station"),"weather" to s.text("小屋","House")).sortedBy{if(it.first==preferred)0 else 1}
    BoxWithConstraints(Modifier.fillMaxWidth()){
        val columns=if(maxWidth>650.dp)3 else 2
        Column(verticalArrangement=Arrangement.spacedBy(20.dp)){
            destinations.chunked(columns).forEach{items->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(16.dp)){
                items.forEach{(id,label)->Card(Modifier.weight(1f).clickable{s.sailTo(id)},shape=RoundedCornerShape(35.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFEDDDAF))){
                    SceneThumbnail(id,Modifier.fillMaxWidth().height(110.dp))
                    Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.Center){Text(label,fontSize=20.sp,color=Color(0xFF283B36));if(s.worldValue("$id.wins")>0)Text("  ✓",color=SeaGreen)}
                    Row(Modifier.fillMaxWidth().padding(bottom=12.dp),horizontalArrangement=Arrangement.Center){repeat(4){index->Canvas(Modifier.padding(horizontal=3.dp).size(8.dp)){drawCircle(if(s.worldValue("$id.wins")>index*3)SeaGreen else Color(0xFFD1BC85))}}}
                }}
                repeat(columns-items.size){Spacer(Modifier.weight(1f))}
            }}
        }
    }
}

@Composable fun IslandGame(s:IslandSession,scene:String){
    Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){
    val round=s.worldValue("$scene.round");val done=s.worldValue("$scene.done")==1
    var hint by remember(scene,round){mutableStateOf("")}
    val titles=mapOf("beach" to ("海灘" to "Beach"),"bridge" to ("小橋" to "Bridge"),"fruit" to ("水果攤" to "Fruit stand"),"delivery" to ("碼頭" to "Harbor"))
    val defaultDifficulty=listOf(0,0,1,2,3)[s.grade]
    val difficulty=if(s.world.has("$scene.difficulty"))s.worldValue("$scene.difficulty").coerceIn(0,3)else defaultDifficulty
    val effectiveGrade=listOf(0,2,3,4)[difficulty]
    LaunchedEffect(scene,round,s.muted,s.audioEnglish){if(!done&&scene in listOf("bridge","fruit"))s.play("${scene}_instruction")}
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){Text(titles[scene]!!.let{s.text(it.first,it.second)},Modifier.weight(1f),fontSize=20.sp,color=SeaGreen);repeat(4){level->OutlinedButton(onClick={s.saveWorld("$scene.difficulty",level);s.saveWorld("$scene.round",0);s.saveWorld("$scene.done",0);s.saveWorld("$scene.count",0);s.saveWorld("$scene.second",0)},contentPadding=PaddingValues(4.dp),modifier=Modifier.sizeIn(minWidth=42.dp,minHeight=48.dp)){Text((if(level==difficulty)"● " else "")+"${level+1}")}}}
    when(scene){
        "beach"->BeachGame(s,effectiveGrade,round,done)
        "bridge"->{
            val target=round%3;val selected=s.worldValue("bridge.selected")-1
            Text(s.text("選一塊木板，讓小兔子過河。","Choose a plank so Rabbit can cross."));SceneStage(scene,done,plank=selected,gap=target)
            if(!done)Column(verticalArrangement=Arrangement.spacedBy(14.dp)){repeat(3){index->Box(Modifier.fillMaxWidth(.5f+index*.2f).height(64.dp).background(Color(0xFFBC864E),RoundedCornerShape(12.dp)).semantics{contentDescription="plank ${index+1}"}.clickable{
                s.saveWorld("bridge.selected",index+1);if(index==target)s.completeGame(scene)else hint=s.text(if(index<target)"還接不到對岸。" else "木板太長了。",if(index<target)"It doesn't reach." else "Too long.")
            }){Canvas(Modifier.fillMaxSize()){repeat(3){i->drawLine(Color(0xFF9D6F42),Offset(10f,size.height*(i+1)/4),Offset(size.width-10,size.height*(i+1)/4),2f)}}}}}
        }
        "fruit"->{
            val(target,second)=GameRules.fruitTargets(round,effectiveGrade);val count=s.worldValue("fruit.count");val pears=s.worldValue("fruit.second");val discovery=difficulty==0;val showPears=discovery||second>0
            Text(s.text("幫小兔子裝水果。","Pack fruit for Rabbit."))
            val expression=if(effectiveGrade<4)"$target" else if(round%2==1&&target<=18)"${target+2} − 2" else "${target/2} ＋ ${target-target/2}"
            if(!discovery)Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){WordArt("apple",Modifier.size(52.dp));Text("× $expression",fontSize=26.sp);if(second>0){WordArt("pear",Modifier.size(52.dp));Text("× $second",fontSize=26.sp)}}
            SceneStage(scene,done,count=count,secondCount=pears)
            if(!done){
                Text(if(discovery)"${count+pears}" else (if(effectiveGrade<4)"$count / $target" else "$count")+if(second>0)"    $pears / $second" else "",fontSize=26.sp,color=SeaGreen)
                Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){ObjectButton("apple",{if(if(discovery)count+pears<20 else count<target+2)s.saveWorld("fruit.count",count+1)},Modifier.weight(1f));if(showPears)ObjectButton("pear",{if(if(discovery)count+pears<20 else pears<second+2)s.saveWorld("fruit.second",pears+1)},Modifier.weight(1f))}
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){OutlinedButton(onClick={if(count>0)s.saveWorld("fruit.count",count-1)}){Text(s.text("拿回蘋果","Remove apple"))};if(showPears)OutlinedButton(onClick={if(pears>0)s.saveWorld("fruit.second",pears-1)}){Text(s.text("拿回梨子","Remove pear"))}}
                Button(onClick={if(if(discovery)count+pears>0 else count==target&&pears==second){if(s.completeGame(scene)&&discovery)s.mark("fruit.explored")}else hint=s.text("看看訂單，調整一下數量。","Check the quantities on the order.")},enabled=!discovery||count+pears>0,modifier=Modifier.heightIn(min=64.dp)){Text(s.text("交給兔子","Give to Rabbit"),fontSize=22.sp)}
            }
        }
        "delivery"->{
            val letterIndex=s.worldValue("delivery.letter")%26;val letter=('A'.code+letterIndex).toChar().toString();val choices=s.words.filter{it.letter==letter};val target=choices[round%4]
            val listening=(if(s.world.has("delivery.hearing"))s.worldValue("delivery.hearing")==1 else difficulty>=2)&&!s.muted
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){('A'..'Z').forEachIndexed{index,c->OutlinedButton(onClick={s.saveWorld("delivery.letter",index);s.saveWorld("delivery.round",0);s.saveWorld("delivery.done",0);s.play("letter_$c","en")},modifier=Modifier.sizeIn(minWidth=56.dp,minHeight=56.dp),contentPadding=PaddingValues(8.dp)){Text(c.toString(),fontSize=22.sp)}}}
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Text("$letter ${letter.lowercase()}",fontSize=24.sp,color=SeaGreen)
                if(difficulty>0){Text(s.text("聽單字","Listen"));Switch(checked=listening,enabled=!s.muted,onCheckedChange={s.saveWorld("delivery.hearing",if(it)1 else 0);s.play("word_${target.id}","en")})}
                if(!s.muted)TextButton(onClick={s.play("word_${target.id}","en")}){Text(s.text("重播","Replay"))}
            }
            if(letter=="X")Text(s.text("找找單字裡的 X。","Find X in these words."))
            LaunchedEffect(round,letter,listening,s.muted){if(!done&&difficulty>0)s.play("word_${target.id}","en")}
            val cargo=if(done)s.worldText("delivery.cargo").ifBlank{target.id}else target.id
            if(done){SceneStage(scene,true,cargo=cargo);Text(s.words.first{it.id==cargo}.en,fontSize=26.sp)}
            else if(difficulty==0){Text(s.text("選一件，送到小屋。","Choose something to deliver."))}
            else if(!listening){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){WordArt("house",Modifier.size(54.dp));WordArt(target.id,Modifier.size(54.dp));Text(target.en,fontSize=20.sp)}}
            else Text(s.text("聽一聽，把東西送到小屋。","Listen and choose the delivery."))
            if(!done)choices.chunked(2).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){row.forEach{word->Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){ObjectButton(word.id,{
                if(difficulty==0||word.id==target.id){s.saveWorldText("delivery.cargo",word.id);if(s.completeGame(scene))s.mark("word.${word.id}.${if(difficulty==0)"explored" else if(listening)"listening" else "visual"}");if(difficulty==0)s.play("word_${word.id}","en")}
                else{hint=s.text("再試一次。","Try again.");s.play("word_${word.id}","en")}
            },Modifier.fillMaxWidth());Text(word.en,fontSize=18.sp)}}}}
        }
    }
    if(hint.isNotBlank()&&!done)Text(hint,color=SeaGreen,fontSize=18.sp)
    if(done){LaunchedEffect(scene,round){delay(1200);if(s.due)s.beginRest()};Button(onClick={s.nextGame(scene)},modifier=Modifier.heightIn(min=64.dp)){Text(s.text("再玩一次","Play again"),fontSize=22.sp)}}
    }
}

@Composable fun ObjectButton(id:String,onClick:()->Unit,modifier:Modifier=Modifier){
    Card(modifier.heightIn(min=76.dp).semantics{contentDescription=id}.clickable(onClick=onClick),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFFAEA)),shape=RoundedCornerShape(20.dp)){
        Box(Modifier.fillMaxWidth().padding(8.dp),contentAlignment=Alignment.Center){WordArt(id,Modifier.size(56.dp))}
    }
}

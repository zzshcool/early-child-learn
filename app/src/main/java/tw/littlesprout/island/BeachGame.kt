package tw.littlesprout.island

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun BeachGame(s:IslandSession,grade:Int,round:Int,done:Boolean){
    val mode=round%(if(grade<2)3 else 9)
    var hint by remember(round){mutableStateOf(false)}
    fun choose(correct:Boolean){if(correct)s.completeGame("beach")else hint=true}
    val motion by animateFloatAsState(if(done)1f else 0f,tween(700),label="beach-object")
    val prompt=when(mode){0->"beach_color";1->"beach_shape";2->if(round%2==0)"beach_big" else "beach_small";3->if(round%2==0)"beach_tall" else "beach_short";4->"beach_below";5->"beach_fruit";6->"beach_pattern";7->"beach_different";else->if(round%2==0)"beach_more" else "beach_less"}
    LaunchedEffect(round,s.muted,s.audioEnglish){if(!done)s.play(prompt)}
    when(mode){
        0->{
            val choices=Lessons.colors.take(if(grade<2)3 else 8);val target=choices[round%choices.size]
            Text(s.text("幫小兔子找這個顏色的傘。","Find this color umbrella for Rabbit."));Picture(target,Modifier.size(65.dp))
            SceneStage("beach",done,color=Color(target.value.toLong(16)))
            if(!done)choices.chunked(3).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){row.forEach{choice->Card(Modifier.weight(1f).semantics{contentDescription=choice.id}.clickable{choose(choice.id==target.id)}){Box(Modifier.fillMaxWidth().padding(14.dp),contentAlignment=Alignment.Center){Picture(choice,Modifier.size(58.dp))}}}}}
        }
        1->{
            val choices=Lessons.shapes.take(if(grade<2)3 else 6);val target=choices[(round/3)%choices.size]
            Text(s.text("把玩具放到一樣的形狀上。","Put the toy on the matching shape."))
            Box(Modifier.fillMaxWidth().height(170.dp),contentAlignment=Alignment.Center){
                Surface(color=Color(0xFFE1C690),shape=androidx.compose.foundation.shape.RoundedCornerShape(25.dp)){Box(Modifier.padding(24.dp)){Picture(target,Modifier.size(100.dp))}}
                if(done)Box(Modifier.offset(x=(-90*(1-motion)).dp).size(70.dp)){Picture(target,Modifier.fillMaxSize())}
            }
            val offered=(listOf(target)+choices.filter{it.id!=target.id}.take(2)).let{it.drop(round%it.size)+it.take(round%it.size)}
            if(!done)Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){offered.forEach{choice->Card(Modifier.weight(1f).semantics{contentDescription=choice.id}.clickable{choose(choice.id==target.id)}){Box(Modifier.padding(12.dp)){Picture(choice,Modifier.size(68.dp))}}}}
        }
        2,3->{
            val larger=round%2==0;val height=mode==3
            Text(s.text(if(height)if(larger)"找比較高的。" else "找比較矮的。" else if(larger)"找比較大的。" else "找比較小的。",if(height)if(larger)"Find the taller one." else "Find the shorter one." else if(larger)"Find the bigger one." else "Find the smaller one."))
            WordArt("rabbit",Modifier.size(75.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(20.dp)){repeat(2){index->
                Card(Modifier.weight(1f).semantics{contentDescription="compare ${index+1}"}.clickable(enabled=!done){choose((index==1)==larger)}){Canvas(Modifier.fillMaxWidth().height(150.dp)){
                    val u=size.minDimension;val factor=if(index==1).8f else .42f
                    if(height){drawRect(Color(0xFF5D9870),Offset(size.width*.38f,size.height*(1-factor)),Size(size.width*.24f,size.height*factor));drawLine(Color(0xFF775537),Offset(0f,size.height-2),Offset(size.width,size.height-2),3f)}
                    else drawCircle(Color(0xFF72ACCA),u*factor/2)
                }}
            }}
        }
        4->{
            Text(s.text("把球放在桌子下面。","Put the ball under the table."))
            Row(horizontalArrangement=Arrangement.spacedBy(18.dp)){repeat(2){index->Card(Modifier.weight(1f).semantics{contentDescription=if(index==0)"above" else "below"}.clickable(enabled=!done){choose(index==1)}){
                Canvas(Modifier.fillMaxWidth().height(155.dp)){
                    val w=size.width;val h=size.height;drawLine(Color(0xFFAA7D58),Offset(w*.12f,h*.5f),Offset(w*.88f,h*.5f),12f)
                    drawLine(Color(0xFFAA7D58),Offset(w*.22f,h*.5f),Offset(w*.22f,h*.9f),7f);drawLine(Color(0xFFAA7D58),Offset(w*.78f,h*.5f),Offset(w*.78f,h*.9f),7f)
                    drawCircle(Color(0xFFE58075),w*.15f,Offset(w*.5f,h*if(index==0).27f else .76f))
                }
            }}}
        }
        5->{
            Text(s.text("把水果放進籃子。","Put a fruit in the basket."))
            WordArt("box",Modifier.size(110.dp))
            if(!done)Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){listOf("apple","cat","pear").forEach{id->ObjectButton(id,{choose(id!="cat")},Modifier.weight(1f))}}
            else WordArt("apple",Modifier.offset(x=(70*(1-motion)).dp).size(80.dp))
        }
        6->{
            Text(s.text("接下來放哪個？","Which one comes next?"))
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){listOf("red","blue","red","blue","red").forEach{id->Picture(Lessons.colors.first{it.id==id},Modifier.weight(1f).height(50.dp))};Text("?",fontSize=32.sp)}
            if(!done)Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Lessons.colors.take(3).forEach{choice->Card(Modifier.weight(1f).semantics{contentDescription=choice.id}.clickable{choose(choice.id=="blue")}){Box(Modifier.padding(12.dp)){Picture(choice,Modifier.size(65.dp))}}}}
        }
        7->{
            Text(s.text("哪個不一樣？","Which one is different?"))
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){listOf("apple","pear","apple").forEach{id->ObjectButton(id,{if(!done)choose(id=="pear")},Modifier.weight(1f))}}
        }
        else->{
            val more=round%2==0
            Text(s.text(if(more)"哪邊比較多？" else "哪邊比較少？",if(more)"Which side has more?" else "Which side has fewer?"))
            Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){listOf(2,5).forEachIndexed{index,number->Card(Modifier.weight(1f).semantics{contentDescription="quantity $number"}.clickable(enabled=!done){choose((index==1)==more)}){Column(Modifier.padding(12.dp)){repeat(number){WordArt("apple",Modifier.size(42.dp))}}}}}
        }
    }
    if(done&&mode!=0)WordArt("rabbit",Modifier.offset(y=(-5*motion).dp).size(80.dp))
    if(hint&&!done)Text(s.text("再看看，試試另一個。","Look again. Try another one."))
}

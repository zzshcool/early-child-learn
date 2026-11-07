package tw.littlesprout.island

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class VisualChecksTest{
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Before fun showTestHost(){ui.activity.runOnUiThread{ui.activity.setShowWhenLocked(true);ui.activity.setTurnScreenOn(true)}}
    private val app get()=ApplicationProvider.getApplicationContext<Application>()
    private fun capture(name:String){
        ui.waitForIdle()
        val image=ui.onRoot().captureToImage().asAndroidBitmap()
        val folder=app.getExternalFilesDir("qa")!!.apply{mkdirs()}
        File(folder,"$name.png").outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)}
    }
    @Test fun renderEveryWordIllustration(){
        val s=IslandSession(app);var page by mutableIntStateOf(0)
        ui.setContent{MaterialTheme{Column(Modifier.fillMaxSize().background(Color(0xFFE4F3F4)).safeDrawingPadding().padding(8.dp)){
            s.words.chunked(26)[page].chunked(4).forEach{row->Row(Modifier.fillMaxWidth()){
                row.forEach{word->Column(Modifier.weight(1f).height(72.dp),horizontalAlignment=Alignment.CenterHorizontally){WordArt(word.id,Modifier.size(50.dp));Text(word.en,fontSize=10.sp)}}
                repeat(4-row.size){Spacer(Modifier.weight(1f))}
            }}
        }}}
        repeat(4){index->ui.runOnIdle{page=index};capture("words-$index")}
    }
    @Test fun renderEveryOfficialZhuyin(){
        val s=IslandSession(app)
        ui.setContent{Column(Modifier.fillMaxSize().background(Color.White).safeDrawingPadding().padding(10.dp)){
            Lessons.zhuyin.toList().chunked(5).forEach{row->Row(Modifier.fillMaxWidth()){row.forEach{char->Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){OfficialZhuyin(s,char.toString(),Modifier.size(57.dp));Text(char.toString(),fontSize=13.sp)}};repeat(5-row.size){Spacer(Modifier.weight(1f))}}}
        }}
        capture("official-zhuyin")
    }
    @Test fun renderAllSevenScenesAndMap(){
        app.getSharedPreferences("island",0).edit().clear().commit()
        val s=IslandSession(app);s.setup("2468");s.configure(silent=true)
        ui.setContent{IslandApp(s)}
        capture("map")
        listOf("beach","bridge","fruit","delivery","clock","weather","writing").forEach{scene->ui.runOnIdle{s.enter(scene)};capture(scene)}
    }
    @Test fun tabletLandscapeLayoutAndEverySceneRender(){
        val s=IslandSession(app);s.setup("2468");s.configure(silent=true)
        ui.setContent{CompositionLocalProvider(LocalDensity provides Density(1f)){Box(Modifier.requiredSize(1000.dp,700.dp).consumeWindowInsets(WindowInsets.systemBars).testTag("tablet")){IslandApp(s)}}}
        val folder=app.getExternalFilesDir("qa")!!.apply{mkdirs()}
        listOf("home","beach","bridge","fruit","delivery","clock","weather","writing").forEach{scene->
            ui.runOnIdle{if(scene=="home")s.navigate("home")else s.enter(scene)};ui.waitForIdle()
            val image=ui.onNodeWithTag("tablet").captureToImage().asAndroidBitmap()
            File(folder,"tablet-$scene.png").outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)}
        }
    }
}

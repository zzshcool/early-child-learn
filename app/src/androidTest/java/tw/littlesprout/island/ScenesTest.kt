package tw.littlesprout.island

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ScenesTest{
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private lateinit var s:IslandSession
    @Before fun setup(){ui.activity.runOnUiThread{ui.activity.setShowWhenLocked(true);ui.activity.setTurnScreenOn(true)};val app=ApplicationProvider.getApplicationContext<Application>();app.getSharedPreferences("island",0).edit().clear().commit();s=IslandSession(app);s.setup("2468");s.configure(silent=true)}
    @Test fun beachWrongColorDoesNotFinishAndRightColorSaves(){
        s.enter("beach");ui.setContent{IslandApp(s)}
        ui.onNode(hasContentDescription("blue") and hasClickAction()).performScrollTo().performClick();assertEquals(0,s.worldValue("beach.done"))
        ui.onNode(hasContentDescription("red") and hasClickAction()).performScrollTo().performClick();assertEquals(1,s.worldValue("beach.done"))
    }
    @Test fun laterShapeRoundStillOffersItsCorrectAnswer(){
        s.configure(level=4);s.enter("beach");s.saveWorld("beach.round",10);ui.setContent{IslandApp(s)}
        ui.onNode(hasContentDescription("rectangle") and hasClickAction()).performScrollTo().performClick()
        assertEquals(1,s.worldValue("beach.done"))
    }
    @Test fun bridgeTooLongCanBeReplacedWithoutLosingRound(){
        s.enter("bridge");ui.setContent{IslandApp(s)}
        ui.onNodeWithContentDescription("plank 3").performScrollTo().performClick();assertEquals(0,s.worldValue("bridge.done"))
        ui.onNodeWithContentDescription("plank 1").performScrollTo().performClick();assertEquals(1,s.worldValue("bridge.done"))
    }
    @Test fun deliveryFinishesOnlyForOrderedObject(){
        s.enter("delivery");s.saveWorld("delivery.difficulty",1);ui.setContent{IslandApp(s)}
        ui.onNode(hasContentDescription("ant") and hasClickAction()).performScrollTo().performClick();assertEquals(0,s.worldValue("delivery.done"))
        ui.onNode(hasContentDescription("apple") and hasClickAction()).performScrollTo().performClick();assertEquals(1,s.worldValue("delivery.done"));assertTrue(s.completed.contains("word.apple.visual"))
    }
    @Test fun discoveryDeliveryShipsTheObjectChosenByTheChild(){
        s.enter("delivery");s.saveWorld("delivery.difficulty",0);ui.setContent{IslandApp(s)}
        ui.onNode(hasContentDescription("ant") and hasClickAction()).performScrollTo().performClick()
        assertEquals("ant",s.worldText("delivery.cargo"));assertEquals(1,s.worldValue("delivery.wins"))
        assertTrue(s.completed.contains("word.ant.explored"));assertFalse(s.completed.contains("word.ant.listening"))
    }
    @Test fun stationDepartureRequiresExactTime(){
        s.enter("clock");ui.setContent{IslandApp(s)}
        ui.onNodeWithText("火車出發").performScrollTo().assertIsNotEnabled()
        ui.runOnIdle{s.saveWorld("clock.time",180)}
        ui.onNodeWithText("火車出發").performScrollTo().assertIsEnabled().performClick();assertEquals(1,s.worldValue("clock.done"))
    }
    @Test fun rainyDayHatCanBeChangedToUmbrella(){
        s.enter("weather");s.saveWorld("weather.difficulty",1);s.saveWorld("weather.kind",2);ui.setContent{IslandApp(s)}
        ui.onNode(hasContentDescription("hat") and hasClickAction()).performScrollTo().performClick();assertEquals(0,s.worldValue("weather.done"))
        ui.onNode(hasContentDescription("umbrella") and hasClickAction()).performScrollTo().performClick();assertEquals(1,s.worldValue("weather.done"))
    }
    @Test fun discoveryFruitAcceptsAChildChosenMixedBasket(){
        s.enter("fruit");s.saveWorld("fruit.difficulty",0);ui.setContent{IslandApp(s)}
        ui.onNodeWithText("交給兔子").performScrollTo().assertIsNotEnabled()
        ui.onNode(hasContentDescription("pear") and hasClickAction()).performScrollTo().performClick()
        repeat(2){ui.onNode(hasContentDescription("apple") and hasClickAction()).performScrollTo().performClick()}
        ui.onNodeWithText("交給兔子").performScrollTo().performClick()
        assertEquals(1,s.worldValue("fruit.done"));assertEquals(2,s.worldValue("fruit.count"));assertEquals(1,s.worldValue("fruit.second"));assertTrue(s.completed.contains("fruit.explored"))
    }
    @Test fun numberedFruitOrderStillRequiresExactQuantities(){
        s.enter("fruit");s.saveWorld("fruit.difficulty",1);ui.setContent{IslandApp(s)}
        repeat(2){ui.onNode(hasContentDescription("apple") and hasClickAction()).performScrollTo().performClick()}
        ui.onNodeWithText("交給兔子").performScrollTo().performClick();assertEquals(0,s.worldValue("fruit.done"))
        ui.onNodeWithText("拿回蘋果").performScrollTo().performClick();ui.onNodeWithText("交給兔子").performScrollTo().performClick();assertEquals(1,s.worldValue("fruit.done"))
    }
    @Test fun discoveryWeatherDoesNotRejectAChildsOutfit(){
        s.enter("weather");s.saveWorld("weather.difficulty",0);s.saveWorld("weather.kind",2);ui.setContent{IslandApp(s)}
        ui.onNode(hasContentDescription("hat") and hasClickAction()).performScrollTo().performClick();assertEquals(1,s.worldValue("weather.done"))
        assertFalse(s.completed.any{it.endsWith("listening")})
    }
    @Test fun quietWeatherChallengeUsesPicturesAndRestoresSelection(){
        s.enter("weather");s.saveWorld("weather.difficulty",3);s.saveWorld("weather.round",2);ui.setContent{IslandApp(s)}
        ui.onNodeWithText("晴天").performScrollTo().performClick();assertEquals(0,s.worldValue("weather.observed"))
        ui.onNodeWithText("下雨").performScrollTo().performClick();assertEquals(1,s.worldValue("weather.observed"))
        assertEquals(2,IslandSession(ApplicationProvider.getApplicationContext()).worldValue("weather.kind"))
        ui.onNode(hasContentDescription("umbrella") and hasClickAction()).performScrollTo().performClick();assertEquals(1,s.worldValue("weather.done"))
        assertFalse(s.completed.contains("weather.rainy.listening"))
    }
    @Test fun allOfficialSymbolsHaveGuidesAndAudioAndDrawingSurvivesRestart(){
        val app=ApplicationProvider.getApplicationContext<Application>()
        Lessons.zhuyin.forEach{symbol->assertTrue(s.guide(symbol.toString())!!.isNotEmpty());app.assets.open("audio/zh/zhuyin_${symbol.code.toString(16)}.wav").close()}
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".forEach{assertNotNull(s.guide(it.toString()))}
        val lines=listOf(listOf(Offset(.2f,.2f),Offset(.5f,.5f),Offset(.8f,.8f)))
        s.saveDrawing("A",lines);val restored=IslandSession(app);assertEquals(lines,restored.drawings.first().strokes)
        s.clearProgress();assertTrue(IslandSession(app).drawings.isEmpty())
    }
    @Test fun writingDraftRestoresAfterRestAndReentry(){
        s.enter("writing");s.saveWorldText("writing.draft.0.false",s.encodeDrawing(listOf(listOf(Offset(.2f,.2f),Offset(.5f,.7f)))))
        s.beginRest();val restored=IslandSession(ApplicationProvider.getApplicationContext());restored.start();assertEquals("rest",restored.page);restored.continueAfterRest();assertEquals("writing",restored.page)
        assertEquals(1,restored.decodeDrawing(restored.worldText("writing.draft.0.false")).size)
    }
}

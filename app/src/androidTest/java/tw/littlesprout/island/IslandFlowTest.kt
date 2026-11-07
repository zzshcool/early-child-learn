package tw.littlesprout.island

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class IslandFlowTest {
    @Test fun fruitDeliveryCompletesOnceAndWorldSurvivesRestart() {
        session.setup("2468"); session.configure(silent = true); session.enter("fruit")
        ui.setContent { IslandApp(session) }
        ui.onNode(hasContentDescription("apple") and hasClickAction()).performScrollTo().performClick()
        ui.onNodeWithText("交給兔子").performScrollTo().performClick()
        ui.runOnIdle {
            assertEquals(1, session.worldValue("fruit.wins"))
            val restored = IslandSession(ApplicationProvider.getApplicationContext())
            assertEquals(1, restored.worldValue("fruit.done"))
            assertEquals(1, restored.worldValue("fruit.count"))
        }
        ui.onNodeWithText("再玩一次").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(0, session.worldValue("fruit.count")); assertEquals(1, session.worldValue("fruit.round")) }
    }
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var session: IslandSession
    @Before fun prepare() {
        ui.activity.runOnUiThread { ui.activity.setShowWhenLocked(true); ui.activity.setTurnScreenOn(true) }
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("island", 0).edit().clear().commit()
        session = IslandSession(app)
        session.foreground = true
    }
    @Test fun wrongAnswerDoesNotCompleteAndCorrectAnswerPersists() {
        session.setup("2468"); session.configure(silent = true); session.enter("cognition")
        ui.setContent { IslandApp(session) }
        val lesson = Lessons.cognition(0).first()
        ui.runOnIdle { session.answer(lesson, lesson.choices.first { it.id != lesson.answer }) }
        assertTrue(session.completed.isEmpty())
        ui.runOnIdle { session.answer(lesson, lesson.choices.first { it.id == lesson.answer }); session.answer(lesson, lesson.choices.first { it.id == lesson.answer }) }
        assertEquals(1, session.completed.size)
        assertEquals(session.completed, IslandSession(ApplicationProvider.getApplicationContext()).completed)
        ui.onNodeWithText("下一個").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, session.lessonIndex)
    }
    @Test fun restSurvivesRestartAndBlocksNavigation() {
        session.setup("2468"); session.enter("numbers"); session.beginRest()
        val restored = IslandSession(ApplicationProvider.getApplicationContext())
        restored.start(); assertEquals("rest", restored.page)
        restored.navigate("home"); assertEquals("rest", restored.page)
        restored.continueAfterRest(); assertEquals("numbers", restored.page)
        assertEquals(0L, restored.elapsed)
    }
    @Test fun quietWeatherIsAnObservationAndDoesNotCompleteListening() {
        session.setup("2468"); session.configure(silent = true); session.enter("weather")
        ui.setContent { IslandApp(session) }
        ui.onNodeWithText("刮風").performClick()
        ui.onNodeWithText("It's windy!").assertIsDisplayed()
        assertTrue(session.completed.contains("weather.windy.explored"))
        assertFalse(session.completed.any { it.endsWith("listening") })
    }
    @Test fun clearPreservesSettingsAndPinButResetRemovesThem() {
        session.setup("2468"); session.configure(en = true, silent = true, level = 3)
        session.mark("number.1.visual"); session.clearProgress()
        assertTrue(session.completed.isEmpty()); assertTrue(session.english); assertTrue(session.checkPin("2468"))
        session.reset(); assertFalse(session.hasPin); assertFalse(session.onboarded)
    }
    @Test fun wordCatalogHasFourCardsForEveryLetterAndNoDuplicateIds() {
        assertEquals(104, session.words.size)
        assertEquals(104, session.words.map { it.id }.toSet().size)
        ('A'..'Z').forEach { letter -> assertEquals(4, session.words.count { it.letter == letter.toString() }) }
    }
    @Test fun boatJourneyIgnoresDoubleTapAndArrivesAtChosenHall() {
        session.setup("2468"); session.configure(silent = true)
        ui.setContent { IslandApp(session) }
        ui.runOnIdle { session.sailTo("numbers"); session.sailTo("english"); assertEquals("numbers", session.sailingTo) }
        ui.waitUntil(5000) { session.page == "numbers" && session.sailingTo == null }
        ui.onNodeWithText("有幾片葉子？").assertExists()
    }
}

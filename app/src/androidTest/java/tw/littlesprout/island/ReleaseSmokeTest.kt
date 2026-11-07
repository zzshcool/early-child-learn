package tw.littlesprout.island

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import androidx.test.platform.app.InstrumentationRegistry

/** Tests the actual production Activity, not a replacement Compose host. */
class ReleaseSmokeTest {
    val ui=createAndroidComposeRule<MainActivity>()
    private val cleanPreferences=object:ExternalResource(){override fun before(){InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("island",0).edit().clear().commit()}}
    @get:Rule val rules:RuleChain=RuleChain.outerRule(cleanPreferences).around(ui)
    @Before fun showOwnedTestWindow(){ui.activity.runOnUiThread{ui.activity.setShowWhenLocked(true);ui.activity.setTurnScreenOn(true)}}
    @Test fun productionCoverAndFirstRunSetupAreUsable(){
        ui.onNodeWithText("小芽學習島").assertExists()
        ui.onNodeWithText("開始玩").performScrollTo().performClick()
        ui.onAllNodes(hasSetTextAction()).onFirst().performScrollTo().performTextInput("2468")
        ui.onAllNodes(hasSetTextAction()).onLast().performScrollTo().performTextInput("2468")
        ui.onNodeWithText("準備好了，開始玩").performScrollTo().performClick()
        ui.onNodeWithText("海灘").assertExists()
        ui.onNodeWithText("水果攤").performScrollTo().performClick()
        ui.waitUntil(5000){ui.onAllNodesWithText("幫小兔子裝水果。").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasContentDescription("apple") and hasClickAction()).performScrollTo().performClick()
        ui.onNodeWithText("交給兔子").performScrollTo().performClick()
        ui.onNodeWithText("再玩一次").performScrollTo().assertIsDisplayed()
    }
}

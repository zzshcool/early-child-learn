package tw.littlesprout.island

import org.junit.Assert.*
import org.junit.Test

class GameplayModelTest {
    @Test
    fun allThirteenScenesExistAndHaveCorrectSections() {
        val scenes = GameplayRegistry.scenes
        assertEquals(13, scenes.size)
        val islandScenes = scenes.filter { it.section == "island" }
        val lifeScenes = scenes.filter { it.section == "life" }
        assertEquals(7, islandScenes.size)
        assertEquals(6, lifeScenes.size)

        // 檢查七個海島場景
        val islandIds = islandScenes.map { it.id }.toSet()
        assertTrue(islandIds.containsAll(listOf("beach", "bridge", "fruit", "delivery", "writing", "clock", "weather")))

        // 檢查六個生活場景
        val lifeIds = lifeScenes.map { it.id }.toSet()
        assertTrue(lifeIds.containsAll(listOf("cleaning", "sports", "toyroom", "dining", "shop", "garden")))
    }

    @Test
    fun eachSceneProvidesTasksForModes() {
        GameplayRegistry.scenes.forEach { scene ->
            val freeTasks = GameplayRegistry.getTasks(scene.id, GameMode.FREE)
            val stepTasks = GameplayRegistry.getTasks(scene.id, GameMode.STEP)
            assertTrue("Scene ${scene.id} should have free play tasks", freeTasks.isNotEmpty())
            assertTrue("Scene ${scene.id} should have step tasks", stepTasks.isNotEmpty())
        }
    }

    @Test
    fun fruitStandTasksVerifyPineappleScenarioAndSolvability() {
        val stepTasks = GameplayRegistry.getTasks("fruit", GameMode.STEP)
        val takeOutTask = stepTasks.firstOrNull { it.id == "fruit_step_1" }
        assertNotNull("Should have take out pineapple task", takeOutTask)
        assertEquals("拿出水果", takeOutTask!!.titleZh)
        assertEquals("pineapple", takeOutTask.targetItem)
        // UX03 檢查：拿出鳳梨題必須在籃內有鳳梨可拿
        assertTrue("Basket must have pineapple initially", takeOutTask.initialBasket.contains("pineapple"))
        assertTrue("Basket must have strawberry initially", takeOutTask.initialBasket.contains("strawberry"))

        val comboTasks = GameplayRegistry.getTasks("fruit", GameMode.COMBO)
        val strawberriesTask = comboTasks.firstOrNull { it.id == "fruit_combo_1" }
        assertNotNull("Should have combo strawberries task", strawberriesTask)
        assertEquals("照單補貨", strawberriesTask!!.titleZh)
        assertEquals(2, strawberriesTask.targetCount)
        // UX03 關鍵檢查：組合題桌上必須剛好有 2 顆草莓供放入，籃內必須有鳳梨供拿出
        assertEquals("Table must have 2 strawberries", 2, strawberriesTask.initialTable.count { it == "strawberry" })
        assertTrue("Basket must have pineapple to take out", strawberriesTask.initialBasket.contains("pineapple"))

        val chalTasks = GameplayRegistry.getTasks("fruit", GameMode.CHALLENGE)
        val fixOrderTask = chalTasks.firstOrNull { it.id == "fruit_chal_1" }
        assertNotNull("Should have fix order task", fixOrderTask)
        // 修正訂單：籃內有 2 顆蘋果與 1 顆多出的鳳梨
        assertEquals(2, fixOrderTask!!.initialBasket.count { it == "apple" })
        assertEquals(1, fixOrderTask.initialBasket.count { it == "pineapple" })
    }

    @Test
    fun sportsAndCleaningTasksHavePhysicalSourceAndTargetContainers() {
        // UX02 檢查：球場與清潔站任務均有明確來源與目標容器定義，非純文字按鈕
        val sportsTasks = GameplayRegistry.getTasks("sports", GameMode.STEP)
        val basketballTask = sportsTasks.firstOrNull { it.id == "sports_step_2" }
        assertNotNull(basketballTask)
        assertEquals("basketball_hoop", basketballTask!!.targetContainer)
        assertEquals("court", basketballTask.sourceContainer)

        val cleanTasks = GameplayRegistry.getTasks("cleaning", GameMode.COMBO)
        val truckTask = cleanTasks.firstOrNull { it.id == "clean_combo_1" }
        assertNotNull(truckTask)
        assertEquals("garbage_truck", truckTask!!.targetContainer)
        assertEquals("ground", truckTask.sourceContainer)
    }

    @Test
    fun englishThemesCoverFourCoreCategories() {
        val categories = EnglishThemes.categories
        assertEquals(4, categories.size)
        val ids = categories.map { it.id }.toSet()
        assertTrue(ids.containsAll(listOf("animals", "everyday", "fruit", "transport")))

        categories.forEach { cat ->
            assertTrue("Category ${cat.id} must have at least 8 words", cat.wordIds.size >= 8)
        }
    }

    @Test
    fun gameModeTitlesAreTraditionalChinese() {
        assertEquals("自由玩", GameMode.FREE.titleZh)
        assertEquals("一步任務", GameMode.STEP.titleZh)
        assertEquals("組合任務", GameMode.COMBO.titleZh)
        assertEquals("情境挑戰", GameMode.CHALLENGE.titleZh)
    }
}

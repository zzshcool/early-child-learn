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
    fun fruitStandTasksVerifyPineappleScenario() {
        val stepTasks = GameplayRegistry.getTasks("fruit", GameMode.STEP)
        val takeOutTask = stepTasks.firstOrNull { it.id == "fruit_step_1" }
        assertNotNull("Should have take out pineapple task", takeOutTask)
        assertEquals("拿出水果", takeOutTask!!.titleZh)
        assertEquals("pineapple", takeOutTask.targetItem)

        val comboTasks = GameplayRegistry.getTasks("fruit", GameMode.COMBO)
        val strawberriesTask = comboTasks.firstOrNull { it.id == "fruit_combo_1" }
        assertNotNull("Should have combo strawberries task", strawberriesTask)
        assertEquals("照單補貨", strawberriesTask!!.titleZh)
        assertEquals(2, strawberriesTask.targetCount)
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


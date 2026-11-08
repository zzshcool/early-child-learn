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

private val SeaGreen = Color(0xFF43856B)
private val CardBg = Color(0xFFFFFAEA)

@Composable
fun IslandMap(s: IslandSession) {
    val islandScenes = GameplayRegistry.scenes.filter { it.section == "island" }
    val lifeScenes = GameplayRegistry.scenes.filter { it.section == "life" }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
        // 分區 1: 海島探索
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(s.text("海島探索", "Island Exploration"), fontSize = 22.sp, color = SeaGreen)
            SceneGrid(s, islandScenes)
        }

        // 分區 2: 生活任務
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(s.text("生活互動任務", "Life Tasks"), fontSize = 22.sp, color = SeaGreen)
            SceneGrid(s, lifeScenes)
        }
    }
}

@Composable
private fun SceneGrid(s: IslandSession, scenes: List<GameplayRegistry.SceneInfo>) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth > 650.dp) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            scenes.chunked(columns).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowItems.forEach { item ->
                        Card(
                            Modifier.weight(1f).clickable { s.sailTo(item.id) },
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDDDAF))
                        ) {
                            SceneThumbnail(item.id, Modifier.fillMaxWidth().height(105.dp))
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(s.text(item.zh, item.en), fontSize = 18.sp, color = Color(0xFF283B36))
                                if (s.worldValue("${item.id}.wins") > 0) {
                                    Text("  ✓", color = SeaGreen, fontSize = 18.sp)
                                }
                            }
                            Row(
                                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(4) { index ->
                                    Canvas(Modifier.padding(horizontal = 3.dp).size(8.dp)) {
                                        drawCircle(if (s.worldValue("${item.id}.wins") > index * 2) SeaGreen else Color(0xFFD1BC85))
                                    }
                                }
                            }
                        }
                    }
                    repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun IslandGame(s: IslandSession, scene: String) {
    val mode = s.getSceneMode(scene)
    val tasks = GameplayRegistry.getTasks(scene, mode)
    val taskIndex = s.getTaskIndex(scene).coerceIn(0, maxOf(0, tasks.lastIndex))
    val currentTask = tasks.getOrNull(taskIndex) ?: tasks.firstOrNull() ?: IslandTask(
        id = "${scene}_task",
        scene = scene,
        mode = mode,
        titleZh = "探索任務",
        titleEn = "Task",
        promptZh = "跟著圖示完成任務。",
        promptEn = "Follow instructions to complete task.",
        audioId = "correct"
    )

    val done = s.worldValue("$scene.done") == 1
    var hint by remember(scene, mode, taskIndex) { mutableStateOf("") }

    LaunchedEffect(scene, mode, taskIndex, s.muted) {
        if (!done) {
            s.currentAudioPrompt = currentTask.audioId
            s.play(currentTask.audioId)
        }
    }

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 模式切換列 (替代舊的 1, 2, 3, 4)
        ModeSelectorBar(s, scene, mode)

        // 題目與重播控制列
        TaskPromptBar(s, currentTask)

        when (scene) {
            "fruit" -> FruitStandGame(s, currentTask, done) { correct ->
                if (correct) s.completeGame(scene) else hint = s.text("看看訂單或要求，再調整一下水果。", "Check the order and adjust fruit.")
            }
            "beach" -> BeachGameWrapper(s, currentTask, done)
            "bridge" -> BridgeGameWrapper(s, currentTask, done) { hint = it }
            "delivery" -> DeliveryHarborGame(s, currentTask, done) { hint = it }
            else -> LifeTasksGame(s, scene, currentTask, done) { correct ->
                if (correct) s.completeGame(scene) else hint = s.text("放錯位置或數量不對喔，再試試看！", "Not quite there. Try again!")
            }
        }

        if (hint.isNotBlank() && !done) {
            Text(hint, color = SeaGreen, fontSize = 18.sp)
        }

        if (done) {
            LaunchedEffect(scene, taskIndex) {
                delay(1200)
                if (s.due) s.beginRest()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedButton(onClick = { s.replayTask(scene); hint = "" }) {
                    Text(s.text("再玩這題", "Play again"))
                }
                Button(onClick = { s.nextTask(scene); hint = "" }) {
                    Text(s.text("下一題", "Next task"))
                }
            }
        }
    }
}

// 4. 水果攤完整互動範本 (UX03 & UX05 修正：PlayItem 唯一 ID、可解題庫、單件選取、視覺同步)
@Composable
private fun FruitStandGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    // 依據 task 配置產生具備唯一 ID 的水果物件 (保證同種水果可獨立選取與移動)
    val initialTable = remember(task.id) {
        if (task.initialTable.isNotEmpty()) {
            task.initialTable.mapIndexed { idx, type -> PlayItem("tbl_${type}_$idx", type) }
        } else {
            listOf("apple", "banana", "pineapple", "strawberry", "pear", "orange").mapIndexed { idx, type ->
                PlayItem("tbl_${type}_$idx", type)
            }
        }
    }
    val initialBasket = remember(task.id) {
        task.initialBasket.mapIndexed { idx, type -> PlayItem("bsk_${type}_$idx", type) }
    }

    var tableFruits by remember(task.id) { mutableStateOf(initialTable) }
    var basketFruits by remember(task.id) { mutableStateOf(initialBasket) }
    var selectedFruit by remember(task.id) { mutableStateOf<Pair<PlayItem, String>?>(null) } // (item, "table" | "basket")

    // 舞台同步顯示真實籃子內蘋果與梨子數量
    SceneStage(
        "fruit",
        done,
        count = basketFruits.count { it.type == "apple" },
        secondCount = basketFruits.count { it.type == "strawberry" || it.type == "pear" }
    )

    // 區域 1: 桌面 (Table)
    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            selectedFruit?.let { (item, loc) ->
                if (loc == "basket") {
                    basketFruits = basketFruits - item
                    tableFruits = tableFruits + item
                    selectedFruit = null
                }
            }
        },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedFruit?.second == "basket") Color(0xFFF3E7C4) else Color(0xFFF7EED8)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                s.text("水果攤桌面（點選水果拿取或放回）：", "Table Area (Tap fruit to move):"),
                fontSize = 14.sp,
                color = Color(0xFF775537)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tableFruits.forEach { fruit ->
                    val isSelected = selectedFruit?.first?.id == fruit.id
                    Card(
                        modifier = Modifier.size(68.dp).clickable {
                            selectedFruit = if (isSelected) null else fruit to "table"
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFDCECDC) else Color.White)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            WordArt(fruit.type, Modifier.size(46.dp))
                        }
                    }
                }
                if (tableFruits.isEmpty()) {
                    Text(
                        s.text("桌面上沒有水果", "Table is empty"),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }

    // 區域 2: 客人的籃子 (Basket)
    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            selectedFruit?.let { (item, loc) ->
                if (loc == "table") {
                    tableFruits = tableFruits - item
                    basketFruits = basketFruits + item
                    selectedFruit = null
                }
            }
        },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedFruit?.second == "table") Color(0xFFDCECDC) else Color(0xFFE8D7B8)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.text("小兔子的籃子：", "Rabbit's Basket:"), fontSize = 14.sp, color = SeaGreen)
                Text(s.text("共 ${basketFruits.size} 個", "Total: ${basketFruits.size}"), fontSize = 13.sp, color = SeaGreen)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                basketFruits.forEach { fruit ->
                    val isSelected = selectedFruit?.first?.id == fruit.id
                    Card(
                        modifier = Modifier.size(68.dp).clickable {
                            selectedFruit = if (isSelected) null else fruit to "basket"
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF9D6C4) else Color.White)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            WordArt(fruit.type, Modifier.size(46.dp))
                        }
                    }
                }
                if (basketFruits.isEmpty()) {
                    Text(
                        s.text("籃子是空的，把水果放進來吧！", "Basket is empty. Put fruit in!"),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }

    if (!done) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val correct = when (task.id) {
                        "fruit_step_1" -> basketFruits.none { it.type == "pineapple" } &&
                                          tableFruits.any { it.type == "pineapple" } &&
                                          basketFruits.any { it.type == "strawberry" }
                        "fruit_step_2" -> basketFruits.any { it.type == "pineapple" }
                        "fruit_combo_1" -> basketFruits.count { it.type == "strawberry" } == 2 &&
                                           basketFruits.none { it.type == "pineapple" }
                        "fruit_combo_2" -> basketFruits.count { it.type == "apple" } == 2 &&
                                           basketFruits.count { it.type == "pear" } == 1
                        "fruit_chal_1" -> basketFruits.count { it.type == "apple" } == 2 &&
                                          basketFruits.none { it.type == "pineapple" }
                        "fruit_chal_2" -> basketFruits.count { it.type == "orange" } == 1 &&
                                          basketFruits.none { it.type == "banana" }
                        else -> if (task.mode == GameMode.FREE) basketFruits.isNotEmpty() else basketFruits.isNotEmpty()
                    }
                    onVerify(correct)
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
            ) {
                Text(s.text("裝好了，交給小兔子", "Give Basket to Rabbit"), fontSize = 17.sp)
            }
        }
    }
}

// 橋樑包裝
@Composable
private fun BridgeGameWrapper(s: IslandSession, task: IslandTask, done: Boolean, onHint: (String) -> Unit) {
    val target = (s.getTaskIndex("bridge") + s.worldValue("bridge.round")) % 3
    val selected = s.worldValue("bridge.selected") - 1
    SceneStage("bridge", done, plank = selected, gap = target)

    if (!done) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { index ->
                Box(
                    Modifier.fillMaxWidth(0.5f + index * 0.2f).height(60.dp)
                        .background(Color(0xFFBC864E), RoundedCornerShape(12.dp))
                        .semantics { contentDescription = "plank ${index + 1}" }
                        .clickable {
                            s.saveWorld("bridge.selected", index + 1)
                            if (index == target) s.completeGame("bridge")
                            else onHint(s.text(if (index < target) "木板還接不到對岸喔。" else "這塊木板太長了。", if (index < target) "It doesn't reach." else "Too long."))
                        }
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        repeat(3) { i ->
                            drawLine(Color(0xFF9D6F42), Offset(10f, size.height * (i + 1) / 4), Offset(size.width - 10, size.height * (i + 1) / 4), 2f)
                        }
                    }
                }
            }
        }
    }
}

// 海灘包裝
@Composable
private fun BeachGameWrapper(s: IslandSession, task: IslandTask, done: Boolean) {
    val round = s.getTaskIndex("beach") + s.worldValue("beach.round")
    BeachGame(s, grade = 2, round = round, done = done)
}

// 碼頭送貨包裝
@Composable
private fun DeliveryHarborGame(s: IslandSession, task: IslandTask, done: Boolean, onHint: (String) -> Unit) {
    val letterIndex = s.worldValue("delivery.letter") % 26
    val letter = ('A'.code + letterIndex).toChar().toString()
    val choices = s.words.filter { it.letter == letter }
    val round = s.getTaskIndex("delivery")
    val target = choices[round % choices.size]
    val listening = task.mode == GameMode.COMBO && !s.muted

    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ('A'..'Z').forEachIndexed { index, c ->
            OutlinedButton(
                onClick = {
                    s.saveWorld("delivery.letter", index)
                    s.saveWorld("delivery.done", 0)
                    s.play("letter_$c", "en")
                },
                modifier = Modifier.sizeIn(minWidth = 50.dp, minHeight = 50.dp),
                contentPadding = PaddingValues(6.dp)
            ) {
                Text(c.toString(), fontSize = 18.sp)
            }
        }
    }

    val cargo = if (done) s.worldText("delivery.cargo").ifBlank { target.id } else target.id
    if (done) {
        SceneStage("delivery", true, cargo = cargo)
        Text(s.words.first { it.id == cargo }.en, fontSize = 24.sp, color = SeaGreen)
    } else {
        SceneStage("delivery", false, cargo = target.id)
        if (!listening) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                WordArt("house", Modifier.size(50.dp))
                WordArt(target.id, Modifier.size(50.dp))
                Text(target.en, fontSize = 20.sp, color = SeaGreen)
            }
        }
        choices.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { word ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        ObjectButton(word.id, {
                            if (task.mode == GameMode.FREE || word.id == target.id) {
                                s.saveWorldText("delivery.cargo", word.id)
                                s.completeGame("delivery")
                                s.play("word_${word.id}", "en")
                            } else {
                                onHint(s.text("再試試看別的貨物。", "Try another cargo."))
                                s.play("word_${word.id}", "en")
                            }
                        }, Modifier.fillMaxWidth())
                        Text(word.en, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ObjectButton(id: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier.heightIn(min = 72.dp).semantics { contentDescription = id }.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
            WordArt(id, Modifier.size(54.dp))
        }
    }
}

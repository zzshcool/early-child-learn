package tw.littlesprout.island

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val SeaGreen = Color(0xFF43856B)
private val CardBg = Color(0xFFFFFAEA)

@Composable
fun LifeTasksGame(s: IslandSession, scene: String) {
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
    var selectedItem by remember(scene, mode, taskIndex) { mutableStateOf<String?>(null) }

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
        // 模式切換列
        ModeSelectorBar(s, scene, mode)

        // 題目與重播列
        TaskPromptBar(s, currentTask)

        // 舞台場景展示
        SceneStage(scene, done)

        // 具體生活任務互動操作區
        when (scene) {
            "cleaning" -> CleaningStationGame(s, currentTask, done) { correct ->
                if (correct) s.completeGame(scene) else hint = s.text("放錯位置囉，再試試看！", "Not quite there. Try again!")
            }
            "sports" -> SportsCourtGame(s, currentTask, done) { correct ->
                if (correct) s.completeGame(scene) else hint = s.text("角度或目標不對喔，再試一次！", "Try aiming again!")
            }
            "toyroom" -> ToyRoomGame(s, currentTask, done) { correct ->
                if (correct) s.completeGame(scene) else hint = s.text("找找看最適合的格子。", "Look for the right place.")
            }
            "dining" -> DiningTableGame(s, currentTask, done) { correct ->
                if (correct) s.completeGame(scene) else hint = s.text("算算看，數量要剛好喔。", "Check the counts again.")
            }
            "shop" -> CornerShopGame(s, currentTask, done) { correct ->
                if (correct) s.completeGame(scene) else hint = s.text("核對一下清單上的物品。", "Check the items on the list.")
            }
            "garden" -> GardenGame(s, currentTask, done) { correct ->
                if (correct) s.completeGame(scene) else hint = s.text("看看土地的需要。", "Observe what the plant needs.")
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
fun ModeSelectorBar(s: IslandSession, scene: String, currentMode: GameMode) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameMode.entries.forEach { mode ->
            val selected = mode == currentMode
            OutlinedButton(
                onClick = { s.setSceneMode(scene, mode) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) Color(0xFFDCECDC) else Color.Transparent
                )
            ) {
                Text(
                    text = (if (selected) "● " else "") + s.text(mode.titleZh, mode.titleEn),
                    fontSize = 13.sp,
                    color = if (selected) SeaGreen else Color(0xFF283B36)
                )
            }
        }
    }
}

@Composable
fun TaskPromptBar(s: IslandSession, task: IslandTask) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1E8)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.text(task.titleZh, task.titleEn), fontSize = 18.sp, color = SeaGreen)
                    if (s.isTaskCompleted(task.scene, task.mode, s.getTaskIndex(task.scene))) {
                        Text("  ✓", color = SeaGreen, fontSize = 18.sp)
                    }
                }
                Text(s.text(task.promptZh, task.promptEn), fontSize = 15.sp, color = Color(0xFF283B36))
            }
            if (!s.muted) {
                TextButton(onClick = { s.play(task.audioId) }) {
                    Text(s.text("再聽一次", "Replay"), fontSize = 14.sp)
                }
            }
        }
    }
}

// 5.1 清潔站與垃圾車
@Composable
private fun CleaningStationGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var bagsInTruck by remember(task.id) { mutableIntStateOf(if (task.id == "clean_combo_2") 2 else 0) }
    var trashInBin by remember(task.id) { mutableIntStateOf(0) }
    var recycleInBox by remember(task.id) { mutableIntStateOf(0) }
    var selectedItem by remember(task.id) { mutableStateOf<String?>(null) }

    val neededTruck = if (task.id == "clean_combo_2") 5 else 3

    if (task.id == "clean_step_2") {
        // 垃圾與回收分開
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                if (selectedItem == "bag") { trashInBin++; selectedItem = null }
            }) {
                WordArt("trash_bin", Modifier.size(72.dp))
                Text(s.text("垃圾桶 ($trashInBin)", "Trash Bin ($trashInBin)"), fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                if (selectedItem == "bottle") { recycleInBox++; selectedItem = null }
            }) {
                WordArt("recycle_bin", Modifier.size(72.dp))
                Text(s.text("回收箱 ($recycleInBox)", "Recycle ($recycleInBox)"), fontSize = 14.sp)
            }
        }
        if (!done) {
            Text(s.text("點選物品，再點目的地容器：", "Tap item, then tap target bin:"), fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ItemCard("bag", selectedItem == "bag") { selectedItem = "bag" }
                ItemCard("bottle", selectedItem == "bottle") { selectedItem = "bottle" }
            }
            Button(
                onClick = { onVerify(trashInBin >= 1 && recycleInBox >= 1) },
                enabled = trashInBin >= 1 && recycleInBox >= 1,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(s.text("分類完成", "Done"))
            }
        }
    } else {
        // 垃圾裝車或單桶整理
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WordArt("garbage_truck", Modifier.size(80.dp))
                Text(s.text("車上: $bagsInTruck 袋", "On Truck: $bagsInTruck"), fontSize = 16.sp, color = SeaGreen)
            }
            if (task.id == "clean_chal_1") {
                // 拿走玩具
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WordArt("box", Modifier.size(60.dp))
                    Text(s.text("保留區", "Hold Area"), fontSize = 14.sp)
                }
            }
        }

        if (!done) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    bagsInTruck++
                    if (bagsInTruck == neededTruck) onVerify(true)
                }) {
                    Text(s.text("搬一袋上車", "Load 1 Bag"))
                }
                if (bagsInTruck > 0) {
                    OutlinedButton(onClick = { bagsInTruck-- }) {
                        Text(s.text("拿下多裝的", "Unload Bag"))
                    }
                }
            }
            if (task.id == "clean_chal_1") {
                Button(onClick = { onVerify(true) }, modifier = Modifier.padding(top = 6.dp)) {
                    Text(s.text("把玩具移出垃圾車", "Remove Toy to Safe Area"))
                }
            }
        }
    }
}

// 5.2 球場
@Composable
private fun SportsCourtGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var score by remember(task.id) { mutableIntStateOf(0) }
    var inBasket by remember(task.id) { mutableIntStateOf(0) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WordArt(if (task.id.contains("soccer")) "soccer_goal" else "basketball_hoop", Modifier.size(76.dp))
            Text(s.text("計分: $score", "Score: $score"), fontSize = 16.sp, color = SeaGreen)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WordArt("box", Modifier.size(64.dp))
            Text(s.text("收納籃 ($inBasket)", "Storage ($inBasket)"), fontSize = 14.sp)
        }
    }

    if (!done) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            if (task.id == "sports_step_1") {
                Button(onClick = {
                    inBasket++
                    if (inBasket >= 2) onVerify(true)
                }) {
                    Text(s.text("把球收進籃子", "Put Ball in Basket"))
                }
            } else {
                Button(onClick = {
                    score++
                    if (score >= (if (task.id == "sports_combo_1") 3 else 1)) onVerify(true)
                }) {
                    Text(s.text(if (task.id.contains("soccer")) "射門進球！" else "投籃進球！", "Shoot / Score!"))
                }
            }
        }
    }
}

// 5.3 玩具屋
@Composable
private fun ToyRoomGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var carInGarage by remember(task.id) { mutableStateOf(false) }
    var dollInBed by remember(task.id) { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Card(colors = CardDefaults.cardColors(containerColor = if (carInGarage) Color(0xFFDCECDC) else CardBg)) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                WordArt("toy_car", Modifier.size(48.dp))
                Text(s.text(if (carInGarage) "小車已歸位" else "車庫", if (carInGarage) "Car Parked" else "Garage"), fontSize = 14.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = if (dollInBed) Color(0xFFDCECDC) else CardBg)) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                WordArt("doll", Modifier.size(48.dp))
                Text(s.text(if (dollInBed) "娃娃已回床上" else "床鋪", if (dollInBed) "Doll on Bed" else "Bed"), fontSize = 14.sp)
            }
        }
    }

    if (!done) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = {
                carInGarage = true
                if (dollInBed || task.id == "toy_step_1") onVerify(true)
            }, enabled = !carInGarage) {
                Text(s.text("小車送回車庫", "Car to Garage"))
            }
            Button(onClick = {
                dollInBed = true
                if (carInGarage || task.id == "toy_step_1") onVerify(true)
            }, enabled = !dollInBed) {
                Text(s.text("娃娃送回床上", "Doll to Bed"))
            }
        }
    }
}

// 5.4 餐桌與點心
@Composable
private fun DiningTableGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var platesCount by remember(task.id) { mutableIntStateOf(0) }
    var cookiesCount by remember(task.id) { mutableIntStateOf(0) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WordArt("plate", Modifier.size(60.dp))
            Text(s.text("盤子: $platesCount", "Plates: $platesCount"), fontSize = 15.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WordArt("cookie", Modifier.size(60.dp))
            Text(s.text("餅乾: $cookiesCount", "Cookies: $cookiesCount"), fontSize = 15.sp)
        }
    }

    if (!done) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = {
                platesCount++
                if (platesCount >= 3 && (task.id != "dining_combo_1" || cookiesCount >= 6)) onVerify(true)
            }) {
                Text(s.text("放一個盤子", "Add Plate"))
            }
            Button(onClick = {
                cookiesCount += 2
                if (cookiesCount >= 6 && (task.id != "dining_step_1" || platesCount >= 3)) onVerify(true)
            }) {
                Text(s.text("分兩片餅乾", "Share 2 Cookies"))
            }
        }
    }
}

// 5.5 商店與送貨
@Composable
private fun CornerShopGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var milkInBasket by remember(task.id) { mutableStateOf(false) }
    var appleInBasket by remember(task.id) { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Card(colors = CardDefaults.cardColors(containerColor = if (milkInBasket) Color(0xFFDCECDC) else CardBg)) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                WordArt("milk", Modifier.size(50.dp))
                Text(s.text("牛奶", "Milk"), fontSize = 14.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = if (appleInBasket) Color(0xFFDCECDC) else CardBg)) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                WordArt("apple", Modifier.size(50.dp))
                Text(s.text("蘋果", "Apple"), fontSize = 14.sp)
            }
        }
    }

    if (!done) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = {
                milkInBasket = true
                if (appleInBasket || task.id == "shop_step_1") onVerify(true)
            }, enabled = !milkInBasket) {
                Text(s.text("放進牛奶", "Pick Milk"))
            }
            Button(onClick = {
                appleInBasket = true
                if (milkInBasket || task.id == "shop_step_1") onVerify(true)
            }, enabled = !appleInBasket) {
                Text(s.text("放進蘋果", "Pick Apple"))
            }
        }
    }
}

// 5.6 花園與動物照顧
@Composable
private fun GardenGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var seedsCount by remember(task.id) { mutableIntStateOf(0) }
    var watered by remember(task.id) { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WordArt("pot", Modifier.size(60.dp))
            Text(s.text("花盆播種: $seedsCount / 3", "Seeds: $seedsCount / 3"), fontSize = 15.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WordArt("watering_can", Modifier.size(60.dp))
            Text(s.text(if (watered) "已澆水長出花！" else "等待澆水", if (watered) "Watered & Bloom!" else "Need Water"), fontSize = 14.sp)
        }
    }

    if (!done) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = {
                seedsCount++
                if (seedsCount >= 3 && (task.id != "garden_combo_1" || watered)) onVerify(true)
            }, enabled = seedsCount < 3) {
                Text(s.text("播一顆種子", "Plant Seed"))
            }
            Button(onClick = {
                watered = true
                if (seedsCount >= 1 || task.id == "garden_step_2") onVerify(true)
            }, enabled = !watered) {
                Text(s.text("給泥土澆水", "Water Soil"))
            }
        }
    }
}

@Composable
private fun ItemCard(id: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(76.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFDCECDC) else CardBg)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WordArt(id, Modifier.size(54.dp))
        }
    }
}


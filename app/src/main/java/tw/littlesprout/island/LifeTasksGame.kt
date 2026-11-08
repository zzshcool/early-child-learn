package tw.littlesprout.island

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SeaGreen = Color(0xFF43856B)
private val CoralRed = Color(0xFFD9534F)

// 頂層題目與視覺化提示列 (UX04: 兒童圖像化指示，目標物 -> 容器)
@Composable
fun TaskPromptBar(
    s: IslandSession,
    task: IslandTask,
    onShowDemo: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1E8)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 文字與音訊重播
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(s.text(task.titleZh, task.titleEn), fontSize = 18.sp, color = SeaGreen)
                    if (s.isTaskCompleted(task.scene, task.mode, s.getTaskIndex(task.scene))) {
                        Text("  ✓", color = SeaGreen, fontSize = 18.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onShowDemo != null) {
                        OutlinedButton(
                            onClick = onShowDemo,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(s.text("看怎麼玩", "Demo"), fontSize = 12.sp)
                        }
                    }
                    if (!s.muted) {
                        TextButton(
                            onClick = { s.play(task.audioId) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(s.text("🔊 再聽一次", "🔊 Replay"), fontSize = 13.sp)
                        }
                    }
                }
            }

            // 幼兒圖像提示區 (UX04 核心：目標物 -> 目的地、數量格)
            Row(
                Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 目標物品圖示
                if (task.targetItem.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        WordArt(task.targetItem, Modifier.size(34.dp))
                        if (task.targetCount > 0) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(minOf(task.targetCount, 5)) {
                                    Box(Modifier.size(10.dp).background(SeaGreen, CircleShape))
                                }
                            }
                        }
                    }
                }

                // 箭頭指示
                if (task.targetContainer.isNotBlank()) {
                    Text("->", fontSize = 16.sp, color = Color.Gray)
                    WordArt(task.targetContainer, Modifier.size(34.dp))
                }

                // 第二條件 (如草莓再拿鳳梨)
                if (task.secondItem.isNotBlank()) {
                    Text("+", fontSize = 16.sp, color = Color.Gray)
                    WordArt(task.secondItem, Modifier.size(34.dp))
                }

                Spacer(Modifier.weight(1f))

                // 題目文字
                Text(
                    text = s.text(task.promptZh, task.promptEn),
                    fontSize = 13.sp,
                    color = Color(0xFF334B44),
                    maxLines = 1
                )
            }
        }
    }
}

// 模式切換列
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

// 生活任務子場景分發 (UX01: 移除重複外框控制列，只負責子場景與物件操作)
@Composable
fun LifeTasksGame(
    s: IslandSession,
    scene: String,
    task: IslandTask,
    done: Boolean,
    onVerify: (Boolean) -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 舞台背景與兔子展示
        SceneStage(scene, done)

        when (scene) {
            "cleaning" -> CleaningStationGame(s, task, done, onVerify)
            "sports" -> SportsCourtGame(s, task, done, onVerify)
            "toyroom" -> ToyRoomGame(s, task, done, onVerify)
            "dining" -> DiningTableGame(s, task, done, onVerify)
            "shop" -> CornerShopGame(s, task, done, onVerify)
            "garden" -> GardenGame(s, task, done, onVerify)
        }
    }
}

// 5.1 清潔站與垃圾車 (UX02 徹底重構：實體垃圾袋/空瓶拖放搬運，無文字按鈕)
@Composable
private fun CleaningStationGame(
    s: IslandSession,
    task: IslandTask,
    done: Boolean,
    onVerify: (Boolean) -> Unit
) {
    // 實體物品清單 (唯一 ID)
    var groundItems by remember(task.id) {
        mutableStateOf(
            when (task.id) {
                "clean_step_2" -> listOf(PlayItem("bag_1", "bag"), PlayItem("bottle_1", "bottle"))
                "clean_combo_1" -> listOf(PlayItem("bag_1", "bag"), PlayItem("bag_2", "bag"), PlayItem("bag_3", "bag"))
                "clean_combo_2" -> listOf(PlayItem("bag_3", "bag"), PlayItem("bag_4", "bag"), PlayItem("bag_5", "bag"))
                "clean_chal_1" -> listOf(PlayItem("bag_1", "bag"))
                else -> listOf(PlayItem("bag_1", "bag"), PlayItem("bottle_1", "bottle"), PlayItem("bag_2", "bag"))
            }
        )
    }

    var truckItems by remember(task.id) {
        mutableStateOf(
            when (task.id) {
                "clean_combo_2" -> listOf(PlayItem("bag_1", "bag"), PlayItem("bag_2", "bag"))
                "clean_chal_1" -> listOf(PlayItem("bag_truck", "bag"), PlayItem("toy_truck", "box"))
                else -> emptyList()
            }
        )
    }

    var binItems by remember(task.id) { mutableStateOf<List<PlayItem>>(emptyList()) }
    var recycleItems by remember(task.id) { mutableStateOf<List<PlayItem>>(emptyList()) }

    var selectedItem by remember(task.id) { mutableStateOf<Pair<PlayItem, String>?>(null) } // (item, location)

    val neededTruck = if (task.id == "clean_combo_2") 5 else 3

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 目的地容器展示區 (垃圾車、垃圾桶、回收箱)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (task.id == "clean_step_2") {
                // 垃圾桶容器
                Card(
                    modifier = Modifier.size(110.dp).clickable {
                        selectedItem?.let { (item, loc) ->
                            if (loc == "ground") {
                                groundItems = groundItems - item
                                binItems = binItems + item
                                selectedItem = null
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedItem?.first?.type == "bag") Color(0xFFDCECDC) else Color(0xFFE8F1EC))
                ) {
                    Column(Modifier.fillMaxSize().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        WordArt("trash_bin", Modifier.size(54.dp))
                        Text(s.text("垃圾桶 (${binItems.size})", "Trash (${binItems.size})"), fontSize = 13.sp, color = SeaGreen)
                    }
                }

                // 資源回收箱容器
                Card(
                    modifier = Modifier.size(110.dp).clickable {
                        selectedItem?.let { (item, loc) ->
                            if (loc == "ground") {
                                groundItems = groundItems - item
                                recycleItems = recycleItems + item
                                selectedItem = null
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedItem?.first?.type == "bottle") Color(0xFFDCECDC) else Color(0xFFE8F1EC))
                ) {
                    Column(Modifier.fillMaxSize().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        WordArt("recycle_bin", Modifier.size(54.dp))
                        Text(s.text("回收箱 (${recycleItems.size})", "Recycle (${recycleItems.size})"), fontSize = 13.sp, color = SeaGreen)
                    }
                }
            } else {
                // 垃圾車收運口容器
                Card(
                    modifier = Modifier.weight(1f).height(116.dp).clickable {
                        selectedItem?.let { (item, loc) ->
                            if (loc == "ground") {
                                groundItems = groundItems - item
                                truckItems = truckItems + item
                                selectedItem = null
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedItem?.second == "ground") Color(0xFFDCECDC) else Color(0xFFE5EDE8))
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            WordArt("garbage_truck", Modifier.size(68.dp))
                            Text(s.text("垃圾車收運口", "Truck Loading Area"), fontSize = 13.sp, color = SeaGreen)
                        }
                        // 車上已裝的實體物件
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            truckItems.forEach { item ->
                                val isSelected = selectedItem?.first?.id == item.id
                                Box(
                                    Modifier.size(48.dp).background(if (isSelected) Color(0xFFF9D6C4) else Color.White, CircleShape).clickable {
                                        selectedItem = if (isSelected) null else item to "truck"
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    WordArt(item.type, Modifier.size(34.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 地面 / 待收區 (來源物件清單)
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                // 點擊地面，將車上裝錯的物品拿回地面
                selectedItem?.let { (item, loc) ->
                    if (loc == "truck") {
                        truckItems = truckItems - item
                        groundItems = groundItems + item
                        selectedItem = null
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7EED8))
        ) {
            Column(Modifier.padding(10.dp)) {
                Text(s.text("地面待收區（點選物品，再點目的地容器）：", "Ground Pickup Area:"), fontSize = 13.sp, color = Color(0xFF705232))
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    groundItems.forEach { item ->
                        val isSelected = selectedItem?.first?.id == item.id
                        Box(
                            Modifier.size(64.dp).background(if (isSelected) Color(0xFFDCECDC) else Color.White, RoundedCornerShape(14.dp)).clickable {
                                selectedItem = if (isSelected) null else item to "ground"
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            WordArt(item.type, Modifier.size(46.dp))
                        }
                    }
                    if (groundItems.isEmpty()) {
                        Text(s.text("地面已清空", "Ground is clean"), fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        // 交貨確認按鈕 (無任何文字跳關按鈕，純核對實體物品數量與目的地)
        if (!done) {
            Button(
                onClick = {
                    val correct = when (task.id) {
                        "clean_step_1" -> binItems.count { it.type == "bag" } >= 1
                        "clean_step_2" -> binItems.any { it.type == "bag" } && recycleItems.any { it.type == "bottle" }
                        "clean_combo_1" -> truckItems.count { it.type == "bag" } == 3
                        "clean_combo_2" -> truckItems.count { it.type == "bag" } == 5
                        "clean_chal_1" -> truckItems.none { it.type == "box" } && truckItems.any { it.type == "bag" }
                        else -> truckItems.isNotEmpty() || binItems.isNotEmpty()
                    }
                    onVerify(correct)
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
            ) {
                Text(s.text("確認收好了！", "All Cleaned & Packed!"), fontSize = 17.sp)
            }
        }
    }
}

// 5.2 球場 (UX02 徹底重構：實體球、投籃/射門路徑動畫、進三球計分板)
@Composable
private fun SportsCourtGame(
    s: IslandSession,
    task: IslandTask,
    done: Boolean,
    onVerify: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var courtBalls by remember(task.id) {
        mutableStateOf(
            when (task.id) {
                "sports_step_1" -> listOf(PlayItem("soccer_1", "soccer"), PlayItem("soccer_2", "soccer"), PlayItem("basket_1", "basketball"))
                "sports_step_2" -> listOf(PlayItem("basket_1", "basketball"), PlayItem("basket_2", "basketball"))
                "sports_step_3" -> listOf(PlayItem("soccer_1", "soccer"), PlayItem("soccer_2", "soccer"))
                "sports_combo_1" -> listOf(PlayItem("b1", "basketball"), PlayItem("b2", "basketball"), PlayItem("b3", "basketball"), PlayItem("b4", "basketball"))
                else -> listOf(PlayItem("basket_1", "basketball"), PlayItem("soccer_1", "soccer"))
            }
        )
    }

    var storedBalls by remember(task.id) { mutableStateOf<List<PlayItem>>(emptyList()) }
    var score by remember(task.id) { mutableIntStateOf(0) }
    var selectedBall by remember(task.id) { mutableStateOf<PlayItem?>(null) }
    val isHoopTask = task.id.contains("basketball") || task.id == "sports_combo_1" || task.id == "sports_free_1"
    val isGoalTask = task.id.contains("soccer")

    // 進球路徑動畫
    var isScoringAnimation by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 目標區 (籃框 / 球門 / 收納籃)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 高處籃框 / 球門 (目標 1)
            Card(
                modifier = Modifier.size(126.dp).clickable {
                    selectedBall?.let { ball ->
                        if (isHoopTask && ball.type == "basketball") {
                            courtBalls = courtBalls - ball
                            selectedBall = null
                            isScoringAnimation = true
                            score++
                            coroutineScope.launch {
                                delay(600)
                                isScoringAnimation = false
                                if (task.id == "sports_combo_1" && score >= 3) onVerify(true)
                                else if (task.id == "sports_step_2" && score >= 1) onVerify(true)
                            }
                        } else if (isGoalTask && ball.type == "soccer") {
                            courtBalls = courtBalls - ball
                            selectedBall = null
                            isScoringAnimation = true
                            score++
                            coroutineScope.launch {
                                delay(600)
                                isScoringAnimation = false
                                if (task.id == "sports_step_3" && score >= 1) onVerify(true)
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (selectedBall != null) Color(0xFFDCECDC) else Color(0xFFEAEFF0))
            ) {
                Column(
                    Modifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    WordArt(if (isGoalTask) "soccer_goal" else "basketball_hoop", Modifier.size(64.dp))
                    Text(
                        text = if (isGoalTask) s.text("球門 (射門)", "Goal") else s.text("籃框 (投籃)", "Hoop"),
                        fontSize = 12.sp,
                        color = SeaGreen
                    )
                }
            }

            // 地面收納箱 (目標 2)
            Card(
                modifier = Modifier.size(126.dp).clickable {
                    selectedBall?.let { ball ->
                        courtBalls = courtBalls - ball
                        storedBalls = storedBalls + ball
                        selectedBall = null
                        if (task.id == "sports_step_1" && storedBalls.count { it.type == "soccer" } >= 2) {
                            onVerify(true)
                        }
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2ECE1))
            ) {
                Column(
                    Modifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    WordArt("box", Modifier.size(56.dp))
                    Text(s.text("收納籃 (${storedBalls.size})", "Storage (${storedBalls.size})"), fontSize = 12.sp, color = Color(0xFF6B4D2C))
                }
            }

            // 計分板 (以圖像圓點顯示，幼兒一眼看懂進幾球)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(s.text("計分板", "Score"), fontSize = 14.sp, color = SeaGreen)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                    val maxScore = if (task.id == "sports_combo_1") 3 else 1
                    repeat(maxScore) { idx ->
                        Box(
                            Modifier.size(16.dp).background(
                                if (idx < score) CoralRed else Color.LightGray,
                                CircleShape
                            )
                        )
                    }
                }
            }
        }

        // 球場地面 (實體球清單，點選單一球)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDEF0DE))
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(s.text("球場地面（點選球，再點籃框或收納籃）：", "Court (Tap a ball, then tap target):"), fontSize = 13.sp, color = SeaGreen)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    courtBalls.forEach { ball ->
                        val isSelected = selectedBall?.id == ball.id
                        Box(
                            Modifier.size(68.dp).background(
                                if (isSelected) Color(0xFFFFF3CD) else Color.White,
                                CircleShape
                            ).clickable {
                                selectedBall = if (isSelected) null else ball
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            WordArt(ball.type, Modifier.size(50.dp))
                        }
                    }
                    if (courtBalls.isEmpty()) {
                        Text(s.text("球都投進去囉！", "All balls cleared!"), fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        if (!done && (task.mode == GameMode.FREE || task.id == "sports_chal_2")) {
            Button(
                onClick = { onVerify(score >= 1 || storedBalls.isNotEmpty()) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
            ) {
                Text(s.text("投完球了！", "Done Playing!"), fontSize = 16.sp)
            }
        }
    }
}

// 5.3 玩具屋
@Composable
private fun ToyRoomGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var inPlace by remember(task.id) { mutableIntStateOf(0) }
    var selectedItem by remember(task.id) { mutableStateOf<String?>(null) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
            if (selectedItem == "car") { inPlace++; selectedItem = null }
        }) {
            WordArt("box", Modifier.size(72.dp))
            Text(s.text("收納格 ($inPlace/3)", "Slot ($inPlace/3)"), fontSize = 14.sp)
        }
    }

    if (!done) {
        Text(s.text("點玩具放到收納格：", "Pick toy into slot:"), fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ItemCard("car", selectedItem == "car") { selectedItem = "car" }
            ItemCard("ball", selectedItem == "ball") { selectedItem = "ball" }
        }
        Button(onClick = { onVerify(inPlace >= 1) }, modifier = Modifier.padding(top = 8.dp)) {
            Text(s.text("收好了", "All Done"))
        }
    }
}

// 5.4 餐桌
@Composable
private fun DiningTableGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var platesOnTable by remember(task.id) { mutableIntStateOf(0) }
    var selectedItem by remember(task.id) { mutableStateOf<String?>(null) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
            if (selectedItem == "plate") { platesOnTable++; selectedItem = null }
        }) {
            WordArt("table", Modifier.size(76.dp))
            Text(s.text("餐桌盤子 ($platesOnTable)", "Table Plates ($platesOnTable)"), fontSize = 14.sp)
        }
    }

    if (!done) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ItemCard("plate", selectedItem == "plate") { selectedItem = "plate" }
            ItemCard("cup", selectedItem == "cup") { selectedItem = "cup" }
        }
        Button(onClick = { onVerify(platesOnTable >= 1) }, modifier = Modifier.padding(top = 8.dp)) {
            Text(s.text("擺好了", "Done"))
        }
    }
}

// 5.5 商店
@Composable
private fun CornerShopGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var cartItems by remember(task.id) { mutableStateOf<List<String>>(emptyList()) }
    var selectedItem by remember(task.id) { mutableStateOf<String?>(null) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
            selectedItem?.let {
                cartItems = cartItems + it
                selectedItem = null
            }
        }) {
            WordArt("box", Modifier.size(72.dp))
            Text(s.text("購物籃 (${cartItems.size})", "Cart (${cartItems.size})"), fontSize = 14.sp)
        }
    }

    if (!done) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ItemCard("apple", selectedItem == "apple") { selectedItem = "apple" }
            ItemCard("orange", selectedItem == "orange") { selectedItem = "orange" }
        }
        Button(onClick = { onVerify(cartItems.isNotEmpty()) }, modifier = Modifier.padding(top = 8.dp)) {
            Text(s.text("結帳確認", "Checkout"))
        }
    }
}

// 5.6 花園
@Composable
private fun GardenGame(s: IslandSession, task: IslandTask, done: Boolean, onVerify: (Boolean) -> Unit) {
    var watered by remember(task.id) { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { watered = true }) {
            WordArt(if (watered) "flower" else "umbrella", Modifier.size(76.dp))
            Text(s.text(if (watered) "花朵開了！" else "點水滴澆花", if (watered) "Bloomed!" else "Tap to water"), fontSize = 14.sp)
        }
    }

    if (!done) {
        Button(onClick = { onVerify(watered) }, enabled = watered, modifier = Modifier.padding(top = 8.dp)) {
            Text(s.text("花園照料好囉", "Garden Done"))
        }
    }
}

@Composable
private fun ItemCard(name: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(68.dp).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFDCECDC) else Color.White)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WordArt(name, Modifier.size(46.dp))
        }
    }
}

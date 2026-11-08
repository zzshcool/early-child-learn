package tw.littlesprout.island

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

private val Ink = Color(0xFF283B36)
private val Green = Color(0xFF43856B)
private val Cream = Color(0xFFE4F3F4)

@Composable
fun IslandApp(s: IslandSession) {
    LaunchedEffect(s) { while (true) { delay(1000); s.tick() } }
    BackHandler(s.page != "cover") {
        if (s.sailingTo != null) s.cancelSailing() else when (s.page) {
            "rest" -> Unit
            "gate", "parents", "privacy" -> s.navigate(s.previousPage)
            "home", "setup" -> s.navigate("cover")
            else -> s.navigate("home")
        }
    }
    MaterialTheme(colorScheme = lightColorScheme(primary = Green, background = Cream, surface = Color.White, onSurface = Ink, onBackground = Ink)) {
        Surface(Modifier.fillMaxSize(), color = Cream) {
            BoxWithConstraints(Modifier.safeDrawingPadding().imePadding().fillMaxSize()) {
                val wide = maxWidth >= 700.dp
                val paperLimit = (maxHeight-410.dp).coerceIn(160.dp,480.dp)
                IslandSea(Modifier.fillMaxSize())
                Column(Modifier.fillMaxSize().verticalScroll(remember(s.page) { ScrollState(0) }).padding(horizontal = if (wide) 48.dp else 20.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (s.page != "cover") Header(s)
                    if (s.due && s.page !in listOf("rest", "gate", "parents", "cover")) Text(s.text("這題結束後，一起休息一下。", "After this, let's take a little break."), color = Green)
                    when (s.page) {
                        "cover" -> Cover(s)
                        "setup" -> Settings(s, true)
                        "home" -> Home(s, wide)
                        "beach", "bridge", "fruit", "delivery", "cleaning", "sports", "toyroom", "dining", "shop", "garden" -> IslandGame(s, s.page)
                        "cognition", "numbers" -> Quiz(s, wide)
                        "english" -> EnglishHall(s, wide)
                        "weather" -> WeatherHouse(s)
                        "zhuyin" -> Zhuyin(s)
                        "clock" -> StationGame(s)
                        "writing" -> WritingHall(s,paperLimit)
                        "gate" -> ParentGate(s)
                        "parents" -> Settings(s, false)
                        "rest" -> Rest(s)
                        "privacy" -> Privacy(s)
                        "finished" -> {
                            Sprout(Modifier.size(190.dp)); Title(s.text("完成了！", "All done!"))
                            Action(s.text("再玩一次", "Play again")) { s.enter(s.activeHall) }
                            Action(s.text("回學習首頁", "Back home")) { s.navigate("home") }
                        }
                    }
                    if (s.audioUnavailable && !s.muted) Text(s.text("這段語音尚未提供，請先看圖片提示。", "Audio is unavailable. Follow the picture."), color = Ink)
                    Spacer(Modifier.height(12.dp))
                }
                s.sailingTo?.let { destination -> BoatJourney(s, destination) }
            }
        }
    }
}

@Composable private fun Header(s: IslandSession) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { if (s.page != "rest") s.navigate("home") }) { Text(s.text("首頁", "Home"), fontSize = 18.sp) }
        SoundButton(s)
        if (s.onboarded) TextButton(onClick = { s.previousPage = s.page; s.navigate("gate") }) { Text(s.text("家長", "Parents")) }
    }
}
@Composable private fun Cover(s: IslandSession) {
    Spacer(Modifier.height(24.dp))
    Sprout(Modifier.fillMaxWidth().height(250.dp))
    Title(s.text("小芽學習島", "Little Sprout Island"))
    Action(s.text("開始玩", "Let's play")) { s.start() }
    SoundButton(s)
    if (s.onboarded) TextButton(onClick = { s.previousPage = "cover"; s.navigate("gate") }) { Text(s.text("家長設定", "Parent settings")) }
}
@Composable private fun Home(s: IslandSession, wide: Boolean) { IslandMap(s) }

@Composable private fun Quiz(s: IslandSession, wide: Boolean) {
    val lessons = if (s.page == "numbers") Lessons.numbers(s.grade) else Lessons.cognition(s.grade)
    val lesson = lessons[s.lessonIndex.coerceIn(0, lessons.lastIndex)]
    var counted by rememberSaveable(lesson.id) { mutableStateOf(listOf<Int>()) }
    LaunchedEffect(lesson.id, s.muted, s.audioEnglish) { s.play(lesson.prompt) }
    Text("${s.lessonIndex + 1} / ${lessons.size}", color = Green)
    Title(s.text(lesson.zh, lesson.en))
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1E8))) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            if (s.page == "numbers") {
                val count = lesson.answer.toInt()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    repeat((count + 3) / 4) { row -> Row { repeat(min(4, count - row * 4)) { column ->
                        val id = row * 4 + column
                        Text(if (id in counted) "✓" else "🍃", fontSize = 30.sp, color = Green, modifier = Modifier.size(50.dp).clickable { if (id !in counted) counted = counted + id })
                    } } }
                    Text(s.text("點過 ${counted.size} 片", "Counted ${counted.size}"), color = Green)
                }
            } else Picture(lesson.choices.first { it.id == lesson.answer }, Modifier.size(115.dp))
        }
    }
    Text(s.text("像上面的圖片一樣，選一個。", "Choose the one that matches."), fontSize = 17.sp)
    Grid(lesson.choices, if (wide) 4 else 2) { choice ->
        Tile(if (s.page == "numbers") choice.value else s.text(choice.zh, choice.en), { s.answer(lesson, choice) }, selected = s.selected == choice.id) { if (s.page != "numbers") Picture(choice, Modifier.size(70.dp)) }
    }
    Text(s.feedback.ifBlank { " " }, color = Green, fontSize = 20.sp)
    if (!s.muted) TextButton(onClick = { s.play(lesson.prompt) }) { Text(s.text("再聽一次", "Hear again")) }
    if (s.selected == lesson.answer) Action(s.text("下一個", "Next")) { s.next(lessons.size) }
}

@Composable private fun EnglishHall(s: IslandSession, wide: Boolean) {
    var viewMode by rememberSaveable { mutableStateOf("themes") } // "themes" or "az"
    var selectedTheme by rememberSaveable { mutableStateOf("animals") }
    var playType by rememberSaveable { mutableStateOf("explore") } // "explore", "listen", "action"
    var letter by rememberSaveable { mutableStateOf("A") }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Title(s.text("英文探索", "English Island"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { viewMode = "themes" },
                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (viewMode == "themes") Color(0xFFDCECDC) else Color.Transparent)
            ) {
                Text(s.text("情境主題", "Themes"), color = if (viewMode == "themes") Green else Ink)
            }
            OutlinedButton(
                onClick = { viewMode = "az" },
                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (viewMode == "az") Color(0xFFDCECDC) else Color.Transparent)
            ) {
                Text("A～Z", color = if (viewMode == "az") Green else Ink)
            }
        }
    }

    if (viewMode == "themes") {
        // 5.8 節 四張情境卡
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EnglishThemes.categories.forEach { cat ->
                val sel = cat.id == selectedTheme
                OutlinedButton(
                    onClick = { selectedTheme = cat.id },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (sel) Color(0xFFDCECDC) else Color.Transparent)
                ) {
                    Text(
                        (if (sel) "● " else "") + s.text(cat.zh.split(" ")[0], cat.en),
                        fontSize = 13.sp,
                        color = if (sel) Green else Ink
                    )
                }
            }
        }

        // 玩法切換: 認識單字 / 聽音找物 / 動手完成
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("explore" to ("認識單字" to "Words"), "listen" to ("聽音找物" to "Listen"), "action" to ("動手完成" to "Action")).forEach { (type, labels) ->
                val active = playType == type
                TextButton(
                    onClick = { playType = type },
                    modifier = Modifier.background(if (active) Color(0xFFDCECDC) else Color.Transparent, RoundedCornerShape(12.dp))
                ) {
                    Text((if (active) "✓ " else "") + s.text(labels.first, labels.second), color = if (active) Green else Ink, fontSize = 15.sp)
                }
            }
        }

        val currentCat = EnglishThemes.categories.first { it.id == selectedTheme }
        val words = s.words.filter { it.id in currentCat.wordIds }

        when (playType) {
            "listen" -> {
                val targetWord = remember(selectedTheme, s.lessonIndex) { words.randomOrNull() ?: words.first() }
                LaunchedEffect(targetWord.id, s.muted) { s.play("word_${targetWord.id}", "en") }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s.text("聽音選圖：", "Listen and pick:"), fontSize = 18.sp, color = Green)
                    if (!s.muted) TextButton(onClick = { s.play("word_${targetWord.id}", "en") }) { Text(s.text("再聽一次", "Hear again")) }
                }
                Grid(words.take(4), if (wide) 4 else 2) { word ->
                    Tile(if (s.selected == targetWord.id && word.id == targetWord.id) word.en else "???", {
                        s.selected = word.id
                        if (word.id == targetWord.id) {
                            s.play("correct")
                            s.mark("word.${word.id}.listening")
                        } else {
                            s.play("word_${word.id}", "en")
                        }
                    }, selected = s.selected == word.id) {
                        WordArt(word.id, Modifier.size(76.dp))
                    }
                }
            }
            "action" -> {
                Text(s.text("依照英文情境動手操作！", "Complete the action!"), fontSize = 16.sp, color = Green)
                Grid(words.take(4), if (wide) 4 else 2) { word ->
                    Tile(word.en, {
                        s.play("word_${word.id}", "en")
                        s.mark("word.${word.id}.action")
                    }) {
                        WordArt(word.id, Modifier.size(76.dp))
                        Text(word.zh, fontSize = 15.sp)
                    }
                }
            }
            else -> {
                // 認識單字
                Text(s.text(if (s.muted) "選一張圖片。" else "點圖片，聽單字發音。", if (s.muted) "Choose a picture." else "Tap a picture to hear the word."), textAlign = TextAlign.Center)
                Grid(words, if (wide) 4 else 2) { word ->
                    Tile(word.en, { s.play("word_${word.id}", "en"); s.mark("word.${word.id}.explored") }) {
                        WordArt(word.id, Modifier.size(76.dp))
                        Text(word.zh, fontSize = 16.sp)
                    }
                }
            }
        }
    } else {
        // A～Z 模式
        Grid(('A'..'Z').map { it.toString() }, if (wide) 9 else 5) { value ->
            OutlinedButton(onClick = { letter = value; s.stopAudio() }, modifier = Modifier.heightIn(min = 52.dp), contentPadding = PaddingValues(4.dp)) {
                Text(value, fontSize = 20.sp, color = if (letter == value) Green else Ink)
            }
        }
        Text("$letter ${letter.lowercase()}", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Green)
        if (letter == "X") Text(s.text("找找單字裡的 X。", "Find X in these words."))
        Grid(s.words.filter { it.letter == letter }, if (wide) 4 else 2) { word ->
            Tile(word.en, { s.play("word_${word.id}", "en"); s.mark("word.${word.id}.explored") }) {
                WordArt(word.id, Modifier.size(76.dp))
                Text(word.zh, fontSize = 16.sp)
            }
        }
    }
}
@Composable private fun Weather(s: IslandSession, wide: Boolean) {
    Title("How is the weather today?")
    Text(s.text("看看窗外，選一個你觀察到的天氣。", "Look outside. Pick the weather you notice."))
    LaunchedEffect(Unit) { s.play("weather_question", "en") }
    Grid(Lessons.weather, if (wide) 4 else 2) { item ->
        Tile(s.text(item.zh, item.en), { s.selected = item.id; s.play(item.id, "en"); s.mark("weather.${item.id}.explored") }, selected = s.selected == item.id) { Picture(item, Modifier.size(76.dp)) }
    }
    if (s.selected.isNotBlank()) Title("It's ${s.selected}!")
    if (s.due) Action(s.text("休息一下", "Take a break")) { s.beginRest() }
}
@Composable private fun Zhuyin(s: IslandSession) {
    var symbol by rememberSaveable { mutableStateOf("ㄅ") }
    Title(s.text("注音", "Zhuyin"))
    Grid(Lessons.zhuyin.map { it.toString() }, 5) { item -> OutlinedButton(onClick = { symbol = item; s.mark("zhuyin.$item.explored") }, contentPadding = PaddingValues(2.dp), modifier = Modifier.heightIn(min = 60.dp)) { Text(item, fontSize = 28.sp) } }
    Text(symbol, fontSize = 100.sp, color = Green)
    Text(s.text("找找看，哪個注音跟這個一樣？", "Find the matching Zhuyin symbol."), textAlign = TextAlign.Center)
    Action(s.text("前往自由習寫", "Free writing")) { s.enter("writing") }
}

@Composable private fun ClockHall(s: IslandSession) {
    var hour by rememberSaveable { mutableIntStateOf(3) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    val targets = listOf(3 to 0, 3 to 30, 12 to 0, 7 to 30, 6 to 15)
    var target by rememberSaveable { mutableIntStateOf(0) }
    Title(s.text("時鐘", "Clock"))
    Text(s.text("把時間調成", "Set the time to") + " ${targets[target].first}:${targets[target].second.toString().padStart(2, '0')}", fontSize = 24.sp)
    ClockFace(hour, minute, Modifier.sizeIn(maxWidth = 330.dp).fillMaxWidth().aspectRatio(1f))
    Title("$hour:${minute.toString().padStart(2, '0')}")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(onClick = { hour = if (hour == 12) 1 else hour + 1 }, modifier = Modifier.heightIn(min = 64.dp)) { Text(s.text("時針 +1", "Hour +1")) }
        OutlinedButton(onClick = { minute += if (s.grade >= 4) 5 else 30; if (minute >= 60) { minute = 0; hour = if (hour == 12) 1 else hour + 1 } }, modifier = Modifier.heightIn(min = 64.dp)) { Text(s.text("分針 +", "Minute +")) }
    }
    Text(s.text("短針是時針，長針是分針。", "The short hand shows hours. The long hand shows minutes."))
    if (hour == targets[target].first && minute == targets[target].second) Action(s.text("找到了！下一個", "That's it! Next")) { s.mark("clock.$hour.$minute.visual"); target = (target + 1) % (if (s.grade >= 4) targets.size else 4); if (s.due) s.beginRest() }
}

@Composable private fun ParentGate(s: IslandSession) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Title(s.text("家長入口", "For grown-ups"))
    Text(s.text("請輸入你設定的 4 位數 PIN。", "Enter your four-digit parent PIN."))
    PinField(pin) { pin = it; error = false }
    if (error) Text(s.text("PIN 不正確，請再試一次。", "That PIN doesn't match. Try again."))
    Action(s.text("開啟設定", "Open settings"), pin.length == 4) { if (s.checkPin(pin)) s.navigate("parents") else error = true }
    TextButton(onClick = { s.navigate(s.previousPage) }) { Text(s.text("返回", "Go back")) }
}
@Composable private fun Settings(s: IslandSession, initial: Boolean) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var clear by remember { mutableStateOf(false) }
    var reset by remember { mutableStateOf(false) }
    Title(s.text(if (initial) "先請家長幫個忙" else "家長設定", if (initial) "A little help from a grown-up" else "Parent settings"))
    Setting(s.text("英文介面", "English interface"), s.english) { s.configure(en = it) }
    Setting(s.text("一般提示使用英文語音", "English spoken instructions"), s.audioEnglish) { s.configure(soundEn = it) }
    Setting(s.text("無聲模式", "Quiet mode"), s.muted) { s.configure(silent = it) }
    Text(s.text("休息提醒（分鐘）", "Break reminder (minutes)"), fontWeight = FontWeight.Bold)
    Grid(listOf(10, 15, 20, 0), 4) { value -> OutlinedButton(onClick = { s.configure(minutes = value) }, contentPadding = PaddingValues(4.dp)) { Text((if (s.interval == value) "✓" else "") + if (value == 0) s.text("關閉", "Off") else "$value") } }
    Text(s.text("本週主題", "This week's theme"), fontWeight = FontWeight.Bold)
    Grid(listOf("all", "fruit", "animals", "weather"), 2) { value -> OutlinedButton(onClick = { s.configure(theme = value) }) { Text((if (s.topic == value) "✓ " else "") + s.text(mapOf("all" to "全部", "fruit" to "水果", "animals" to "動物", "weather" to "天氣")[value]!!, value)) } }
    Text(s.text(if (initial) "設定 4 位數家長 PIN" else "更換家長 PIN（選填）", if (initial) "Choose a four-digit PIN" else "Change PIN (optional)"))
    PinField(pin) { pin = it }; PinField(confirm) { confirm = it }
    if (pin.isNotBlank() && confirm.isNotBlank() && pin != confirm) Text(s.text("兩次 PIN 需要相同。", "The PINs must match."))
    Action(s.text(if (initial) "準備好了，開始玩" else "儲存並返回", if (initial) "Ready to play" else "Save and return"), if (initial) pin.length == 4 && pin == confirm else pin.isEmpty() || pin.length == 4 && pin == confirm) {
        if (initial) s.setup(pin) else { if (pin.length == 4) s.changePin(pin); s.navigate(if (s.restUntil > 0) "rest" else "home") }
    }
    if (!initial) {
        if (s.restUntil > 0) TextButton(onClick = { s.continueAfterRest() }) { Text(s.text("家長：提前結束休息", "Parent: finish break early")) }
        Text(s.text("探索紀錄：${s.completed.size}", "Saved discoveries: ${s.completed.size}"))
        TextButton(onClick = { clear = true }) { Text(s.text("清除學習進度", "Clear learning progress")) }
        TextButton(onClick = { reset = true }) { Text(s.text("重設全部資料與 PIN", "Reset all data and PIN")) }
    }
    TextButton(onClick = { s.navigate("privacy") }) { Text(s.text("隱私與教材說明", "Privacy & learning materials")) }
    if (clear || reset) AlertDialog(onDismissRequest = { clear = false; reset = false }, title = { Text(s.text("確認清除？", "Confirm reset?")) }, text = { Text(s.text(if (reset) "將清除進度、設定與家長 PIN。" else "清除學習進度、島嶼狀態、草稿與作品，保留設定與 PIN。", if (reset) "This removes progress, settings and your PIN." else "Progress, island state, drafts and drawings will be cleared. Settings and PIN stay.")) }, confirmButton = { TextButton(onClick = { if (reset) s.reset() else s.clearProgress(); clear = false; reset = false }) { Text(s.text("確認", "Confirm")) } }, dismissButton = { TextButton(onClick = { clear = false; reset = false }) { Text(s.text("取消", "Cancel")) } })
}
@Composable private fun Rest(s: IslandSession) {
    val remaining = ((s.restUntil - s.restNow + 999) / 1000).coerceAtLeast(0)
    Sprout(Modifier.size(200.dp)); Title(s.text("休息一下", "Take a break"))
    Text(s.text("放下螢幕，看看遠方，伸展一下。", "Put the screen down. Look around and stretch."), textAlign = TextAlign.Center, fontSize = 20.sp)
    Text("${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}", fontSize = 36.sp, color = Green)
    if (remaining == 0L) Action(s.text("休息好了，繼續", "Ready to continue")) { s.continueAfterRest() }
}
@Composable private fun Privacy(s: IslandSession) {
    var showLicenses by remember { mutableStateOf(false) }
    Title(s.text("隱私與教材說明", "Privacy & materials"))
    Text(s.text("設定、島嶼進度、草稿與保存的畫作留在這台裝置。App 不建立帳號，不要求姓名、生日、位置、相機或麥克風權限，也不連線傳送資料。Android 雲端備份與裝置移轉已停用。", "Settings, island progress, drafts and saved drawings stay on this device. The app has no accounts or network access and does not request names, birthdays, location, camera or microphone access. Cloud backup and device transfer are disabled."), fontSize = 18.sp)
    Text(s.text("清除學習進度會刪除島嶼狀態、草稿與作品，保留家長設定與 PIN。重設全部資料也會刪除設定與 PIN。解除安裝會失去本機資料。", "Clearing progress removes island state, drafts and drawings while keeping settings and PIN. Resetting all data also removes settings and PIN. Uninstalling removes local data."), fontSize = 18.sp)
    Text(s.text("注音字形、筆順與錄音：2017 © 教育部，國語注音符號手冊－開放部件，CC BY 4.0。筆畫軌跡已轉換座標供互動描寫。其他圖稿為本專案自製向量插畫。英文錄音採 Piper／LJ Speech；中文操作語音採 MeloTTS，均預先產生並打包。", "Zhuyin forms, strokes and recordings: 2017 © Taiwan Ministry of Education, Mandarin Phonetic Symbols Handbook open components, CC BY 4.0. Stroke coordinates are adapted for tracing. Other illustrations are original project vector art. English speech uses Piper/LJ Speech; Chinese instructions use MeloTTS. All audio is rendered in advance and bundled."), fontSize = 18.sp)
    Text(s.text("教材供生活探索與練習，不作學力診斷或官方認證。X 組包含字首或字中有 X 的單字。", "Materials are for exploration and practice, with no diagnostic or official certification claim. X includes words with X at the start or inside the word."))
    TextButton(onClick = { showLicenses = !showLicenses }) { Text(s.text("素材來源與授權", "Sources & licenses")) }
    if (showLicenses) {
        val assets = androidx.compose.ui.platform.LocalContext.current.assets
        listOf("moe-zhuyin.txt", "melotts-MIT.txt", "ljspeech-model-card.txt", "Apache-2.0.txt").forEach { file ->
            Text(file, fontWeight = FontWeight.Bold)
            Text(remember(file) { assets.open("legal/$file").bufferedReader().use { it.readText() } }, fontSize = 14.sp)
        }
    }
    Action(s.text("返回", "Back")) { s.navigate(if (s.onboarded) "parents" else "setup") }
}

@Composable private fun PinField(value: String, update: (String) -> Unit) { OutlinedTextField(value, { update(it.filter(Char::isDigit).take(4)) }, modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp), label = { Text("PIN") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)) }
@Composable private fun Setting(label: String, checked: Boolean, update: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f), fontSize = 18.sp); Switch(checked, update) } }
@Composable private fun SoundButton(s: IslandSession) {
    TextButton(onClick = { s.configure(silent = !s.muted) }, modifier = Modifier.semantics { contentDescription = if (s.muted) s.text("開啟聲音", "Turn sound on") else s.text("關閉聲音", "Turn sound off") }) {
        Canvas(Modifier.size(24.dp)) {
            val w=size.width;val h=size.height
            val speaker=Path().apply{moveTo(w*.12f,h*.36f);lineTo(w*.3f,h*.36f);lineTo(w*.55f,h*.16f);lineTo(w*.55f,h*.84f);lineTo(w*.3f,h*.64f);lineTo(w*.12f,h*.64f);close()};drawPath(speaker,Green)
            if(s.muted)drawLine(Green,Offset(w*.7f,h*.3f),Offset(w*.94f,h*.7f),2.dp.toPx())
            else drawArc(Green,-55f,110f,false,Offset(w*.45f,h*.12f),Size(w*.5f,h*.76f),style=Stroke(2.dp.toPx()))
        }
    }
}
@Composable private fun Title(text: String) { Text(text, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Ink) }
@Composable private fun Action(text: String, enabled: Boolean = true, click: () -> Unit) { Button(onClick = click, enabled = enabled, modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth().heightIn(min = 64.dp), shape = RoundedCornerShape(22.dp), contentPadding = PaddingValues(16.dp)) { Text(text, fontSize = 21.sp, textAlign = TextAlign.Center) } }
@Composable private fun <T> Grid(items: List<T>, columns: Int, content: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) { items.chunked(columns).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { row.forEach { item -> Box(Modifier.weight(1f)) { content(item) } }; repeat(columns - row.size) { Spacer(Modifier.weight(1f)) } } } }
}
@Composable private fun Tile(label: String, click: () -> Unit, selected: Boolean = false, body: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().heightIn(min = 145.dp).clickable(onClick = click), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFDCECDC) else Color.White)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { body(); Text(label, fontSize = 20.sp, color = Ink, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium) }
    }
}
@Composable internal fun Picture(item: Choice, modifier: Modifier) {
    if (item.kind == "emoji") Box(modifier, contentAlignment = Alignment.Center) { Text(item.value, fontSize = 52.sp) }
    else Canvas(modifier.semantics { contentDescription = item.en.ifBlank { item.id } }) {
        val unit = min(size.width, size.height)
        when (item.kind) {
            "color" -> { drawCircle(Color(item.value.toLong(16)), unit * .43f); drawCircle(Ink, unit * .43f, style = Stroke(2.dp.toPx())) }
            "compare" -> {
                val factor = if (item.value in listOf("big", "long")) .85f else .38f
                if (item.value in listOf("big", "small")) drawCircle(Green, unit * factor / 2)
                else drawRoundRect(Green, Offset(0f, size.height * .35f), Size(size.width * factor, size.height * .3f))
            }
            else -> when (item.value) {
                "circle" -> drawCircle(Green, unit * .43f)
                "square" -> drawRect(Green, Offset(unit * .1f, unit * .1f), Size(unit * .8f, unit * .8f))
                "rectangle" -> drawRect(Green, Offset(0f, unit * .25f), Size(unit, unit * .5f))
                "oval" -> drawOval(Green, Offset(0f, unit * .2f), Size(unit, unit * .6f))
                else -> { val path = Path(); if (item.value == "triangle") { path.moveTo(unit / 2, 0f); path.lineTo(unit, unit); path.lineTo(0f, unit) } else { repeat(10) { i -> val a = -PI / 2 + i * PI / 5; val r = unit * if (i % 2 == 0) .48 else .22; val x = unit / 2 + (cos(a) * r).toFloat(); val y = unit / 2 + (sin(a) * r).toFloat(); if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) } }; path.close(); drawPath(path, Green) }
            }
        }
    }
}
@Composable private fun Sprout(modifier: Modifier) { Canvas(modifier.semantics { contentDescription = "小芽學習島 / Little Sprout Island" }) {
    val w = size.width; val h = size.height
    drawOval(Color(0xFFEDDCA7), Offset(w * .12f, h * .68f), Size(w * .76f, h * .25f))
    drawOval(Color(0xFFD8E5AD), Offset(w * .2f, h * .68f), Size(w * .6f, h * .18f))
    drawLine(Green, Offset(w * .5f, h * .75f), Offset(w * .5f, h * .34f), 8.dp.toPx(), StrokeCap.Round)
    drawOval(Green, Offset(w * .29f, h * .3f), Size(w * .22f, h * .18f))
    drawOval(Color(0xFF80AE75), Offset(w * .5f, h * .2f), Size(w * .23f, h * .22f))
    drawCircle(Ink, 3.dp.toPx(), Offset(w * .46f, h * .78f)); drawCircle(Ink, 3.dp.toPx(), Offset(w * .54f, h * .78f))
    drawCircle(Color(0xFFF2D782), w * .065f, Offset(w * .82f, h * .18f))
} }
@Composable internal fun ClockFace(hour: Int, minute: Int, modifier: Modifier) { Canvas(modifier.semantics { contentDescription = "$hour:${minute.toString().padStart(2, '0')}" }) {
    val r = size.minDimension * .45f; val c = center
    drawCircle(Color.White, r); drawCircle(Green, r, style = Stroke(4.dp.toPx()))
    repeat(60) { i -> val angle = i * PI / 30 - PI / 2; val length = if (i % 5 == 0) .13 else .05; drawLine(if (i % 5 == 0) Ink else Green, c + Offset((cos(angle) * r * (1 - length)).toFloat(), (sin(angle) * r * (1 - length)).toFloat()), c + Offset((cos(angle) * r * .96).toFloat(), (sin(angle) * r * .96).toFloat()), if (i % 5 == 0) 3.dp.toPx() else 1.dp.toPx()) }
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(40, 59, 54); textSize = r * .15f; textAlign = android.graphics.Paint.Align.CENTER }
    (1..12).forEach { number -> val angle = number * PI / 6 - PI / 2; drawContext.canvas.nativeCanvas.drawText(number.toString(), c.x + (cos(angle) * r * .76).toFloat(), c.y + (sin(angle) * r * .76).toFloat() - (paint.ascent() + paint.descent()) / 2, paint) }
    fun hand(angle: Double, length: Float, width: Float, color: Color) { val a = angle * PI / 180 - PI / 2; drawLine(color, c, c + Offset((cos(a) * r * length).toFloat(), (sin(a) * r * length).toFloat()), width, StrokeCap.Round) }
    hand((hour % 12) * 30.0 + minute * .5, .5f, 8.dp.toPx(), Ink); hand(minute * 6.0, .77f, 5.dp.toPx(), Green); drawCircle(Green, 7.dp.toPx())
} }

package tw.littlesprout.island

import android.app.Application
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import java.security.MessageDigest

data class Word(val id: String, val letter: String, val en: String, val zh: String, val picture: String)
data class Choice(val id: String, val zh: String, val en: String, val kind: String, val value: String)
data class Lesson(val id: String, val zh: String, val en: String, val prompt: String, val choices: List<Choice>, val answer: String)
data class SavedDrawing(val id: String, val symbol: String, val strokes: List<List<androidx.compose.ui.geometry.Offset>>)

class IslandSession(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("island", 0)
    var english by mutableStateOf(prefs.getBoolean("english", false)); private set
    var audioEnglish by mutableStateOf(prefs.getBoolean("audioEnglish", false)); private set
    var muted by mutableStateOf(prefs.getBoolean("muted", false)); private set
    var grade by mutableIntStateOf(prefs.getInt("grade", 0)); private set
    var interval by mutableIntStateOf(prefs.getInt("interval", 15)); private set
    var topic by mutableStateOf(prefs.getString("topic", "all")!!); private set
    var onboarded by mutableStateOf(prefs.getBoolean("onboarded", false)); private set
    var page by mutableStateOf("cover"); private set
    var sailingTo by mutableStateOf<String?>(null); private set
    var previousPage = "home"
    var lessonIndex by mutableIntStateOf(prefs.getInt("lessonIndex", 0)); private set
    var feedback by mutableStateOf("")
    var selected by mutableStateOf("")
    var activeHall by mutableStateOf(prefs.getString("activeHall", "cognition")!!); private set
    var elapsed by mutableLongStateOf(prefs.getLong("elapsed", 0)); private set
    var restUntil by mutableLongStateOf(prefs.getLong("restUntil", 0)); private set
    var restNow by mutableLongStateOf(System.currentTimeMillis()); private set
    var completed by mutableStateOf(prefs.getStringSet("completed", emptySet())!!.toSet()); private set
    var foreground = false
    private val drawingDirectory = java.io.File(app.filesDir, "drawings").apply { mkdirs() }
    var drawings by mutableStateOf(drawingDirectory.listFiles()?.sortedByDescending { it.name }?.map { file ->
        val data = org.json.JSONObject(file.readText()); SavedDrawing(file.name, data.getString("symbol"), decodeDrawing(data.getString("strokes")))
    } ?: emptyList()); private set
    fun encodeDrawing(strokes: List<List<androidx.compose.ui.geometry.Offset>>) = org.json.JSONArray(strokes.map { stroke -> stroke.map { listOf(it.x, it.y) } }).toString()
    fun decodeDrawing(text: String): List<List<androidx.compose.ui.geometry.Offset>> {
        if (text.isBlank()) return emptyList()
        val lines = org.json.JSONArray(text)
        return List(lines.length()) { i -> val line = lines.getJSONArray(i); List(line.length()) { j -> val p = line.getJSONArray(j); androidx.compose.ui.geometry.Offset(p.getDouble(0).toFloat(), p.getDouble(1).toFloat()) } }
    }
    fun saveDrawing(symbol: String, strokes: List<List<androidx.compose.ui.geometry.Offset>>) {
        if (strokes.isEmpty()) return
        val id = "${System.currentTimeMillis()}.json"
        java.io.File(drawingDirectory, id).writeText(org.json.JSONObject().put("symbol", symbol).put("strokes", encodeDrawing(strokes)).toString())
        drawings = listOf(SavedDrawing(id, symbol, strokes)) + drawings
        saveWorld("writing.wins", worldValue("writing.wins") + 1)
    }
    val zhuyinData = org.json.JSONObject(app.assets.open("zhuyin.json").bufferedReader().readText())
    fun guide(symbol: String): List<List<androidx.compose.ui.geometry.Offset>>? {
        if (!zhuyinData.has(symbol)) return Tracing.guide(symbol)
        val strokes = zhuyinData.getJSONObject(symbol).getJSONArray("strokes")
        return List(strokes.length()) { i -> val points = strokes.getJSONArray(i); List(points.length()) { j -> val point = points.getJSONArray(j); androidx.compose.ui.geometry.Offset(point.getDouble(0).toFloat(), point.getDouble(1).toFloat()) } }
    }
    var currentAudioPrompt by mutableStateOf("")
    fun getSceneMode(scene: String): GameMode {
        val saved = worldText("$scene.mode")
        return if (saved.isNotBlank()) GameMode.fromId(saved) else GameMode.FREE
    }
    fun setSceneMode(scene: String, mode: GameMode) {
        saveWorldText("$scene.mode", mode.id)
        saveWorld("$scene.taskIndex", 0)
        saveWorld("$scene.done", 0)
        saveWorld("$scene.round", 0)
        stopAudio()
    }
    fun getTaskIndex(scene: String): Int = worldValue("$scene.taskIndex")
    fun setTaskIndex(scene: String, index: Int) {
        saveWorld("$scene.taskIndex", index)
        saveWorld("$scene.done", 0)
        stopAudio()
    }
    fun completeTask(scene: String, mode: GameMode, index: Int): Boolean {
        val key = "task.$scene.${mode.id}.$index"
        val wasDone = worldValue("$scene.done") == 1
        world = org.json.JSONObject(world.toString()).put("$scene.done", 1).put("$scene.wins", worldValue("$scene.wins") + 1)
        prefs.edit().putString("world", world.toString()).apply()
        mark(key)
        mark("game.$scene.${worldValue("$scene.round")}")
        if (!wasDone) play("correct")
        return true
    }
    fun nextTask(scene: String) {
        val mode = getModeForScene(scene)
        val tasks = GameplayRegistry.getTasks(scene, mode)
        val current = getTaskIndex(scene)
        val next = if (tasks.isNotEmpty()) (current + 1) % tasks.size else current + 1
        world = org.json.JSONObject(world.toString()).put("$scene.taskIndex", next).put("$scene.round", worldValue("$scene.round") + 1).put("$scene.done", 0)
        listOf("count", "selected", "second", "phase", "step").forEach { world.remove("$scene.$it") }
        prefs.edit().putString("world", world.toString()).apply()
        if (due) beginRest()
    }
    fun replayTask(scene: String) {
        world = org.json.JSONObject(world.toString()).put("$scene.done", 0)
        listOf("count", "selected", "second", "phase", "step").forEach { world.remove("$scene.$it") }
        prefs.edit().putString("world", world.toString()).apply()
        if (currentAudioPrompt.isNotBlank()) play(currentAudioPrompt)
    }
    fun getModeForScene(scene: String): GameMode = getSceneMode(scene)
    fun isTaskCompleted(scene: String, mode: GameMode, index: Int): Boolean = "task.$scene.${mode.id}.$index" in completed
    fun completeGame(scene: String): Boolean {
        if (worldValue("$scene.done") == 1) return false
        val mode = getSceneMode(scene)
        val taskIndex = getTaskIndex(scene)
        return completeTask(scene, mode, taskIndex)
    }
    fun nextGame(scene: String) {
        nextTask(scene)
    }
    var world by mutableStateOf(org.json.JSONObject(prefs.getString("world", "{}")!!)); private set
    fun worldValue(key: String) = world.optInt(key)
    fun worldText(key: String) = world.optString(key, "")
    fun saveWorldText(key: String, value: String) {
        world = org.json.JSONObject(world.toString()).put(key, value)
        prefs.edit().putString("world", world.toString()).apply()
    }
    fun saveWorld(key: String, value: Int) {
        world = org.json.JSONObject(world.toString()).put(key, value)
        prefs.edit().putString("world", world.toString()).apply()
    }
    private var dueSeconds = 0
    private var player: MediaPlayer? = null
    var audioUnavailable by mutableStateOf(false); private set
    val words: List<Word> = JSONArray(app.assets.open("words.json").bufferedReader().readText()).let { rows ->
        List(rows.length()) { i -> rows.getJSONObject(i).let { Word(it.getString("id"), it.getString("letter"), it.getString("en"), it.getString("zh"), it.getString("picture")) } }
    }
    val hasPin get() = prefs.contains("pin")
    val due get() = interval > 0 && elapsed >= interval * 60L
    fun text(zh: String, en: String) = if (english) en else zh
    fun configure(en: Boolean = english, soundEn: Boolean = audioEnglish, silent: Boolean = muted, level: Int = grade, minutes: Int = interval, theme: String = topic) {
        english = en; audioEnglish = soundEn; muted = silent; grade = level; interval = minutes; topic = theme
        prefs.edit().putBoolean("english", en).putBoolean("audioEnglish", soundEn).putBoolean("muted", silent).putInt("grade", level).putInt("interval", minutes).putString("topic", theme).apply()
        stopAudio()
    }
    fun setup(pin: String) {
        if (!hasPin) prefs.edit().putString("pin", digest(pin)).commit()
        onboarded = true; prefs.edit().putBoolean("onboarded", true).apply(); navigate("home")
    }
    private fun digest(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
    fun checkPin(value: String) = digest(value) == prefs.getString("pin", "")
    fun changePin(value: String) { prefs.edit().putString("pin", digest(value)).apply() }
    fun navigate(target: String) {
        stopAudio(); selected = ""; feedback = ""
        page = if (!onboarded && target !in listOf("cover", "setup", "privacy")) "setup"
            else if (restUntil > 0 && target !in listOf("parents", "gate", "privacy", "cover")) "rest" else target
    }
    fun start() = navigate(if (!onboarded) "setup" else if (restUntil > 0) "rest" else "home")
    fun enter(hall: String) {
        activeHall = hall; lessonIndex = 0; persistActivity(); navigate(hall)
    }
    fun sailTo(hall: String) { if (sailingTo == null) { stopAudio(); sailingTo = hall } }
    fun arrive() { val hall = sailingTo ?: return; sailingTo = null; enter(hall) }
    fun cancelSailing() { sailingTo = null }
    fun resumeLesson() { navigate(activeHall) }
    fun persistActivity() { prefs.edit().putString("activeHall", activeHall).putInt("lessonIndex", lessonIndex).apply() }
    fun mark(id: String) {
        if (id !in completed) { completed = completed + id; prefs.edit().putStringSet("completed", completed).apply() }
    }
    fun answer(lesson: Lesson, choice: Choice) {
        if (selected == lesson.answer) return
        selected = choice.id
        if (choice.id == lesson.answer) { mark(lesson.id + ".visual"); feedback = text("找到了！", "You found it!"); play("correct") }
        else { feedback = text("再看看上面的提示，試試另一個。", "Look at the example. Try another one.") }
    }
    fun next(total: Int) {
        if (due) { beginRest(); return }
        if (lessonIndex + 1 >= total) { navigate("finished"); return }
        lessonIndex++; persistActivity(); feedback = ""; selected = ""; stopAudio()
    }
    fun tick() {
        restNow = System.currentTimeMillis()
        if (!foreground || !onboarded || page in listOf("cover", "setup", "gate", "parents", "privacy", "rest")) return
        elapsed++; prefs.edit().putLong("elapsed", elapsed).apply()
        if (due) { dueSeconds++; if (page == "home" || dueSeconds >= 30) beginRest() } else dueSeconds = 0
    }
    fun beginRest() {
        if (restUntil == 0L) { previousPage = page; prefs.edit().putString("resumeAfterRest", page).apply() }
        restUntil = System.currentTimeMillis() + 120_000
        prefs.edit().putLong("restUntil", restUntil).commit(); navigate("rest")
    }
    fun continueAfterRest() {
        val destination = prefs.getString("resumeAfterRest", "home")!!
        restUntil = 0; elapsed = 0; dueSeconds = 0
        prefs.edit().putLong("restUntil", 0).putLong("elapsed", 0).commit()
        navigate(destination)
    }
    fun clearProgress() {
        drawingDirectory.listFiles()?.forEach { it.delete() }; drawings = emptyList()
        world = org.json.JSONObject(); prefs.edit().remove("world").apply()
        completed = emptySet(); lessonIndex = 0; activeHall = "cognition"
        prefs.edit().remove("completed").remove("lessonIndex").remove("activeHall").apply()
    }
    fun reset() {
        drawingDirectory.listFiles()?.forEach { it.delete() }; drawings = emptyList()
        world = org.json.JSONObject()
        stopAudio(); prefs.edit().clear().commit(); completed = emptySet(); onboarded = false
        english = false; audioEnglish = false; muted = false; grade = 0; interval = 15; topic = "all"
        elapsed = 0; restUntil = 0; lessonIndex = 0; activeHall = "cognition"; navigate("cover")
    }
    fun play(id: String, language: String? = null) {
        stopAudio(); audioUnavailable = false
        if (muted) return
        val locale = language ?: if (audioEnglish) "en" else "zh"
        try {
            getApplication<Application>().assets.openFd("audio/$locale/$id.wav").use { file ->
                player = MediaPlayer().apply {
                    setDataSource(file.fileDescriptor, file.startOffset, file.length)
                    setOnCompletionListener { stopAudio() }; prepare(); start()
                }
            }
        } catch (_: java.io.IOException) { audioUnavailable = true; stopAudio() }
    }
    fun stopAudio() { player?.release(); player = null }
    fun pause() { foreground = false; stopAudio(); prefs.edit().putLong("elapsed", elapsed).commit() }
    override fun onCleared() { stopAudio(); super.onCleared() }
}

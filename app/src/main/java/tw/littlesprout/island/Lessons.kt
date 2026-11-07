package tw.littlesprout.island

object Lessons {
    val colors = listOf(
        Choice("red", "紅色", "Red", "color", "FFE05555"), Choice("blue", "藍色", "Blue", "color", "FF4385C6"),
        Choice("yellow", "黃色", "Yellow", "color", "FFF4CB45"), Choice("green", "綠色", "Green", "color", "FF4C966A"),
        Choice("orange", "橙色", "Orange", "color", "FFF28C40"), Choice("purple", "紫色", "Purple", "color", "FF9868B5"),
        Choice("black", "黑色", "Black", "color", "FF303938"), Choice("white", "白色", "White", "color", "FFFFFFFF")
    )
    val shapes = listOf(
        Choice("circle", "圓形", "Circle", "shape", "circle"), Choice("triangle", "三角形", "Triangle", "shape", "triangle"),
        Choice("square", "正方形", "Square", "shape", "square"), Choice("rectangle", "長方形", "Rectangle", "shape", "rectangle"),
        Choice("oval", "橢圓形", "Oval", "shape", "oval"), Choice("star", "星形", "Star", "shape", "star")
    )
    val weather = listOf(Choice("sunny", "晴天", "Sunny", "emoji", "☀️"), Choice("cloudy", "多雲", "Cloudy", "emoji", "☁️"), Choice("rainy", "下雨", "Rainy", "emoji", "🌧️"), Choice("windy", "刮風", "Windy", "emoji", "🌬️"))
    const val zhuyin = "ㄅㄆㄇㄈㄉㄊㄋㄌㄍㄎㄏㄐㄑㄒㄓㄔㄕㄖㄗㄘㄙㄧㄨㄩㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦ"
    fun cognition(level: Int): List<Lesson> {
        val options = if (level == 0) 2 else if (level == 1) 3 else 4
        val result = (colors + shapes).mapIndexed { i, target ->
            val family = if (target.kind == "color") colors else shapes
            val choices = (listOf(target) + family.filter { it.id != target.id }.take(options - 1)).let { it.drop(i % it.size) + it.take(i % it.size) }
            Lesson("cognition.${target.id}", "找一找：${target.zh}", "Find: ${target.en}", target.id, choices, target.id)
        }.toMutableList()
        run {
            listOf("big" to ("大" to "Big"), "small" to ("小" to "Small"), "long" to ("長" to "Long"), "short" to ("短" to "Short")).forEach { (id, names) ->
                val family = if (id in listOf("big", "small")) listOf("big", "small") else listOf("long", "short")
                result += Lesson("cognition.$id", "找一找：${names.first}", "Find: ${names.second}", id, family.map { Choice(it, if (it == id) names.first else "", "", "compare", it) }, id)
            }
        }
        return result
    }
    fun numbers(level: Int): List<Lesson> {
        val max = listOf(3, 5, 10, 10, 20)[level]
        return (1..max).map { n ->
            val candidates = listOf(n, if (n == max) 0 else n + 1, if (n > 1) n - 1 else max).distinct().sorted()
            Lesson("number.$n", "有幾片葉子？", "How many leaves?", "count", candidates.map { Choice("$it", "$it", "$it", "number", "$it") }, "$n")
        }
    }
}

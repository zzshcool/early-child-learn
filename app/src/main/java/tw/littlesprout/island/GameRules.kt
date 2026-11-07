package tw.littlesprout.island

object GameRules {
    fun fruitTargets(round: Int, grade: Int): Pair<Int, Int> {
        val maximum = listOf(3, 3, 5, 10, 20)[grade]
        return (1 + round % maximum) to if (grade >= 3 && round % 3 == 2) 1 + round % 3 else 0
    }
    fun weatherAccepts(weather: Int, equipment: Int) = when(weather) {
        0 -> equipment == 1 || equipment == 0
        1 -> equipment in 0..2
        2 -> equipment == 0 || equipment == 2
        else -> equipment == 1 || equipment == 2
    }
    fun clockTarget(round: Int, grade: Int): Int {
        val hour = listOf(3, 7, 12, 8, 6, 10)[round % 6] % 12
        val minute = when { grade < 3 -> 0; grade == 3 -> (round % 2)*30; else -> listOf(0,30,15,45,5,20)[round % 6] }
        return hour*60+minute
    }
    fun hourAngle(totalMinutes: Int) = (totalMinutes % 720) * .5f
    fun minuteAngle(totalMinutes: Int) = (totalMinutes % 60) * 6f
    fun clockLabel(totalMinutes: Int): String { val t=((totalMinutes%720)+720)%720; return "${if(t/60==0)12 else t/60}:${(t%60).toString().padStart(2,'0')}" }
}

package tw.littlesprout.island

import org.junit.Assert.*
import org.junit.Test

class GameRulesTest {
    @Test fun clockHandsMoveContinuouslyAtHalfPast(){assertEquals(105f,GameRules.hourAngle(210),.001f);assertEquals(180f,GameRules.minuteAngle(210),.001f)}
    @Test fun midnightAndWrapDisplayAsTwelve(){assertEquals("12:00",GameRules.clockLabel(0));assertEquals("12:00",GameRules.clockLabel(720));assertEquals("11:55",GameRules.clockLabel(-5))}
    @Test fun timeTargetsRespectAgeProgression(){repeat(40){round->assertEquals(0,GameRules.clockTarget(round,0)%60);assertTrue(GameRules.clockTarget(round,3)%60 in listOf(0,30));assertEquals(0,GameRules.clockTarget(round,4)%5)}}
    @Test fun cloudyWeatherAllowsMoreThanOneReasonableOutfit(){assertTrue(GameRules.weatherAccepts(1,0));assertTrue(GameRules.weatherAccepts(1,1));assertTrue(GameRules.weatherAccepts(1,2));assertFalse(GameRules.weatherAccepts(2,1))}
    @Test fun fruitOrdersStayWithinAgeRange(){repeat(100){round->repeat(5){grade->val(a,b)=GameRules.fruitTargets(round,grade);assertTrue(a in 1..if(grade==4)20 else 10);if(grade<3)assertEquals(0,b)}}}
}

package tw.littlesprout.island

import androidx.compose.ui.graphics.Color

enum class GameMode(val id: String, val titleZh: String, val titleEn: String) {
    FREE("free", "自由玩", "Free Play"),
    STEP("step", "一步任務", "One Step"),
    COMBO("combo", "組合任務", "Combo"),
    CHALLENGE("challenge", "情境挑戰", "Challenge");

    companion object {
        fun fromId(id: String) = entries.firstOrNull { it.id == id } ?: FREE
    }
}

data class TaskOption(val id: String, val zh: String, val en: String, val icon: String = "")

data class IslandTask(
    val id: String,
    val scene: String,
    val mode: GameMode,
    val titleZh: String,
    val titleEn: String,
    val promptZh: String,
    val promptEn: String,
    val audioId: String,
    val targetItem: String = "",
    val targetCount: Int = 1,
    val secondItem: String = "",
    val secondCount: Int = 0,
    val stepCount: Int = 1
)

object GameplayRegistry {
    val scenes = listOf(
        // 海島探索 7 場景
        SceneInfo("beach", "海灘", "Beach", "island"),
        SceneInfo("bridge", "小橋", "Bridge", "island"),
        SceneInfo("fruit", "水果攤", "Fruit Stand", "island"),
        SceneInfo("delivery", "碼頭", "Harbor", "island"),
        SceneInfo("writing", "沙灘畫畫", "Sand Drawing", "island"),
        SceneInfo("clock", "車站", "Station", "island"),
        SceneInfo("weather", "小屋", "House", "island"),
        // 生活互動 6 場景
        SceneInfo("cleaning", "清潔站", "Clean Station", "life"),
        SceneInfo("sports", "球場", "Sports Court", "life"),
        SceneInfo("toyroom", "玩具屋", "Toy Room", "life"),
        SceneInfo("dining", "餐桌", "Dining Table", "life"),
        SceneInfo("shop", "商店", "Corner Shop", "life"),
        SceneInfo("garden", "花園", "Sprout Garden", "life")
    )

    data class SceneInfo(val id: String, val zh: String, val en: String, val section: String)

    fun getTasks(scene: String, mode: GameMode): List<IslandTask> {
        return allTasks.filter { it.scene == scene && it.mode == mode }
    }

    private val allTasks: List<IslandTask> = listOf(
        // 1. 水果攤 (Fruit Stand)
        IslandTask("fruit_free_1", "fruit", GameMode.FREE, "自由裝籃", "Free Packing", "自由選水果，裝籃、拿出或交給小兔子。", "Pick fruit freely, pack or give to Rabbit.", "fruit_free_prompt", "apple", 0),
        IslandTask("fruit_step_1", "fruit", GameMode.STEP, "拿出水果", "Take Fruit Out", "把籃子裡的鳳梨拿出來，放到桌上。", "Take the pineapple out of the basket onto table.", "fruit_step_take_pineapple", "pineapple", 1),
        IslandTask("fruit_step_2", "fruit", GameMode.STEP, "放進水果", "Put Fruit In", "把一顆鳳梨放進籃子裡。", "Put one pineapple into the basket.", "fruit_step_put_pineapple", "pineapple", 1),
        IslandTask("fruit_combo_1", "fruit", GameMode.COMBO, "照單補貨", "Follow Order", "放入兩顆草莓，再把鳳梨拿出來。", "Put two strawberries in, then take the pineapple out.", "fruit_combo_strawberries", "strawberry", 2, "pineapple", 1, stepCount = 2),
        IslandTask("fruit_combo_2", "fruit", GameMode.COMBO, "兩籃分裝", "Sort Two Baskets", "給第一位客人兩顆蘋果，第二位一顆梨子。", "Give two apples to guest 1, one pear to guest 2.", "fruit_combo_two_guests", "apple", 2, "pear", 1, stepCount = 2),
        IslandTask("fruit_chal_1", "fruit", GameMode.CHALLENGE, "修正訂單", "Fix Order", "籃子多了一顆鳳梨，請修正成訂單上的內容。", "There is an extra pineapple. Fix the basket.", "fruit_chal_fix_order", "pineapple", 0),
        IslandTask("fruit_chal_2", "fruit", GameMode.CHALLENGE, "退換水果", "Exchange Fruit", "客人想把香蕉換成橘子，請調整籃子內容。", "Exchange banana for orange according to order.", "fruit_chal_exchange", "orange", 1),

        // 2. 海灘 (Beach)
        IslandTask("beach_free_1", "beach", GameMode.FREE, "自由堆沙", "Beach Play", "在沙灘上自由擺放玩具與遮陽傘。", "Place beach toys and umbrellas freely.", "beach_free_prompt"),
        IslandTask("beach_step_1", "beach", GameMode.STEP, "找同色傘", "Match Color", "替小兔子裝上同色的遮陽傘。", "Find the matching color umbrella for Rabbit.", "beach_color"),
        IslandTask("beach_step_2", "beach", GameMode.STEP, "配對形狀", "Match Shape", "把玩具放進對應的形狀凹槽。", "Put the toy into the matching shape slot.", "beach_shape"),
        IslandTask("beach_combo_1", "beach", GameMode.COMBO, "空間位置", "Spatial Place", "把球放到桌子下，把帽子放椅上。", "Put ball under table and hat on chair.", "beach_below", stepCount = 2),
        IslandTask("beach_chal_1", "beach", GameMode.CHALLENGE, "分類與接續", "Sort & Pattern", "觀察規律，選出下一個接續的物品。", "Observe the pattern and pick what comes next.", "beach_pattern"),

        // 3. 小橋 (Bridge)
        IslandTask("bridge_free_1", "bridge", GameMode.FREE, "自由試放", "Bridge Testing", "試試各種長度的木板搭在橋上。", "Test various planks across the river.", "bridge_free_prompt"),
        IslandTask("bridge_step_1", "bridge", GameMode.STEP, "試放木板", "Try Plank", "選一塊長度剛好的木板，讓兔子過河。", "Choose a plank of correct length.", "bridge_instruction"),
        IslandTask("bridge_combo_1", "bridge", GameMode.COMBO, "拼接橋面", "Combine Planks", "用兩塊木板補齊有分段的橋面。", "Use two planks to complete the segmented bridge.", "bridge_combo_prompt", stepCount = 2),
        IslandTask("bridge_chal_1", "bridge", GameMode.CHALLENGE, "修橋檢查", "Inspect Bridge", "抽走放錯的太長或太短木板，換上剛好的。", "Remove wrong plank and replace with correct one.", "bridge_chal_prompt"),

        // 4. 送貨碼頭 (Harbor / Delivery)
        IslandTask("delivery_free_1", "delivery", GameMode.FREE, "自由裝船", "Free Loading", "自己選貨物裝上小船，點物品聽發音。", "Load cargo onto boat freely, tap to hear names.", "delivery_free_prompt"),
        IslandTask("delivery_step_1", "delivery", GameMode.STEP, "照圖裝貨", "Picture Order", "對照圖片訂單，把指定貨物裝上船。", "Match picture order and load cargo onto boat.", "delivery_step_prompt"),
        IslandTask("delivery_combo_1", "delivery", GameMode.COMBO, "聽音裝貨", "Listen & Load", "聽英文單字發音，選對貨物送到小屋。", "Listen to English word and deliver to house.", "delivery_combo_prompt"),
        IslandTask("delivery_chal_1", "delivery", GameMode.CHALLENGE, "分貨與退件", "Sort & Return", "從船上拿下多裝的貨物，送對小屋。", "Remove excess cargo from boat and deliver right.", "delivery_chal_prompt"),

        // 5. 沙灘畫畫 (Sand Drawing)
        IslandTask("writing_free_1", "writing", GameMode.FREE, "自由塗鴉", "Free Drawing", "在沙灘畫布自由畫畫，畫完可存進作品簿。", "Draw freely on sand, save to sketchbook.", "writing_instruction"),
        IslandTask("writing_step_1", "writing", GameMode.STEP, "單筆跟畫", "Single Stroke", "從起點開始，跟著方向練習畫線。", "Start from dot and follow stroke direction.", "writing_step_prompt"),
        IslandTask("writing_combo_1", "writing", GameMode.COMBO, "按筆順描寫", "Stroke Order", "依順序描寫數字、字母與注音。", "Trace numbers, letters and zhuyin in order.", "writing_combo_prompt"),
        IslandTask("writing_chal_1", "writing", GameMode.CHALLENGE, "看圖選字寫", "Picture & Write", "看數量或單字選出對應符號並描寫。", "Pick symbol by count or word, then trace.", "writing_chal_prompt"),

        // 6. 車站 (Station / Clock)
        IslandTask("clock_free_1", "clock", GameMode.FREE, "自由調鐘", "Free Clock", "自由撥動時針與分針，觀察時間變化。", "Rotate hour and minute hands freely.", "clock_free_prompt"),
        IslandTask("clock_step_1", "clock", GameMode.STEP, "找指針", "Identify Hands", "指出短的時針或長的分針。", "Find the short hour hand or long minute hand.", "clock_step_prompt"),
        IslandTask("clock_combo_1", "clock", GameMode.COMBO, "照車票調鐘", "Ticket Time", "依車票上的出發時間把時鐘調好。", "Set the clock to departure time on ticket.", "station_instruction"),
        IslandTask("clock_chal_1", "clock", GameMode.CHALLENGE, "生活時間", "Train Schedule", "火車還有五分鐘開車，推算時間讓火車出發。", "Train leaves in 5 minutes. Set clock to go.", "clock_chal_prompt"),

        // 7. 小屋 (Weather House)
        IslandTask("weather_free_1", "weather", GameMode.FREE, "自由換天氣", "Free Weather", "切換四種天氣，觀察窗外並聽英文。", "Switch weather freely and observe outside.", "weather_question"),
        IslandTask("weather_step_1", "weather", GameMode.STEP, "看圖找天氣", "Match Weather", "看窗外景色，選出對應的天氣圖示。", "Look out window and choose matching weather.", "weather_step_prompt"),
        IslandTask("weather_combo_1", "weather", GameMode.COMBO, "準備出門", "Get Ready", "下雨了，幫小兔子拿雨傘穿雨衣。", "It's raining. Pick umbrella or raincoat for Rabbit.", "weather_combo_prompt"),
        IslandTask("weather_chal_1", "weather", GameMode.CHALLENGE, "天氣變了", "Weather Changed", "拿走不適合的用品，換上現在需要的。", "Take off unfitted item, put on right gear.", "weather_chal_prompt"),

        // 8. 清潔站與垃圾車 (Clean Station) - 5.1 節
        IslandTask("clean_free_1", "cleaning", GameMode.FREE, "清潔自由玩", "Free Clean", "把場景中的垃圾袋、空瓶或箱子自由整理。", "Tidy up garbage bags and bottles freely.", "clean_free_prompt"),
        IslandTask("clean_step_1", "cleaning", GameMode.STEP, "把垃圾放進桶", "Trash in Bin", "撿起地上的紙團與垃圾，拖入垃圾桶。", "Pick up trash on ground into trash bin.", "clean_trash_bin"),
        IslandTask("clean_step_2", "cleaning", GameMode.STEP, "垃圾資源分開", "Separate Recycle", "一般垃圾袋放垃圾桶，空瓶空罐放回收箱。", "Trash bags in bin, bottles in recycle box.", "clean_recycle"),
        IslandTask("clean_combo_1", "cleaning", GameMode.COMBO, "裝上垃圾車", "Load Truck", "把三袋垃圾搬上垃圾車收運口。", "Load three trash bags into garbage truck.", "clean_load_truck", targetItem = "bag", targetCount = 3),
        IslandTask("clean_combo_2", "cleaning", GameMode.COMBO, "還少幾袋", "How Many Missing", "車上已有兩袋，還差幾袋補滿五袋？", "Two on truck already. Add more to make five.", "clean_missing_bags", targetItem = "bag", targetCount = 5),
        IslandTask("clean_chal_1", "cleaning", GameMode.CHALLENGE, "拿下裝錯的", "Unload Wrong Item", "垃圾車內混入玩具，拖回保留區才能出發。", "Remove toy from garbage truck back to table.", "clean_remove_wrong"),
        IslandTask("clean_chal_2", "cleaning", GameMode.CHALLENGE, "路線清掃", "Route Clean", "依照停靠點收好垃圾，送到收集站。", "Pick up trash at each stop and finish route.", "clean_route"),

        // 9. 球場 (Sports Court) - 5.2 節
        IslandTask("sports_free_1", "sports", GameMode.FREE, "自由投球", "Free Shooting", "自由點擊或拖曳籃球、足球進行練習。", "Shoot hoops or kick balls freely.", "sports_free_prompt"),
        IslandTask("sports_step_1", "sports", GameMode.STEP, "球放回收納籃", "Store Balls", "把散落的籃球足球收進地上的球籃。", "Put scattered balls into the storage basket.", "sports_store_basket"),
        IslandTask("sports_step_2", "sports", GameMode.STEP, "籃球投籃", "Basketball Shoot", "把籃球滑動或點擊投進高處的籃框。", "Shoot basketball into the hoop.", "sports_basketball_hoop"),
        IslandTask("sports_step_3", "sports", GameMode.STEP, "足球射門", "Soccer Goal", "把足球瞄準踢進球門中。", "Kick the soccer ball into the goal net.", "sports_soccer_goal"),
        IslandTask("sports_combo_1", "sports", GameMode.COMBO, "進三球得分", "Score Three", "連續投進或踢進三球，完成計分板。", "Make three successful goals to fill scoreboard.", "sports_score_three", targetCount = 3),
        IslandTask("sports_combo_2", "sports", GameMode.COMBO, "球歸對位", "Sort Balls", "籃球進籃框、足球進球門、排球回收納箱。", "Basketball to hoop, soccer to goal, volleyball in box.", "sports_sort_places"),
        IslandTask("sports_chal_1", "sports", GameMode.CHALLENGE, "輪流傳球", "Pass Ball", "按圖示順序把球傳給兔子與小熊。", "Pass ball in order from Rabbit to Bear.", "sports_pass_ball"),
        IslandTask("sports_chal_2", "sports", GameMode.CHALLENGE, "平分給兩隊", "Divide Teams", "給紅隊與藍隊各三顆球，數量要一樣多。", "Give three balls to each team evenly.", "sports_divide_teams"),

        // 10. 玩具屋 (Toy Room) - 5.3 節
        IslandTask("toy_free_1", "toyroom", GameMode.FREE, "自由整理", "Free Tidy", "自由把小車、積木與娃娃擺在房間各處。", "Place cars, blocks, and dolls freely.", "toy_free_prompt"),
        IslandTask("toy_step_1", "toyroom", GameMode.STEP, "玩具回家", "Toy Home", "小車進車庫、積木回盒子、娃娃回床上。", "Car to garage, blocks to box, doll to bed.", "toy_home_prompt"),
        IslandTask("toy_step_2", "toyroom", GameMode.STEP, "配成一雙", "Pair Up", "從鞋子襪子中拖出花紋與顏色相同的一對。", "Find the matching pair of shoes or socks.", "toy_pair_prompt"),
        IslandTask("toy_combo_1", "toyroom", GameMode.COMBO, "依大小收好", "Sort by Size", "大、中、小盒子放進對應大小的櫃格。", "Place big, medium, and small boxes in slots.", "toy_size_prompt"),
        IslandTask("toy_combo_2", "toyroom", GameMode.COMBO, "按位置擺放", "Place by Position", "書放架上、球放桌下、小熊放椅旁。", "Book on shelf, ball under table, bear by chair.", "toy_position_prompt"),
        IslandTask("toy_chal_1", "toyroom", GameMode.CHALLENGE, "找出多放的", "Find Excess", "三個車位停了四台車，把多的一台移開。", "Remove the extra car from parking slots.", "toy_excess_prompt"),

        // 11. 餐桌與點心 (Dining Table) - 5.4 節
        IslandTask("dining_free_1", "dining", GameMode.FREE, "自由備餐", "Free Dining", "自由在餐盤放餅乾與水果準備點心。", "Prepare snacks and fruit on plates freely.", "dining_free_prompt"),
        IslandTask("dining_step_1", "dining", GameMode.STEP, "幫大家擺餐具", "Set Table", "為每位小客人前放一個盤子、一支湯匙。", "Put one plate and spoon for each guest.", "dining_set_table"),
        IslandTask("dining_step_2", "dining", GameMode.STEP, "補齊缺少的", "Fill Missing", "四位客人已有三個杯子，補上缺少的一個。", "Add the missing cup for four guests.", "dining_fill_missing"),
        IslandTask("dining_combo_1", "dining", GameMode.COMBO, "分點心", "Share Treats", "六片餅乾平分給三位客人，每人兩片。", "Share six cookies evenly among three guests.", "dining_share_treats"),
        IslandTask("dining_combo_2", "dining", GameMode.COMBO, "收拾餐桌", "Clean Table", "杯子放托盤、餐具收盒子，桌面擦乾淨。", "Cups on tray, spoons in box, wipe table.", "dining_clean_table"),
        IslandTask("dining_chal_1", "dining", GameMode.CHALLENGE, "客人改訂單", "Change Order", "客人原要兩顆草莓，改成一顆草莓一根香蕉。", "Rabbit wants 1 strawberry & 1 banana now. Adjust.", "dining_change_order"),

        // 12. 商店與送貨 (Shop) - 5.5 節
        IslandTask("shop_free_1", "shop", GameMode.FREE, "自由購物", "Free Shop", "從貨架挑選牛奶、麵包與蘋果放入購物籃。", "Pick groceries from shelf to basket freely.", "shop_free_prompt"),
        IslandTask("shop_step_1", "shop", GameMode.STEP, "照清單購物", "Shopping List", "按圖片清單挑選牛奶和麵包進籃子。", "Pick milk and bread matching shopping list.", "shop_shopping_list"),
        IslandTask("shop_step_2", "shop", GameMode.STEP, "貨架補滿", "Restock Shelf", "貨架有空位，從箱子拿貨物補滿。", "Restock empty shelf spaces from inventory.", "shop_restock_shelf"),
        IslandTask("shop_combo_1", "shop", GameMode.COMBO, "送指定地址", "Deliver Address", "依照門牌上的形狀與顏色送包裹到小屋。", "Deliver parcel to house matching door sign.", "shop_deliver_address"),
        IslandTask("shop_chal_1", "shop", GameMode.CHALLENGE, "拿回多買的", "Remove Extra", "籃子比清單多了一盒餅乾，放回原貨架。", "Basket has extra cookie. Put it back to shelf.", "shop_remove_extra"),

        // 13. 花園與動物 (Garden) - 5.6 節
        IslandTask("garden_free_1", "garden", GameMode.FREE, "自由種植", "Free Garden", "在花園花盆中播種、澆水或採收紅蘿蔔。", "Plant seeds, water flowers or harvest carrots.", "garden_free_prompt"),
        IslandTask("garden_step_1", "garden", GameMode.STEP, "一個洞一顆種子", "One Seed Each", "把種子放進三個花盆中，每盆一顆。", "Put one seed into each of the three pots.", "garden_seed_prompt"),
        IslandTask("garden_step_2", "garden", GameMode.STEP, "給乾土澆水", "Water Dry Soil", "觀察乾燥的土壤圖示，幫它澆水長花。", "Find dry soil pot and water it to bloom.", "garden_water_prompt"),
        IslandTask("garden_combo_1", "garden", GameMode.COMBO, "照顧花朵", "Care Flowers", "按順序放種子、覆土、澆水，長出美麗花朵。", "Put seed, add soil, and water to grow flower.", "garden_grow_prompt", stepCount = 3),
        IslandTask("garden_combo_2", "garden", GameMode.COMBO, "採收紅蘿蔔", "Harvest Carrots", "採收四根紅蘿蔔放進推車，田裡保留兩根。", "Harvest four carrots into cart, leave two in field.", "garden_harvest_prompt"),
        IslandTask("garden_chal_1", "garden", GameMode.CHALLENGE, "餵指定動物", "Feed Animals", "把紅蘿蔔給小兔子、乾草給小羊吃。", "Feed carrot to Rabbit and hay to Sheep.", "garden_feed_prompt")
    )
}

object EnglishThemes {
    data class ThemeCategory(val id: String, val zh: String, val en: String, val wordIds: List<String>)

    val categories = listOf(
        ThemeCategory("animals", "動物 Animals", "Animals", listOf("cat", "dog", "rabbit", "cow", "sheep", "lion", "elephant", "bear", "duck", "frog", "horse", "pig")),
        ThemeCategory("everyday", "生活 Everyday Life", "Everyday Life", listOf("cup", "plate", "spoon", "chair", "hat", "ball", "basket", "clock", "bed", "book", "box", "umbrella")),
        ThemeCategory("fruit", "水果 Fruit", "Fruit", listOf("apple", "banana", "pineapple", "strawberry", "pear", "orange", "grape", "lemon", "watermelon")),
        ThemeCategory("transport", "交通 Transport", "Transport", listOf("car", "bus", "train", "boat", "airplane", "bicycle", "truck", "helicopter", "ship"))
    )
}

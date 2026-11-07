package tw.littlesprout.island

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.*

private val Outline = Color(0xFF344C46)
private val Leaf = Color(0xFF5D9870)
private val Gold = Color(0xFFECC36A)
private val Rose = Color(0xFFE58075)
private val Blue = Color(0xFF72ACCA)
private val Sand = Color(0xFFE7C991)

@Composable fun WordArt(id: String, modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = id.replace('_', ' ') }) {
        val unit = size.minDimension / 100f
        scale(unit, unit, Offset.Zero) { art(id) }
    }
}

internal fun DrawScope.art(id: String) {
    fun oval(x: Float, y: Float, w: Float, h: Float, c: Color) = drawOval(c, Offset(x,y), Size(w,h))
    fun box(x: Float, y: Float, w: Float, h: Float, c: Color) = drawRoundRect(c, Offset(x,y), Size(w,h), androidx.compose.ui.geometry.CornerRadius(4f))
    fun line(x: Float, y: Float, xx: Float, yy: Float, c: Color = Outline, width: Float = 3f) = drawLine(c, Offset(x,y), Offset(xx,yy), width, StrokeCap.Round)
    fun poly(c: Color, vararg xy: Float) { val p=Path(); p.moveTo(xy[0],xy[1]); var i=2; while(i<xy.size){p.lineTo(xy[i],xy[i+1]);i+=2};p.close();drawPath(p,c) }
    fun eye(x: Float, y: Float) { oval(x,y,4f,5f,Outline); oval(x+.7f,y+.4f,1.2f,1.2f,Color.White) }
    fun wheels() { oval(19f,70f,17f,17f,Outline);oval(64f,70f,17f,17f,Outline);oval(24f,75f,7f,7f,Sand);oval(69f,75f,7f,7f,Sand) }
    fun head(c: Color) { oval(22f,27f,58f,56f,c);eye(37f,48f);eye(61f,48f);oval(47f,60f,7f,5f,Outline) }
    fun fruit(c: Color) { oval(23f,33f,52f,49f,c);line(49f,36f,53f,20f,Outline,4f);oval(53f,21f,20f,9f,Leaf) }
    val mammals=setOf("cat","dog","bear","cow","elephant","goat","giraffe","horse","koala","lion","monkey","mouse","pig","panda","rabbit","tiger","unicorn","yak","zebra","fox")
    if(id in mammals) {
        val c=when(id){"pig"->Color(0xFFE4A39B);"elephant","koala","mouse"->Color(0xFFA2B7B6);"panda","cow","zebra"->Color(0xFFF7F2DF);"iguana"->Leaf;"fox","tiger","lion","giraffe"->Gold;"unicorn"->Color(0xFFF4E7DF);else->Sand}
        if(id=="lion") repeat(12){i->val a=i*PI/6;oval(39f+cos(a).toFloat()*29,43f+sin(a).toFloat()*26,22f,25f,Color(0xFFBA8450))}
        when(id) {
            "rabbit"->{oval(28f,2f,16f,43f,c);oval(56f,2f,16f,43f,c);oval(33f,7f,6f,29f,Rose);oval(61f,7f,6f,29f,Rose)}
            "elephant"->{oval(5f,30f,33f,49f,c);oval(65f,30f,33f,49f,c)}
            "cat","fox"->{poly(c,22f,42f,24f,11f,43f,30f);poly(c,59f,30f,77f,11f,80f,42f)}
            "horse","giraffe","zebra","unicorn"->{oval(22f,8f,15f,36f,c);oval(65f,8f,15f,36f,c)}
            "dog"->{oval(10f,23f,25f,46f,Color(0xFFBA8450));oval(67f,23f,25f,46f,Color(0xFFBA8450))}
            else->{oval(17f,20f,24f,25f,c);oval(61f,20f,24f,25f,c)}
        }
        head(c)
        when(id) {
            "panda"->{oval(29f,40f,20f,23f,Outline);oval(55f,40f,20f,23f,Outline);oval(36f,46f,5f,6f,Color.White);oval(61f,46f,5f,6f,Color.White);oval(19f,20f,23f,22f,Outline);oval(62f,20f,23f,22f,Outline)}
            "cow","yak","goat"->{poly(Outline,28f,32f,15f,10f,36f,23f);poly(Outline,64f,23f,85f,10f,72f,32f);oval(34f,60f,35f,20f,if(id=="yak") Color(0xFFAA7D58) else Rose);if(id=="cow")oval(24f,35f,18f,18f,Outline);if(id=="yak"){repeat(5){i->poly(Color(0xFFAA7D58),25f+i*10,65f,35f+i*10,65f,30f+i*10,90f)}};if(id=="goat")poly(Color.White,40f,78f,60f,78f,50f,94f)}
            "elephant"->{box(44f,58f,15f,35f,c);line(49f,88f,63f,85f,c,10f)}
            "pig"->{oval(35f,57f,32f,20f,Rose);eye(43f,64f);eye(55f,64f)}
            "cat","tiger"->{line(24f,61f,9f,57f);line(24f,66f,9f,69f);line(76f,61f,91f,57f);line(76f,66f,91f,69f);if(id=="tiger"){line(40f,30f,43f,40f);line(52f,28f,52f,40f);line(64f,30f,61f,40f)}}
            "zebra"->{line(35f,31f,42f,42f);line(53f,29f,53f,42f);line(69f,32f,63f,42f);line(24f,60f,34f,65f);line(69f,65f,79f,60f)}
            "fox"->{poly(Color.White,24f,56f,47f,64f,51f,80f);poly(Color.White,76f,56f,54f,64f,51f,80f)}
            "monkey"->{oval(29f,40f,45f,37f,Color(0xFFF7E4C0));eye(37f,47f);eye(61f,47f);oval(46f,61f,8f,5f,Outline)}
            "giraffe"->{line(37f,25f,37f,10f,Outline,5f);line(64f,25f,64f,10f,Outline,5f);oval(31f,30f,10f,10f,Color(0xFFBA8450));oval(65f,61f,10f,12f,Color(0xFFBA8450))}
            "unicorn"->poly(Gold,42f,30f,51f,2f,59f,30f)
            "koala"->oval(43f,49f,16f,22f,Outline)
            "mouse"->{oval(15f,15f,29f,30f,c);oval(59f,15f,29f,30f,c);oval(22f,21f,15f,16f,Rose);oval(66f,21f,15f,16f,Rose)}
            "iguana"->{repeat(5){i->poly(Leaf,23f+i*11,34f,28f+i*11,17f,34f+i*11,34f)};line(25f,75f,78f,75f,Leaf,8f)}
            else->Unit
        };return
    }
    when(id) {
        "kangaroo"->{val tail=Path().apply{moveTo(38f,70f);quadraticTo(20f,93f,5f,88f);quadraticTo(26f,88f,31f,57f)};drawPath(tail,Sand);oval(30f,40f,37f,44f,Sand);oval(41f,55f,20f,24f,Color(0xFFF4E5C1));oval(39f,18f,29f,29f,Sand);oval(40f,2f,8f,25f,Sand);oval(58f,2f,8f,25f,Sand);line(35f,75f,65f,91f,Sand,10f);line(65f,91f,83f,91f,Sand,8f);line(59f,48f,78f,58f,Sand,7f);eye(58f,26f)}
        "iguana"->{val tail=Path().apply{moveTo(30f,62f);quadraticTo(6f,88f,3f,65f);quadraticTo(12f,76f,29f,54f)};drawPath(tail,Leaf);oval(20f,42f,59f,29f,Leaf);oval(64f,30f,30f,24f,Leaf);repeat(6){i->poly(Leaf,23f+i*8,46f,27f+i*8,29f,33f+i*8,46f)};line(34f,65f,27f,81f,Leaf,7f);line(67f,65f,76f,81f,Leaf,7f);line(21f,82f,35f,82f,Leaf,4f);line(69f,82f,84f,82f,Leaf,4f);eye(81f,35f)}
        "alligator"->{oval(12f,37f,62f,40f,Leaf);poly(Leaf,58f,45f,93f,47f,93f,66f,58f,67f);repeat(6){i->poly(Color(0xFF397A56),19f+i*9,42f,24f+i*9,25f,29f+i*9,42f)};eye(60f,39f);line(68f,58f,93f,58f);repeat(4){i->poly(Color.White,69f+i*6,58f,72f+i*6,64f,75f+i*6,58f)};box(22f,70f,13f,17f,Leaf);box(53f,70f,13f,17f,Leaf)}
        "apple"->{val p=Path().apply{moveTo(50f,36f);cubicTo(25f,20f,12f,42f,25f,68f);cubicTo(33f,88f,42f,86f,50f,80f);cubicTo(61f,87f,72f,80f,79f,59f);cubicTo(91f,31f,68f,23f,50f,36f)};drawPath(p,Rose);line(49f,35f,53f,18f,Outline,4f);oval(54f,21f,20f,9f,Leaf)}
        "orange"->fruit(Color(0xFFEFA24F))
        "lemon"->{oval(18f,32f,66f,46f,Gold);poly(Gold,18f,44f,9f,54f,20f,65f);poly(Gold,81f,44f,92f,54f,80f,65f)}
        "pear"->{oval(32f,22f,35f,45f,Leaf);oval(23f,44f,55f,43f,Leaf);line(49f,25f,55f,12f)}
        "banana"->{drawArc(Gold,10f,160f,false,Offset(16f,10f),Size(69f,69f),style=Stroke(17f,cap=StrokeCap.Round));line(85f,44f,89f,35f)}
        "grape"->{repeat(4){r->repeat(4-r){i->oval(20f+i*16+r*8,30f+r*15,21f,21f,Color(0xFF8C78AD))}};line(49f,29f,56f,13f,Leaf,6f)}
        "watermelon"->{drawArc(Leaf,0f,180f,true,Offset(10f,10f),Size(80f,75f));drawArc(Rose,0f,180f,true,Offset(17f,10f),Size(66f,63f));repeat(5){i->oval(26f+i*11,35f+(i%2)*12,3f,7f,Outline)}}
        "ant"->{oval(35f,20f,27f,26f,Outline);oval(41f,44f,17f,18f,Outline);oval(33f,61f,33f,27f,Outline);repeat(3){i->line(43f,49f+i*9,18f,40f+i*16);line(58f,49f+i*9,82f,40f+i*16)};line(43f,22f,33f,9f);line(56f,22f,66f,9f)}
        "insect"->{oval(24f,30f,52f,56f,Rose);oval(36f,16f,28f,24f,Outline);line(50f,33f,50f,85f);repeat(3){i->oval(32f,40f+i*15,7f,7f,Outline);oval(61f,40f+i*15,7f,7f,Outline)}}
        "fish","whale"->{oval(15f,33f,65f,42f,Blue);poly(Blue,72f,52f,96f,28f,96f,76f);eye(28f,45f);if(id=="whale"){line(31f,30f,27f,12f,Blue,4f);line(31f,25f,42f,12f,Blue,4f);oval(25f,58f,30f,13f,Color(0xFFD3E9EC))}}
        "frog"->{oval(20f,32f,60f,48f,Leaf);oval(23f,19f,23f,28f,Leaf);oval(55f,19f,23f,28f,Leaf);eye(32f,25f);eye(64f,25f);drawArc(Outline,0f,180f,false,Offset(33f,43f),Size(36f,22f),style=Stroke(3f))}
        "duck","owl","quail"->{oval(25f,36f,53f,48f,if(id=="duck") Gold else Sand);oval(29f,17f,47f,45f,if(id=="duck") Gold else Sand);poly(Color(0xFFE6A052),47f,42f,67f,46f,48f,52f);eye(42f,30f);if(id=="owl"){oval(29f,25f,22f,24f,Color.White);oval(52f,25f,22f,24f,Color.White);eye(39f,33f);eye(60f,33f)};if(id=="quail"){line(51f,20f,57f,7f);oval(55f,5f,12f,8f,Outline);repeat(5){i->oval(34f+i*8,66f+(i%2)*8,4f,4f,Outline)}}}
        "jellyfish"->{drawArc(Color(0xFFD4A8BA),180f,180f,true,Offset(19f,18f),Size(63f,69f));repeat(5){i->line(28f+i*11,53f,23f+i*13,88f,Color(0xFFD4A8BA),5f)}}
        "octopus"->{oval(24f,15f,53f,52f,Color(0xFFD4A8BA));repeat(8){i->line(28f+i*6,56f,8f+i*12,85f,Color(0xFFD4A8BA),8f)};eye(38f,36f);eye(59f,36f)}
        "snake"->{val p=Path().apply{moveTo(15f,75f);cubicTo(90f,95f,85f,40f,36f,48f);cubicTo(5f,55f,20f,10f,65f,22f)};drawPath(p,Leaf,style=Stroke(15f,cap=StrokeCap.Round));oval(55f,15f,26f,18f,Leaf);eye(69f,18f);line(80f,25f,93f,25f,Rose,2f)}
        "turtle"->{oval(15f,37f,63f,42f,Leaf);oval(73f,45f,23f,20f,Leaf);oval(21f,70f,13f,16f,Leaf);oval(59f,70f,13f,16f,Leaf);drawOval(Outline,Offset(25f,41f),Size(39f,28f),style=Stroke(3f));eye(84f,48f)}
        "airplane"->{poly(Blue,47f,7f,56f,7f,58f,38f,93f,57f,93f,67f,58f,56f,57f,79f,72f,89f,72f,95f,51f,88f,30f,95f,30f,89f,45f,79f,44f,56f,9f,67f,9f,57f,44f,38f)}
        "car","bus","van","fire_truck","train"->{box(12f,34f,76f,43f,if(id=="fire_truck") Rose else Blue);repeat(if(id=="bus")4 else 2){i->box(20f+i*16,40f,12f,14f,Color(0xFFD9EFF2))};if(id=="car")poly(Blue,25f,36f,37f,20f,65f,20f,80f,38f);if(id=="fire_truck"){line(22f,25f,82f,25f,Outline,5f);line(22f,31f,82f,31f,Outline,3f)};if(id=="train"){box(12f,16f,18f,20f,Outline);box(61f,17f,26f,28f,Blue);oval(10f,5f,17f,10f,Color(0xFFD6DAD0))};wheels()}
        "ball"->{oval(17f,17f,66f,66f,Gold);drawOval(Outline,Offset(17f,17f),Size(66f,66f),style=Stroke(3f));line(18f,51f,82f,51f);drawArc(Outline,90f,180f,false,Offset(33f,17f),Size(34f,66f),style=Stroke(3f))}
        "cake"->{box(19f,47f,63f,35f,Sand);box(19f,43f,63f,14f,Rose);line(50f,43f,50f,24f,Blue,5f);oval(46f,14f,9f,12f,Gold)}
        "egg"->oval(27f,16f,47f,69f,Color(0xFFF5E9C9))
        "ear"->{drawArc(Sand,240f,270f,false,Offset(27f,12f),Size(45f,72f),style=Stroke(12f,cap=StrokeCap.Round));drawArc(Outline,240f,210f,false,Offset(37f,28f),Size(23f,37f),style=Stroke(4f,cap=StrokeCap.Round))}
        "eye"->{oval(10f,28f,80f,45f,Color.White);oval(32f,29f,37f,43f,Blue);oval(43f,39f,17f,23f,Outline);oval(47f,39f,7f,8f,Color.White)}
        "door"->{box(22f,10f,56f,80f,Sand);box(29f,18f,42f,65f,Color(0xFFBA8450));oval(59f,51f,6f,6f,Gold)}
        "drum"->{box(20f,35f,60f,45f,Rose);oval(20f,25f,60f,21f,Color(0xFFF4E6C6));repeat(3){i->line(25f+i*20,43f,35f+i*20,77f,Gold)};line(13f,12f,48f,30f,Sand,5f);line(87f,12f,52f,30f,Sand,5f)}
        "flower"->{line(50f,50f,50f,90f,Leaf,5f);oval(50f,68f,25f,12f,Leaf);repeat(6){i->val a=i*PI/3;oval(38f+cos(a).toFloat()*21,31f+sin(a).toFloat()*21,25f,25f,Rose)};oval(38f,32f,25f,25f,Gold)}
        "guitar"->{oval(23f,52f,48f,40f,Sand);oval(31f,38f,33f,34f,Sand);box(44f,7f,9f,48f,Color(0xFFBA8450));oval(41f,53f,13f,14f,Outline);line(48f,14f,48f,80f,Color.White,1f)}
        "hat"->{oval(10f,58f,80f,22f,Gold);box(27f,22f,46f,46f,Gold);box(26f,52f,48f,11f,Rose)}
        "house"->{box(23f,44f,56f,44f,Sand);poly(Rose,12f,46f,51f,12f,91f,46f);box(44f,60f,16f,28f,Color(0xFFBA8450));box(29f,55f,13f,15f,Blue);box(65f,55f,11f,15f,Blue)}
        "hand"->{box(29f,38f,45f,48f,Sand);repeat(4){i->line(33f+i*12,50f,33f+i*12,15f+abs(i-1)*6,Sand,10f)};line(30f,67f,14f,45f,Sand,12f)}
        "ice_cream"->{poly(Sand,27f,49f,73f,49f,50f,94f);oval(20f,19f,35f,39f,Rose);oval(47f,19f,35f,39f,Color(0xFFF7E3AF));oval(35f,7f,33f,35f,Color(0xFFAA7D58))}
        "igloo"->{drawArc(Color(0xFFD6EDF0),180f,180f,true,Offset(9f,8f),Size(82f,150f));repeat(3){i->line(18f,36f+i*17,82f,36f+i*17,Blue,2f)};repeat(3){i->line(30f+i*20,28f,30f+i*20,75f,Blue,2f)};drawArc(Blue,180f,180f,true,Offset(37f,45f),Size(30f,65f))}
        "juice"->{poly(Gold,24f,29f,76f,29f,68f,88f,33f,88f);line(56f,62f,66f,11f,Rose,5f);line(66f,11f,84f,11f,Rose,5f);line(27f,43f,73f,43f,Color.White,3f)}
        "jacket","vest","uniform"->{if(id=="vest")poly(Gold,32f,18f,43f,13f,58f,13f,69f,18f,65f,42f,73f,89f,27f,89f,35f,42f)else poly(Blue,31f,18f,43f,13f,58f,13f,70f,18f,91f,43f,77f,55f,70f,43f,73f,89f,27f,89f,30f,43f,23f,55f,9f,43f);line(50f,26f,50f,86f,Outline,2f);poly(Color.White,33f,18f,45f,14f,50f,26f,41f,38f);poly(Color.White,67f,18f,56f,14f,50f,26f,59f,38f);if(id=="uniform")poly(Rose,49f,28f,55f,38f,51f,62f,45f,38f)}
        "jar"->{box(25f,23f,50f,64f,Color(0xFFB8D9D6));box(23f,13f,54f,13f,Gold);box(31f,46f,38f,25f,Color(0xFFF9EAC7))}
        "kite"->{poly(Rose,50f,8f,77f,39f,50f,69f,23f,39f);line(50f,8f,50f,69f);line(23f,39f,77f,39f);line(50f,69f,39f,84f);line(39f,84f,58f,96f)}
        "key"->{drawCircle(Gold,18f,Offset(30f,29f),style=Stroke(9f));line(42f,42f,79f,79f,Gold,10f);line(62f,62f,72f,51f,Gold,9f);line(73f,73f,84f,62f,Gold,9f)}
        "leaf"->{val p=Path().apply{moveTo(20f,79f);cubicTo(0f,20f,48f,10f,86f,14f);cubicTo(92f,63f,60f,92f,20f,79f)};drawPath(p,Leaf);line(18f,86f,69f,32f,Color(0xFF35684C),3f)}
        "lamp"->{poly(Gold,33f,12f,68f,12f,85f,52f,15f,52f);line(50f,52f,50f,86f,Outline,6f);box(29f,85f,43f,7f,Outline)}
        "moon"->{oval(16f,12f,70f,74f,Gold);oval(40f,4f,52f,61f,Color(0xFFE4F3F4))}
        "milk"->{box(28f,27f,45f,62f,Color(0xFFF9F2DF));poly(Blue,28f,27f,39f,10f,62f,10f,73f,27f);box(28f,47f,45f,24f,Blue)}
        "nose"->{poly(Sand,43f,20f,57f,20f,71f,67f,60f,79f,39f,79f,28f,67f);oval(34f,67f,11f,7f,Outline);oval(56f,67f,11f,7f,Outline)}
        "nest"->{oval(13f,48f,75f,35f,Sand);repeat(5){i->line(18f,55f+i*5,83f,60f+i*4,Color(0xFFAA7D58),3f)};oval(29f,30f,22f,32f,Blue);oval(50f,29f,22f,33f,Color(0xFFF2EBD2))}
        "net"->{box(15f,20f,70f,65f,Color.White);repeat(6){i->line(17f+i*13,22f,17f+i*13,83f,Blue,1.5f);line(17f,23f+i*12,83f,23f+i*12,Blue,1.5f)};line(15f,88f,15f,20f,Outline,4f);line(15f,20f,85f,20f,Outline,4f);line(85f,20f,85f,88f,Outline,4f)}
        "noodles","yogurt"->{drawArc(Blue,0f,180f,true,Offset(12f,8f),Size(77f,74f));oval(12f,31f,77f,23f,if(id=="noodles") Gold else Color.White);if(id=="noodles"){repeat(5){i->drawArc(Sand,180f,280f,false,Offset(22f+i*11,28f),Size(15f,18f),style=Stroke(3f))};line(28f,18f,84f,2f,Outline,3f);line(32f,23f,88f,7f,Outline,3f)}else{line(65f,45f,82f,9f,Outline,5f);oval(77f,2f,12f,20f,Outline)}}
        "onion"->{fruit(Color(0xFFC69DB3));line(40f,35f,46f,13f,Leaf,4f);line(52f,35f,56f,10f,Leaf,4f);drawArc(Color(0xFFF5E7EC),90f,180f,false,Offset(37f,36f),Size(26f,44f),style=Stroke(2f))}
        "pencil"->{poly(Gold,20f,70f,66f,14f,80f,26f,34f,82f);poly(Sand,20f,70f,34f,82f,12f,92f);poly(Outline,17f,83f,23f,88f,12f,92f);poly(Rose,66f,14f,74f,5f,88f,17f,80f,26f)}
        "queen"->{head(Sand);poly(Gold,23f,32f,17f,7f,39f,22f,50f,3f,62f,22f,83f,7f,77f,32f);oval(47f,20f,7f,8f,Rose)}
        "quilt"->{box(14f,14f,72f,75f,Sand);repeat(3){r->repeat(3){c->box(18f+c*23,18f+r*23,20f,20f,listOf(Rose,Blue,Gold,Leaf)[(r+c)%4])}}}
        "quill"->{val p=Path().apply{moveTo(25f,77f);cubicTo(10f,20f,70f,0f,80f,13f);cubicTo(87f,46f,44f,82f,25f,77f)};drawPath(p,Blue);line(16f,92f,66f,25f,Outline,3f);line(45f,53f,73f,47f,Color.White,2f)}
        "rainbow"->{listOf(Rose,Gold,Leaf,Blue).forEachIndexed{i,c->drawArc(c,180f,180f,false,Offset(10f+i*8,16f+i*8),Size(80f-i*16,100f-i*16),style=Stroke(8f))}}
        "robot"->{box(21f,25f,59f,52f,Blue);box(29f,77f,14f,16f,Outline);box(59f,77f,14f,16f,Outline);line(50f,25f,50f,12f,Outline);oval(44f,5f,12f,12f,Rose);box(29f,37f,16f,14f,Color.White);box(56f,37f,16f,14f,Color.White);box(33f,61f,35f,6f,Outline)}
        "ring"->{drawCircle(Gold,25f,Offset(50f,61f),style=Stroke(9f));poly(Blue,29f,27f,38f,12f,63f,12f,73f,27f,50f,47f)}
        "sun"->{repeat(12){i->val a=i*PI/6;line(50f+cos(a).toFloat()*33,50f+sin(a).toFloat()*33,50f+cos(a).toFloat()*44,50f+sin(a).toFloat()*44,Gold,4f)};oval(24f,24f,52f,52f,Gold)}
        "star"->{val p=Path();repeat(10){i->val a=-PI/2+i*PI/5;val r=if(i%2==0)42f else 19f;val x=50+cos(a).toFloat()*r;val y=50+sin(a).toFloat()*r;if(i==0)p.moveTo(x,y)else p.lineTo(x,y)};p.close();drawPath(p,Gold)}
        "shoe"->{val p=Path().apply{moveTo(17f,39f);lineTo(44f,39f);lineTo(60f,61f);lineTo(84f,65f);lineTo(91f,80f);lineTo(14f,80f);close()};drawPath(p,Blue);line(16f,81f,91f,81f,Color.White,7f);line(45f,52f,57f,48f,Color.White);line(52f,61f,63f,57f,Color.White)}
        "tree"->{box(44f,46f,13f,47f,Color(0xFFAA7D58));oval(13f,27f,45f,40f,Leaf);oval(43f,26f,45f,42f,Leaf);oval(29f,8f,45f,48f,Leaf)}
        "umbrella"->{drawArc(Blue,180f,180f,true,Offset(8f,9f),Size(84f,80f));line(50f,47f,50f,83f,Outline,4f);drawArc(Outline,0f,180f,false,Offset(33f,74f),Size(17f,20f),style=Stroke(4f))}
        "uncle"->{oval(24f,13f,53f,57f,Sand);poly(Outline,23f,39f,27f,15f,42f,7f,65f,10f,78f,29f,71f,34f,62f,24f,40f,28f);eye(37f,36f);eye(60f,36f);box(20f,71f,60f,24f,Blue);drawArc(Outline,0f,180f,false,Offset(40f,44f),Size(22f,14f),style=Stroke(2f))}
        "vase"->{val p=Path().apply{moveTo(36f,17f);lineTo(65f,17f);lineTo(61f,35f);cubicTo(93f,68f,81f,89f,50f,91f);cubicTo(18f,89f,7f,68f,40f,35f);close()};drawPath(p,Blue);line(27f,68f,75f,68f,Gold,7f)}
        "violin"->{oval(25f,51f,46f,41f,Color(0xFFBA8450));oval(30f,35f,35f,34f,Color(0xFFBA8450));box(43f,5f,9f,47f,Outline);line(47f,8f,47f,82f,Gold,2f);line(84f,10f,72f,91f,Sand,4f)}
        "watch"->{box(37f,3f,26f,94f,Color(0xFFBA8450));oval(20f,20f,60f,60f,Gold);oval(27f,27f,46f,46f,Color.White);line(50f,50f,50f,34f);line(50f,50f,63f,56f)}
        "window"->{box(12f,12f,77f,77f,Sand);box(20f,20f,60f,60f,Blue);line(50f,19f,50f,81f,Color.White,6f);line(19f,50f,81f,50f,Color.White,6f)}
        "x_ray"->{box(18f,9f,64f,84f,Outline);line(50f,19f,50f,83f,Color.White,4f);repeat(5){i->drawArc(Color.White,0f,180f,false,Offset(29f,22f+i*9),Size(42f,14f),style=Stroke(3f))}}
        "box"->{poly(Sand,15f,35f,50f,17f,85f,35f,85f,79f,50f,95f,15f,79f);line(15f,35f,50f,53f);line(85f,35f,50f,53f);line(50f,53f,50f,95f);line(33f,26f,68f,44f,Gold,9f)}
        "six","zero"->{val p=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=android.graphics.Color.rgb(67,133,107);textSize=88f;textAlign=android.graphics.Paint.Align.CENTER;typeface=android.graphics.Typeface.DEFAULT_BOLD};drawContext.canvas.nativeCanvas.drawText(if(id=="six")"6" else "0",50f,81f,p)}
        "yarn"->{oval(16f,20f,67f,64f,Color(0xFFD4A8BA));repeat(5){i->drawArc(Color(0xFFA57C9C),240f,180f,false,Offset(22f+i*7,22f),Size(30f,58f),style=Stroke(2f))};line(68f,80f,91f,89f,Color(0xFFD4A8BA),4f)}
        "yo_yo"->{oval(20f,39f,57f,46f,Rose);oval(27f,44f,43f,35f,Gold);line(50f,60f,70f,10f,Outline,2f);oval(44f,54f,13f,13f,Rose)}
        "zoo"->{box(11f,32f,78f,60f,Sand);repeat(6){i->line(18f+i*13,39f,18f+i*13,90f,Outline,3f)};poly(Leaf,5f,32f,50f,8f,95f,32f);oval(29f,45f,37f,30f,Gold);eye(39f,54f);eye(54f,54f)}
        "zipper"->{box(29f,5f,44f,90f,Blue);repeat(10){i->box(40f+(i%2)*10,9f+i*8,11f,5f,Gold)};box(39f,30f,23f,28f,Outline);box(44f,37f,13f,13f,Sand)}
    }
}

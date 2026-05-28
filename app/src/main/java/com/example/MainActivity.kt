package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- COLORS (Exact HTML CSS Variables Map) ---
val W = Color(0xFFFFFFFF)
val Bg = Color(0xFFF4F5F9)
val Fg = Color(0xFF111827)
val F2 = Color(0xFF6B7280)
val F3 = Color(0xFF9CA3AF)
val Vi = Color(0xFF8B5CF6)
val Vid = Color(0xFF7C3AED)
val Cy = Color(0xFF0EA5E9)
val Gradient = Brush.linearGradient(listOf(Vi, Cy))
val GradientH = Brush.linearGradient(listOf(Vid, Color(0xFF0284C7)))
val Bd = Color(0xFFEDEDF2)
val Bdf = Color(0x598B5CF6) // rgba(139,92,246,0.35)
val Ok = Color(0xFF10B981)
val Okb = Color(0xFFECFDF5)
val Er = Color(0xFFEF4444)
val Erb = Color(0xFFFEF2F2)

const val APP_LOGO_URL = "https://raw.githubusercontent.com/sukhdevr898/Chatverse/refs/heads/main/file_000000001c287208ba6c4e5b58c752ff.png"


// --- APP ENTRY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(typography = Typography(bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif))) {
                ChatVerseApp()
            }
        }
    }
}

// --- STATE & MODELS ---
enum class AppScreen { LOGIN, HOME, CHAT }
enum class IslandType { SUCCESS, ERROR }
data class IslandData(val type: IslandType, val title: String, val sub: String)

data class ChatMsg(val isDate: Boolean = false, val dateText: String = "", val isSent: Boolean = false, val text: String = "", val time: String = "", val sender: String? = null, val read: Boolean = false)
data class Chat(val id: Int, val name: String, val av: String, val on: Boolean, val unread: Int, val isGrp: Boolean, var lm: String, var lt: String, val msgs: MutableList<ChatMsg>)
data class CallHistory(val name: String, val av: String, val type: String, val time: String, val dur: String)
data class OnlineFriend(val name: String, val av: String)

// --- MOCK DATA ---
val mockOnlineFriends = listOf(
    OnlineFriend("Mara", "https://picsum.photos/seed/mara22/200/200.jpg"),
    OnlineFriend("Jonas", "https://picsum.photos/seed/jonas44/200/200.jpg"),
    OnlineFriend("Kai", "https://picsum.photos/seed/kai55/200/200.jpg"),
    OnlineFriend("Riya", "https://picsum.photos/seed/riya101/200/200.jpg")
)

val mockChats = mutableListOf(
    Chat(1, "Mara Chen", "https://picsum.photos/seed/mara22/200/200.jpg", true, 2, false, "That sounds perfect, let's do it", "2m", mutableListOf(
        ChatMsg(isDate = true, dateText = "Today"),
        ChatMsg(isSent = false, text = "Hey, are you free this evening?", time = "6:12 PM"),
        ChatMsg(isSent = true, text = "I think so, what did you have in mind?", time = "6:14 PM", read = true),
        ChatMsg(isSent = false, text = "There's this new rooftop bar downtown. A few of us are going.", time = "6:15 PM"),
        ChatMsg(isSent = true, text = "That sounds perfect, let's do it", time = "6:21 PM", read = false)
    )),
    Chat(2, "Design Team", "https://picsum.photos/seed/team88/200/200.jpg", false, 3, true, "Liam: Updated the prototype link", "18m", mutableListOf(
        ChatMsg(isDate = true, dateText = "Today"),
        ChatMsg(isSent = false, text = "Morning everyone. Sprint review at 2pm.", time = "9:00 AM", sender = "Sara"),
        ChatMsg(isSent = false, text = "Updated the prototype link", time = "11:48 AM", sender = "Liam")
    ))
)

val mockCalls = listOf(
    CallHistory("Mara Chen", "https://picsum.photos/seed/mara22/200/200.jpg", "video-in", "Today, 6:12 PM", "5 min"),
    CallHistory("Jonas Berg", "https://picsum.photos/seed/jonas44/200/200.jpg", "audio-out", "Today, 2:30 PM", "12 min"),
    CallHistory("Design Team", "https://picsum.photos/seed/team88/200/200.jpg", "video-miss", "Today, 11:00 AM", "")
)

// --- EXTENSION: BOUNCE CLICK (Like scale(0.985) active state) ---
@Composable
fun Modifier.bounceClick(scaleDown: Float = 0.95f, onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) scaleDown else 1f, animationSpec = tween(150), label = "")
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

// --- MAIN APP ---
@Composable
fun ChatVerseApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var activeChatId by remember { mutableStateOf<Int?>(null) }
    var activeCallType by remember { mutableStateOf<String?>(null) } // "audio" or "video"
    var islandData by remember { mutableStateOf<IslandData?>(null) }
    val scope = rememberCoroutineScope()

    fun showIsland(type: IslandType, title: String, sub: String) {
        scope.launch {
            islandData = IslandData(type, title, sub)
            delay(3200)
            islandData = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        // App Screens
        AnimatedContent(targetState = currentScreen, transitionSpec = {
            if (targetState == AppScreen.HOME && initialState == AppScreen.LOGIN) {
                slideInVertically { it } + fadeIn(tween(500)) togetherWith slideOutVertically { -it } + fadeOut(tween(500))
            } else if (targetState == AppScreen.CHAT) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        }, label = "") { screen ->
            when (screen) {
                AppScreen.LOGIN -> LoginScreen(onLogin = { showIsland(IslandType.SUCCESS, "Welcome Back", "Successfully logged in"); currentScreen = AppScreen.HOME }, showIsland = ::showIsland)
                AppScreen.HOME -> HomeScreen(onChatClick = { id -> activeChatId = id; currentScreen = AppScreen.CHAT }, onLogout = { showIsland(IslandType.SUCCESS, "Logged Out", "See you soon!"); currentScreen = AppScreen.LOGIN }, showIsland = ::showIsland)
                AppScreen.CHAT -> ChatDetailScreen(chatId = activeChatId, onBack = { currentScreen = AppScreen.HOME }, onCall = { type -> activeCallType = type }, showIsland = ::showIsland)
            }
        }

        // Call Overlays
        AnimatedVisibility(visible = activeCallType == "audio", enter = fadeIn(tween(500)), exit = fadeOut(tween(500))) {
            AudioCallScreen(chat = mockChats.find { it.id == activeChatId }, onEnd = { activeCallType = null; showIsland(IslandType.SUCCESS, "Call Ended", "Duration: 0m 0s") })
        }
        AnimatedVisibility(visible = activeCallType == "video", enter = fadeIn(tween(500)), exit = fadeOut(tween(500))) {
            VideoCallScreen(chat = mockChats.find { it.id == activeChatId }, onEnd = { activeCallType = null; showIsland(IslandType.SUCCESS, "Call Ended", "Duration: 0m 0s") })
        }

        DynamicIsland(islandData)
    }
}

// --- DYNAMIC ISLAND ---
@Composable
fun DynamicIsland(data: IslandData?) {
    val expanded = data != null
    val alpha by animateFloatAsState(if (expanded) 1f else 0f, tween(350), label = "")

    if (alpha > 0f) {
        val width by animateDpAsState(if (expanded) 320.dp else 40.dp, spring(dampingRatio = 0.65f, stiffness = 400f), label = "")
        val height by animateDpAsState(if (expanded) 72.dp else 40.dp, spring(dampingRatio = 0.65f, stiffness = 400f), label = "")

        Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 16.dp), contentAlignment = Alignment.TopCenter) {
            // Glow effect
            Box(modifier = Modifier.size(width + 64.dp, height + 64.dp).alpha(alpha).background(Brush.radialGradient(listOf(if (data?.type == IslandType.SUCCESS) Ok.copy(0.12f) else Er.copy(0.12f), Color.Transparent), radius = 180f)))

            Box(
                modifier = Modifier
                    .size(width, height)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFF161616))
                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(9999.dp))
                    .shadow(32.dp, RoundedCornerShape(9999.dp), ambientColor = if (data?.type == IslandType.SUCCESS) Ok.copy(0.2f) else Er.copy(0.2f), spotColor = if (data?.type == IslandType.SUCCESS) Ok.copy(0.4f) else Er.copy(0.4f))
                    .alpha(alpha)
            ) {
                androidx.compose.animation.AnimatedVisibility(visible = expanded, enter = fadeIn(tween(450, delayMillis = 150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(450, delayMillis = 150, easing = FastOutSlowInEasing))) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(if (data?.type == IslandType.SUCCESS) Ok.copy(0.1f) else Er.copy(0.1f)).border(1.dp, if (data?.type == IslandType.SUCCESS) Ok.copy(0.2f) else Er.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(if (data?.type == IslandType.SUCCESS) Icons.Default.Check else Icons.Default.Close, null, tint = if (data?.type == IslandType.SUCCESS) Ok else Er, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(data?.title ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White, lineHeight = 16.sp)
                            Text(data?.sub ?: "", fontSize = 12.sp, color = Color(0xFFAAAAAA), lineHeight = 14.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
                // Timer Bar
                Box(modifier = Modifier.align(Alignment.BottomStart).height(3.dp).fillMaxWidth(alpha).background(if (data?.type == IslandType.SUCCESS) Ok else Er))
            }
        }
    }
}

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(onLogin: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    var isLoading1 by remember { mutableStateOf(false) }
    var isLoading2 by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var failCount by remember { mutableStateOf(0) }

    val inf = rememberInfiniteTransition(label = "")
    val anim1 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(12000, easing = EaseInOut), RepeatMode.Reverse), label = "")
    val anim2 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = EaseInOut), RepeatMode.Reverse), label = "")

    Box(modifier = Modifier.fillMaxSize().background(W)) {
        // Grid background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 36.dp.toPx()
            for (i in 0 until size.width.toInt() step step.toInt()) drawLine(Color.Black.copy(0.012f), Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), 1f)
            for (i in 0 until size.height.toInt() step step.toInt()) drawLine(Color.Black.copy(0.012f), Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), 1f)
        }

        // Orbs
        Box(modifier = Modifier.offset(x = (-70 + anim1 * 25).dp, y = (-50 + anim1 * 18).dp).size(260.dp).align(Alignment.TopEnd).scale(1f + anim1 * 0.1f).blur(80.dp).background(Vi.copy(0.06f), CircleShape))
        Box(modifier = Modifier.offset(x = (-50 - anim2 * 18).dp, y = (-80 - anim2 * 25).dp).size(200.dp).align(Alignment.BottomStart).scale(1f + anim2 * 0.12f).blur(80.dp).background(Cy.copy(0.05f), CircleShape))

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = null, modifier = Modifier.size(140.dp).padding(bottom = 24.dp).shadow(20.dp, ambientColor = Vi.copy(0.18f), spotColor = Vi.copy(0.18f)))
            Text("ChatVerse", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp, style = TextStyle(brush = Gradient))
            Text("rista dil se dil tak.", fontSize = 16.sp, color = F2, letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 44.dp))

            Text("GET STARTED", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = F3, letterSpacing = 1.6.sp, modifier = Modifier.padding(bottom = 14.dp))

            // Primary Button
            Button(
                onClick = {
                    isLoading1 = true
                    scope.launch {
                        delay((1400..2000).random().toLong())
                        isLoading1 = false
                        failCount++
                        if (failCount == 1) { showIsland(IslandType.ERROR, "Authentication Failed", "Unable to connect. Please try again.") } 
                        else { onLogin() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Gradient, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                    if (isLoading1) CircularProgressIndicator(color = W, strokeWidth = 2.5.dp, modifier = Modifier.size(20.dp))
                    else Row(verticalAlignment = Alignment.CenterVertically) {
                        GoogleIcon(isWhite = true)
                        Spacer(Modifier.width(12.dp))
                        Text("Sign up with Google", color = W, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Secondary Button
            Button(
                onClick = { isLoading2 = true; scope.launch { delay(1500); isLoading2 = false; onLogin() } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = W), border = BorderStroke(1.5.dp, Bd), shape = RoundedCornerShape(15.dp)
            ) {
                if (isLoading2) CircularProgressIndicator(color = Fg, strokeWidth = 2.5.dp, modifier = Modifier.size(20.dp))
                else Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleIcon(isWhite = false)
                    Spacer(Modifier.width(12.dp))
                    Text("Login with Google", color = Fg, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 18.dp)) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Bd))
                Text(" Secured by Google ", fontSize = 13.sp, color = F3, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Bd))
            }
            Text("By continuing, you agree to our Terms of Service and Privacy Policy", fontSize = 12.sp, color = F3, textAlign = TextAlign.Center, lineHeight = 16.sp)
        }
        Text("Made with ♥ by ChatVerse", fontSize = 12.sp, color = F3, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 24.dp))
    }
}

// --- HOME SCREEN ---
@Composable
fun HomeScreen(onChatClick: (Int) -> Unit, onLogout: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    var currentTab by remember { mutableStateOf("chats") }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Bg).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(modifier = Modifier.background(W).padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = null, modifier = Modifier.size(54.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("ChatVerse", fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp, style = TextStyle(brush = Gradient))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(modifier = Modifier.size(44.dp).bounceClick { showIsland(IslandType.ERROR, "Camera", "Camera access not available") }, contentAlignment = Alignment.Center) { Icon(Icons.Outlined.PhotoCamera, null, tint = F2, modifier = Modifier.size(24.dp)) }
                        Box(modifier = Modifier.size(44.dp).bounceClick { showIsland(IslandType.SUCCESS, "Notifications", "You have 3 new messages") }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Notifications, null, tint = F2, modifier = Modifier.size(24.dp))
                            Box(modifier = Modifier.align(Alignment.TopEnd).offset((-6).dp, 6.dp).size(10.dp).background(Er, CircleShape).border(2.dp, W, CircleShape))
                        }
                    }
                }

                // Search Bar
                var isFocused by remember { mutableStateOf(false) }
                BasicTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 14.dp).onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Row(modifier = Modifier.fillMaxWidth().background(if (isFocused) W else Bg, RoundedCornerShape(15.dp)).border(1.5.dp, if (isFocused) Bdf else Color.Transparent, RoundedCornerShape(15.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null, tint = if (isFocused) Vi else F3, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            if (searchQuery.isEmpty()) Text("Search conversations...", color = F3, fontSize = 15.sp) else innerTextField()
                        }
                    }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    "chats" -> ChatsList(searchQuery, onChatClick, showIsland)
                    "calls" -> CallsList(showIsland)
                    "status" -> StatusList()
                    "profile" -> ProfileTab(onLogout, showIsland)
                }
            }
        }

        // Bottom Nav (Glassmorphism simulation)
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 16.dp, start = 20.dp, end = 20.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(0.1f), spotColor = Color.Black.copy(0.1f))
                .background(W, RoundedCornerShape(32.dp)).padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically
            ) {
                NavIcon("chats", Icons.Outlined.ChatBubbleOutline, "Chats", currentTab, true) { currentTab = "chats" }
                NavIcon("calls", Icons.Outlined.Phone, "Calls", currentTab) { currentTab = "calls" }
                NavIcon("status", Icons.Outlined.MotionPhotosOn, "Status", currentTab) { currentTab = "status" }
                NavIcon("profile", Icons.Outlined.PersonOutline, "Profile", currentTab) { currentTab = "profile" }
            }
        }
    }
}

@Composable
fun ChatsList(searchQuery: String, onChatClick: (Int) -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    val filtered = mockChats.filter { it.name.contains(searchQuery, true) || it.lm.contains(searchQuery, true) }
    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp, top = 6.dp)) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 10.dp)) {
                Text("ONLINE NOW", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = F3, letterSpacing = 1.2.sp, modifier = Modifier.padding(bottom = 10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(mockOnlineFriends) { friend ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.bounceClick { showIsland(IslandType.SUCCESS, friend.name, "Starting conversation...") }) {
                            Box(modifier = Modifier.size(60.dp)) {
                                AsyncImage(model = friend.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, W, CircleShape).shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(0.06f)))
                                Box(modifier = Modifier.align(Alignment.BottomEnd).offset((-1).dp, (-1).dp).size(14.dp).background(Ok, CircleShape).border(2.5.dp, W, CircleShape))
                            }
                            Text(friend.name, fontSize = 12.sp, color = F2, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 5.dp).width(64.dp), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        itemsIndexed(filtered) { index, chat ->
            // Staggered Animation
            val animAlpha = remember { Animatable(0f) }
            val animOffset = remember { Animatable(-10f) }
            LaunchedEffect(Unit) { delay(index * 35L); launch { animAlpha.animateTo(1f, tween(400)) }; launch { animOffset.animateTo(0f, tween(400, easing = FastOutSlowInEasing)) } }

            Row(modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = animAlpha.value; translationX = animOffset.value }.bounceClick { onChatClick(chat.id) }.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(56.dp)) {
                    AsyncImage(model = chat.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                    if (chat.on) Box(modifier = Modifier.align(Alignment.BottomEnd).size(14.dp).background(Ok, CircleShape).border(2.5.dp, W, CircleShape))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = (-0.1).sp, color = Fg)
                        Text(chat.lt, fontSize = 12.sp, color = if (chat.unread > 0) Vi else F3, fontWeight = if (chat.unread > 0) FontWeight.SemiBold else FontWeight.Normal)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(chat.lm, fontSize = 14.sp, color = F3, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        if (chat.unread > 0) Box(modifier = Modifier.padding(start = 5.dp).height(22.dp).widthIn(min = 22.dp).background(Gradient, CircleShape), contentAlignment = Alignment.Center) {
                            Text(chat.unread.toString(), color = W, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp))
                        }
                    }
                }
            }
            if (index < filtered.lastIndex) Box(modifier = Modifier.padding(start = 70.dp, end = 12.dp).fillMaxWidth().height(1.dp).background(Bd))
        }
    }
}

// --- PROFILE & OTHER TABS ---
@Composable
fun ProfileTab(onLogout: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(W).padding(horizontal = 24.dp, vertical = 56.dp).verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                AsyncImage(model = "https://picsum.photos/seed/myprofile99/200/200.jpg", contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(100.dp).clip(CircleShape).border(3.dp, Color.Transparent, CircleShape).background(Gradient))
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(30.dp).background(Gradient, CircleShape).border(2.5.dp, W, CircleShape).bounceClick { showIsland(IslandType.ERROR, "Edit Profile", "Coming soon") }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Edit, null, tint = W, modifier = Modifier.size(14.dp)) }
            }
            Spacer(Modifier.height(10.dp))
            Text("Arjun Mehta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Fg)
            Text("rista dil se dil tak. Living in the moment,\none message at a time.", fontSize = 14.sp, color = F2, textAlign = TextAlign.Center, lineHeight = 20.sp, modifier = Modifier.width(260.dp))
        }
        Spacer(Modifier.height(28.dp))
        ProfileMenuItem(Icons.Outlined.Shield, "Account & Security", Vi, Vi.copy(0.08f)) { showIsland(IslandType.SUCCESS, "Account", "Verified and secure") }
        ProfileMenuItem(Icons.Outlined.Lock, "Privacy Settings", Cy, Cy.copy(0.08f)) { showIsland(IslandType.ERROR, "Privacy", "Coming soon") }
        ProfileMenuItem(Icons.Outlined.Storage, "Storage & Data", Ok, Ok.copy(0.08f)) { showIsland(IslandType.ERROR, "Storage", "Coming soon") }
        Box(modifier = Modifier.padding(vertical = 5.dp, horizontal = 14.dp).fillMaxWidth().height(1.dp).background(Bd))
        ProfileMenuItem(Icons.Outlined.Palette, "Appearance", F2, Bg) { showIsland(IslandType.ERROR, "Theme", "Coming soon") }
        ProfileMenuItem(Icons.Outlined.HelpOutline, "Help & Support", F2, Bg) { showIsland(IslandType.ERROR, "Help", "Coming soon") }
        Box(modifier = Modifier.padding(vertical = 5.dp, horizontal = 14.dp).fillMaxWidth().height(1.dp).background(Bd))
        ProfileMenuItem(Icons.Outlined.ExitToApp, "Log Out", Er, Er.copy(0.08f), true) { onLogout() }
        Spacer(Modifier.height(50.dp))
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, tint: Color, bg: Color, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().bounceClick { onClick() }.padding(horizontal = 14.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(42.dp).background(bg, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.width(13.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (isDestructive) Er else Fg, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = F3, modifier = Modifier.size(16.dp))
    }
}

// --- CHAT DETAIL SCREEN ---
@Composable
fun ChatDetailScreen(chatId: Int?, onBack: () -> Unit, onCall: (String) -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    val chat = mockChats.find { it.id == chatId } ?: return
    var messageText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Bg).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().background(W).padding(top = 10.dp, bottom = 10.dp, start = 12.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(W, CircleShape).border(1.dp, Bd, CircleShape).bounceClick { onBack() }, contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Fg, modifier = Modifier.size(24.dp)) }
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.size(44.dp)) {
                    AsyncImage(model = chat.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                    if (chat.on) Box(modifier = Modifier.align(Alignment.BottomEnd).size(12.dp).background(Ok, CircleShape).border(2.dp, W, CircleShape))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = (-0.1).sp, color = Fg)
                    Text(if (chat.on) "Online" else "Last seen recently", fontSize = 13.sp, color = if (chat.on) Ok else F3, fontWeight = FontWeight.Medium)
                }
                Row {
                    Box(modifier = Modifier.size(44.dp).bounceClick { onCall("audio") }, contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Phone, null, tint = F2, modifier = Modifier.size(24.dp)) }
                    Box(modifier = Modifier.size(44.dp).bounceClick { onCall("video") }, contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Videocam, null, tint = F2, modifier = Modifier.size(24.dp)) }
                    Box(modifier = Modifier.size(44.dp).bounceClick { showMenu = true }, contentAlignment = Alignment.Center) { Icon(Icons.Default.MoreVert, null, tint = F2, modifier = Modifier.size(24.dp)) }
                }
            }

            // Messages
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp), reverseLayout = true) {
                items(chat.msgs.reversed()) { msg ->
                    if (msg.isDate) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Text(msg.dateText, fontSize = 12.sp, color = F3, letterSpacing = 0.3.sp, modifier = Modifier.background(W, CircleShape).border(1.dp, Bd, CircleShape).padding(horizontal = 12.dp, vertical = 3.dp))
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = if (msg.isSent) Arrangement.End else Arrangement.Start) {
                            Column(horizontalAlignment = if (msg.isSent) Alignment.End else Alignment.Start, modifier = Modifier.fillMaxWidth(0.85f)) {
                                Box(
                                    modifier = Modifier
                                        .background(if (msg.isSent) Gradient else SolidColor(W), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = if (msg.isSent) 5.dp else 18.dp, bottomStart = if (!msg.isSent) 5.dp else 18.dp))
                                        .border(if (!msg.isSent) 1.dp else 0.dp, Bd, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = if (msg.isSent) 5.dp else 18.dp, bottomStart = if (!msg.isSent) 5.dp else 18.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Column {
                                        if (!msg.isSent && msg.sender != null) Text(msg.sender, fontSize = 13.sp, color = Vi, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 2.dp))
                                        Text(msg.text, fontSize = 16.sp, color = if (msg.isSent) W else Fg, lineHeight = 22.sp)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Text(msg.time, fontSize = 11.sp, color = if(msg.isSent) Color.Black.copy(0.4f) else F3)
                                    if (msg.isSent) { Spacer(Modifier.width(4.dp)); Icon(if (msg.read) Icons.Default.DoneAll else Icons.Default.Check, null, tint = if (msg.read) Cy else F3, modifier = Modifier.size(16.dp)) }
                                }
                            }
                        }
                    }
                }
            }

            // Input
            Row(modifier = Modifier.fillMaxWidth().background(W).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                Box(modifier = Modifier.size(44.dp).bounceClick { showIsland(IslandType.ERROR, "Attach", "Coming soon") }, contentAlignment = Alignment.Center) { Icon(Icons.Outlined.AttachFile, null, tint = F2, modifier = Modifier.size(24.dp)) }
                BasicTextField(
                    value = messageText, onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f).padding(bottom = 4.dp).background(Bg, RoundedCornerShape(24.dp)).border(1.5.dp, Bd, RoundedCornerShape(24.dp)).padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                    textStyle = TextStyle(fontSize = 16.sp, color = Fg),
                    decorationBox = { inner ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) { if (messageText.isEmpty()) Text("Message...", color = F3, fontSize = 16.sp) else inner() }
                            Icon(Icons.Outlined.SentimentSatisfied, null, tint = F3, modifier = Modifier.size(22.dp))
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.padding(bottom = 4.dp).size(44.dp).background(if (messageText.isNotBlank()) Gradient else SolidColor(F3), CircleShape).bounceClick {
                        if (messageText.isNotBlank()) {
                            chat.msgs.add(ChatMsg(isSent = true, text = messageText, time = "Now"))
                            chat.lm = messageText
                            messageText = ""
                            focusManager.clearFocus()
                        }
                    }, contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowUpward, null, tint = W, modifier = Modifier.size(24.dp)) }
            }
        }
        
        // 3 Dot Menu
        androidx.compose.animation.AnimatedVisibility(visible = showMenu, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.15f)).clickable { showMenu = false }) {
                Box(modifier = Modifier.fillMaxHeight().width(260.dp).align(Alignment.CenterEnd).background(W).clickable(enabled = false){}) {
                    Column(modifier = Modifier.fillMaxSize().padding(top = 20.dp)) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text(chat.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Fg)
                            Text(if(chat.on) "Online" else "Last seen recently", fontSize = 12.sp, color = F3, modifier = Modifier.padding(top = 2.dp))
                        }
                        HorizontalDivider(color = Bd)
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                            ProfileMenuItem(Icons.Default.Checklist, "Select Messages", Vi, Vi.copy(0.08f)) { showMenu = false; showIsland(IslandType.ERROR, "Select", "Coming soon") }
                            ProfileMenuItem(Icons.Default.Search, "Search in Chat", Cy, Cy.copy(0.08f)) { showMenu = false; showIsland(IslandType.ERROR, "Search", "Coming soon") }
                            ProfileMenuItem(Icons.Outlined.NotificationsOff, "Mute Notifications", F2, Bg) { showMenu = false; showIsland(IslandType.SUCCESS, "Muted", "Notifications muted") }
                            ProfileMenuItem(Icons.Outlined.Image, "Wallpaper", Ok, Ok.copy(0.08f)) { showMenu = false; showIsland(IslandType.ERROR, "Wallpaper", "Coming soon") }
                            HorizontalDivider(color = Bd, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                            ProfileMenuItem(Icons.Outlined.DeleteSweep, "Clear Chat", F2, Bg) { showMenu = false; chat.msgs.clear(); showIsland(IslandType.SUCCESS, "Cleared", "Chat cleared") }
                            ProfileMenuItem(Icons.Outlined.Block, "Block Contact", Er, Er.copy(0.08f), true) { showMenu = false; showIsland(IslandType.ERROR, "Blocked", "Contact blocked") }
                        }
                    }
                }
            }
        }
    }
}

// --- CALL SCREENS ---
@Composable
fun AudioCallScreen(chat: Chat?, onEnd: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val shadowPulse by infiniteTransition.animateFloat(initialValue = 40f, targetValue = 60f, animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse), label = "")

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding().background(Brush.linearGradient(listOf(Color(0xFF0F0A1F), Color(0xFF1A1035), Color(0xFF0D1B2A)))), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(modifier = Modifier.weight(1f).padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(model = chat?.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(120.dp).clip(CircleShape).border(3.dp, Vi.copy(0.4f), CircleShape).shadow(shadowPulse.dp, CircleShape, ambientColor = Vi.copy(0.35f), spotColor = Vi.copy(0.35f)))
            Spacer(Modifier.height(24.dp))
            Text(chat?.name ?: "", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = W, modifier = Modifier.padding(bottom = 6.dp))
            Text("Calling...", fontSize = 16.sp, color = W.copy(0.5f), fontWeight = FontWeight.Normal)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 24.dp, end = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            CallButton(Icons.Default.MicOff, "Mute", false) {}
            CallButton(Icons.Default.VolumeUp, "Speaker", false) {}
            CallButton(Icons.Default.Videocam, "Video", false) {}
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEnd, modifier = Modifier.size(72.dp).background(Er, CircleShape).shadow(20.dp, CircleShape, ambientColor = Er.copy(0.4f), spotColor = Er.copy(0.4f))) { Icon(Icons.Outlined.PhoneDisabled, null, tint = W, modifier = Modifier.size(32.dp)) }
                Text("End", fontSize = 14.sp, color = W.copy(0.7f), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun VideoCallScreen(chat: Chat?, onEnd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).systemBarsPadding()) {
        AsyncImage(model = chat?.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().blur(10.dp), colorFilter = ColorFilter.tint(Color.Black.copy(0.6f), BlendMode.Darken))
        Box(modifier = Modifier.padding(top = 60.dp, end = 16.dp).size(110.dp, 155.dp).align(Alignment.TopEnd).clip(RoundedCornerShape(16.dp)).border(2.dp, W.copy(0.15f), RoundedCornerShape(16.dp)).shadow(20.dp, ambientColor = Color.Black.copy(0.3f))) {
            AsyncImage(model = "https://picsum.photos/seed/myprofile99/200/200.jpg", contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 16.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEnd, modifier = Modifier.background(W.copy(0.12f), CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = W) }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text(chat?.name ?: "", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = W)
                    Text("Calling...", fontSize = 13.sp, color = W.copy(0.6f))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 24.dp, end = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                CallButton(Icons.Default.MicOff, "Mute", false) {}
                CallButton(Icons.Default.FlipCameraIos, "Flip", false) {}
                CallButton(Icons.Default.VideocamOff, "Camera", false) {}
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onEnd, modifier = Modifier.size(64.dp).background(Er, CircleShape).shadow(20.dp, CircleShape, ambientColor = Er.copy(0.4f), spotColor = Er.copy(0.4f))) { Icon(Icons.Outlined.PhoneDisabled, null, tint = W, modifier = Modifier.size(26.dp)) }
                    Text("End", fontSize = 12.sp, color = W.copy(0.7f), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

// --- UTILS ---
@Composable
fun CallButton(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(64.dp).background(if (isActive) W else W.copy(0.12f), CircleShape)) { Icon(icon, null, tint = if (isActive) Color.Black else W, modifier = Modifier.size(28.dp)) }
        Text(label, fontSize = 14.sp, color = W.copy(0.7f), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun NavIcon(id: String, icon: ImageVector, label: String, current: String, hasBadge: Boolean = false, onClick: () -> Unit) {
    val sel = id == current
    val tint by animateColorAsState(if (sel) Vi else F3, label = "")
    Column(modifier = Modifier.bounceClick { onClick() }.padding(vertical = 6.dp, horizontal = 8.dp).widthIn(min = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.TopCenter) {
            androidx.compose.animation.AnimatedVisibility(visible = sel, enter = expandHorizontally(tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350)), exit = fadeOut(tween(0))) {
                Box(modifier = Modifier.offset(y = (-9).dp).size(28.dp, 3.dp).clip(RoundedCornerShape(2.dp)).background(Gradient))
            }
            Box(modifier = Modifier.padding(top = 4.dp)) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(32.dp).offset(y = if (sel) (-2).dp else 0.dp).scale(if (sel) 1.15f else 1f))
                if (hasBadge && !sel) Box(modifier = Modifier.align(Alignment.TopEnd).offset(6.dp, (-2).dp).size(20.dp).background(Er, CircleShape).border(2.dp, W, CircleShape), contentAlignment = Alignment.Center) { Text("5", color = W, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Text(label, fontSize = 14.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium, color = tint, modifier = Modifier.padding(top = 4.dp))
    }
}

// Exact Google Icon SVG translation
@Composable
fun GoogleIcon(isWhite: Boolean) {
    val vector = remember {
        ImageVector.Builder("Google", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(
                pathData = listOf(PathNode.MoveTo(22.56f, 12.25f), PathNode.RelativeCurveTo(0f, -0.78f, -0.07f, -1.53f, -0.2f, -2.25f), PathNode.LineTo(12f, 10f), PathNode.RelativeLineTo(0f, 4.26f), PathNode.RelativeLineTo(5.92f, 0f), PathNode.RelativeCurveTo(0f, 0f, -0.84f, 3.32f, -2.2f, 3.32f), PathNode.RelativeLineTo(0f, 2.77f), PathNode.RelativeLineTo(3.57f, 0f), PathNode.RelativeCurveTo(2.08f, -1.92f, 3.28f, -4.74f, 3.28f, -8.1f), PathNode.Close),
                fill = SolidColor(if (isWhite) W else Color(0xFF4285F4))
            )
            addPath(
                pathData = listOf(PathNode.MoveTo(12f, 23f), PathNode.RelativeCurveTo(2.97f, 0f, 5.46f, -0.98f, 7.28f, -2.66f), PathNode.RelativeLineTo(-3.57f, -2.77f), PathNode.RelativeCurveTo(-0.98f, 0.66f, -2.23f, 1.06f, -3.71f, 1.06f), PathNode.RelativeCurveTo(-2.86f, 0f, -5.29f, -1.93f, -6.16f, -4.53f), PathNode.LineTo(2.18f, 14.1f), PathNode.RelativeLineTo(0f, 2.84f), PathNode.CurveTo(3.99f, 20.53f, 7.7f, 23f, 12f, 23f), PathNode.Close),
                fill = SolidColor(if (isWhite) W.copy(0.85f) else Color(0xFF34A853))
            )
            addPath(
                pathData = listOf(PathNode.MoveTo(5.84f, 14.09f), PathNode.RelativeCurveTo(-0.22f, -0.66f, -0.35f, -1.36f, -0.35f, -2.09f), PathNode.RelativeCurveTo(0f, -0.73f, 0.13f, -1.43f, 0.35f, -2.09f), PathNode.LineTo(2.18f, 7.07f), PathNode.CurveTo(1.39f, 8.55f, 1f, 10.23f, 1f, 12f), PathNode.RelativeCurveTo(0f, 1.77f, 0.42f, 3.45f, 1.18f, 4.93f), PathNode.RelativeLineTo(3.66f, -2.84f), PathNode.Close),
                fill = SolidColor(if (isWhite) W.copy(0.7f) else Color(0xFFFBBC05))
            )
            addPath(
                pathData = listOf(PathNode.MoveTo(12f, 5.38f), PathNode.RelativeCurveTo(1.62f, 0f, 3.06f, 0.56f, 4.21f, 1.64f), PathNode.RelativeLineTo(3.15f, -3.15f), PathNode.CurveTo(17.45f, 2.09f, 14.97f, 1f, 12f, 1f), PathNode.CurveTo(7.7f, 1f, 3.99f, 3.47f, 2.18f, 7.07f), PathNode.RelativeLineTo(3.66f, 2.84f), PathNode.RelativeCurveTo(0.87f, -2.6f, 3.3f, -4.53f, 6.16f, -4.53f), PathNode.Close),
                fill = SolidColor(if (isWhite) W.copy(0.55f) else Color(0xFFEA4335))
            )
        }.build()
    }
    Icon(vector, null, tint = Color.Unspecified, modifier = Modifier.size(19.dp))
}

@Composable
fun StatusList() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(56.dp).background(Brush.linearGradient(listOf(Vi.copy(0.07f), Cy.copy(0.07f))), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.MotionPhotosOn, null, tint = Vi, modifier = Modifier.size(32.dp)) }
        Spacer(Modifier.height(14.dp))
        Text("Status Updates", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Fg)
        Text("Share your moments with contacts.\nStatus updates disappear after 24 hours.", fontSize = 13.sp, color = F3, textAlign = TextAlign.Center, lineHeight = 18.sp, modifier = Modifier.width(260.dp).padding(top = 5.dp))
    }
}

@Composable
fun CallsList(showIsland: (IslandType, String, String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp, top = 6.dp)) {
        itemsIndexed(mockCalls) { index, call ->
            Row(modifier = Modifier.fillMaxWidth().bounceClick { }.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = call.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(56.dp).clip(CircleShape))
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(call.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = if (call.type.contains("miss")) Er else Fg, modifier = Modifier.padding(bottom = 2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (call.type.contains("in")) Icons.Default.CallReceived else if (call.type.contains("out")) Icons.Default.CallMade else Icons.Default.CallMissed, null, tint = if (call.type.contains("miss")) Er else if (call.type.contains("in")) Ok else Vi, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Icon(if(call.type.contains("video")) Icons.Default.Videocam else Icons.Default.Phone, null, tint = F3, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (call.type.contains("in")) "Incoming${if(call.dur.isNotEmpty())" · "+call.dur else ""}" else if(call.type.contains("out")) "Outgoing${if(call.dur.isNotEmpty())" · "+call.dur else ""}" else "Missed", fontSize = 14.sp, color = F3)
                    }
                }
                Text(call.time.split(",").last().trim(), fontSize = 13.sp, color = F3, modifier = Modifier.padding(end = 8.dp))
                Box(modifier = Modifier.size(44.dp).background(W, CircleShape).border(1.dp, Bd, CircleShape).bounceClick { showIsland(IslandType.SUCCESS, "Audio Call", "Calling ${call.name}...") }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Phone, null, tint = Vi, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.size(44.dp).background(W, CircleShape).border(1.dp, Bd, CircleShape).bounceClick { showIsland(IslandType.SUCCESS, "Video Call", "Starting video with ${call.name}...") }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Videocam, null, tint = Cy, modifier = Modifier.size(20.dp)) }
            }
            if (index < mockCalls.lastIndex) Box(modifier = Modifier.padding(start = 70.dp, end = 12.dp).fillMaxWidth().height(1.dp).background(Bd))
        }
    }
}

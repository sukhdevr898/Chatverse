package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
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

// --- COLORS ---
val W = Color(0xFFFFFFFF)
val Bg = Color(0xFFF4F5F9)
val Fg = Color(0xFF111827)
val F2 = Color(0xFF6B7280)
val F3 = Color(0xFF9CA3AF)
val Vi = Color(0xFF8B5CF6)
val Cy = Color(0xFF0EA5E9)
val Ok = Color(0xFF10B981)
val Er = Color(0xFFEF4444)
val Bd = Color(0xFFEDEDF2)
val Gradient = Brush.linearGradient(listOf(Vi, Cy))

// --- CONSTANTS ---
const val APP_LOGO_URL = "https://raw.githubusercontent.com/sukhdevr898/Chatverse/refs/heads/main/file_000000001c287208ba6c4e5b58c752ff.png"

// --- APP ENTRY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide System UI (Full Screen Immersive)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MaterialTheme {
                ChatVerseApp()
            }
        }
    }
}

// --- STATE & MODELS ---
enum class AppScreen { LOGIN, HOME, CHAT, AUDIO_CALL, VIDEO_CALL }
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

// --- MAIN APP CONTENT ---
@Composable
fun ChatVerseApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var activeChatId by remember { mutableStateOf<Int?>(null) }
    var islandData by remember { mutableStateOf<IslandData?>(null) }
    val scope = rememberCoroutineScope()

    fun showIsland(type: IslandType, title: String, sub: String) {
        scope.launch {
            islandData = IslandData(type, title, sub)
            delay(3200)
            islandData = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Handle Transitions
        AnimatedContent(targetState = currentScreen, transitionSpec = {
            if (targetState == AppScreen.HOME && initialState == AppScreen.LOGIN) {
                slideInVertically { height -> height } + fadeIn() togetherWith slideOutVertically { height -> -height } + fadeOut()
            } else if (targetState == AppScreen.CHAT || targetState == AppScreen.AUDIO_CALL || targetState == AppScreen.VIDEO_CALL) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith slideOutHorizontally { width -> width } + fadeOut()
            }
        }, label = "screens") { screen ->
            when (screen) {
                AppScreen.LOGIN -> LoginScreen(
                    onLogin = {
                        showIsland(IslandType.SUCCESS, "Welcome Back", "Successfully logged in")
                        currentScreen = AppScreen.HOME
                    },
                    showIsland = ::showIsland
                )
                AppScreen.HOME -> HomeScreen(
                    onChatClick = { id -> activeChatId = id; currentScreen = AppScreen.CHAT },
                    onCallClick = { isVideo -> currentScreen = if(isVideo) AppScreen.VIDEO_CALL else AppScreen.AUDIO_CALL },
                    onLogout = { currentScreen = AppScreen.LOGIN },
                    showIsland = ::showIsland
                )
                AppScreen.CHAT -> ChatDetailScreen(
                    chatId = activeChatId,
                    onBack = { currentScreen = AppScreen.HOME },
                    onCall = { isVideo -> currentScreen = if(isVideo) AppScreen.VIDEO_CALL else AppScreen.AUDIO_CALL },
                    showIsland = ::showIsland
                )
                AppScreen.AUDIO_CALL -> AudioCallScreen(
                    chat = mockChats.find { it.id == activeChatId },
                    onEnd = { currentScreen = AppScreen.CHAT; showIsland(IslandType.SUCCESS, "Call Ended", "Call disconnected") }
                )
                AppScreen.VIDEO_CALL -> VideoCallScreen(
                    chat = mockChats.find { it.id == activeChatId },
                    onEnd = { currentScreen = AppScreen.CHAT; showIsland(IslandType.SUCCESS, "Call Ended", "Call disconnected") }
                )
            }
        }
        
        DynamicIsland(islandData)
    }
}

// --- DYNAMIC ISLAND ---
@Composable
fun DynamicIsland(data: IslandData?) {
    val expanded = data != null
    val width by animateDpAsState(if (expanded) 300.dp else 36.dp, tween(500, easing = FastOutSlowInEasing), label = "")
    val height by animateDpAsState(if (expanded) 68.dp else 36.dp, tween(500, easing = FastOutSlowInEasing), label = "")
    val alpha by animateFloatAsState(if (expanded) 1f else 0f, tween(300), label = "")

    Box(modifier = Modifier.fillMaxWidth().padding(top = 14.dp).offset(y = if(expanded) 0.dp else (-50).dp), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(50))
                .background(if (expanded) W else Color(0xFF111111))
                .border(1.dp, if (expanded) Bd else Color.Transparent, RoundedCornerShape(50))
                .shadow(if (expanded) 16.dp else 0.dp, RoundedCornerShape(50))
        ) {
            androidx.compose.animation.AnimatedVisibility(visible = expanded, enter = fadeIn(tween(400, delayMillis = 100))) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(if (data?.type == IslandType.SUCCESS) Color(0xFFECFDF5) else Color(0xFFFEF2F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (data?.type == IslandType.SUCCESS) Icons.Default.Check else Icons.Default.Close, null, tint = if (data?.type == IslandType.SUCCESS) Ok else Er, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text(data?.title ?: "", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Fg)
                        Text(data?.sub ?: "", fontSize = 11.sp, color = F2)
                    }
                }
            }
            // Bottom Bar Indicator
            Box(modifier = Modifier.align(Alignment.BottomStart).height(2.5.dp).fillMaxWidth(alpha).background(if(data?.type == IslandType.SUCCESS) Ok else Er))
        }
    }
}

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(onLogin: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Orb animations
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offset1 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 25f, animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), label = "")
    val offset2 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = -20f, animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse), label = "")

    Box(modifier = Modifier.fillMaxSize().background(W)) {
        // Grid pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 36.dp.toPx()
            for (i in 0 until size.width.toInt() step step.toInt()) drawLine(Color.Black.copy(alpha = 0.012f), Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height))
            for (i in 0 until size.height.toInt() step step.toInt()) drawLine(Color.Black.copy(alpha = 0.012f), Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()))
        }
        
        // Orbs
        Box(modifier = Modifier.offset(x = offset1.dp, y = offset1.dp).size(260.dp).align(Alignment.TopEnd).offset(x = 70.dp, y = (-50).dp).blur(80.dp).background(Vi.copy(alpha = 0.06f), CircleShape))
        Box(modifier = Modifier.offset(x = offset2.dp, y = offset2.dp).size(200.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = (-80).dp).blur(80.dp).background(Cy.copy(alpha = 0.05f), CircleShape))

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AsyncImage(model = APP_LOGO_URL, contentDescription = "Logo", modifier = Modifier.size(82.dp).padding(bottom = 24.dp))
            Text("ChatVerse", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, style = TextStyle(brush = Gradient))
            Text("rista dil se dil tak.", fontSize = 14.5.sp, color = F2, modifier = Modifier.padding(bottom = 44.dp))

            Text("GET STARTED", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = F3, letterSpacing = 1.6.sp, modifier = Modifier.padding(bottom = 14.dp))

            Button(
                onClick = {
                    isLoading = true
                    scope.launch { delay(1500); isLoading = false; onLogin() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Gradient), contentAlignment = Alignment.Center) {
                    if (isLoading) CircularProgressIndicator(color = W, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Login, null, tint = W, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Sign in to continue", color = W, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Bd)
                Text(" Secured Connection ", fontSize = 11.sp, color = F3, fontWeight = FontWeight.Medium)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Bd)
            }
        }
        Text("Made with ♥ by ChatVerse", fontSize = 10.5.sp, color = F3, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
    }
}

// --- HOME SCREEN ---
@Composable
fun HomeScreen(onChatClick: (Int) -> Unit, onCallClick: (Boolean) -> Unit, onLogout: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    var currentTab by remember { mutableStateOf("chats") }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(modifier = Modifier.background(W).padding(top = 46.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = APP_LOGO_URL, contentDescription = null, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(9.dp))
                        Text("ChatVerse", fontSize = 19.sp, fontWeight = FontWeight.Bold, style = TextStyle(brush = Gradient))
                    }
                    Row {
                        IconButton(onClick = { showIsland(IslandType.ERROR, "Camera", "Camera access not available") }) { Icon(Icons.Default.PhotoCamera, null, tint = F2) }
                        IconButton(onClick = { showIsland(IslandType.SUCCESS, "Notifications", "You have 3 new messages") }) {
                            Box {
                                Icon(Icons.Default.Notifications, null, tint = F2)
                                Box(modifier = Modifier.align(Alignment.TopEnd).offset((-2).dp, 2.dp).size(8.dp).background(Er, CircleShape).border(1.5.dp, W, CircleShape))
                            }
                        }
                    }
                }
                
                // Search
                BasicTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        Row(modifier = Modifier.fillMaxWidth().background(Bg, RoundedCornerShape(15.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null, tint = F3, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            if (searchQuery.isEmpty()) Text("Search conversations...", color = F3, fontSize = 13.5.sp) else innerTextField()
                        }
                    }
                )
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    "chats" -> ChatsList(searchQuery, onChatClick, showIsland)
                    "calls" -> CallsList(showIsland)
                    "status" -> StatusList()
                    "profile" -> ProfileTab(onLogout, showIsland)
                }
            }
        }

        // Bottom Navigation
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(W.copy(alpha = 0.95f)).padding(vertical = 10.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavIcon("chats", Icons.Outlined.ChatBubbleOutline, "Chats", currentTab, true) { currentTab = "chats" }
            NavIcon("calls", Icons.Outlined.Phone, "Calls", currentTab) { currentTab = "calls" }
            NavIcon("status", Icons.Outlined.MotionPhotosOn, "Status", currentTab) { currentTab = "status" }
            NavIcon("profile", Icons.Outlined.PersonOutline, "Profile", currentTab) { currentTab = "profile" }
        }
    }
}

@Composable
fun ChatsList(searchQuery: String, onChatClick: (Int) -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    val filtered = mockChats.filter { it.name.contains(searchQuery, true) || it.lm.contains(searchQuery, true) }
    
    LazyColumn(contentPadding = PaddingValues(bottom = 90.dp)) {
        item {
            Column(modifier = Modifier.background(W).padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("ONLINE NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = F3, letterSpacing = 1.2.sp)
                LazyRow(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(mockOnlineFriends) { friend ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showIsland(IslandType.SUCCESS, friend.name, "Starting conversation...") }) {
                            Box {
                                AsyncImage(model = friend.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(50.dp).clip(CircleShape).border(2.dp, W, CircleShape))
                                Box(modifier = Modifier.size(12.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Ok).border(2.5.dp, W, CircleShape))
                            }
                            Text(friend.name, fontSize = 10.sp, color = F2, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
        itemsIndexed(filtered) { _, chat ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onChatClick(chat.id) }.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box {
                    AsyncImage(model = chat.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(46.dp).clip(CircleShape))
                    if (chat.on) Box(modifier = Modifier.size(10.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Ok).border(2.dp, W, CircleShape))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Fg)
                        Text(chat.lt, fontSize = 10.5.sp, color = if (chat.unread > 0) Vi else F3, fontWeight = if (chat.unread > 0) FontWeight.Bold else FontWeight.Normal)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(chat.lm, fontSize = 12.sp, color = F3, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        if (chat.unread > 0) {
                            Box(modifier = Modifier.padding(start = 8.dp).size(19.dp).clip(CircleShape).background(Gradient), contentAlignment = Alignment.Center) {
                                Text(chat.unread.toString(), color = W, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(start = 78.dp, end = 20.dp), color = Bd, thickness = 1.dp)
        }
    }
}

@Composable
fun CallsList(showIsland: (IslandType, String, String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 90.dp)) {
        items(mockCalls) { call ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = call.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(46.dp).clip(CircleShape))
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(call.name, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = if (call.type.contains("miss")) Er else Fg)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (call.type.contains("in")) Icons.Default.CallReceived else if (call.type.contains("out")) Icons.Default.CallMade else Icons.Default.CallMissed, null, tint = if (call.type.contains("miss")) Er else if (call.type.contains("in")) Ok else Vi, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (call.type.contains("in")) "Incoming" else if(call.type.contains("out")) "Outgoing" else "Missed", fontSize = 11.5.sp, color = F3)
                    }
                }
                Text(call.time.split(",").last().trim(), fontSize = 10.5.sp, color = F3, modifier = Modifier.padding(end = 12.dp))
                IconButton(onClick = { showIsland(IslandType.SUCCESS, "Audio Call", "Calling...") }, modifier = Modifier.size(34.dp).background(W, CircleShape).border(1.dp, Bd, CircleShape)) {
                    Icon(Icons.Default.Phone, null, tint = Vi, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { showIsland(IslandType.SUCCESS, "Video Call", "Starting video...") }, modifier = Modifier.size(34.dp).background(W, CircleShape).border(1.dp, Bd, CircleShape)) {
                    Icon(Icons.Default.Videocam, null, tint = Cy, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(start = 78.dp, end = 20.dp), color = Bd, thickness = 1.dp)
        }
    }
}

@Composable
fun StatusList() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Vi.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.MotionPhotosOn, null, tint = Vi, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text("Status Updates", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Fg)
        Text("Share your moments with contacts.\nStatus updates disappear after 24 hours.", fontSize = 12.sp, color = F3, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProfileTab(onLogout: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(W).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                AsyncImage(model = "https://picsum.photos/seed/myprofile99/200/200.jpg", contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(76.dp).clip(CircleShape).border(3.dp, Vi, CircleShape))
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(24.dp).clip(CircleShape).background(Gradient).border(2.dp, W, CircleShape).clickable { showIsland(IslandType.ERROR, "Edit Profile", "Coming soon") }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = W, modifier = Modifier.size(10.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Arjun Mehta", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Fg)
            Text("rista dil se dil tak. Living in the moment,\none message at a time.", fontSize = 12.5.sp, color = F2, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(28.dp))
        ProfileMenuItem(Icons.Outlined.Shield, "Account & Security", Vi, Vi.copy(0.08f)) { showIsland(IslandType.SUCCESS, "Account", "Secure") }
        ProfileMenuItem(Icons.Outlined.Lock, "Privacy Settings", Cy, Cy.copy(0.08f)) { showIsland(IslandType.ERROR, "Privacy", "Coming soon") }
        ProfileMenuItem(Icons.Outlined.Storage, "Storage & Data", Ok, Ok.copy(0.08f)) { showIsland(IslandType.ERROR, "Storage", "Coming soon") }
        HorizontalDivider(color = Bd, modifier = Modifier.padding(vertical = 5.dp))
        ProfileMenuItem(Icons.Outlined.Palette, "Appearance", F2, Bg) { showIsland(IslandType.ERROR, "Theme", "Coming soon") }
        ProfileMenuItem(Icons.Outlined.HelpOutline, "Help & Support", F2, Bg) { showIsland(IslandType.ERROR, "Help", "Coming soon") }
        HorizontalDivider(color = Bd, modifier = Modifier.padding(vertical = 5.dp))
        ProfileMenuItem(Icons.Outlined.Logout, "Log Out", Er, Er.copy(0.08f), true) { onLogout() }
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, tint: Color, bg: Color, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(13.dp))
        Text(title, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = if (isDestructive) Er else Fg, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = F3, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun NavIcon(id: String, icon: ImageVector, label: String, current: String, hasBadge: Boolean = false, onClick: () -> Unit) {
    val sel = id == current
    val tint by animateColorAsState(if (sel) Vi else F3, label = "")
    Column(modifier = Modifier.clickable { onClick() }.padding(vertical = 5.dp, horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.TopCenter) {
            androidx.compose.animation.AnimatedVisibility(visible = sel) { Box(modifier = Modifier.offset(y = (-8).dp).size(22.dp, 2.5.dp).clip(RoundedCornerShape(50)).background(Gradient)) }
            Box {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                if (hasBadge && !sel) Box(modifier = Modifier.align(Alignment.TopEnd).offset(4.dp, (-2).dp).size(15.dp).background(Er, CircleShape).border(2.dp, W, CircleShape), contentAlignment = Alignment.Center) {
                    Text("5", color = W, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(label, fontSize = 9.5.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium, color = tint)
    }
}

// --- CHAT DETAIL SCREEN ---
@Composable
fun ChatDetailScreen(chatId: Int?, onBack: () -> Unit, onCall: (Boolean) -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    val chat = mockChats.find { it.id == chatId } ?: return
    var messageText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().background(W).padding(top = 10.dp, bottom = 10.dp, start = 12.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(34.dp).background(W, CircleShape).border(1.dp, Bd, CircleShape)) {
                    Icon(Icons.Default.ChevronLeft, null, tint = Fg)
                }
                Spacer(Modifier.width(10.dp))
                Box {
                    AsyncImage(model = chat.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(36.dp).clip(CircleShape))
                    if (chat.on) Box(modifier = Modifier.size(9.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Ok).border(2.dp, W, CircleShape))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, color = Fg)
                    Text(if (chat.on) "Online" else "Last seen recently", fontSize = 11.sp, color = if (chat.on) Ok else F3)
                }
                Row {
                    IconButton(onClick = { onCall(false) }) { Icon(Icons.Default.Phone, null, tint = F2) }
                    IconButton(onClick = { onCall(true) }) { Icon(Icons.Default.Videocam, null, tint = F2) }
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = F2) }
                }
            }

            // Messages
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp), reverseLayout = true) {
                items(chat.msgs.reversed()) { msg ->
                    if (msg.isDate) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(msg.dateText, fontSize = 10.5.sp, color = F3, modifier = Modifier.background(W, CircleShape).border(1.dp, Bd, CircleShape).padding(horizontal = 12.dp, vertical = 3.dp))
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = if (msg.isSent) Arrangement.End else Arrangement.Start) {
                            Column(horizontalAlignment = if (msg.isSent) Alignment.End else Alignment.Start) {
                                Box(
                                    modifier = Modifier
                                        .background(if (msg.isSent) Gradient else SolidColor(W), shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = if (msg.isSent) 5.dp else 18.dp, bottomStart = if (!msg.isSent) 5.dp else 18.dp))
                                        .border(if (!msg.isSent) 1.dp else 0.dp, Bd, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = if (msg.isSent) 5.dp else 18.dp, bottomStart = if (!msg.isSent) 5.dp else 18.dp))
                                        .padding(horizontal = 13.dp, vertical = 9.dp)
                                        .widthIn(max = 260.dp)
                                ) {
                                    Column {
                                        if (!msg.isSent && msg.sender != null) Text(msg.sender, fontSize = 10.5.sp, color = Vi, fontWeight = FontWeight.SemiBold)
                                        Text(msg.text, fontSize = 13.5.sp, color = if (msg.isSent) W else Fg)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Text(msg.time, fontSize = 9.5.sp, color = F3)
                                    if (msg.isSent) {
                                        Spacer(Modifier.width(3.dp))
                                        Icon(if (msg.read) Icons.Default.DoneAll else Icons.Default.Check, null, tint = if (msg.read) Cy else F3, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Area
            Row(modifier = Modifier.fillMaxWidth().background(W).padding(horizontal = 10.dp, vertical = 8.dp).navigationBarsPadding(), verticalAlignment = Alignment.Bottom) {
                IconButton(onClick = { showIsland(IslandType.ERROR, "Attach", "Coming soon") }) { Icon(Icons.Default.AttachFile, null, tint = F3) }
                BasicTextField(
                    value = messageText, onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f).background(Bg, RoundedCornerShape(22.dp)).border(1.5.dp, Bd, RoundedCornerShape(22.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                    decorationBox = { inner ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) { if (messageText.isEmpty()) Text("Message...", color = F3, fontSize = 13.5.sp) else inner() }
                            Icon(Icons.Outlined.SentimentSatisfied, null, tint = F3, modifier = Modifier.size(20.dp))
                        }
                    }
                )
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            chat.msgs.add(ChatMsg(isSent = true, text = messageText, time = "Now"))
                            chat.lm = messageText
                            messageText = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.size(42.dp).background(if (messageText.isNotBlank()) Gradient else SolidColor(F3), CircleShape)
                ) { Icon(Icons.Default.ArrowUpward, null, tint = W, modifier = Modifier.size(20.dp)) }
            }
        }
        
        // Custom Three Dot Menu Drawer
        androidx.compose.animation.AnimatedVisibility(visible = showMenu, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)).clickable { showMenu = false }) {
                Box(modifier = Modifier.fillMaxHeight().width(260.dp).align(Alignment.CenterEnd).background(W).clickable(enabled = false){}) {
                    Column(modifier = Modifier.fillMaxSize().padding(top = 56.dp)) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text(chat.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Fg)
                            Text(if(chat.on) "Online" else "Offline", fontSize = 12.sp, color = F3)
                        }
                        HorizontalDivider(color = Bd)
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            ProfileMenuItem(Icons.Default.Checklist, "Select Messages", Vi, Vi.copy(0.08f)) { showMenu = false; showIsland(IslandType.ERROR, "Select", "Coming soon") }
                            ProfileMenuItem(Icons.Default.Search, "Search in Chat", Cy, Cy.copy(0.08f)) { showMenu = false; showIsland(IslandType.ERROR, "Search", "Coming soon") }
                            ProfileMenuItem(Icons.Default.NotificationsOff, "Mute Notifications", F2, Bg) { showMenu = false; showIsland(IslandType.SUCCESS, "Muted", "Notifications muted") }
                            ProfileMenuItem(Icons.Default.Wallpaper, "Wallpaper", Ok, Ok.copy(0.08f)) { showMenu = false; showIsland(IslandType.ERROR, "Wallpaper", "Coming soon") }
                            HorizontalDivider(color = Bd, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                            ProfileMenuItem(Icons.Default.DeleteSweep, "Clear Chat", F2, Bg) { showMenu = false; chat.msgs.clear(); showIsland(IslandType.SUCCESS, "Cleared", "Chat cleared") }
                            ProfileMenuItem(Icons.Default.Block, "Block Contact", Er, Er.copy(0.08f), true) { showMenu = false; showIsland(IslandType.ERROR, "Blocked", "Contact blocked") }
                        }
                    }
                }
            }
        }
    }
}

// --- AUDIO CALL SCREEN ---
@Composable
fun AudioCallScreen(chat: Chat?, onEnd: () -> Unit) {
    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF0F0A1F), Color(0xFF1A1035), Color(0xFF0D1B2A)))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.weight(1f).padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(model = chat?.av, contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape).border(3.dp, Vi.copy(0.4f), CircleShape))
            Spacer(Modifier.height(20.dp))
            Text(chat?.name ?: "Unknown", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = W)
            Spacer(Modifier.height(6.dp))
            Text("Calling...", fontSize = 15.sp, color = W.copy(alpha = 0.7f))
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CallButton(Icons.Default.MicOff, "Mute", muted) { muted = !muted }
            CallButton(Icons.Default.VolumeUp, "Speaker", speaker) { speaker = !speaker }
            CallButton(Icons.Default.Videocam, "Video", false) {}
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEnd, modifier = Modifier.size(64.dp).background(Er, CircleShape)) { Icon(Icons.Outlined.PhoneDisabled, null, tint = W, modifier = Modifier.size(28.dp)) }
                Text("End", fontSize = 10.sp, color = W.copy(0.45f), modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

// --- VIDEO CALL SCREEN ---
@Composable
fun VideoCallScreen(chat: Chat?, onEnd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(model = chat?.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.6f), blendMode = BlendMode.Darken))
        
        Box(modifier = Modifier.padding(top = 60.dp, end = 16.dp).size(110.dp, 155.dp).align(Alignment.TopEnd).clip(RoundedCornerShape(16.dp)).border(2.dp, W.copy(0.15f), RoundedCornerShape(16.dp))) {
            AsyncImage(model = "https://picsum.photos/seed/myprofile99/200/200.jpg", contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 56.dp, start = 16.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEnd, modifier = Modifier.background(W.copy(0.12f), CircleShape)) { Icon(Icons.Default.ChevronLeft, null, tint = W) }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text(chat?.name ?: "", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = W)
                    Text("Calling...", fontSize = 13.sp, color = W.copy(0.6f))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CallButton(Icons.Default.MicOff, "Mute", false) {}
                CallButton(Icons.Default.FlipCameraIos, "Flip", false) {}
                CallButton(Icons.Default.VideocamOff, "Camera", false) {}
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onEnd, modifier = Modifier.size(64.dp).background(Er, CircleShape)) { Icon(Icons.Outlined.PhoneDisabled, null, tint = W, modifier = Modifier.size(28.dp)) }
                    Text("End", fontSize = 10.sp, color = W.copy(0.45f), modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
fun CallButton(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp).background(if (isActive) W else W.copy(0.12f), CircleShape)
        ) {
            Icon(icon, null, tint = if (isActive) Color.Black else W)
        }
        Text(label, fontSize = 10.sp, color = W.copy(0.45f), modifier = Modifier.padding(top = 6.dp))
    }
}

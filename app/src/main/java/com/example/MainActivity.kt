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
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- COLORS & THEME ---
val BrandViolet = Color(0xFF8B5CF6)
val BrandCyan = Color(0xFF0EA5E9)
val BrandGradient = Brush.linearGradient(listOf(BrandViolet, BrandCyan))
val BgColor = Color(0xFFF4F5F9)
val TextMain = Color(0xFF111827)
val TextSec = Color(0xFF6B7280)
val TextTert = Color(0xFF9CA3AF)
val SuccessColor = Color(0xFF10B981)
val ErrorColor = Color(0xFFEF4444)
val CardColor = Color.White
val BorderColor = Color(0xFFEDEDF2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ChatVerseApp()
            }
        }
    }
}

// --- DATA MODELS ---
data class Chat(val id: Int, val name: String, val av: String, val on: Boolean, val unread: Int, val isGrp: Boolean, val lm: String, val lt: String)
data class OnlineFriend(val name: String, val av: String)
data class CallHistory(val name: String, val av: String, val type: String, val time: String, val dur: String)
data class IslandData(val type: IslandType, val title: String, val sub: String)
enum class IslandType { SUCCESS, ERROR }

// --- MOCK DATA ---
val onlineFriends = listOf(
    OnlineFriend("Mara", "https://picsum.photos/seed/mara22/200/200.jpg"),
    OnlineFriend("Jonas", "https://picsum.photos/seed/jonas44/200/200.jpg"),
    OnlineFriend("Kai", "https://picsum.photos/seed/kai55/200/200.jpg"),
    OnlineFriend("Riya", "https://picsum.photos/seed/riya101/200/200.jpg"),
    OnlineFriend("Sam", "https://picsum.photos/seed/sam202/200/200.jpg")
)
val chatsData = listOf(
    Chat(1, "Mara Chen", "https://picsum.photos/seed/mara22/200/200.jpg", true, 2, false, "That sounds perfect, let's do it", "2m"),
    Chat(2, "Design Team", "https://picsum.photos/seed/team88/200/200.jpg", false, 3, true, "Liam: Updated the prototype link", "18m"),
    Chat(3, "Jonas Berg", "https://picsum.photos/seed/jonas44/200/200.jpg", true, 0, false, "See you at the gym tomorrow", "1h"),
    Chat(4, "Ava Okafor", "https://picsum.photos/seed/ava77/200/200.jpg", false, 0, false, "The article was published!", "3h")
)
val callHistoryData = listOf(
    CallHistory("Mara Chen", "https://picsum.photos/seed/mara22/200/200.jpg", "video-in", "Today, 6:12 PM", "5 min"),
    CallHistory("Jonas Berg", "https://picsum.photos/seed/jonas44/200/200.jpg", "audio-out", "Today, 2:30 PM", "12 min"),
    CallHistory("Design Team", "https://picsum.photos/seed/team88/200/200.jpg", "video-miss", "Today, 11:00 AM", "")
)

@Composable
fun ChatVerseApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var islandData by remember { mutableStateOf<IslandData?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun showIsland(type: IslandType, title: String, sub: String) {
        coroutineScope.launch {
            islandData = IslandData(type, title, sub)
            delay(3200)
            islandData = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AnimatedContent(targetState = isLoggedIn, transitionSpec = {
            fadeIn(tween(500)) togetherWith fadeOut(tween(500))
        }, label = "") { loggedIn ->
            if (loggedIn) {
                HomeScreen(
                    onLogout = {
                        showIsland(IslandType.SUCCESS, "Logged Out", "See you soon!")
                        isLoggedIn = false
                    },
                    showIsland = ::showIsland
                )
            } else {
                LoginScreen(
                    onAuthSuccess = {
                        showIsland(IslandType.SUCCESS, "Welcome Back", "Successfully logged in")
                        isLoggedIn = true
                    },
                    showIsland = ::showIsland
                )
            }
        }
        
        // Dynamic Island Overlay
        DynamicIsland(islandData)
    }
}

@Composable
fun DynamicIsland(data: IslandData?) {
    val expanded = data != null
    val width by animateDpAsState(if (expanded) 300.dp else 36.dp, tween(500, easing = FastOutSlowInEasing), label = "")
    val height by animateDpAsState(if (expanded) 68.dp else 36.dp, tween(500, easing = FastOutSlowInEasing), label = "")
    
    Box(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(50))
                .background(if (expanded) Color.White else Color(0xFF111111))
                .border(1.dp, if(expanded) BorderColor else Color.Transparent, RoundedCornerShape(50))
                .shadow(if (expanded) 16.dp else 0.dp, RoundedCornerShape(50), ambientColor = data?.let { if (it.type == IslandType.SUCCESS) SuccessColor else ErrorColor } ?: Color.Black)
        ) {
            AnimatedVisibility(visible = expanded, enter = fadeIn(tween(300, delayMillis = 200))) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (data?.type == IslandType.SUCCESS) Color(0xFFECFDF5) else Color(0xFFFEF2F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (data?.type == IslandType.SUCCESS) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (data?.type == IslandType.SUCCESS) SuccessColor else ErrorColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(11.dp))
                    Column {
                        Text(text = data?.title ?: "", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                        Text(text = data?.sub ?: "", fontSize = 11.sp, color = TextSec)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onAuthSuccess: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Grid Pattern Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 36.dp.toPx()
            for (i in 0 until size.width.toInt() step step.toInt()) drawLine(Color.Black.copy(alpha = 0.02f), Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height))
            for (i in 0 until size.height.toInt() step step.toInt()) drawLine(Color.Black.copy(alpha = 0.02f), Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()))
        }
        
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = "https://raw.githubusercontent.com/sukhdevr898/Chatverse/refs/heads/main/file_000000001c287208ba6c4e5b58c752ff.png",
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(90.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text("ChatVerse", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, style = androidx.compose.ui.text.TextStyle(brush = BrandGradient))
            Text("rista dil se dil tak.", fontSize = 14.5.sp, color = TextSec, modifier = Modifier.padding(bottom = 44.dp))

            Text("GET STARTED", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = TextTert, letterSpacing = 1.6.sp, modifier = Modifier.padding(bottom = 14.dp))

            // Primary Auth Button
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        delay(1500)
                        isLoading = false
                        onAuthSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(BrandGradient), contentAlignment = Alignment.Center) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Login with Google", color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                Text(" Secured by Google ", fontSize = 11.sp, color = TextTert, fontWeight = FontWeight.Medium)
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
            }
        }
        
        Text(
            "Made with ♥ by ChatVerse",
            fontSize = 10.5.sp, color = TextTert,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
        )
    }
}

@Composable
fun HomeScreen(onLogout: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    var currentTab by remember { mutableStateOf("chats") }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(modifier = Modifier.background(Color.White).padding(top = 46.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("ChatVerse", fontSize = 20.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(brush = BrandGradient))
                    Row {
                        IconButton(onClick = { showIsland(IslandType.ERROR, "Camera", "Camera access not available") }) {
                            Icon(Icons.Default.PhotoCamera, "Camera", tint = TextSec)
                        }
                        IconButton(onClick = { showIsland(IslandType.SUCCESS, "Notifications", "You have 3 new messages") }) {
                            Icon(Icons.Default.Notifications, "Notifications", tint = TextSec)
                        }
                    }
                }
                
                // Search Bar
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(BgColor, RoundedCornerShape(15.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = TextTert, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            if (searchQuery.isEmpty()) Text("Search conversations...", color = TextTert, fontSize = 13.5.sp)
                            else innerTextField()
                        }
                    }
                )
            }

            // Tab Content Wrapper
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    "chats" -> ChatsContent(searchQuery, showIsland)
                    "calls" -> CallsContent(showIsland)
                    "status" -> StatusContent()
                    "profile" -> ProfileContent(onLogout, showIsland)
                }
            }
        }

        // Bottom Navigation
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.95f))
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavItem("chats", Icons.Default.ChatBubbleOutline, "Chats", currentTab) { currentTab = "chats" }
            NavItem("calls", Icons.Default.Phone, "Calls", currentTab) { currentTab = "calls" }
            NavItem("status", Icons.Default.MotionPhotosOn, "Status", currentTab) { currentTab = "status" }
            NavItem("profile", Icons.Outlined.Person, "Profile", currentTab) { currentTab = "profile" }
        }
    }
}

@Composable
fun ChatsContent(searchQuery: String, showIsland: (IslandType, String, String) -> Unit) {
    val filteredChats = chatsData.filter { it.name.contains(searchQuery, true) || it.lm.contains(searchQuery, true) }
    
    LazyColumn(contentPadding = PaddingValues(bottom = 90.dp)) {
        item {
            Column(modifier = Modifier.background(Color.White).padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("ONLINE NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTert, letterSpacing = 1.2.sp)
                LazyRow(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(onlineFriends) { friend ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                            showIsland(IslandType.SUCCESS, friend.name, "Starting conversation...")
                        }) {
                            Box {
                                AsyncImage(model = friend.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(50.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape))
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(SuccessColor).border(2.dp, Color.White, CircleShape).align(Alignment.BottomEnd))
                            }
                            Text(friend.name, fontSize = 10.sp, color = TextSec, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
        itemsIndexed(filteredChats) { _, chat ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { showIsland(IslandType.SUCCESS, chat.name, "Opening conversation...") }.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    AsyncImage(model = chat.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(46.dp).clip(CircleShape))
                    if (chat.on) Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SuccessColor).border(2.dp, Color.White, CircleShape).align(Alignment.BottomEnd))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = TextMain)
                        Text(chat.lt, fontSize = 10.5.sp, color = if(chat.unread > 0) BrandViolet else TextTert, fontWeight = if(chat.unread > 0) FontWeight.Bold else FontWeight.Normal)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(chat.lm, fontSize = 12.sp, color = TextSec, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        if (chat.unread > 0) {
                            Box(modifier = Modifier.padding(start = 8.dp).size(19.dp).clip(CircleShape).background(BrandGradient), contentAlignment = Alignment.Center) {
                                Text(chat.unread.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(start = 78.dp, end = 20.dp), color = BorderColor, thickness = 1.dp)
        }
    }
}

@Composable
fun CallsContent(showIsland: (IslandType, String, String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 90.dp)) {
        items(callHistoryData) { call ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable {}.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(model = call.av, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(46.dp).clip(CircleShape))
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(call.name, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = if (call.type.contains("miss")) ErrorColor else TextMain)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (call.type.contains("in")) Icons.Default.CallReceived else if(call.type.contains("out")) Icons.Default.CallMade else Icons.Default.CallMissed, null, tint = if (call.type.contains("miss")) ErrorColor else if (call.type.contains("in")) SuccessColor else BrandViolet, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (call.type.contains("in")) "Incoming" else if(call.type.contains("out")) "Outgoing" else "Missed", fontSize = 11.5.sp, color = TextTert)
                    }
                }
                Text(call.time.split(",").last().trim(), fontSize = 10.5.sp, color = TextTert, modifier = Modifier.padding(end = 12.dp))
                IconButton(onClick = { showIsland(IslandType.SUCCESS, "Audio Call", "Calling ${call.name}...") }, modifier = Modifier.size(34.dp).background(CardColor, CircleShape).border(1.dp, BorderColor, CircleShape)) {
                    Icon(Icons.Default.Phone, null, tint = BrandViolet, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { showIsland(IslandType.SUCCESS, "Video Call", "Starting video...") }, modifier = Modifier.size(34.dp).background(CardColor, CircleShape).border(1.dp, BorderColor, CircleShape)) {
                    Icon(Icons.Default.Videocam, null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(start = 78.dp, end = 20.dp), color = BorderColor, thickness = 1.dp)
        }
    }
}

@Composable
fun StatusContent() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(BrandViolet.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MotionPhotosOn, null, tint = BrandViolet, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text("Status Updates", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
        Text("Share your moments with contacts.\nStatus updates disappear after 24 hours.", fontSize = 12.sp, color = TextTert, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun ProfileContent(onLogout: () -> Unit, showIsland: (IslandType, String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                AsyncImage(model = "https://picsum.photos/seed/myprofile99/200/200.jpg", contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(76.dp).clip(CircleShape).border(3.dp, BrandViolet, CircleShape))
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(24.dp).clip(CircleShape).background(BrandGradient).border(2.dp, Color.White, CircleShape).clickable { showIsland(IslandType.ERROR, "Edit Profile", "Profile editing coming soon") }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Arjun Mehta", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextMain)
            Text("rista dil se dil tak. Living in the moment,\none message at a time.", fontSize = 12.5.sp, color = TextSec, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(28.dp))
        
        ProfileMenuItem(Icons.Default.Shield, "Account & Security", BrandViolet, Color(0xFF8B5CF6).copy(alpha = 0.08f)) { showIsland(IslandType.SUCCESS, "Account", "Your account is secure") }
        ProfileMenuItem(Icons.Default.Lock, "Privacy Settings", BrandCyan, Color(0xFF0EA5E9).copy(alpha = 0.08f)) { showIsland(IslandType.ERROR, "Privacy", "Coming soon") }
        ProfileMenuItem(Icons.Default.Storage, "Storage & Data", SuccessColor, SuccessColor.copy(alpha = 0.08f)) { showIsland(IslandType.ERROR, "Storage", "Coming soon") }
        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 5.dp, horizontal = 14.dp))
        ProfileMenuItem(Icons.Default.Palette, "Appearance", TextSec, BgColor) { showIsland(IslandType.ERROR, "Theme", "Coming soon") }
        ProfileMenuItem(Icons.Default.Help, "Help & Support", TextSec, BgColor) { showIsland(IslandType.ERROR, "Help", "Coming soon") }
        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 5.dp, horizontal = 14.dp))
        ProfileMenuItem(Icons.Default.Logout, "Log Out", ErrorColor, ErrorColor.copy(alpha = 0.08f), isDestructive = true) { onLogout() }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, tint: Color, bg: Color, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(13.dp))
        Text(title, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = if(isDestructive) ErrorColor else TextMain, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextTert, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun RowScope.NavItem(id: String, icon: ImageVector, label: String, current: String, onClick: () -> Unit) {
    val selected = id == current
    val tint by animateColorAsState(if (selected) BrandViolet else TextTert, label = "")
    
    Column(
        modifier = Modifier.clickable { onClick() }.padding(vertical = 5.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            androidx.compose.animation.AnimatedVisibility(visible = selected) {
                Box(modifier = Modifier.offset(y = (-8).dp).size(22.dp, 2.5.dp).clip(RoundedCornerShape(50)).background(BrandGradient))
            }
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        }
        Text(label, fontSize = 9.5.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = tint)
    }
}


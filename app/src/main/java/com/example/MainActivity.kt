package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.OverlayService
import com.example.state.VpnStateTracker
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedPreferences = getSharedPreferences("TP_MODZ_LOGIN_PREFS", Context.MODE_PRIVATE)
        VpnStateTracker.init(this)

        setContent {
            MyApplicationTheme {
                MainAppFlow(
                    activity = this,
                    onLogout = {
                        sharedPreferences.edit().putBoolean("is_logged_in", false).apply()
                        stopService(Intent(this, OverlayService::class.java))
                        VpnStateTracker.setOverlayActive(this, false)
                        
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Auto-restart overlay button if logged in and permission is granted
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (isLoggedIn && PermissionHelper.hasOverlayPermission(this)) {
            val isOverlayActive = VpnStateTracker.isOverlayActive.value
            if (!isOverlayActive) {
                try {
                    startOverlayService(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

@Composable
fun MainAppFlow(activity: ComponentActivity, onLogout: () -> Unit) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }

    // Launcher for overlay permission settings
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermission = PermissionHelper.hasOverlayPermission(context)
        if (hasOverlayPermission) {
            startOverlayService(context)
        }
    }

    LaunchedEffect(hasOverlayPermission) {
        if (hasOverlayPermission) {
            startOverlayService(context)
        }
    }

    if (!hasOverlayPermission) {
        // Overlay Permission request screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF202124))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E3033))
                    .border(2.dp, Color(0xFF00E5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "YÊU CẦU QUYỀN NỔI",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Để hiển thị nút nổi TP×MD trên màn hình khi chơi game, vui lòng cấp quyền vẽ lên ứng dụng khác cho hệ thống.",
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 32.dp)
            )

            Button(
                onClick = {
                    val intent = PermissionHelper.getOverlayPermissionIntent(context)
                    overlayPermissionLauncher.launch(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "CẤP QUYỀN TRUY CẬP (OVERLAY)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onLogout) {
                Text(
                    text = "Đăng xuất / Quay lại",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        // Main Dashboard Control UI
        MainDashboard(onLogout = onLogout)
    }
}

@Composable
fun MainDashboard(onLogout: () -> Unit) {
    val context = LocalContext.current
    val isOverlayActive by VpnStateTracker.isOverlayActive.collectAsState()

    Scaffold(
        containerColor = Color(0xFF202124),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        text = "LAG TP×MDUC",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF202124)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF202124))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Logo Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3033)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFF00E5FF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF202124))
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TP",
                            color = Color(0xFF00E5FF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Trạng thái hệ thống",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Đã khởi động dịch vụ nút nổi.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3033)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TRẠNG THÁI NÚT NỔI",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isOverlayActive) "Đang hiển thị trên màn hình" else "Đang tạm dừng",
                            color = if (isOverlayActive) Color(0xFF00E5FF) else Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isOverlayActive,
                        onCheckedChange = { active ->
                            if (active) {
                                startOverlayService(context)
                            } else {
                                context.stopService(Intent(context, OverlayService::class.java))
                                VpnStateTracker.setOverlayActive(context, false)
                                Toast.makeText(context, "Đã tắt nút nổi", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF202124),
                            checkedTrackColor = Color(0xFF00E5FF),
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Version info footer
            Text(
                text = "Phiên bản 1.0 • Phát triển bởi Minh Đức",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

private fun startOverlayService(context: Context) {
    if (PermissionHelper.hasOverlayPermission(context)) {
        val intent = Intent(context, OverlayService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            VpnStateTracker.setOverlayActive(context, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

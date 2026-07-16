package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NetworkHelper
import kotlinx.coroutines.launch

@Composable
fun ControlPanel(
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onOpenMenu: () -> Unit,
    onDrag: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var digits by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .width(260.dp)
            .wrapContentHeight()
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF202124)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with drag area and action controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x.toInt(), dragAmount.y.toInt())
                        }
                    }
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LAG CONTROL",
                        color = Color(0xFF00E5FF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Trình điều khiển lag",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize Button (—)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E3033))
                            .clickable { onMinimize() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "—",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Close Button (✕)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE53935))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF2E3033))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // QUAY LẠI MENU Button
            Button(
                onClick = onOpenMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E3033),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Menu",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "← QUAY LẠI MENU",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7 Input Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until 7) {
                    val digit = digits.getOrNull(i)?.toString() ?: ""
                    val hasDigit = digit.isNotEmpty()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(
                                1.dp,
                                if (hasDigit) Color(0xFF00E5FF) else Color(0xFF424242),
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            color = if (hasDigit) Color.White else Color(0xFF757575),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status message label
            if (statusMessage.isNotEmpty() || isSending) {
                Text(
                    text = if (isSending) "Đang kết nối..." else statusMessage,
                    color = if (isSending) Color.White else if (isSuccess) Color(0xFF00E676) else Color(0xFFFF1744),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Custom Numberpad
            NumberPad(
                onKeyClick = { key ->
                    when (key) {
                        "C" -> {
                            digits = ""
                            statusMessage = ""
                        }
                        "DEL" -> {
                            if (digits.isNotEmpty()) {
                                digits = digits.dropLast(1)
                            }
                            statusMessage = ""
                        }
                        else -> {
                            if (digits.length < 7) {
                                digits += key
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // OK request submission trigger button
            Button(
                onClick = {
                    if (digits.length != 7) {
                        statusMessage = "Nhập đủ 7 số"
                        isSuccess = false
                        return@Button
                    }

                    isSending = true
                    statusMessage = ""
                    scope.launch {
                        val result = NetworkHelper.sendKickRequest(digits)
                        isSending = false
                        result.fold(
                            onSuccess = { msg ->
                                isSuccess = true
                                statusMessage = msg
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                isSuccess = false
                                statusMessage = "Không kết nối được máy chủ"
                                Toast.makeText(context, "Không kết nối được máy chủ", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "OK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

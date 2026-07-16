package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingView(
    onDrag: (Int, Int) -> Unit,
    onCloseAll: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                (fadeIn(animationSpec = tween(250)) + scaleIn(animationSpec = tween(250)))
                    .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(animationSpec = tween(200)))
            },
            label = "ExpandCollapseAnimation"
        ) { expanded ->
            if (!expanded) {
                // Circular Floating Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF202124))
                        .border(2.dp, Color(0xFF00E5FF), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount.x.toInt(), dragAmount.y.toInt())
                                }
                            )
                        }
                        .clickable {
                            isExpanded = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "TP×MD",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                    }
                }
            } else {
                // Expanded Control Panel
                ControlPanel(
                    onMinimize = { isExpanded = false },
                    onClose = onCloseAll,
                    onOpenMenu = {
                        onOpenMenu()
                        isExpanded = false // minimize back to button after triggering menu
                    },
                    onDrag = onDrag
                )
            }
        }
    }
}

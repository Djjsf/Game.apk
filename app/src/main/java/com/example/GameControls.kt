package com.example

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberBurger
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.Emerald40
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun GameControls(
    currentDirection: Direction,
    boostActive: Boolean,
    onDirectionChange: (Direction) -> Unit,
    onBoostToggle: (Boolean) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_controls_panel"),
        color = Slate900.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 16.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF334155),
                    Color(0xFF0F172A)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action / Boost Side
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Turbo Boost Button
                TurboBoostButton(
                    isActive = boostActive,
                    onPressChange = { active ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBoostToggle(active)
                    }
                )

                // Quick Restart Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                        .border(1.dp, Slate700, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRestart()
                        }
                        .testTag("button_quick_restart"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Restart Game",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Right: D-Pad Touch Buttons (UP, DOWN, LEFT, RIGHT)
            DPadController(
                activeDirection = currentDirection,
                onDirectionClick = { dir ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDirectionChange(dir)
                }
            )
        }
    }
}

@Composable
fun DPadController(
    activeDirection: Direction,
    onDirectionClick: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSize = 58.dp
    val spacing = 4.dp

    Column(
        modifier = modifier
            .testTag("dpad_controller"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // UP Button
        DirectionButton(
            icon = Icons.Rounded.KeyboardArrowUp,
            label = "UP",
            testTag = "button_up",
            isActive = activeDirection == Direction.UP,
            size = buttonSize,
            onClick = { onDirectionClick(Direction.UP) }
        )

        // LEFT, CENTER, RIGHT Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT Button
            DirectionButton(
                icon = Icons.Rounded.KeyboardArrowLeft,
                label = "LEFT",
                testTag = "button_left",
                isActive = activeDirection == Direction.LEFT,
                size = buttonSize,
                onClick = { onDirectionClick(Direction.LEFT) }
            )

            // Center Arcade Hub
            Box(
                modifier = Modifier
                    .size(buttonSize * 0.85f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Slate700, Slate900)
                        )
                    )
                    .border(1.dp, Color(0xFF475569), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Emerald40.copy(alpha = 0.8f))
                )
            }

            // RIGHT Button
            DirectionButton(
                icon = Icons.Rounded.KeyboardArrowRight,
                label = "RIGHT",
                testTag = "button_right",
                isActive = activeDirection == Direction.RIGHT,
                size = buttonSize,
                onClick = { onDirectionClick(Direction.RIGHT) }
            )
        }

        // DOWN Button
        DirectionButton(
            icon = Icons.Rounded.KeyboardArrowDown,
            label = "DOWN",
            testTag = "button_down",
            isActive = activeDirection == Direction.DOWN,
            size = buttonSize,
            onClick = { onDirectionClick(Direction.DOWN) }
        )
    }
}

@Composable
fun DirectionButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    isActive: Boolean,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        label = "scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isActive) Emerald40 else Color(0xFF475569),
        label = "border"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) Emerald40.copy(alpha = 0.35f)
        else if (isActive) Slate700
        else Slate800,
        label = "bg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isActive || isPressed) Emerald40 else Color.White,
        label = "iconTint"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
fun TurboBoostButton(
    isActive: Boolean,
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 0.93f else 1.0f,
        label = "boostScale"
    )

    val bgBrush = if (isActive) {
        Brush.linearGradient(listOf(AmberBurger, Color(0xFFEA580C)))
    } else {
        Brush.linearGradient(listOf(Slate800, Slate900))
    }

    Box(
        modifier = modifier
            .size(76.dp)
            .scale(scale)
            .shadow(if (isActive) 8.dp else 2.dp, CircleShape)
            .clip(CircleShape)
            .background(bgBrush)
            .border(
                2.dp,
                if (isActive) Color(0xFFFDE047) else Color(0xFF475569),
                CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            }
            .testTag("button_boost"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FastForward,
                contentDescription = "Turbo Boost",
                tint = if (isActive) Color.White else AmberBurger,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "BOOST",
                color = if (isActive) Color.White else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

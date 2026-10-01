package com.example

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VolumeMute
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun ScoreBoard(
    score: Int,
    highScore: Int,
    burgersEaten: Int,
    bounces: Int,
    isPaused: Boolean,
    soundEnabled: Boolean,
    tailCollisionEnabled: Boolean,
    onTogglePause: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleCollision: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scoreboard_header"),
        color = Slate900.copy(alpha = 0.95f),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: App Title & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🐍",
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = "SLITHER SNAKE",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isPaused) "PAUSED" else "SCREEN WRAP ARCADE",
                            color = if (isPaused) AmberBurger else Emerald40,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Quick Action Bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Global Leaderboard Button
                    HeaderActionButton(
                        onClick = onOpenLeaderboard,
                        testTag = "button_leaderboard"
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Global Leaderboard",
                            tint = AmberBurger,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Help Dialog Button
                    HeaderActionButton(
                        onClick = { showHelpDialog = true },
                        testTag = "button_help"
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Game Info",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sound Toggle Button
                    HeaderActionButton(
                        onClick = onToggleSound,
                        testTag = "button_sound"
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeMute,
                            contentDescription = if (soundEnabled) "Mute Sound" else "Enable Sound",
                            tint = if (soundEnabled) Emerald40 else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Pause Button
                    HeaderActionButton(
                        onClick = onTogglePause,
                        testTag = "button_pause"
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = if (isPaused) "Resume Game" else "Pause Game",
                            tint = if (isPaused) AmberBurger else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom Row: Score, High Score, Burger Count, Bounces
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Box
                ScoreChip(
                    emoji = "🍔",
                    label = "SCORE",
                    value = score.toString(),
                    highlightColor = Emerald40
                )

                // High Score Box (clickable to open Leaderboard!)
                ScoreChip(
                    emoji = "🏆",
                    label = "BEST",
                    value = highScore.toString(),
                    highlightColor = AmberBurger,
                    modifier = Modifier.clickable { onOpenLeaderboard() }
                )

                // Burgers Count
                ScoreChip(
                    emoji = "😋",
                    label = "EATEN",
                    value = "$burgersEaten",
                    highlightColor = Color(0xFF38BDF8)
                )

                // Screen Wraps Count
                ScoreChip(
                    emoji = "🌀",
                    label = "WRAPS",
                    value = "$bounces",
                    highlightColor = CyanNeon
                )
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = Slate900,
            title = {
                Text(
                    text = "Slither Snake Guide 🐍🍔",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "• 🌀 Screen Wrapping: Going off any edge (left, right, top, bottom) wraps your snake around to the opposite side seamlessly! Game Over triggers ONLY on self-collision (hitting your own body).",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• 🍔 Burger Food: Munch delicious burgers to grow longer and score +10 points! Look out for rare Golden Burgers (+30)!",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• 👅 Animated Snake: Watch the snake's expressive eyes and flickering red forked tongue searching for food!",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• 🎮 Controls: Tap the UP, DOWN, LEFT, RIGHT buttons or swipe the screen. Press & hold BOOST for extra speed!",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate800)
                            .clickable { onToggleCollision() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tail Collision (Classic Mode)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (tailCollisionEnabled) "ON" else "OFF (Zen)",
                            color = if (tailCollisionEnabled) Color(0xFFF87171) else Emerald40,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("GOT IT", color = Emerald40, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun ScoreChip(
    emoji: String,
    label: String,
    value: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate800.copy(alpha = 0.8f))
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Column {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        slideInVertically { it } togetherWith slideOutVertically { -it }
                    },
                    label = "ScoreText"
                ) { targetValue ->
                    Text(
                        text = targetValue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = highlightColor
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderActionButton(
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Slate800)
            .border(1.dp, Slate700, CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

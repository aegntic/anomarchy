package com.example.omarchy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.CyberMascot
import com.example.omarchy.model.MascotCatalog
import com.example.omarchy.model.MascotId
import com.example.omarchy.model.OmarchyThemeConfig
import kotlinx.coroutines.delay

@Composable
fun LoadingSplashScreen(
  theme: OmarchyThemeConfig,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var bootProgress by remember { mutableFloatStateOf(0f) }
  var bootStep by remember { mutableIntStateOf(0) }

  val bootLogs = remember {
    listOf(
      "[  0.000000] Omarchy Linux 6.13.2-aegntic-zen #1 SMP PREEMPT_DYNAMIC",
      "[  0.118920] Original OS & Vision: David Heinemeier Hansson (DHH)",
      "[  0.245012] Mobile Architecture: @aegntic • 100% Sovereign & Offline",
      "[  0.389102] INITIATING NEURAL LINK & WAYLAND COMPOSITOR...",
      "[  0.589201] LOADED MASCOTS: [CATFACE, LONGNECK, PLINKY]",
      "[  0.781203] HYPRLAND TILING COMPOSITOR: DWINDLE READY",
      "[  0.924819] QUICKSELL ENVIRONMENT: MOUNTED OK",
      "[  1.000000] SYSTEM OPTIMIZED — PROGRAMMER HAPPINESS SECURED"
    )
  }

  LaunchedEffect(Unit) {
    for (i in 1..8) {
      delay(220L)
      bootStep = i
      bootProgress = (i / 8f)
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "mascot_floating")
  val floatCat by infiniteTransition.animateFloat(
    initialValue = -5f,
    targetValue = 5f,
    animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
    label = "float_cat"
  )
  val floatLong by infiniteTransition.animateFloat(
    initialValue = 4f,
    targetValue = -4f,
    animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing), RepeatMode.Reverse),
    label = "float_long"
  )
  val floatPlink by infiniteTransition.animateFloat(
    initialValue = -6f,
    targetValue = 6f,
    animationSpec = infiniteRepeatable(tween(1550, easing = LinearEasing), RepeatMode.Reverse),
    label = "float_plink"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF090B14))
      .clickable { onDismiss() }
      .testTag("loading_splash_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // -------------------------------------------------------------
      // TOP BRAND HEADER
      // -------------------------------------------------------------
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0E1322))
            .border(1.dp, Color(0xFF78F1A0).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "󰣇",
              color = Color(0xFF78F1A0),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "OMARCHY",
              color = Color(0xFF78F1A0),
              fontSize = 15.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp
            )
          }
        }

        Text(
          text = "STANDALONE LINUX MOBILE OS",
          color = Color(0xFF7DCEFF),
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )

        Text(
          text = "Original OS by DHH • Mobile Architecture by @aegntic",
          color = Color(0xFF6B7280),
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      // -------------------------------------------------------------
      // CENTER: ANIMATED CYBER MASCOT TRIO
      // -------------------------------------------------------------
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "CYBER COMPANION TRIO",
          color = Color(0xFF94A3B8),
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 1. CATFACE
          MascotBadge(
            mascot = MascotCatalog.Catface,
            floatY = floatCat,
            accentColor = Color(0xFF78F1A0),
            iconVector = Icons.Default.Code
          )

          // 2. LONGNECK
          MascotBadge(
            mascot = MascotCatalog.Longneck,
            floatY = floatLong,
            accentColor = Color(0xFFF6C177),
            iconVector = Icons.Default.Memory
          )

          // 3. PLINKY
          MascotBadge(
            mascot = MascotCatalog.Plinky,
            floatY = floatPlink,
            accentColor = Color(0xFFEBBCBA),
            iconVector = Icons.Default.Palette
          )
        }
      }

      // -------------------------------------------------------------
      // BOTTOM: TERMINAL BOOT CONSOLE & PROGRESS
      // -------------------------------------------------------------
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Mini Boot Console
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0D111F))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            val visibleLogs = bootLogs.take(bootStep)
            visibleLogs.forEachIndexed { index, log ->
              val isLast = index == visibleLogs.lastIndex
              val logColor = when {
                log.contains("DHH") || log.contains("@aegntic") -> Color(0xFF78F1A0)
                log.contains("LOADED MASCOTS") -> Color(0xFF7DCEFF)
                log.contains("OPTIMIZED") -> Color(0xFFA6E3A1)
                else -> Color(0xFF94A3B8)
              }
              Text(
                text = log + if (isLast && bootStep < bootLogs.size) " ▋" else "",
                color = logColor,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 11.sp
              )
            }
          }
        }

        // Progress Bar
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = if (bootStep >= bootLogs.size) "HYPRLAND READY" else "BOOTING SYSTEM...",
              color = Color(0xFF78F1A0),
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${(bootProgress * 100).toInt()}%",
              color = Color(0xFF7DCEFF),
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(Color(0xFF1E293B))
          ) {
            val animatedWidth by animateFloatAsState(
              targetValue = bootProgress,
              animationSpec = tween(180, easing = FastOutSlowInEasing),
              label = "progress_bar"
            )
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedWidth.coerceIn(0f, 1f))
                .background(
                  Brush.horizontalGradient(
                    listOf(Color(0xFF78F1A0), Color(0xFF7DCEFF))
                  )
                )
            )
          }
        }

        // Enter Desktop Button
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF78F1A0)
          ),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .testTag("enter_desktop_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = if (bootStep >= bootLogs.size) "ENTER OMARCHY DESKTOP" else "SKIP TO DESKTOP",
              color = Color.Black,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = null,
              tint = Color.Black,
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MascotBadge(
  mascot: CyberMascot,
  floatY: Float,
  accentColor: Color,
  iconVector: androidx.compose.ui.graphics.vector.ImageVector
) {
  Column(
    modifier = Modifier
      .offset(y = floatY.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(Color(0xFF0E1322))
      .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp, vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    // Hologram Avatar Icon
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(accentColor.copy(alpha = 0.15f))
        .border(1.dp, accentColor, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = iconVector,
        contentDescription = mascot.name,
        tint = accentColor,
        modifier = Modifier.size(18.dp)
      )
    }

    Text(
      text = mascot.name,
      color = accentColor,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )

    Text(
      text = mascot.subtitle.split(" ").first(),
      color = Color(0xFF94A3B8),
      fontSize = 8.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

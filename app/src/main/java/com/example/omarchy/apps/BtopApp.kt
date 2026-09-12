package com.example.omarchy.apps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.ProcessItem

@Composable
fun BtopApp(
  theme: OmarchyThemeConfig,
  cpuPercent: Float,
  memPercent: Float,
  cpuHistory: List<Float>,
  processes: List<ProcessItem>,
  onKillProcess: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(theme.terminalBackground)
      .padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Top Row: CPU Graph Box & Memory Gauge Box
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(110.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // CPU Box
      Box(
        modifier = Modifier
          .weight(1.2f)
          .fillMaxHeight()
          .background(theme.surfaceColor)
          .border(1.dp, theme.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
          .padding(6.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "CPU [8 Cores] 4.2GHz",
              color = theme.accentPrimary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${cpuPercent.toInt()}%",
              color = if (cpuPercent > 70) theme.terminalRed else theme.terminalGreen,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Black
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Mini Canvas sparkline
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          ) {
            if (cpuHistory.size > 1) {
              val path = Path()
              val stepX = size.width / (cpuHistory.size - 1)
              val maxY = 100f

              cpuHistory.forEachIndexed { i, value ->
                val x = i * stepX
                val y = size.height - (value.coerceIn(0f, maxY) / maxY) * size.height
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
              }

              drawPath(
                path = path,
                color = theme.accentPrimary,
                style = Stroke(width = 2.5f)
              )
            }
          }
        }
      }

      // Memory Box
      Box(
        modifier = Modifier
          .weight(0.8f)
          .fillMaxHeight()
          .background(theme.surfaceColor)
          .border(1.dp, theme.terminalCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
          .padding(6.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "MEM",
              color = theme.terminalCyan,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${(memPercent * 0.16f).format(1)} / 16.0 GiB",
              color = theme.textSecondary,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          // Memory bar
          Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(theme.surfaceBorder)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(memPercent / 100f)
                  .fillMaxHeight()
                  .background(theme.terminalCyan)
              )
            }
            Text(
              text = "${memPercent.toInt()}% Used",
              color = theme.textPrimary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          Text(
            text = "Swap: 0.2 / 8.0 GiB",
            color = theme.textMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Process Table Box
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(theme.surfaceColor)
        .border(1.dp, theme.surfaceBorder, RoundedCornerShape(4.dp))
        .padding(6.dp)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Table Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceVariant)
            .padding(horizontal = 4.dp, vertical = 3.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("PID", color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))
          Text("COMMAND", color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
          Text("CPU%", color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(42.dp))
          Text("MEM%", color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(42.dp))
          Text("ACT", color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))
        }

        // Process items
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          items(processes) { p ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 3.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${p.pid}",
                color = theme.textMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(36.dp)
              )
              Text(
                text = p.name,
                color = theme.textPrimary,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
              )
              Text(
                text = "${p.cpuPercent}%",
                color = if (p.cpuPercent > 3f) theme.terminalYellow else theme.terminalGreen,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(42.dp)
              )
              Text(
                text = "${p.memPercent}%",
                color = theme.terminalCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(42.dp)
              )

              // Kill button
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(3.dp))
                  .background(theme.terminalRed.copy(alpha = 0.2f))
                  .border(0.5.dp, theme.terminalRed.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                  .clickable { onKillProcess(p.pid) }
                  .padding(horizontal = 4.dp, vertical = 1.dp)
                  .testTag("kill_pid_${p.pid}"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "KILL",
                  color = theme.terminalRed,
                  fontSize = 8.5.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}

private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)

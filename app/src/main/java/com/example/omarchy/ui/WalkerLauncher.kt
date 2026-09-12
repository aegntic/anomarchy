package com.example.omarchy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.AppType
import com.example.omarchy.model.OmarchyThemeConfig

@Composable
fun WalkerLauncher(
  theme: OmarchyThemeConfig,
  query: String,
  onQueryChange: (String) -> Unit,
  onLaunchApp: (AppType) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allApps = AppType.entries
  val filteredApps = if (query.isBlank()) {
    allApps
  } else {
    allApps.filter {
      it.displayName.contains(query, ignoreCase = true) ||
      it.binaryName.contains(query, ignoreCase = true) ||
      it.description.contains(query, ignoreCase = true) ||
      it.category.contains(query, ignoreCase = true)
    }
  }

  // Quick calculator evaluation
  val calcResult = evaluateMath(query)

  // Backdrop
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.65f))
      .clickable { onClose() }
      .padding(horizontal = 16.dp, vertical = 32.dp),
    contentAlignment = Alignment.TopCenter
  ) {
    // Dialog Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(theme.surfaceColor)
        .border(1.5.dp, theme.accentPrimary, RoundedCornerShape(12.dp))
        .clickable(enabled = false) {}
        .padding(14.dp)
        .testTag("walker_launcher_modal")
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Walker Search Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.surfaceVariant)
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = theme.accentPrimary,
            modifier = Modifier.size(18.dp)
          )

          BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
              .weight(1f)
              .testTag("walker_search_input"),
            textStyle = TextStyle(
              color = theme.textPrimary,
              fontSize = 13.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            cursorBrush = SolidColor(theme.accentPrimary),
            decorationBox = { inner ->
              if (query.isEmpty()) {
                Text(
                  text = "Walker Application Launcher (Type app or calculation)...",
                  color = theme.textMuted,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
              inner()
            }
          )

          IconButton(
            onClick = onClose,
            modifier = Modifier.size(20.dp).testTag("walker_close_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = theme.textMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        // Calculator result if any
        if (calcResult != null) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(theme.terminalBackground)
              .border(1.dp, theme.terminalGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Calculate,
              contentDescription = null,
              tint = theme.terminalGreen,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "$query = $calcResult",
              color = theme.terminalGreen,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Applications List
        Text(
          text = "APPLICATIONS (${filteredApps.size})",
          color = theme.textMuted,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )

        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(filteredApps) { app ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(theme.surfaceVariant.copy(alpha = 0.6f))
                .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
                .clickable {
                  onLaunchApp(app)
                  onClose()
                }
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .testTag("walker_item_${app.binaryName}"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(theme.surfaceColor)
                  .border(1.dp, theme.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = app.icon,
                  contentDescription = null,
                  tint = theme.accentPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = app.displayName,
                    color = theme.textPrimary,
                    fontSize = 12.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = app.binaryName,
                    color = theme.terminalCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
                Text(
                  text = app.description,
                  color = theme.textSecondary,
                  fontSize = 10.5.sp
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(theme.surfaceColor)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = app.category,
                  color = theme.textMuted,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }
  }
}

private fun evaluateMath(expr: String): String? {
  val clean = expr.replace(" ", "")
  if (clean.matches(Regex("""^\d+[\+\-\*\/]\d+$"""))) {
    try {
      val op = clean.first { it in "+-*/" }
      val parts = clean.split(op)
      val a = parts[0].toDoubleOrNull() ?: return null
      val b = parts[1].toDoubleOrNull() ?: return null
      val res = when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> if (b != 0.0) a / b else return "NaN"
        else -> return null
      }
      return if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
    } catch (_: Exception) {
      return null
    }
  }
  return null
}

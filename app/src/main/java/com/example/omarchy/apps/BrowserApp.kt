package com.example.omarchy.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.OmarchyThemeConfig

@Composable
fun BrowserApp(
  theme: OmarchyThemeConfig,
  currentUrl: String,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var urlInput by remember(currentUrl) { mutableStateOf(currentUrl) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(theme.surfaceColor)
  ) {
    // Browser Navigation Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(42.dp)
        .background(theme.surfaceVariant)
        .border(0.5.dp, theme.surfaceBorder)
        .padding(horizontal = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = theme.textMuted, modifier = Modifier.size(16.dp))
      Icon(Icons.Default.ArrowForward, contentDescription = "Forward", tint = theme.textMuted, modifier = Modifier.size(16.dp))
      Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = theme.textSecondary, modifier = Modifier.size(16.dp))

      // URL Bar
      Row(
        modifier = Modifier
          .weight(1f)
          .height(30.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(theme.surfaceColor)
          .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(Icons.Default.Lock, contentDescription = "SSL", tint = theme.terminalGreen, modifier = Modifier.size(12.dp))
        BasicTextField(
          value = urlInput,
          onValueChange = { urlInput = it },
          modifier = Modifier.weight(1f).testTag("browser_url_input"),
          textStyle = TextStyle(
            color = theme.textPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          ),
          singleLine = true,
          cursorBrush = SolidColor(theme.accentPrimary)
        )
      }

      IconButton(
        onClick = { onNavigate(urlInput) },
        modifier = Modifier.size(28.dp).testTag("browser_go_button")
      ) {
        Icon(Icons.Default.Search, contentDescription = "Go", tint = theme.accentPrimary, modifier = Modifier.size(15.dp))
      }
    }

    // Bookmarks Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(theme.surfaceColor)
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      BookmarkPill("Omarchy", "https://omarchy.org", theme) { onNavigate("https://omarchy.org") }
      BookmarkPill("Rails 8", "https://rubyonrails.org", theme) { onNavigate("https://rubyonrails.org") }
      BookmarkPill("ArchWiki", "https://wiki.archlinux.org", theme) { onNavigate("https://wiki.archlinux.org") }
      BookmarkPill("GitHub", "https://github.com/omacom/omarchy", theme) { onNavigate("https://github.com/omacom/omarchy") }
      BookmarkPill("ONCE", "https://once.com", theme) { onNavigate("https://once.com") }
      BookmarkPill("Basecamp", "https://basecamp.com", theme) { onNavigate("https://basecamp.com") }
    }

    // Web Page Content Simulation
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(14.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      if (currentUrl.contains("omarchy", ignoreCase = true)) {
        WebOmarchyPage(theme)
      } else if (currentUrl.contains("rails", ignoreCase = true)) {
        WebRailsPage(theme)
      } else {
        WebGenericPage(url = currentUrl, theme = theme)
      }
    }
  }
}

@Composable
private fun WebOmarchyPage(theme: OmarchyThemeConfig) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(theme.surfaceVariant)
      .border(1.dp, theme.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "OMARCHY",
        color = theme.accentPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp
      )
      Text(
        text = "Beautiful, Modern & Opinionated Linux",
        color = theme.textPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Omarchy combines Arch Linux with the cutting-edge Hyprland tiling Wayland compositor and the Quickshell desktop shell. Crafted by David Heinemeier Hansson (DHH) for developers who appreciate speed, aesthetics, and agentic workflows.",
        color = theme.textSecondary,
        fontSize = 12.sp,
        lineHeight = 17.sp
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp)
      ) {
        FeatureBadge("Hyprland 0.42", theme)
        FeatureBadge("Quickshell", theme)
        FeatureBadge("Agentic Linux", theme)
        FeatureBadge("22 Themes", theme)
      }
    }
  }
}

@Composable
private fun WebRailsPage(theme: OmarchyThemeConfig) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(theme.surfaceVariant)
      .border(1.dp, theme.terminalRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "Ruby on Rails 8.0",
        color = theme.terminalRed,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black
      )
      Text(
        text = "Compress the complexity of modern web apps.",
        color = theme.textPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = "Omarchy ships with a pre-configured, optimized Ruby on Rails development environment. Powered by SQLite3, Solid Cache, Solid Queue, and Kamal deployment out of the box.",
        color = theme.textSecondary,
        fontSize = 12.sp,
        lineHeight = 17.sp
      )
    }
  }
}

@Composable
private fun WebGenericPage(url: String, theme: OmarchyThemeConfig) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(theme.surfaceVariant)
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(text = "Viewing: $url", color = theme.accentPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
      Text(text = "Simulated secure sandbox rendering via Omarchy Wayland chromium wrapper.", color = theme.textSecondary, fontSize = 11.sp)
    }
  }
}

@Composable
private fun BookmarkPill(title: String, url: String, theme: OmarchyThemeConfig, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(theme.surfaceVariant)
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 3.dp)
  ) {
    Text(text = title, color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
  }
}

@Composable
private fun FeatureBadge(text: String, theme: OmarchyThemeConfig) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(theme.accentPrimary.copy(alpha = 0.15f))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(text = text, color = theme.accentPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
  }
}

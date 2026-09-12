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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.TelescopeMode
import com.example.omarchy.state.NeovimBuffer
import com.example.omarchy.ui.NeovimCommandPalette

@Composable
fun NeovimApp(
  theme: OmarchyThemeConfig,
  buffers: List<NeovimBuffer>,
  activeBufferIndex: Int,
  neovimMode: String,
  onSelectBuffer: (Int) -> Unit,
  onContentChange: (String) -> Unit,
  onModeChange: (String) -> Unit,
  onOpenFile: (String) -> Unit = {},
  onCloseBuffer: (Int) -> Unit = {},
  onExecuteCommand: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var isTelescopeOpen by remember { mutableStateOf(false) }
  var telescopeMode by remember { mutableStateOf(TelescopeMode.FIND_FILES) }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  val currentBuffer = buffers.getOrNull(activeBufferIndex) ?: NeovimBuffer("empty", "txt", "")
  val lines = currentBuffer.content.split("\n")
  val lineCount = lines.size

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(theme.terminalBackground)
    ) {
      // -------------------------------------------------------------
      // Top Buffer Tabs & Telescope Quick Action Bar
      // -------------------------------------------------------------
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(theme.surfaceColor)
          .border(0.5.dp, theme.surfaceBorder)
          .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Quick Telescope Button
        Box(
          modifier = Modifier
            .background(theme.accentPrimary.copy(alpha = 0.15f))
            .border(1.dp, theme.accentPrimary.copy(alpha = 0.6f))
            .clickable {
              telescopeMode = TelescopeMode.FIND_FILES
              isTelescopeOpen = true
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("nvim_telescope_btn"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              Icons.Default.Search,
              contentDescription = "Telescope Find Files",
              tint = theme.accentPrimary,
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = "󰍉 Find Files (C-p)",
              color = theme.accentPrimary,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Quick Buffers Button
        Box(
          modifier = Modifier
            .background(theme.surfaceVariant.copy(alpha = 0.6f))
            .border(0.5.dp, theme.surfaceBorder)
            .clickable {
              telescopeMode = TelescopeMode.BUFFERS
              isTelescopeOpen = true
            }
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("nvim_buffers_btn"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "󰈙 Buffers",
            color = theme.textSecondary,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        // Quick Commands Button
        Box(
          modifier = Modifier
            .background(theme.surfaceVariant.copy(alpha = 0.6f))
            .border(0.5.dp, theme.surfaceBorder)
            .clickable {
              telescopeMode = TelescopeMode.COMMANDS
              isTelescopeOpen = true
            }
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("nvim_commands_btn"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "󰘳 Cmds (:)",
            color = theme.textSecondary,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Open Buffer Tabs
        buffers.forEachIndexed { index, buf ->
          val isActive = index == activeBufferIndex
          val bg = if (isActive) theme.terminalBackground else theme.surfaceColor
          val border = if (isActive) theme.accentPrimary else Color.Transparent

          Row(
            modifier = Modifier
              .background(bg)
              .border(1.dp, border)
              .clickable { onSelectBuffer(index) }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("nvim_buffer_tab_$index"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Text(
              text = "${index + 1}: ${buf.filename}${if (buf.isModified) " [+]" else ""}",
              color = if (isActive) theme.accentPrimary else theme.textSecondary,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )

            // Close buffer button
            if (buffers.size > 1) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close buffer ${buf.filename}",
                tint = if (isActive) theme.accentPrimary else theme.textMuted,
                modifier = Modifier
                  .size(11.dp)
                  .clickable { onCloseBuffer(index) }
                  .testTag("nvim_close_buffer_$index")
              )
            }
          }
        }
      }

      // -------------------------------------------------------------
      // Code Canvas with Line Numbers
      // -------------------------------------------------------------
      Row(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Line numbers
        Column(
          modifier = Modifier
            .width(36.dp)
            .fillMaxHeight()
            .background(theme.surfaceColor.copy(alpha = 0.5f))
            .padding(vertical = 6.dp, horizontal = 4.dp),
          horizontalAlignment = Alignment.End
        ) {
          for (i in 1..maxOf(lineCount, 15)) {
            Text(
              text = "$i",
              color = theme.textMuted,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              lineHeight = 18.sp
            )
          }
        }

        // Code content
        Box(
          modifier = Modifier
            .weight(1f)
            .padding(6.dp)
        ) {
          BasicTextField(
            value = currentBuffer.content,
            onValueChange = {
              onContentChange(it)
              if (neovimMode != "INSERT") {
                onModeChange("INSERT")
              }
            },
            textStyle = TextStyle(
              color = theme.terminalForeground,
              fontSize = 11.5.sp,
              fontFamily = FontFamily.Monospace,
              lineHeight = 18.sp
            ),
            cursorBrush = SolidColor(theme.terminalGreen),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("neovim_editor_field")
          )
        }
      }

      // -------------------------------------------------------------
      // Neovim Lualine Status Bar
      // -------------------------------------------------------------
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(28.dp)
          .background(theme.surfaceColor)
          .border(0.5.dp, theme.surfaceBorder)
          .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left Mode & Buffer status
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(if (neovimMode == "NORMAL") theme.accentPrimary else theme.terminalGreen)
              .clickable {
                onModeChange(if (neovimMode == "NORMAL") "INSERT" else "NORMAL")
              }
              .padding(horizontal = 6.dp, vertical = 2.dp)
              .testTag("nvim_mode_toggle"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = neovimMode,
              color = Color.Black,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Black
            )
          }

          Text(
            text = "󰊢 main",
            color = theme.textSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )

          Text(
            text = currentBuffer.filename,
            color = theme.textPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )

          if (statusMessage != null) {
            Text(
              text = "• $statusMessage",
              color = theme.accentPrimary,
              fontSize = 9.5.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Right Stats & Telescope trigger
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Bottom Telescope Trigger Button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(theme.accentPrimary.copy(alpha = 0.18f))
              .border(0.5.dp, theme.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
              .clickable {
                telescopeMode = TelescopeMode.FIND_FILES
                isTelescopeOpen = true
              }
              .padding(horizontal = 6.dp, vertical = 2.dp)
              .testTag("nvim_status_telescope"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "󰍉 Telescope <C-p>",
              color = theme.accentPrimary,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          // Command line button (:)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(theme.surfaceVariant)
              .clickable {
                telescopeMode = TelescopeMode.COMMANDS
                isTelescopeOpen = true
              }
              .padding(horizontal = 5.dp, vertical = 2.dp)
              .testTag("nvim_cmdline_btn"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = ":",
              color = theme.terminalYellow,
              fontSize = 9.5.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "[${currentBuffer.language.uppercase()}]",
            color = theme.terminalCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "utf-8",
            color = theme.textMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Ln $lineCount",
            color = theme.textPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // -------------------------------------------------------------
    // Telescope Fuzzy File Finder & Command Palette Modal
    // -------------------------------------------------------------
    if (isTelescopeOpen) {
      NeovimCommandPalette(
        theme = theme,
        buffers = buffers,
        activeBufferIndex = activeBufferIndex,
        initialMode = telescopeMode,
        onOpenFile = { path ->
          onOpenFile(path)
          statusMessage = "󰍉 Opened '$path'"
          isTelescopeOpen = false
        },
        onSelectBuffer = { index ->
          onSelectBuffer(index)
          statusMessage = "Buffer ${index + 1} active"
          isTelescopeOpen = false
        },
        onCloseBuffer = { index ->
          onCloseBuffer(index)
        },
        onExecuteCommand = { cmd ->
          onExecuteCommand(cmd)
          statusMessage = "Executed $cmd"
          isTelescopeOpen = false
        },
        onClose = { isTelescopeOpen = false }
      )
    }
  }
}

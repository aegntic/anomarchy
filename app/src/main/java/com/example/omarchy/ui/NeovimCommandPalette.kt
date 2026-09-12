package com.example.omarchy.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.FileCategory
import com.example.omarchy.model.NeovimPaletteCommand
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.TelescopeMode
import com.example.omarchy.model.WorkspaceFileCatalog
import com.example.omarchy.model.WorkspaceProjectFile
import com.example.omarchy.state.NeovimBuffer

/**
 * Telescope-style Fuzzy File Finder and Command Palette for Neovim.
 * Provides rapid fuzzy navigation across project files, active editor buffers,
 * commands, and live file contents with real-time code preview.
 */
@Composable
fun NeovimCommandPalette(
  theme: OmarchyThemeConfig,
  buffers: List<NeovimBuffer>,
  activeBufferIndex: Int,
  initialQuery: String = "",
  initialMode: TelescopeMode = TelescopeMode.FIND_FILES,
  onOpenFile: (String) -> Unit,
  onSelectBuffer: (Int) -> Unit,
  onCloseBuffer: (Int) -> Unit = {},
  onExecuteCommand: (String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var query by remember { mutableStateOf(initialQuery) }
  var currentMode by remember { mutableStateOf(initialMode) }
  var selectedCategory by remember { mutableStateOf(FileCategory.ALL) }
  var selectedItemIndex by remember { mutableIntStateOf(0) }

  val allFiles = remember { WorkspaceFileCatalog.workspaceFiles }
  val allCommands = remember { WorkspaceFileCatalog.paletteCommands }

  // Fuzzy-filtered workspace files
  val filteredFiles by remember(query, selectedCategory) {
    derivedStateOf {
      val baseList = if (selectedCategory == FileCategory.ALL) {
        allFiles
      } else {
        allFiles.filter { it.category == selectedCategory }
      }

      if (query.isBlank()) {
        baseList.map { file ->
          Pair(file, WorkspaceFileCatalog.fuzzyMatch("", file.path))
        }
      } else {
        baseList.mapNotNull { file ->
          val fullMatch = WorkspaceFileCatalog.fuzzyMatch(query, file.path)
          val filenameMatch = WorkspaceFileCatalog.fuzzyMatch(query, file.filename)
          val bestScore = maxOf(fullMatch.score, filenameMatch.score + 15)
          if (fullMatch.isMatch || filenameMatch.isMatch) {
            Pair(
              file,
              fullMatch.copy(
                score = bestScore,
                matchedIndicesInPath = if (fullMatch.isMatch) fullMatch.matchedIndicesInPath else emptyList()
              )
            )
          } else null
        }.sortedByDescending { it.second.score }
      }
    }
  }

  // Filtered active buffers
  val filteredBuffers by remember(query, buffers) {
    derivedStateOf {
      if (query.isBlank()) {
        buffers.mapIndexed { index, buf -> Triple(index, buf, true) }
      } else {
        buffers.mapIndexedNotNull { index, buf ->
          val match = WorkspaceFileCatalog.fuzzyMatch(query, buf.filename)
          if (match.isMatch) Triple(index, buf, true) else null
        }
      }
    }
  }

  // Filtered commands
  val filteredCommands by remember(query) {
    derivedStateOf {
      if (query.isBlank()) {
        allCommands
      } else {
        allCommands.filter {
          it.command.contains(query, ignoreCase = true) ||
          it.description.contains(query, ignoreCase = true) ||
          it.category.contains(query, ignoreCase = true)
        }
      }
    }
  }

  // Live Grep results (searching in file contents)
  val liveGrepResults by remember(query) {
    derivedStateOf {
      if (query.length < 2) {
        emptyList()
      } else {
        val q = query.lowercase()
        val results = mutableListOf<GrepMatch>()
        allFiles.forEach { file ->
          file.content.lines().forEachIndexed { lineNum, lineText ->
            if (lineText.contains(q, ignoreCase = true)) {
              results.add(
                GrepMatch(
                  file = file,
                  lineNumber = lineNum + 1,
                  lineSnippet = lineText.trim()
                )
              )
            }
          }
        }
        results
      }
    }
  }

  // Active selected file for preview
  val selectedFile: WorkspaceProjectFile? by remember(currentMode, selectedItemIndex, filteredFiles, filteredBuffers, liveGrepResults) {
    derivedStateOf {
      when (currentMode) {
        TelescopeMode.FIND_FILES -> filteredFiles.getOrNull(selectedItemIndex)?.first
        TelescopeMode.BUFFERS -> {
          val buf = filteredBuffers.getOrNull(selectedItemIndex)?.second
          if (buf != null) {
            WorkspaceFileCatalog.getFileByPath(buf.filename) ?: WorkspaceProjectFile(
              path = buf.filename,
              filename = buf.filename.substringAfterLast('/'),
              directory = buf.filename.substringBeforeLast('/', "."),
              language = buf.language,
              category = FileCategory.ALL,
              content = buf.content
            )
          } else null
        }
        TelescopeMode.LIVE_GREP -> liveGrepResults.getOrNull(selectedItemIndex)?.file
        TelescopeMode.COMMANDS -> null
      }
    }
  }

  // Backdrop overlay
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.72f))
      .clickable { onClose() }
      .padding(horizontal = 14.dp, vertical = 20.dp),
    contentAlignment = Alignment.Center
  ) {
    // Main Telescope Modal Frame
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .clip(RoundedCornerShape(10.dp))
        .background(theme.surfaceColor)
        .border(1.5.dp, theme.accentPrimary, RoundedCornerShape(10.dp))
        .clickable(enabled = false) {}
        .testTag("telescope_modal")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // -------------------------------------------------------------
        // Header Bar: Telescope Title & Mode Selector
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(theme.terminalBackground)
            .border(0.5.dp, theme.surfaceBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(theme.accentPrimary.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "󰍉 TELESCOPE.NVIM",
                color = theme.accentPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
              )
            }

            Text(
              text = "Fuzzy Finder & Command Palette",
              color = theme.textSecondary,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          IconButton(
            onClick = onClose,
            modifier = Modifier
              .size(24.dp)
              .testTag("telescope_close_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close Telescope", tint = theme.textMuted, modifier = Modifier.size(16.dp))
          }
        }

        // -------------------------------------------------------------
        // Mode Selector Tabs (Files, Buffers, Commands, Live Grep)
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceVariant.copy(alpha = 0.5f))
            .border(0.5.dp, theme.surfaceBorder)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          TelescopeMode.entries.forEach { mode ->
            val isSelected = currentMode == mode
            val count = when (mode) {
              TelescopeMode.FIND_FILES -> filteredFiles.size
              TelescopeMode.BUFFERS -> filteredBuffers.size
              TelescopeMode.COMMANDS -> filteredCommands.size
              TelescopeMode.LIVE_GREP -> liveGrepResults.size
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) theme.accentPrimary else theme.surfaceColor)
                .border(
                  1.dp,
                  if (isSelected) theme.accentPrimary else theme.surfaceBorder,
                  RoundedCornerShape(6.dp)
                )
                .clickable {
                  currentMode = mode
                  selectedItemIndex = 0
                }
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("telescope_mode_${mode.name.lowercase()}"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "${mode.icon} ${mode.displayName}",
                  color = if (isSelected) Color.Black else theme.textPrimary,
                  fontSize = 10.5.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Box(
                  modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.Black.copy(alpha = 0.2f) else theme.surfaceVariant)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "$count",
                    color = if (isSelected) Color.Black else theme.textMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        // Category Filter Chips (Only shown in FIND_FILES mode)
        if (currentMode == TelescopeMode.FIND_FILES) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(theme.surfaceColor)
              .padding(horizontal = 10.dp, vertical = 4.dp)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            FileCategory.entries.forEach { cat ->
              val isCatActive = selectedCategory == cat
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (isCatActive) theme.accentSecondary.copy(alpha = 0.2f) else theme.surfaceVariant)
                  .border(
                    0.5.dp,
                    if (isCatActive) theme.accentSecondary else theme.surfaceBorder,
                    RoundedCornerShape(4.dp)
                  )
                  .clickable {
                    selectedCategory = cat
                    selectedItemIndex = 0
                  }
                  .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = cat.displayName,
                  color = if (isCatActive) theme.accentSecondary else theme.textSecondary,
                  fontSize = 9.5.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = if (isCatActive) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        }

        // -------------------------------------------------------------
        // Search Prompt Input Bar (Telescope Prompt: ❯ [input])
        // -------------------------------------------------------------
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(theme.terminalBackground)
            .border(1.dp, theme.surfaceBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = currentMode.commandPrompt,
              color = theme.terminalCyan,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )

            BasicTextField(
              value = query,
              onValueChange = {
                query = it
                selectedItemIndex = 0
              },
              textStyle = TextStyle(
                color = theme.terminalForeground,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
              ),
              cursorBrush = SolidColor(theme.terminalGreen),
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("telescope_input"),
              decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                  Text(
                    text = when (currentMode) {
                      TelescopeMode.FIND_FILES -> "Type to fuzzy filter files (e.g. article, schema, routes, .rb)..."
                      TelescopeMode.BUFFERS -> "Filter open active buffers..."
                      TelescopeMode.COMMANDS -> "Type command (e.g. :w, :q, split, Format, checkhealth)..."
                      TelescopeMode.LIVE_GREP -> "Search text across all files in workspace..."
                    },
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
                innerTextField()
              }
            )

            if (query.isNotEmpty()) {
              Icon(
                Icons.Default.Close,
                contentDescription = "Clear query",
                tint = theme.textMuted,
                modifier = Modifier
                  .size(16.dp)
                  .clickable { query = "" }
              )
            }
          }
        }

        // -------------------------------------------------------------
        // Dual Pane: Results (Left) + Live Preview (Right)
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          // ==============================
          // LEFT PANE: Filtered Results List
          // ==============================
          Box(
            modifier = Modifier
              .weight(1.1f)
              .fillMaxHeight()
              .background(theme.surfaceColor)
              .border(0.5.dp, theme.surfaceBorder)
          ) {
            when (currentMode) {
              TelescopeMode.FIND_FILES -> {
                if (filteredFiles.isEmpty()) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text("󰍉", fontSize = 28.sp, color = theme.textMuted)
                      Text("No workspace files matching '$query'", color = theme.textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                  }
                } else {
                  LazyColumn(
                    modifier = Modifier.fillMaxSize()
                  ) {
                    itemsIndexed(filteredFiles) { index, (file, match) ->
                      val isSelected = index == selectedItemIndex
                      val isOpen = buffers.any { it.filename == file.path }
                      val isCurrentActive = buffers.getOrNull(activeBufferIndex)?.filename == file.path

                      TelescopeFileRow(
                        theme = theme,
                        file = file,
                        matchedIndices = match.matchedIndicesInPath,
                        query = query,
                        isSelected = isSelected,
                        isOpen = isOpen,
                        isCurrentActive = isCurrentActive,
                        onClick = {
                          selectedItemIndex = index
                          onOpenFile(file.path)
                        },
                        onHover = { selectedItemIndex = index },
                        testTag = "telescope_item_$index"
                      )
                    }
                  }
                }
              }

              TelescopeMode.BUFFERS -> {
                if (filteredBuffers.isEmpty()) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("No active buffers match", color = theme.textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                  }
                } else {
                  LazyColumn(
                    modifier = Modifier.fillMaxSize()
                  ) {
                    itemsIndexed(filteredBuffers) { index, (bufIndex, buf, _) ->
                      val isSelected = index == selectedItemIndex
                      val isCurrentActive = bufIndex == activeBufferIndex

                      TelescopeBufferRow(
                        theme = theme,
                        bufferIndex = bufIndex,
                        buffer = buf,
                        isSelected = isSelected,
                        isCurrentActive = isCurrentActive,
                        onClick = {
                          selectedItemIndex = index
                          onSelectBuffer(bufIndex)
                          onClose()
                        },
                        onCloseBuffer = {
                          onCloseBuffer(bufIndex)
                        },
                        onHover = { selectedItemIndex = index },
                        testTag = "telescope_buffer_$index"
                      )
                    }
                  }
                }
              }

              TelescopeMode.COMMANDS -> {
                if (filteredCommands.isEmpty()) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("No commands found matching '$query'", color = theme.textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                  }
                } else {
                  LazyColumn(
                    modifier = Modifier.fillMaxSize()
                  ) {
                    itemsIndexed(filteredCommands) { index, cmd ->
                      val isSelected = index == selectedItemIndex

                      TelescopeCommandRow(
                        theme = theme,
                        command = cmd,
                        isSelected = isSelected,
                        onClick = {
                          onExecuteCommand(cmd.command)
                          onClose()
                        },
                        testTag = "telescope_cmd_$index"
                      )
                    }
                  }
                }
              }

              TelescopeMode.LIVE_GREP -> {
                if (query.length < 2) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("Type at least 2 characters to live grep...", color = theme.textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                  }
                } else if (liveGrepResults.isEmpty()) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("No grep matches found for '$query'", color = theme.textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                  }
                } else {
                  LazyColumn(
                    modifier = Modifier.fillMaxSize()
                  ) {
                    itemsIndexed(liveGrepResults) { index, match ->
                      val isSelected = index == selectedItemIndex

                      TelescopeGrepRow(
                        theme = theme,
                        match = match,
                        query = query,
                        isSelected = isSelected,
                        onClick = {
                          selectedItemIndex = index
                          onOpenFile(match.file.path)
                        },
                        onHover = { selectedItemIndex = index },
                        testTag = "telescope_grep_$index"
                      )
                    }
                  }
                }
              }
            }
          }

          // ==============================
          // RIGHT PANE: Live File Preview
          // ==============================
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .background(theme.terminalBackground)
              .border(0.5.dp, theme.surfaceBorder)
              .testTag("telescope_preview_pane")
          ) {
            if (selectedFile != null) {
              val previewFile = selectedFile!!
              Column(modifier = Modifier.fillMaxSize()) {
                // Preview header
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor)
                    .border(0.5.dp, theme.surfaceBorder)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(
                      imageVector = getFileIcon(previewFile.language),
                      contentDescription = null,
                      tint = getFileLanguageColor(previewFile.language, theme),
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = previewFile.filename,
                      color = theme.textPrimary,
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(getFileLanguageColor(previewFile.language, theme).copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = previewFile.language.uppercase(),
                        color = getFileLanguageColor(previewFile.language, theme),
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                      )
                    }

                    Text(
                      text = "${previewFile.lineCount}L",
                      color = theme.textMuted,
                      fontSize = 9.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }

                if (previewFile.description.isNotBlank()) {
                  Text(
                    text = previewFile.description,
                    color = theme.textSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(theme.surfaceVariant.copy(alpha = 0.4f))
                      .padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }

                // Code contents with line numbers
                Row(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                ) {
                  // Line numbers column
                  val lines = previewFile.content.lines()
                  Column(
                    modifier = Modifier
                      .width(28.dp)
                      .background(theme.surfaceColor.copy(alpha = 0.3f))
                      .padding(vertical = 4.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.End
                  ) {
                    lines.indices.forEach { i ->
                      Text(
                        text = "${i + 1}",
                        color = theme.textMuted,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                      )
                    }
                  }

                  // Code text
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .padding(horizontal = 6.dp, vertical = 4.dp)
                  ) {
                    Text(
                      text = previewFile.content,
                      color = theme.terminalForeground,
                      fontSize = 9.5.sp,
                      fontFamily = FontFamily.Monospace,
                      lineHeight = 14.sp
                    )
                  }
                }

                // Quick Open Button in preview footer
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor)
                    .border(0.5.dp, theme.surfaceBorder)
                    .clickable {
                      onOpenFile(previewFile.path)
                    }
                    .padding(vertical = 6.dp)
                    .testTag("telescope_open_button"),
                  contentAlignment = Alignment.Center
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Text(
                      text = "󰁔 Open '${previewFile.filename}' in Neovim",
                      color = theme.accentPrimary,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            } else {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text("󰈙", fontSize = 24.sp, color = theme.textMuted)
                  Text("No file selected for preview", color = theme.textMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
              }
            }
          }
        }

        // -------------------------------------------------------------
        // Footer: Keybindings Bar (Telescope Shortcuts)
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceColor)
            .border(0.5.dp, theme.surfaceBorder)
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            KeybindBadge(key = "<CR>", action = "Open", theme = theme)
            KeybindBadge(key = "<Tab>", action = "Mode", theme = theme)
            KeybindBadge(key = "<C-v>", action = "Split", theme = theme)
            KeybindBadge(key = "<Esc>", action = "Close", theme = theme)
          }

          Text(
            text = "Omarchy Neovim Workspace",
            color = theme.textMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------
// Component Rows
// -----------------------------------------------------------------------------

@Composable
private fun TelescopeFileRow(
  theme: OmarchyThemeConfig,
  file: WorkspaceProjectFile,
  matchedIndices: List<Int>,
  query: String,
  isSelected: Boolean,
  isOpen: Boolean,
  isCurrentActive: Boolean,
  onClick: () -> Unit,
  onHover: () -> Unit,
  testTag: String
) {
  val bg = if (isSelected) theme.accentPrimary.copy(alpha = 0.18f) else Color.Transparent
  val border = if (isSelected) theme.accentPrimary.copy(alpha = 0.5f) else Color.Transparent

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bg)
      .border(0.5.dp, border)
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(
        text = if (isSelected) "▶" else " ",
        color = theme.accentPrimary,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
      )

      Icon(
        imageVector = getFileIcon(file.language),
        contentDescription = null,
        tint = getFileLanguageColor(file.language, theme),
        modifier = Modifier.size(13.dp)
      )

      // Highlighted Path
      val annotatedString = buildAnnotatedString {
        file.path.forEachIndexed { idx, char ->
          if (idx in matchedIndices) {
            withStyle(
              style = SpanStyle(
                color = theme.accentPrimary,
                fontWeight = FontWeight.Bold
              )
            ) {
              append(char)
            }
          } else {
            append(char)
          }
        }
      }

      Text(
        text = annotatedString,
        color = if (isSelected) theme.textPrimary else theme.textSecondary,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        maxLines = 1
      )
    }

    // Status Badges
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isCurrentActive) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(theme.terminalGreen.copy(alpha = 0.2f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text("ACTIVE", color = theme.terminalGreen, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
      } else if (isOpen) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(theme.accentSecondary.copy(alpha = 0.2f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text("BUFFER", color = theme.accentSecondary, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
        }
      }

      Text(
        text = "${file.lineCount}L",
        color = theme.textMuted,
        fontSize = 8.5.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun TelescopeBufferRow(
  theme: OmarchyThemeConfig,
  bufferIndex: Int,
  buffer: NeovimBuffer,
  isSelected: Boolean,
  isCurrentActive: Boolean,
  onClick: () -> Unit,
  onCloseBuffer: () -> Unit,
  onHover: () -> Unit,
  testTag: String
) {
  val bg = if (isSelected) theme.accentPrimary.copy(alpha = 0.18f) else Color.Transparent

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bg)
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(
        text = "%${bufferIndex + 1}",
        color = theme.terminalCyan,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )

      Icon(
        imageVector = getFileIcon(buffer.language),
        contentDescription = null,
        tint = getFileLanguageColor(buffer.language, theme),
        modifier = Modifier.size(13.dp)
      )

      Text(
        text = buffer.filename,
        color = if (isCurrentActive) theme.accentPrimary else theme.textPrimary,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = if (isCurrentActive) FontWeight.Bold else FontWeight.Normal
      )

      if (buffer.isModified) {
        Text("[+]", color = theme.terminalRed, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (isCurrentActive) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(theme.terminalGreen.copy(alpha = 0.2f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text("CURRENT", color = theme.terminalGreen, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
      }

      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close buffer",
        tint = theme.textMuted,
        modifier = Modifier
          .size(14.dp)
          .clickable { onCloseBuffer() }
      )
    }
  }
}

@Composable
private fun TelescopeCommandRow(
  theme: OmarchyThemeConfig,
  command: NeovimPaletteCommand,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  val bg = if (isSelected) theme.accentPrimary.copy(alpha = 0.18f) else Color.Transparent

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bg)
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 7.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = command.command,
          color = theme.accentPrimary,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "• ${command.category}",
          color = theme.textMuted,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }
      Text(
        text = command.description,
        color = theme.textSecondary,
        fontSize = 9.5.sp,
        fontFamily = FontFamily.Monospace
      )
    }

    if (command.shortcut != null) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(theme.surfaceVariant)
          .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(4.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = command.shortcut,
          color = theme.terminalCyan,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
private fun TelescopeGrepRow(
  theme: OmarchyThemeConfig,
  match: GrepMatch,
  query: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  onHover: () -> Unit,
  testTag: String
) {
  val bg = if (isSelected) theme.accentPrimary.copy(alpha = 0.18f) else Color.Transparent

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(bg)
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag(testTag),
    verticalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(
        text = match.file.path,
        color = theme.terminalCyan,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = ":${match.lineNumber}",
        color = theme.terminalGreen,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )
    }

    Text(
      text = match.lineSnippet,
      color = theme.textSecondary,
      fontSize = 9.5.sp,
      fontFamily = FontFamily.Monospace,
      maxLines = 1
    )
  }
}

@Composable
private fun KeybindBadge(key: String, action: String, theme: OmarchyThemeConfig) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(3.dp))
        .background(theme.surfaceVariant)
        .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(3.dp))
        .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
      Text(
        text = key,
        color = theme.terminalCyan,
        fontSize = 8.5.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
    }
    Text(
      text = action,
      color = theme.textMuted,
      fontSize = 8.5.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

data class GrepMatch(
  val file: WorkspaceProjectFile,
  val lineNumber: Int,
  val lineSnippet: String
)

fun getFileIcon(language: String): ImageVector {
  return when (language.lowercase()) {
    "ruby", "rb" -> Icons.Default.Code
    "lua" -> Icons.Default.Settings
    "conf", "toml" -> Icons.Default.Settings
    "html", "erb" -> Icons.Default.Code
    "yaml", "yml" -> Icons.Default.Folder
    "markdown", "md", "txt" -> Icons.Default.Description
    "sh", "zsh", "bash" -> Icons.Default.Terminal
    else -> Icons.Default.Code
  }
}

fun getFileLanguageColor(language: String, theme: OmarchyThemeConfig): Color {
  return when (language.lowercase()) {
    "ruby", "rb" -> theme.terminalRed
    "lua" -> theme.terminalCyan
    "conf", "toml" -> theme.terminalYellow
    "html", "erb" -> theme.accentSecondary
    "yaml", "yml" -> theme.terminalYellow
    "markdown", "md", "txt" -> theme.terminalGreen
    "sh", "zsh", "bash" -> theme.terminalCyan
    else -> theme.accentPrimary
  }
}

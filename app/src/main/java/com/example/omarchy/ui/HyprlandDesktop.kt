package com.example.omarchy.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.apps.AgenticConsoleApp
import com.example.omarchy.apps.BrowserApp
import com.example.omarchy.apps.BtopApp
import com.example.omarchy.apps.NeovimApp
import com.example.omarchy.apps.ObsidianApp
import com.example.omarchy.apps.TerminalApp
import com.example.omarchy.model.AppType
import com.example.omarchy.model.NoteItem
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.ProcessItem
import com.example.omarchy.model.WindowInstance
import com.example.omarchy.model.WindowLayoutMode
import com.example.omarchy.model.TilingControllerState
import com.example.omarchy.model.TilingSplitPreset
import com.example.omarchy.model.TilingZoneType
import com.example.omarchy.model.SplitRatioPreset
import com.example.omarchy.state.AgentMessage
import com.example.omarchy.state.NeovimBuffer
import com.example.omarchy.state.TerminalLine

@Composable
fun HyprlandDesktop(
  theme: OmarchyThemeConfig,
  currentWorkspace: Int,
  windows: List<WindowInstance>,
  focusedWindowId: String?,
  layoutMode: WindowLayoutMode,
  isFullscreen: Boolean,
  terminalLines: List<TerminalLine>,
  neovimBuffers: List<NeovimBuffer>,
  activeBufferIndex: Int,
  neovimMode: String,
  notes: List<NoteItem>,
  activeNoteId: String?,
  isObsidianPreview: Boolean,
  cpuPercent: Float,
  memPercent: Float,
  cpuHistory: List<Float>,
  processes: List<ProcessItem>,
  agentMessages: List<AgentMessage>,
  isAgentThinking: Boolean,
  browserUrl: String,
  onFocusWindow: (String) -> Unit,
  onCloseWindow: (String) -> Unit,
  onToggleFullscreen: () -> Unit,
  onToggleFloating: (String) -> Unit,
  onOpenApp: (AppType) -> Unit,
  onOpenWalker: () -> Unit,
  onExecuteTerminalCommand: (String) -> Unit,
  onSelectNeovimBuffer: (Int) -> Unit,
  onUpdateNeovimContent: (String) -> Unit,
  onSetNeovimMode: (String) -> Unit,
  onOpenFileInNeovim: (String) -> Unit = {},
  onCloseNeovimBuffer: (Int) -> Unit = {},
  onExecuteNeovimCommand: (String) -> Unit = {},
  onSelectNote: (String) -> Unit,
  onUpdateNoteContent: (String) -> Unit,
  onCreateNote: () -> Unit,
  onToggleObsidianPreview: () -> Unit,
  onKillProcess: (Int) -> Unit,
  onSendAgentPrompt: (String) -> Unit,
  onNavigateBrowser: (String) -> Unit,
  tilingControllerState: TilingControllerState = TilingControllerState(),
  onSelectTilingPreset: (TilingSplitPreset) -> Unit = {},
  onSelectTilingRatio: (SplitRatioPreset) -> Unit = {},
  onToggleTilingZone: (TilingZoneType) -> Unit = {},
  onToggleTilingZoneMaximized: (TilingZoneType) -> Unit = {},
  onSwapTilingZones: () -> Unit = {},
  onSpawnDevTriad: () -> Unit = {},
  onToggleTilingControllerExpanded: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val workspaceWindows = windows.filter { it.workspaceId == currentWorkspace }

  Box(modifier = modifier.fillMaxSize()) {
    // Wallpaper Background with procedural subtle grid
    DesktopWallpaper(theme = theme)

    if (layoutMode == WindowLayoutMode.TRI_ZONE_DEV) {
      // Dedicated Dev Tri-Zone Tiling Cockpit (Terminal, Editor, Assistant)
      Column(modifier = Modifier.fillMaxSize()) {
        TilingLayoutControllerBar(
          state = tilingControllerState,
          theme = theme,
          onSelectPreset = onSelectTilingPreset,
          onSelectRatio = onSelectTilingRatio,
          onToggleZone = onToggleTilingZone,
          onSwapZones = onSwapTilingZones,
          onSpawnDevTriad = onSpawnDevTriad,
          onToggleExpanded = onToggleTilingControllerExpanded
        )

        TriZoneTilingLayout(
          state = tilingControllerState,
          theme = theme,
          terminalLines = terminalLines,
          neovimBuffers = neovimBuffers,
          activeBufferIndex = activeBufferIndex,
          neovimMode = neovimMode,
          agentMessages = agentMessages,
          isAgentThinking = isAgentThinking,
          onFocusZone = { z ->
            val matched = workspaceWindows.firstOrNull { it.appType == z.defaultAppType }
            if (matched != null) onFocusWindow(matched.id)
          },
          onToggleMaximizeZone = onToggleTilingZoneMaximized,
          onCloseZone = onToggleTilingZone,
          onExecuteTerminalCommand = onExecuteTerminalCommand,
          onSelectNeovimBuffer = onSelectNeovimBuffer,
          onUpdateNeovimContent = onUpdateNeovimContent,
          onSetNeovimMode = onSetNeovimMode,
          onOpenFileInNeovim = onOpenFileInNeovim,
          onCloseNeovimBuffer = onCloseNeovimBuffer,
          onExecuteNeovimCommand = onExecuteNeovimCommand,
          onSendAgentPrompt = onSendAgentPrompt,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        )
      }
    } else if (workspaceWindows.isEmpty()) {
      // Empty workspace welcome screen
      EmptyWorkspaceView(
        theme = theme,
        workspaceId = currentWorkspace,
        onOpenApp = onOpenApp,
        onOpenWalker = onOpenWalker,
        onSpawnDevTriad = onSpawnDevTriad
      )
    } else if (isFullscreen && focusedWindowId != null) {
      val fullWin = workspaceWindows.firstOrNull { it.id == focusedWindowId } ?: workspaceWindows.first()
      WindowFrame(
        window = fullWin,
        theme = theme,
        isFocused = true,
        modifier = Modifier
          .fillMaxSize()
          .padding(4.dp),
        onFocus = { onFocusWindow(fullWin.id) },
        onClose = { onCloseWindow(fullWin.id) },
        onToggleFullscreen = onToggleFullscreen,
        onToggleFloating = { onToggleFloating(fullWin.id) }
      ) {
        WindowContent(
          appType = fullWin.appType,
          theme = theme,
          terminalLines = terminalLines,
          neovimBuffers = neovimBuffers,
          activeBufferIndex = activeBufferIndex,
          neovimMode = neovimMode,
          notes = notes,
          activeNoteId = activeNoteId,
          isObsidianPreview = isObsidianPreview,
          cpuPercent = cpuPercent,
          memPercent = memPercent,
          cpuHistory = cpuHistory,
          processes = processes,
          agentMessages = agentMessages,
          isAgentThinking = isAgentThinking,
          browserUrl = browserUrl,
          onExecuteTerminalCommand = onExecuteTerminalCommand,
          onSelectNeovimBuffer = onSelectNeovimBuffer,
          onUpdateNeovimContent = onUpdateNeovimContent,
          onSetNeovimMode = onSetNeovimMode,
          onOpenFileInNeovim = onOpenFileInNeovim,
          onCloseNeovimBuffer = onCloseNeovimBuffer,
          onExecuteNeovimCommand = onExecuteNeovimCommand,
          onSelectNote = onSelectNote,
          onUpdateNoteContent = onUpdateNoteContent,
          onCreateNote = onCreateNote,
          onToggleObsidianPreview = onToggleObsidianPreview,
          onKillProcess = onKillProcess,
          onSendAgentPrompt = onSendAgentPrompt,
          onNavigateBrowser = onNavigateBrowser
        )
      }
    } else {
      // Tiled Windows according to layoutMode
      TiledWindowContainer(
        windows = workspaceWindows,
        focusedWindowId = focusedWindowId,
        layoutMode = layoutMode,
        theme = theme,
        onFocusWindow = onFocusWindow,
        onCloseWindow = onCloseWindow,
        onToggleFullscreen = onToggleFullscreen,
        onToggleFloating = onToggleFloating,
        renderContent = { win ->
          WindowContent(
            appType = win.appType,
            theme = theme,
            terminalLines = terminalLines,
            neovimBuffers = neovimBuffers,
            activeBufferIndex = activeBufferIndex,
            neovimMode = neovimMode,
            notes = notes,
            activeNoteId = activeNoteId,
            isObsidianPreview = isObsidianPreview,
            cpuPercent = cpuPercent,
            memPercent = memPercent,
            cpuHistory = cpuHistory,
            processes = processes,
            agentMessages = agentMessages,
            isAgentThinking = isAgentThinking,
            browserUrl = browserUrl,
            onExecuteTerminalCommand = onExecuteTerminalCommand,
            onSelectNeovimBuffer = onSelectNeovimBuffer,
            onUpdateNeovimContent = onUpdateNeovimContent,
            onSetNeovimMode = onSetNeovimMode,
            onOpenFileInNeovim = onOpenFileInNeovim,
            onCloseNeovimBuffer = onCloseNeovimBuffer,
            onExecuteNeovimCommand = onExecuteNeovimCommand,
            onSelectNote = onSelectNote,
            onUpdateNoteContent = onUpdateNoteContent,
            onCreateNote = onCreateNote,
            onToggleObsidianPreview = onToggleObsidianPreview,
            onKillProcess = onKillProcess,
            onSendAgentPrompt = onSendAgentPrompt,
            onNavigateBrowser = onNavigateBrowser
          )
        }
      )
    }
  }
}

@Composable
private fun TiledWindowContainer(
  windows: List<WindowInstance>,
  focusedWindowId: String?,
  layoutMode: WindowLayoutMode,
  theme: OmarchyThemeConfig,
  onFocusWindow: (String) -> Unit,
  onCloseWindow: (String) -> Unit,
  onToggleFullscreen: () -> Unit,
  onToggleFloating: (String) -> Unit,
  renderContent: @Composable (WindowInstance) -> Unit
) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .padding(6.dp)
  ) {
    if (windows.size == 1) {
      val win = windows.first()
      WindowFrame(
        window = win,
        theme = theme,
        isFocused = win.id == focusedWindowId,
        modifier = Modifier.fillMaxSize(),
        onFocus = { onFocusWindow(win.id) },
        onClose = { onCloseWindow(win.id) },
        onToggleFullscreen = onToggleFullscreen,
        onToggleFloating = { onToggleFloating(win.id) }
      ) {
        renderContent(win)
      }
    } else {
      when (layoutMode) {
        WindowLayoutMode.SPLIT_HORIZONTAL -> {
          // Horizontal side by side
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            windows.forEach { win ->
              WindowFrame(
                window = win,
                theme = theme,
                isFocused = win.id == focusedWindowId,
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                onFocus = { onFocusWindow(win.id) },
                onClose = { onCloseWindow(win.id) },
                onToggleFullscreen = onToggleFullscreen,
                onToggleFloating = { onToggleFloating(win.id) }
              ) {
                renderContent(win)
              }
            }
          }
        }
        WindowLayoutMode.SPLIT_VERTICAL -> {
          // Vertical stacked
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            windows.forEach { win ->
              WindowFrame(
                window = win,
                theme = theme,
                isFocused = win.id == focusedWindowId,
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth(),
                onFocus = { onFocusWindow(win.id) },
                onClose = { onCloseWindow(win.id) },
                onToggleFullscreen = onToggleFullscreen,
                onToggleFloating = { onToggleFloating(win.id) }
              ) {
                renderContent(win)
              }
            }
          }
        }
        WindowLayoutMode.MASTER_STACK, WindowLayoutMode.FLOATING, WindowLayoutMode.FULLSCREEN, WindowLayoutMode.TRI_ZONE_DEV -> {
          // Master window (first) + stacked side
          val master = windows.first()
          val stack = windows.drop(1)

          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            WindowFrame(
              window = master,
              theme = theme,
              isFocused = master.id == focusedWindowId,
              modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth(),
              onFocus = { onFocusWindow(master.id) },
              onClose = { onCloseWindow(master.id) },
              onToggleFullscreen = onToggleFullscreen,
              onToggleFloating = { onToggleFloating(master.id) }
            ) {
              renderContent(master)
            }

            if (stack.isNotEmpty()) {
              Row(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                stack.forEach { win ->
                  WindowFrame(
                    window = win,
                    theme = theme,
                    isFocused = win.id == focusedWindowId,
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxHeight(),
                    onFocus = { onFocusWindow(win.id) },
                    onClose = { onCloseWindow(win.id) },
                    onToggleFullscreen = onToggleFullscreen,
                    onToggleFloating = { onToggleFloating(win.id) }
                  ) {
                    renderContent(win)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun WindowFrame(
  window: WindowInstance,
  theme: OmarchyThemeConfig,
  isFocused: Boolean,
  onFocus: () -> Unit,
  onClose: () -> Unit,
  onToggleFullscreen: () -> Unit,
  onToggleFloating: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val borderWidth by animateDpAsState(if (isFocused) 2.dp else 1.dp, label = "border_w")
  val cornerRadius = 10.dp

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(cornerRadius))
      .then(
        if (isFocused) {
          Modifier.border(
            width = borderWidth,
            brush = theme.activeBorderBrush,
            shape = RoundedCornerShape(cornerRadius)
          )
        } else {
          Modifier.border(
            width = borderWidth,
            color = theme.inactiveBorderColor,
            shape = RoundedCornerShape(cornerRadius)
          )
        }
      )
      .clickable { onFocus() }
      .testTag("window_${window.id}")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Hyprland Window Titlebar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(28.dp)
          .background(if (isFocused) theme.surfaceVariant else theme.surfaceColor)
          .border(
            0.5.dp,
            if (isFocused) theme.accentPrimary.copy(alpha = 0.3f) else theme.surfaceBorder
          )
          .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // App title & icon
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          modifier = Modifier.weight(1f, fill = false)
        ) {
          Text(
            text = theme.getAppGlyph(window.appType),
            color = if (isFocused) theme.accentPrimary else theme.textSecondary,
            fontSize = 11.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = window.title,
            color = if (isFocused) theme.textPrimary else theme.textSecondary,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Window Controls (Floating, Fullscreen, Close)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Toggle layout / float
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(theme.surfaceColor)
              .clickable { onToggleFloating() }
              .testTag("window_float_${window.id}"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.GridView,
              contentDescription = "Tile/Float",
              tint = theme.textMuted,
              modifier = Modifier.size(10.dp)
            )
          }

          // Fullscreen toggle
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(theme.surfaceColor)
              .clickable { onToggleFullscreen() }
              .testTag("window_fullscreen_${window.id}"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.OpenInFull,
              contentDescription = "Fullscreen",
              tint = theme.terminalCyan,
              modifier = Modifier.size(10.dp)
            )
          }

          // Close button
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(theme.terminalRed.copy(alpha = 0.2f))
              .clickable { onClose() }
              .testTag("window_close_${window.id}"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = theme.terminalRed,
              modifier = Modifier.size(10.dp)
            )
          }
        }
      }

      // App Content
      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        content()
      }
    }
  }
}

@Composable
private fun WindowContent(
  appType: AppType,
  theme: OmarchyThemeConfig,
  terminalLines: List<TerminalLine>,
  neovimBuffers: List<NeovimBuffer>,
  activeBufferIndex: Int,
  neovimMode: String,
  notes: List<NoteItem>,
  activeNoteId: String?,
  isObsidianPreview: Boolean,
  cpuPercent: Float,
  memPercent: Float,
  cpuHistory: List<Float>,
  processes: List<ProcessItem>,
  agentMessages: List<AgentMessage>,
  isAgentThinking: Boolean,
  browserUrl: String,
  onExecuteTerminalCommand: (String) -> Unit,
  onSelectNeovimBuffer: (Int) -> Unit,
  onUpdateNeovimContent: (String) -> Unit,
  onSetNeovimMode: (String) -> Unit,
  onOpenFileInNeovim: (String) -> Unit = {},
  onCloseNeovimBuffer: (Int) -> Unit = {},
  onExecuteNeovimCommand: (String) -> Unit = {},
  onSelectNote: (String) -> Unit,
  onUpdateNoteContent: (String) -> Unit,
  onCreateNote: () -> Unit,
  onToggleObsidianPreview: () -> Unit,
  onKillProcess: (Int) -> Unit,
  onSendAgentPrompt: (String) -> Unit,
  onNavigateBrowser: (String) -> Unit
) {
  when (appType) {
    AppType.TERMINAL -> TerminalApp(
      theme = theme,
      lines = terminalLines,
      onExecuteCommand = onExecuteTerminalCommand
    )
    AppType.NEOVIM -> NeovimApp(
      theme = theme,
      buffers = neovimBuffers,
      activeBufferIndex = activeBufferIndex,
      neovimMode = neovimMode,
      onSelectBuffer = onSelectNeovimBuffer,
      onContentChange = onUpdateNeovimContent,
      onModeChange = onSetNeovimMode,
      onOpenFile = onOpenFileInNeovim,
      onCloseBuffer = onCloseNeovimBuffer,
      onExecuteCommand = onExecuteNeovimCommand
    )
    AppType.OBSIDIAN -> ObsidianApp(
      theme = theme,
      notes = notes,
      activeNoteId = activeNoteId,
      isPreviewMode = isObsidianPreview,
      onSelectNote = onSelectNote,
      onContentChange = onUpdateNoteContent,
      onCreateNote = onCreateNote,
      onTogglePreview = onToggleObsidianPreview
    )
    AppType.BTOP -> BtopApp(
      theme = theme,
      cpuPercent = cpuPercent,
      memPercent = memPercent,
      cpuHistory = cpuHistory,
      processes = processes,
      onKillProcess = onKillProcess
    )
    AppType.AGENT -> AgenticConsoleApp(
      theme = theme,
      messages = agentMessages,
      isThinking = isAgentThinking,
      onSendMessage = onSendAgentPrompt
    )
    AppType.BROWSER -> BrowserApp(
      theme = theme,
      currentUrl = browserUrl,
      onNavigate = onNavigateBrowser
    )
  }
}

@Composable
private fun DesktopWallpaper(theme: OmarchyThemeConfig) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    // Gradient Background
    drawRect(
      brush = Brush.radialGradient(
        colors = listOf(theme.desktopSecondary, theme.desktopBackground),
        center = Offset(size.width / 2, size.height / 3),
        radius = size.width.coerceAtLeast(size.height)
      )
    )

    // Subtle decorative grid dots
    val dotSpacing = 32.dp.toPx()
    val dotRadius = 1.dp.toPx()
    val dotColor = theme.surfaceBorder.copy(alpha = 0.4f)

    var x = dotSpacing / 2
    while (x < size.width) {
      var y = dotSpacing / 2
      while (y < size.height) {
        drawCircle(
          color = dotColor,
          radius = dotRadius,
          center = Offset(x, y)
        )
        y += dotSpacing
      }
      x += dotSpacing
    }
  }
}

@Composable
private fun EmptyWorkspaceView(
  theme: OmarchyThemeConfig,
  workspaceId: Int,
  onOpenApp: (AppType) -> Unit,
  onOpenWalker: () -> Unit,
  onSpawnDevTriad: () -> Unit = {}
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Arch/Omarchy logo emblem
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(theme.surfaceColor)
        .border(1.5.dp, theme.accentPrimary, RoundedCornerShape(16.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "󰣇",
        color = theme.accentPrimary,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "Workspace $workspaceId  ${theme.getWorkspaceGlyph(workspaceId)}",
      color = theme.textPrimary,
      fontSize = 18.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )

    Text(
      text = "No active Hyprland windows in this workspace",
      color = theme.textSecondary,
      fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { onOpenApp(AppType.TERMINAL) },
        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, theme.terminalGreen, RoundedCornerShape(6.dp))
      ) {
        Text("${theme.getAppGlyph(AppType.TERMINAL)} Terminal", color = theme.terminalGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }

      Button(
        onClick = { onOpenWalker() },
        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, theme.accentPrimary, RoundedCornerShape(6.dp))
      ) {
        Text("󰍉 Launcher", color = theme.accentPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }

      Button(
        onClick = { onOpenApp(AppType.AGENT) },
        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, theme.accentSecondary, RoundedCornerShape(6.dp))
      ) {
        Text("${theme.getAppGlyph(AppType.AGENT)} Agent", color = theme.accentSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }

      Button(
        onClick = onSpawnDevTriad,
        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, theme.terminalCyan, RoundedCornerShape(6.dp))
      ) {
        Text("${theme.getAppGlyph(AppType.NEOVIM)} Cockpit", color = theme.terminalCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }
    }
  }
}

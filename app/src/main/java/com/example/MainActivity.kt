package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omarchy.model.AppType
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.state.OmarchyViewModel
import com.example.omarchy.ui.ControlCenterModal
import com.example.omarchy.ui.HyprlandDesktop
import com.example.omarchy.ui.KeyboardShortcutsHud
import com.example.omarchy.ui.KeybindsCheatSheet
import com.example.omarchy.ui.MacroManagerModal
import com.example.omarchy.ui.QuickshellBar
import com.example.omarchy.ui.SuperDock
import com.example.omarchy.ui.WalkerLauncher
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: OmarchyViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        OmarchyApp(viewModel = viewModel)
      }
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    // Physical hardware keyboard shortcut listener for Hyprland experience
    val isMeta = event?.isMetaPressed == true || event?.isAltPressed == true
    val isShift = event?.isShiftPressed == true
    val isCtrl = event?.isCtrlPressed == true
    val isAlt = event?.isAltPressed == true

    // Check user-defined and preset command macros matching this key event!
    val matchedMacro = viewModel.macros.value.firstOrNull { macro ->
      macro.keyCode == keyCode &&
        (!macro.requiresSuper || isMeta) &&
        (!macro.requiresShift || isShift) &&
        (!macro.requiresCtrl || isCtrl) &&
        (!macro.requiresAlt || isAlt)
    }
    if (matchedMacro != null) {
      viewModel.playMacro(matchedMacro)
      return true
    }

    if (isMeta) {
      when (keyCode) {
        KeyEvent.KEYCODE_ENTER -> {
          viewModel.openApp(AppType.TERMINAL)
          return true
        }
        KeyEvent.KEYCODE_SPACE -> {
          viewModel.openWalker()
          return true
        }
        KeyEvent.KEYCODE_Q -> {
          viewModel.closeFocusedWindow()
          return true
        }
        KeyEvent.KEYCODE_F -> {
          viewModel.toggleFullscreen()
          return true
        }
        KeyEvent.KEYCODE_V -> {
          viewModel.toggleLayoutMode()
          return true
        }
        KeyEvent.KEYCODE_H -> {
          viewModel.toggleHud()
          return true
        }
        KeyEvent.KEYCODE_M -> {
          viewModel.toggleMacroManager()
          return true
        }
        KeyEvent.KEYCODE_1 -> { viewModel.switchWorkspace(1); return true }
        KeyEvent.KEYCODE_2 -> { viewModel.switchWorkspace(2); return true }
        KeyEvent.KEYCODE_3 -> { viewModel.switchWorkspace(3); return true }
        KeyEvent.KEYCODE_4 -> { viewModel.switchWorkspace(4); return true }
        KeyEvent.KEYCODE_5 -> { viewModel.switchWorkspace(5); return true }
      }
    }
    return super.onKeyDown(keyCode, event)
  }
}

@Composable
fun OmarchyApp(viewModel: OmarchyViewModel) {
  val theme by viewModel.themeConfig.collectAsStateWithLifecycle()
  val currentWorkspace by viewModel.currentWorkspace.collectAsStateWithLifecycle()
  val windows by viewModel.windows.collectAsStateWithLifecycle()
  val focusedWindowId by viewModel.focusedWindowId.collectAsStateWithLifecycle()
  val layoutMode by viewModel.layoutMode.collectAsStateWithLifecycle()
  val isFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()

  val isWalkerOpen by viewModel.isWalkerOpen.collectAsStateWithLifecycle()
  val walkerQuery by viewModel.walkerQuery.collectAsStateWithLifecycle()
  val isControlCenterOpen by viewModel.isControlCenterOpen.collectAsStateWithLifecycle()
  val isKeybindsOpen by viewModel.isKeybindsOpen.collectAsStateWithLifecycle()
  val isHudOpen by viewModel.isHudOpen.collectAsStateWithLifecycle()
  val isHudCollapsed by viewModel.isHudCollapsed.collectAsStateWithLifecycle()
  val tilingControllerState by viewModel.tilingControllerState.collectAsStateWithLifecycle()

  val isPersisted by viewModel.isPersisted.collectAsStateWithLifecycle()
  val lastSavedTime by viewModel.lastSavedTime.collectAsStateWithLifecycle()
  val persistenceStatus by viewModel.persistenceStatus.collectAsStateWithLifecycle()

  val cpuPercent by viewModel.cpuPercent.collectAsStateWithLifecycle()
  val memPercent by viewModel.memPercent.collectAsStateWithLifecycle()
  val cpuHistory by viewModel.cpuHistory.collectAsStateWithLifecycle()
  val processes by viewModel.processes.collectAsStateWithLifecycle()

  val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()

  val neovimBuffers by viewModel.neovimBuffers.collectAsStateWithLifecycle()
  val activeBufferIndex by viewModel.activeBufferIndex.collectAsStateWithLifecycle()
  val neovimMode by viewModel.neovimMode.collectAsStateWithLifecycle()

  val notes by viewModel.notes.collectAsStateWithLifecycle()
  val activeNoteId by viewModel.activeNoteId.collectAsStateWithLifecycle()
  val isObsidianPreview by viewModel.isObsidianPreview.collectAsStateWithLifecycle()

  val agentMessages by viewModel.agentMessages.collectAsStateWithLifecycle()
  val isAgentThinking by viewModel.isAgentThinking.collectAsStateWithLifecycle()

  val browserUrl by viewModel.browserUrl.collectAsStateWithLifecycle()

  val macros by viewModel.macros.collectAsStateWithLifecycle()
  val isRecordingMacro by viewModel.isRecordingMacro.collectAsStateWithLifecycle()
  val recordedActions by viewModel.recordedActions.collectAsStateWithLifecycle()
  val recordingSeconds by viewModel.recordingSeconds.collectAsStateWithLifecycle()
  val recordingMacroName by viewModel.recordingMacroName.collectAsStateWithLifecycle()
  val isMacroManagerOpen by viewModel.isMacroManagerOpen.collectAsStateWithLifecycle()
  val macroExecutionProgress by viewModel.macroExecutionProgress.collectAsStateWithLifecycle()
  val macroNotification by viewModel.macroNotification.collectAsStateWithLifecycle()

  val focusedWindow = windows.firstOrNull { it.id == focusedWindowId }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(theme.desktopBackground)
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = theme.desktopBackground,
      topBar = {
        QuickshellBar(
          theme = theme,
          currentWorkspace = currentWorkspace,
          windows = windows,
          focusedWindow = focusedWindow,
          layoutMode = layoutMode,
          cpuPercent = cpuPercent,
          memPercent = memPercent,
          isHudOpen = isHudOpen,
          cpuHistory = cpuHistory,
          onWorkspaceSelect = { viewModel.switchWorkspace(it) },
          onToggleControlCenter = { viewModel.toggleControlCenter() },
          onToggleKeybinds = { viewModel.toggleKeybinds() },
          onToggleHud = { viewModel.toggleHud() },
          onOpenWalker = { viewModel.openWalker() },
          onToggleLayoutMode = { viewModel.toggleLayoutMode() },
          onLaunchBtop = { viewModel.openApp(AppType.BTOP) }
        )
      },
      bottomBar = {
        SuperDock(
          theme = theme,
          onSuperKey = { viewModel.openWalker() },
          onTerminalKey = { viewModel.openApp(AppType.TERMINAL) },
          onKillKey = { viewModel.closeFocusedWindow() },
          onFullscreenKey = { viewModel.toggleFullscreen() },
          onSplitKey = { viewModel.toggleLayoutMode() },
          onThemeKey = { viewModel.toggleControlCenter() },
          onHudKey = { viewModel.toggleHud() },
          isHudOpen = isHudOpen,
          onMacroKey = { viewModel.toggleMacroManager() },
          isRecordingMacro = isRecordingMacro,
          onLaunchApp = { viewModel.openApp(it) }
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        HyprlandDesktop(
          theme = theme,
          currentWorkspace = currentWorkspace,
          windows = windows,
          focusedWindowId = focusedWindowId,
          layoutMode = layoutMode,
          isFullscreen = isFullscreen,
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
          onFocusWindow = { viewModel.focusWindow(it) },
          onCloseWindow = { viewModel.closeWindow(it) },
          onToggleFullscreen = { viewModel.toggleFullscreen() },
          onToggleFloating = { viewModel.toggleFloating(it) },
          onOpenApp = { viewModel.openApp(it) },
          onOpenWalker = { viewModel.openWalker() },
          onExecuteTerminalCommand = { viewModel.executeTerminalCommand(it) },
          onSelectNeovimBuffer = { viewModel.selectBuffer(it) },
          onUpdateNeovimContent = { viewModel.updateNeovimContent(it) },
          onSetNeovimMode = { viewModel.setNeovimMode(it) },
          onOpenFileInNeovim = { viewModel.openFileInNeovim(it) },
          onCloseNeovimBuffer = { viewModel.closeBuffer(it) },
          onExecuteNeovimCommand = { viewModel.executeNeovimCommand(it) },
          onSelectNote = { viewModel.selectNote(it) },
          onUpdateNoteContent = { viewModel.updateNoteContent(it) },
          onCreateNote = { viewModel.createNewNote() },
          onToggleObsidianPreview = { viewModel.toggleObsidianPreview() },
          onKillProcess = { viewModel.killProcess(it) },
          onSendAgentPrompt = { viewModel.sendAgentPrompt(it) },
          onNavigateBrowser = { viewModel.setBrowserUrl(it) },
          tilingControllerState = tilingControllerState,
          onSelectTilingPreset = { viewModel.setTilingPreset(it) },
          onSelectTilingRatio = { viewModel.setTilingSplitRatio(it) },
          onToggleTilingZone = { viewModel.toggleTilingZoneVisibility(it) },
          onToggleTilingZoneMaximized = { viewModel.toggleTilingZoneMaximized(it) },
          onSwapTilingZones = { viewModel.swapTilingZones() },
          onSpawnDevTriad = { viewModel.spawnDevTriad() },
          onToggleTilingControllerExpanded = { viewModel.toggleTilingControllerExpanded() },
          modifier = Modifier.fillMaxSize()
        )

        // Floating Keyboard Shortcuts HUD Overlay (Heads-Up Display)
        if (isHudOpen) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(bottom = 10.dp, end = 10.dp),
            contentAlignment = Alignment.BottomEnd
          ) {
            KeyboardShortcutsHud(
              theme = theme,
              focusedWindow = focusedWindow,
              isWalkerOpen = isWalkerOpen,
              isControlCenterOpen = isControlCenterOpen,
              isKeybindsOpen = isKeybindsOpen,
              isCollapsed = isHudCollapsed,
              layoutMode = layoutMode,
              isFullscreen = isFullscreen,
              neovimMode = neovimMode,
              onToggleCollapse = { viewModel.toggleHudCollapsed() },
              onClose = { viewModel.closeHud() },
              onLaunchTerminal = { viewModel.openApp(AppType.TERMINAL) },
              onOpenWalker = { viewModel.openWalker() },
              onCloseWindow = { viewModel.closeFocusedWindow() },
              onToggleFullscreen = { viewModel.toggleFullscreen() },
              onToggleLayout = { viewModel.toggleLayoutMode() },
              onExecuteCmd = { viewModel.executeTerminalCommand(it) },
              onSetNeovimMode = { viewModel.setNeovimMode(it) },
              onToggleObsidianPreview = { viewModel.toggleObsidianPreview() },
              onCreateNote = { viewModel.createNewNote() },
              onCycleTheme = { viewModel.cycleTheme() }
            )
          }
        }
      }
    }

    // Walker Launcher Overlay
    if (isWalkerOpen) {
      WalkerLauncher(
        theme = theme,
        query = walkerQuery,
        onQueryChange = { viewModel.setWalkerQuery(it) },
        onLaunchApp = { viewModel.openApp(it) },
        onClose = { viewModel.closeWalker() }
      )
    }

    // Control Center Modal
    if (isControlCenterOpen) {
      ControlCenterModal(
        theme = theme,
        currentLayoutMode = layoutMode,
        isFullscreen = isFullscreen,
        isPersisted = isPersisted,
        lastSavedTime = lastSavedTime,
        persistenceStatus = persistenceStatus,
        onSelectTheme = { viewModel.setTheme(it) },
        onSelectIconSet = { viewModel.setIconSet(it) },
        onCycleTheme = { viewModel.cycleTheme() },
        onCycleIconSet = { viewModel.cycleIconSet() },
        onToggleLayoutMode = { viewModel.toggleLayoutMode() },
        onToggleFullscreen = { viewModel.toggleFullscreen() },
        onSaveWorkspace = { viewModel.saveWorkspaceToDb(manual = true) },
        onRestoreWorkspace = { viewModel.restoreWorkspaceFromDb(manual = true) },
        onClearWorkspace = { viewModel.clearWorkspaceDb(manual = true) },
        onClose = { viewModel.toggleControlCenter() }
      )
    }

    // Keybindings Cheat Sheet Modal
    if (isKeybindsOpen) {
      KeybindsCheatSheet(
        theme = theme,
        onClose = { viewModel.toggleKeybinds() }
      )
    }

    // Command Macro Studio Modal
    if (isMacroManagerOpen) {
      MacroManagerModal(
        theme = theme,
        macros = macros,
        isRecording = isRecordingMacro,
        recordedActions = recordedActions,
        recordingSeconds = recordingSeconds,
        recordingName = recordingMacroName,
        executionProgress = macroExecutionProgress,
        onStartRecording = { viewModel.startRecordingMacro(it) },
        onStopRecording = { name, shortcut, keyCode, isSuper, isShift, isCtrl, isAlt, desc ->
          viewModel.stopRecordingMacro(name, shortcut, keyCode, isSuper, isShift, isCtrl, isAlt, desc)
        },
        onCancelRecording = { viewModel.cancelRecordingMacro() },
        onDeleteRecordedAction = { viewModel.deleteRecordedAction(it) },
        onPlayMacro = { viewModel.playMacro(it) },
        onDeleteMacro = { viewModel.deleteMacro(it) },
        onUpdateShortcut = { id, label, code, superReq, shiftReq, ctrlReq, altReq ->
          viewModel.updateMacroShortcut(id, label, code, superReq, shiftReq, ctrlReq, altReq)
        },
        onClose = { viewModel.closeMacroManager() }
      )
    }
  }
}

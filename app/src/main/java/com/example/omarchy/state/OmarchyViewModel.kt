package com.example.omarchy.state

import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.OmarchyApplication
import com.example.omarchy.data.BufferEntity
import com.example.omarchy.data.MacroEntity
import com.example.omarchy.data.NoteEntity
import com.example.omarchy.data.OmarchyWorkspaceRepository
import com.example.omarchy.data.WindowEntity
import com.example.omarchy.data.WorkspaceSessionEntity
import com.example.omarchy.data.WorkspaceSnapshot
import com.example.omarchy.model.AppType
import com.example.omarchy.model.CommandMacro
import com.example.omarchy.model.DefaultCommandMacros
import com.example.omarchy.model.IconSetPreset
import com.example.omarchy.model.MacroAction
import com.example.omarchy.model.MacroActionType
import com.example.omarchy.model.MacroExecutionProgress
import com.example.omarchy.model.NoteItem
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.ProcessItem
import com.example.omarchy.model.SplitRatioPreset
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.model.TilingControllerState
import com.example.omarchy.model.TilingSplitPreset
import com.example.omarchy.model.TilingZoneType
import com.example.omarchy.model.WindowInstance
import com.example.omarchy.model.WindowLayoutMode
import com.example.omarchy.model.WorkspaceFileCatalog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

data class TerminalLine(
  val text: String,
  val type: TerminalLineType = TerminalLineType.OUTPUT
)

enum class TerminalLineType {
  COMMAND, OUTPUT, ERROR, SUCCESS, ACCENT, ASCII
}

data class NeovimBuffer(
  val filename: String,
  val language: String,
  val content: String,
  val isModified: Boolean = false
)

data class AgentMessage(
  val id: String = UUID.randomUUID().toString(),
  val sender: AgentSender,
  val text: String,
  val commandPreview: String? = null,
  val status: String? = null,
  val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
)

enum class AgentSender {
  USER, AGENT, SYSTEM, TOOL
}

class OmarchyViewModel(
  private val repository: OmarchyWorkspaceRepository? = null
) : ViewModel() {

  private val workspaceRepository: OmarchyWorkspaceRepository? by lazy {
    repository ?: runCatching { OmarchyApplication.instance.repository }.getOrNull()
  }

  // Room Persistence State
  private val _isPersisted = MutableStateFlow(false)
  val isPersisted: StateFlow<Boolean> = _isPersisted.asStateFlow()

  private val _lastSavedTime = MutableStateFlow<String?>(null)
  val lastSavedTime: StateFlow<String?> = _lastSavedTime.asStateFlow()

  private val _persistenceStatus = MutableStateFlow("Room DB Ready")
  val persistenceStatus: StateFlow<String> = _persistenceStatus.asStateFlow()

  private var autoSaveJob: Job? = null

  // Theme & Icon State
  private val _iconSet = MutableStateFlow(IconSetPreset.NERD_FONTS)
  val iconSet: StateFlow<IconSetPreset> = _iconSet.asStateFlow()

  private val _themeConfig = MutableStateFlow(OmarchyThemeConfig.fromPreset(ThemePreset.TOKYO_NIGHT, IconSetPreset.NERD_FONTS))
  val themeConfig: StateFlow<OmarchyThemeConfig> = _themeConfig.asStateFlow()

  // Desktop & Window Management
  private val _currentWorkspace = MutableStateFlow(1)
  val currentWorkspace: StateFlow<Int> = _currentWorkspace.asStateFlow()

  private val _windows = MutableStateFlow<List<WindowInstance>>(emptyList())
  val windows: StateFlow<List<WindowInstance>> = _windows.asStateFlow()

  private val _focusedWindowId = MutableStateFlow<String?>(null)
  val focusedWindowId: StateFlow<String?> = _focusedWindowId.asStateFlow()

  private val _layoutMode = MutableStateFlow(WindowLayoutMode.SPLIT_VERTICAL)
  val layoutMode: StateFlow<WindowLayoutMode> = _layoutMode.asStateFlow()

  private val _isFullscreen = MutableStateFlow(false)
  val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

  // Overlays
  private val _isWalkerOpen = MutableStateFlow(false)
  val isWalkerOpen: StateFlow<Boolean> = _isWalkerOpen.asStateFlow()

  private val _walkerQuery = MutableStateFlow("")
  val walkerQuery: StateFlow<String> = _walkerQuery.asStateFlow()

  private val _isControlCenterOpen = MutableStateFlow(false)
  val isControlCenterOpen: StateFlow<Boolean> = _isControlCenterOpen.asStateFlow()

  private val _isKeybindsOpen = MutableStateFlow(false)
  val isKeybindsOpen: StateFlow<Boolean> = _isKeybindsOpen.asStateFlow()

  // Shortcuts HUD Overlay
  private val _isHudOpen = MutableStateFlow(true)
  val isHudOpen: StateFlow<Boolean> = _isHudOpen.asStateFlow()

  private val _isHudCollapsed = MutableStateFlow(false)
  val isHudCollapsed: StateFlow<Boolean> = _isHudCollapsed.asStateFlow()

  // Splash Screen & Cyber Mascot Assistant State
  private val _isSplashScreenVisible = MutableStateFlow(true)
  val isSplashScreenVisible: StateFlow<Boolean> = _isSplashScreenVisible.asStateFlow()

  private val _isMascotAssistantVisible = MutableStateFlow(true)
  val isMascotAssistantVisible: StateFlow<Boolean> = _isMascotAssistantVisible.asStateFlow()

  // Tiling Window Layout Controller State
  private val _tilingControllerState = MutableStateFlow(TilingControllerState())
  val tilingControllerState: StateFlow<TilingControllerState> = _tilingControllerState.asStateFlow()

  // System Stats
  private val _cpuPercent = MutableStateFlow(18f)
  val cpuPercent: StateFlow<Float> = _cpuPercent.asStateFlow()

  private val _memPercent = MutableStateFlow(42f)
  val memPercent: StateFlow<Float> = _memPercent.asStateFlow()

  private val _cpuHistory = MutableStateFlow(listOf(12f, 15f, 22f, 18f, 25f, 20f, 18f, 28f, 19f, 21f))
  val cpuHistory: StateFlow<List<Float>> = _cpuHistory.asStateFlow()

  private val _processes = MutableStateFlow<List<ProcessItem>>(emptyList())
  val processes: StateFlow<List<ProcessItem>> = _processes.asStateFlow()

  // Terminal State
  private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
  val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

  private val _terminalCommandHistory = MutableStateFlow<List<String>>(emptyList())
  val terminalCommandHistory: StateFlow<List<String>> = _terminalCommandHistory.asStateFlow()

  // Neovim State
  private val _neovimBuffers = MutableStateFlow<List<NeovimBuffer>>(emptyList())
  val neovimBuffers: StateFlow<List<NeovimBuffer>> = _neovimBuffers.asStateFlow()

  private val _activeBufferIndex = MutableStateFlow(0)
  val activeBufferIndex: StateFlow<Int> = _activeBufferIndex.asStateFlow()

  private val _neovimMode = MutableStateFlow("NORMAL")
  val neovimMode: StateFlow<String> = _neovimMode.asStateFlow()

  // Obsidian State
  private val _notes = MutableStateFlow<List<NoteItem>>(emptyList())
  val notes: StateFlow<List<NoteItem>> = _notes.asStateFlow()

  private val _activeNoteId = MutableStateFlow<String?>(null)
  val activeNoteId: StateFlow<String?> = _activeNoteId.asStateFlow()

  private val _isObsidianPreview = MutableStateFlow(false)
  val isObsidianPreview: StateFlow<Boolean> = _isObsidianPreview.asStateFlow()

  // Agent State
  private val _agentMessages = MutableStateFlow<List<AgentMessage>>(emptyList())
  val agentMessages: StateFlow<List<AgentMessage>> = _agentMessages.asStateFlow()

  private val _isAgentThinking = MutableStateFlow(false)
  val isAgentThinking: StateFlow<Boolean> = _isAgentThinking.asStateFlow()

  // Browser State
  private val _browserUrl = MutableStateFlow("https://omarchy.org")
  val browserUrl: StateFlow<String> = _browserUrl.asStateFlow()

  // Command Macro System State
  private val _macros = MutableStateFlow<List<CommandMacro>>(DefaultCommandMacros.getPresets())
  val macros: StateFlow<List<CommandMacro>> = _macros.asStateFlow()

  private val _isMacroManagerOpen = MutableStateFlow(false)
  val isMacroManagerOpen: StateFlow<Boolean> = _isMacroManagerOpen.asStateFlow()

  private val _isRecordingMacro = MutableStateFlow(false)
  val isRecordingMacro: StateFlow<Boolean> = _isRecordingMacro.asStateFlow()

  private val _recordedActions = MutableStateFlow<List<MacroAction>>(emptyList())
  val recordedActions: StateFlow<List<MacroAction>> = _recordedActions.asStateFlow()

  private val _recordingMacroName = MutableStateFlow("New Macro")
  val recordingMacroName: StateFlow<String> = _recordingMacroName.asStateFlow()

  private val _recordingSeconds = MutableStateFlow(0)
  val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

  private val _macroExecutionProgress = MutableStateFlow<MacroExecutionProgress?>(null)
  val macroExecutionProgress: StateFlow<MacroExecutionProgress?> = _macroExecutionProgress.asStateFlow()

  private val _macroNotification = MutableStateFlow<String?>(null)
  val macroNotification: StateFlow<String?> = _macroNotification.asStateFlow()

  private var recordingJob: Job? = null
  private var isExecutingMacroInternal = false

  init {
    initDefaultWindows()
    initProcesses()
    initTerminal()
    initNeovim()
    initObsidian()
    initAgent()
    startSystemMetricsLoop()
    checkAndRestorePersistedWorkspace()
  }

  private fun initDefaultWindows() {
    val winTerminal = WindowInstance(
      id = "win-term",
      appType = AppType.TERMINAL,
      title = "omarchy@archlinux:~ (zsh)",
      workspaceId = 1,
      isFocused = true
    )
    val winAgent = WindowInstance(
      id = "win-agent",
      appType = AppType.AGENT,
      title = "Agentic Linux Copilot",
      workspaceId = 1,
      isFocused = false
    )
    val winEditor = WindowInstance(
      id = "win-editor",
      appType = AppType.NEOVIM,
      title = "nvim — app/models/article.rb",
      workspaceId = 2,
      isFocused = false
    )
    val winObsidian = WindowInstance(
      id = "win-notes",
      appType = AppType.OBSIDIAN,
      title = "Obsidian — DHH Manifesto",
      workspaceId = 3,
      isFocused = false
    )
    val winBtop = WindowInstance(
      id = "win-btop",
      appType = AppType.BTOP,
      title = "btop++ — Arch Linux",
      workspaceId = 4,
      isFocused = false
    )

    _windows.value = listOf(winTerminal, winAgent, winEditor, winObsidian, winBtop)
    _focusedWindowId.value = winTerminal.id
  }

  private fun initProcesses() {
    _processes.value = listOf(
      ProcessItem(1, "systemd", "root", 0.1f, 0.4f),
      ProcessItem(412, "hyprland", "omarchy", 4.2f, 3.8f),
      ProcessItem(489, "quickshell", "omarchy", 2.1f, 2.4f),
      ProcessItem(560, "agentd", "omarchy", 1.8f, 5.2f),
      ProcessItem(710, "alacritty", "omarchy", 1.2f, 1.9f),
      ProcessItem(892, "nvim", "omarchy", 0.6f, 1.1f),
      ProcessItem(1024, "obsidian", "omarchy", 3.4f, 6.7f),
      ProcessItem(1209, "chromium", "omarchy", 5.6f, 8.4f),
      ProcessItem(1430, "pipewire", "omarchy", 0.4f, 0.9f),
      ProcessItem(1820, "waybar", "omarchy", 0.2f, 0.8f)
    )
  }

  private fun initTerminal() {
    _terminalLines.value = listOf(
      TerminalLine("OMARCHY (v4.2.0-zen) - Beautiful, Modern & Opinionated Linux", TerminalLineType.ACCENT),
      TerminalLine("Agentic Linux Desktop environment initialized with Hyprland & Quickshell.", TerminalLineType.OUTPUT),
      TerminalLine("Type 'help' or 'omafetch' for system details, 'theme' to switch themes.", TerminalLineType.OUTPUT),
      TerminalLine("", TerminalLineType.OUTPUT),
      TerminalLine("omarchy@archlinux ~ $ omafetch", TerminalLineType.COMMAND)
    ) + getOmafetchLines()
  }

  private fun getOmafetchLines(): List<TerminalLine> {
    return listOf(
      TerminalLine("       /\\         omarchy@archlinux", TerminalLineType.ACCENT),
      TerminalLine("      /  \\        -----------------", TerminalLineType.OUTPUT),
      TerminalLine("     /\\   \\       OS: Omarchy Linux (Arch based) x86_64", TerminalLineType.OUTPUT),
      TerminalLine("    /      \\      Host: Sovereign Android Handheld (SDK 36)", TerminalLineType.OUTPUT),
      TerminalLine("   /   ,,   \\     Kernel: 6.13.2-aegntic-zen", TerminalLineType.OUTPUT),
      TerminalLine("  /   |  |  -\\    Lineage: Desktop OS & Vision by David Heinemeier Hansson (DHH)", TerminalLineType.OUTPUT),
      TerminalLine(" /_-''    ''-_\\   Mobile: Engineered by @aegntic (100% Sovereign & Offline)", TerminalLineType.SUCCESS),
      TerminalLine("                  Uptime: 4 days, 12 hours, 38 mins", TerminalLineType.OUTPUT),
      TerminalLine("                  Packages: 1,142 (pacman), 18 (flatpak)", TerminalLineType.OUTPUT),
      TerminalLine("                  Shell: zsh 5.9 (omarchy-p10k)", TerminalLineType.OUTPUT),
      TerminalLine("                  WM: Hyprland (Wayland Tiling Compositor)", TerminalLineType.OUTPUT),
      TerminalLine("                  Shell UI: Quickshell 0.4.0", TerminalLineType.OUTPUT),
      TerminalLine("                  Editor: Neovim 0.10 + LuaRocks", TerminalLineType.OUTPUT),
      TerminalLine("                  Companions: Catface, Longneck, Plinky", TerminalLineType.OUTPUT),
      TerminalLine("                  Theme: Tokyo Night (Omarchy Official)", TerminalLineType.SUCCESS),
      TerminalLine("                  Memory: 4.8GiB / 16.0GiB (30%)", TerminalLineType.OUTPUT),
      TerminalLine("", TerminalLineType.OUTPUT)
    )
  }

  private fun initNeovim() {
    _neovimBuffers.value = listOf(
      NeovimBuffer(
        filename = "app/models/article.rb",
        language = "ruby",
        content = """class Article < ApplicationRecord
  include Visible

  has_many :comments, dependent: :destroy

  validates :title, presence: true, length: { minimum: 5 }
  validates :body, presence: true, length: { minimum: 10 }

  scope :published, -> { where(status: 'public') }

  def summary
    body.truncate(140)
  end
end"""
      ),
      NeovimBuffer(
        filename = "~/.config/hypr/hyprland.conf",
        language = "conf",
        content = """# Omarchy Hyprland Configuration
monitor=,preferred,auto,1

general {
    gaps_in = 6
    gaps_out = 12
    border_size = 2
    col.active_border = rgba(7aa2f7ee) rgba(bb9af7ee) 45deg
    col.inactive_border = rgba(292e42aa)
    layout = dwindle
}

decoration {
    rounding = 10
    blur {
        enabled = true
        size = 8
        passes = 3
    }
}

# Keybindings
bind = SUPER, RETURN, exec, alacritty
bind = SUPER, Q, killactive,
bind = SUPER, SPACE, exec, walker
bind = SUPER, 1, workspace, 1
bind = SUPER, 2, workspace, 2"""
      ),
      NeovimBuffer(
        filename = "config/routes.rb",
        language = "ruby",
        content = """Rails.application.routes.draw do
  root "articles#index"

  resources :articles do
    resources :comments
  end

  get "up" => "rails/health#show", as: :rails_health_check
end"""
      )
    )
  }

  private fun initObsidian() {
    _notes.value = listOf(
      NoteItem(
        id = "n1",
        title = "DHH Omarchy Philosophy",
        content = """# Omarchy: Beautiful, Modern & Opinionated Linux

> "Linux without the boilerplate, tuned for makers who love keyboard elegance."

## Core Tenets
1. **Hyprland First**: Dynamic tiling window management that feels fluid and purposeful.
2. **Quickshell Integration**: A unified desktop shell instead of a fragmented puzzle of status bars and trays.
3. **Agentic Linux**: The desktop is built for two users: the human developer and their AI coding agent.
4. **Ruby on Rails & Modern Web**: Pre-configured out of the box with SQLite, solid cache, and Kamal.
5. **No AI Slop**: Clean, hyper-responsive software built with taste.
""",
        tag = "#philosophy",
        updatedAt = "Today, 10:45"
      ),
      NoteItem(
        id = "n2",
        title = "Hyprland Keybindings QuickRef",
        content = """# Hyprland Keybindings in Omarchy

- `Super + Enter`: Launch Terminal (`alacritty`)
- `Super + Space`: Open Walker Launcher
- `Super + Q`: Close focused window
- `Super + F`: Fullscreen toggle
- `Super + V`: Split orientation toggle
- `Super + 1..5`: Switch to Workspace
- `Super + Shift + 1..5`: Move window to Workspace
- `Super + E`: Open Neovim
- `Super + B`: Open Chromium
""",
        tag = "#cheatsheet",
        updatedAt = "Yesterday"
      ),
      NoteItem(
        id = "n3",
        title = "Agentic Linux Architecture",
        content = """# Agentic Linux Notes

Omarchy treats AI coding agents not as a browser chatbot tab, but as an integral co-pilot process:
- Has shell visibility via `agentd`
- Capable of reading terminal outputs and workspace states
- Instant script execution with user safety sandbox
- Automated git commits and rollback points
""",
        tag = "#architecture",
        updatedAt = "Sep 10"
      )
    )
    _activeNoteId.value = "n1"
  }

  private fun initAgent() {
    _agentMessages.value = listOf(
      AgentMessage(
        sender = AgentSender.SYSTEM,
        text = "Omarchy Agentic Subsystem v2.4 initialized. Ready to execute desktop commands and inspect code."
      ),
      AgentMessage(
        sender = AgentSender.AGENT,
        text = "Hello David! I am your Omarchy agent. I can monitor system performance, scaffold Rails models, reconfigure Hyprland tiling, or manage Neovim buffers. What are we building today?"
      )
    )
  }

  private fun startSystemMetricsLoop() {
    viewModelScope.launch {
      while (true) {
        delay(2500)
        val deltaCpu = (Random.nextFloat() * 8f) - 4f
        val newCpu = (_cpuPercent.value + deltaCpu).coerceIn(10f, 85f)
        _cpuPercent.value = newCpu

        val updatedHistory = (_cpuHistory.value.drop(1) + newCpu).takeLast(10)
        _cpuHistory.value = updatedHistory

        // Slight memory fluctuation
        val deltaMem = (Random.nextFloat() * 1.5f) - 0.7f
        _memPercent.value = (_memPercent.value + deltaMem).coerceIn(35f, 65f)
      }
    }
  }

  // Workspace actions
  fun switchWorkspace(workspaceId: Int) {
    recordAction(MacroAction(MacroActionType.SWITCH_WORKSPACE, workspaceId.toString()))
    if (workspaceId in 1..5) {
      _currentWorkspace.value = workspaceId
      // Focus first window in this workspace if available
      val firstWin = _windows.value.firstOrNull { it.workspaceId == workspaceId }
      if (firstWin != null) {
        focusWindow(firstWin.id)
      }
      scheduleAutoSave()
    }
  }

  fun focusWindow(windowId: String) {
    _focusedWindowId.value = windowId
    _windows.value = _windows.value.map {
      it.copy(isFocused = it.id == windowId)
    }
    scheduleAutoSave()
  }

  fun openApp(appType: AppType, workspaceId: Int = _currentWorkspace.value) {
    recordAction(MacroAction(MacroActionType.LAUNCH_APP, appType.name))
    val existing = _windows.value.firstOrNull { it.appType == appType && it.workspaceId == workspaceId }
    if (existing != null) {
      focusWindow(existing.id)
      closeWalker()
      return
    }

    val newWindow = WindowInstance(
      id = "win-${UUID.randomUUID().toString().take(6)}",
      appType = appType,
      title = "${appType.binaryName} - ${appType.displayName}",
      workspaceId = workspaceId,
      isFocused = true
    )

    _windows.value = _windows.value.map { it.copy(isFocused = false) } + newWindow
    _focusedWindowId.value = newWindow.id
    closeWalker()
    scheduleAutoSave()
  }

  fun closeWindow(windowId: String) {
    val remaining = _windows.value.filterNot { it.id == windowId }
    _windows.value = remaining
    if (_focusedWindowId.value == windowId) {
      val nextFocus = remaining.filter { it.workspaceId == _currentWorkspace.value }.lastOrNull()
      _focusedWindowId.value = nextFocus?.id
      if (nextFocus != null) {
        _windows.value = remaining.map { it.copy(isFocused = it.id == nextFocus.id) }
      }
    }
    scheduleAutoSave()
  }

  fun closeFocusedWindow() {
    _focusedWindowId.value?.let { closeWindow(it) }
  }

  fun toggleFullscreen() {
    recordAction(MacroAction(MacroActionType.LAYOUT_ACTION, "TOGGLE_FULLSCREEN"))
    _isFullscreen.value = !_isFullscreen.value
    scheduleAutoSave()
  }

  fun toggleLayoutMode() {
    recordAction(MacroAction(MacroActionType.LAYOUT_ACTION, "TOGGLE_LAYOUT"))
    _layoutMode.value = when (_layoutMode.value) {
      WindowLayoutMode.SPLIT_VERTICAL -> WindowLayoutMode.SPLIT_HORIZONTAL
      WindowLayoutMode.SPLIT_HORIZONTAL -> WindowLayoutMode.MASTER_STACK
      WindowLayoutMode.MASTER_STACK -> WindowLayoutMode.TRI_ZONE_DEV
      WindowLayoutMode.TRI_ZONE_DEV -> WindowLayoutMode.SPLIT_VERTICAL
      else -> WindowLayoutMode.SPLIT_VERTICAL
    }
    scheduleAutoSave()
  }

  fun toggleFloating(windowId: String) {
    _windows.value = _windows.value.map {
      if (it.id == windowId) it.copy(isFloating = !it.isFloating) else it
    }
    scheduleAutoSave()
  }

  // Theme & Icon actions
  fun setTheme(preset: ThemePreset) {
    _themeConfig.value = OmarchyThemeConfig.fromPreset(preset, _iconSet.value)
    addTerminalLine("Switched color scheme to: ${preset.displayName}", TerminalLineType.SUCCESS)
    scheduleAutoSave()
  }

  fun setIconSet(iconSet: IconSetPreset) {
    _iconSet.value = iconSet
    _themeConfig.value = _themeConfig.value.copyWithIconSet(iconSet)
    addTerminalLine("Switched icon set to: ${iconSet.displayName}", TerminalLineType.SUCCESS)
    scheduleAutoSave()
  }

  fun cycleTheme(forward: Boolean = true) {
    recordAction(MacroAction(MacroActionType.LAYOUT_ACTION, "CYCLE_THEME"))
    val presets = ThemePreset.entries
    val currentIndex = presets.indexOf(_themeConfig.value.preset).coerceAtLeast(0)
    val nextIndex = if (forward) {
      (currentIndex + 1) % presets.size
    } else {
      (currentIndex - 1 + presets.size) % presets.size
    }
    setTheme(presets[nextIndex])
  }

  fun cycleIconSet(forward: Boolean = true) {
    recordAction(MacroAction(MacroActionType.LAYOUT_ACTION, "CYCLE_ICONSET"))
    val sets = IconSetPreset.entries
    val currentIndex = sets.indexOf(_iconSet.value).coerceAtLeast(0)
    val nextIndex = if (forward) {
      (currentIndex + 1) % sets.size
    } else {
      (currentIndex - 1 + sets.size) % sets.size
    }
    setIconSet(sets[nextIndex])
  }

  fun toggleTheme() = cycleTheme(true)
  fun toggleIconSet() = cycleIconSet(true)

  // Overlays
  fun openWalker() {
    _isWalkerOpen.value = true
    _walkerQuery.value = ""
  }

  fun closeWalker() {
    _isWalkerOpen.value = false
    _walkerQuery.value = ""
  }

  fun setWalkerQuery(query: String) {
    _walkerQuery.value = query
  }

  fun toggleControlCenter() {
    _isControlCenterOpen.value = !_isControlCenterOpen.value
  }

  fun toggleKeybinds() {
    _isKeybindsOpen.value = !_isKeybindsOpen.value
  }

  fun toggleHud() {
    _isHudOpen.value = !_isHudOpen.value
  }

  fun setHudOpen(open: Boolean) {
    _isHudOpen.value = open
  }

  fun toggleHudCollapsed() {
    _isHudCollapsed.value = !_isHudCollapsed.value
  }

  fun closeHud() {
    _isHudOpen.value = false
  }

  // Tiling Layout Controller Actions
  fun setLayoutMode(mode: WindowLayoutMode) {
    _layoutMode.value = mode
    scheduleAutoSave()
  }

  fun setTilingPreset(preset: TilingSplitPreset) {
    _tilingControllerState.value = _tilingControllerState.value.copy(preset = preset)
    _layoutMode.value = WindowLayoutMode.TRI_ZONE_DEV
    scheduleAutoSave()
  }

  fun setTilingSplitRatio(ratioPreset: SplitRatioPreset) {
    _tilingControllerState.value = _tilingControllerState.value.copy(ratioPreset = ratioPreset)
    scheduleAutoSave()
  }

  fun toggleTilingZoneVisibility(zone: TilingZoneType) {
    val current = _tilingControllerState.value.visibleZones
    val newSet = if (current.contains(zone)) {
      if (current.size > 1) current - zone else current // Keep at least 1 zone visible
    } else {
      current + zone
    }
    _tilingControllerState.value = _tilingControllerState.value.copy(visibleZones = newSet)
    scheduleAutoSave()
  }

  fun toggleTilingZoneMaximized(zone: TilingZoneType) {
    val currentMax = _tilingControllerState.value.maximizedZone
    val newMax = if (currentMax == zone) null else zone
    _tilingControllerState.value = _tilingControllerState.value.copy(maximizedZone = newMax)
    scheduleAutoSave()
  }

  fun focusTilingZone(zone: TilingZoneType) {
    _tilingControllerState.value = _tilingControllerState.value.copy(focusedZone = zone)
    // Also focus corresponding window if it exists
    val matched = _windows.value.firstOrNull { it.workspaceId == _currentWorkspace.value && it.appType == zone.defaultAppType }
    if (matched != null) {
      focusWindow(matched.id)
    }
  }

  fun swapTilingZones() {
    val currentOrder = _tilingControllerState.value.zoneOrder
    val rotated = if (currentOrder.size >= 2) {
      listOf(currentOrder[1], currentOrder[0]) + currentOrder.drop(2)
    } else currentOrder
    _tilingControllerState.value = _tilingControllerState.value.copy(zoneOrder = rotated)
    scheduleAutoSave()
  }

  fun toggleTilingControllerExpanded() {
    _tilingControllerState.value = _tilingControllerState.value.copy(
      isControllerExpanded = !_tilingControllerState.value.isControllerExpanded
    )
  }

  /**
   * Spawns the Dev Triad (Terminal, Neovim, Agent) on the current workspace if they aren't already open,
   * and switches the desktop to the TRI_ZONE_DEV layout.
   */
  fun spawnDevTriad() {
    val currentWins = _windows.value.filter { it.workspaceId == _currentWorkspace.value }
    val hasTerm = currentWins.any { it.appType == AppType.TERMINAL }
    val hasEditor = currentWins.any { it.appType == AppType.NEOVIM }
    val hasAgent = currentWins.any { it.appType == AppType.AGENT }

    if (!hasEditor) openApp(AppType.NEOVIM)
    if (!hasTerm) openApp(AppType.TERMINAL)
    if (!hasAgent) openApp(AppType.AGENT)

    _layoutMode.value = WindowLayoutMode.TRI_ZONE_DEV
    _tilingControllerState.value = _tilingControllerState.value.copy(
      visibleZones = setOf(TilingZoneType.EDITOR, TilingZoneType.TERMINAL, TilingZoneType.ASSISTANT),
      maximizedZone = null
    )
    scheduleAutoSave()
  }

  // Terminal Execution
  fun executeTerminalCommand(input: String) {
    val cmd = input.trim()
    if (cmd.isEmpty()) return

    if (!cmd.startsWith("macro", ignoreCase = true)) {
      recordAction(MacroAction(MacroActionType.TERMINAL_COMMAND, cmd))
    }

    _terminalCommandHistory.value = _terminalCommandHistory.value + cmd
    addTerminalLine("omarchy@archlinux ~ $ $cmd", TerminalLineType.COMMAND)

    val parts = cmd.split("\\s+".toRegex())
    val base = parts.firstOrNull()?.lowercase() ?: ""
    val args = parts.drop(1)

    when (base) {
      "help", "man" -> {
        addTerminalLine("Omarchy Linux Built-in Commands:", TerminalLineType.ACCENT)
        addTerminalLine("  omafetch / neofetch - System information and ASCII banner", TerminalLineType.OUTPUT)
        addTerminalLine("  macro [list|rec|stop|run|gui] - Command macro recorder & shortcut binds", TerminalLineType.OUTPUT)
        addTerminalLine("  theme [name|next|list] - Toggle or list Hyprland color schemes (${ThemePreset.entries.size} available)", TerminalLineType.OUTPUT)
        addTerminalLine("  icons [name|next|list] - Toggle or list Hyprland icon sets (${IconSetPreset.entries.size} available)", TerminalLineType.OUTPUT)
        addTerminalLine("  session [cmd]          - Room SQLite persistence (save, restore, status, clear)", TerminalLineType.OUTPUT)
        addTerminalLine("  rails new <app>        - Scaffold a Ruby on Rails application", TerminalLineType.OUTPUT)
        addTerminalLine("  pacman -S <pkg>        - Install Arch Linux packages", TerminalLineType.OUTPUT)
        addTerminalLine("  agent <prompt>         - Send prompt to Agentic Linux Copilot", TerminalLineType.OUTPUT)
        addTerminalLine("  nvim [file]            - Open file in Neovim", TerminalLineType.OUTPUT)
        addTerminalLine("  obsidian               - Open Obsidian notes", TerminalLineType.OUTPUT)
        addTerminalLine("  btop                   - Open system resource monitor", TerminalLineType.OUTPUT)
        addTerminalLine("  ls, pwd, whoami, clear, date, uname -a", TerminalLineType.OUTPUT)
      }
      "omafetch", "neofetch" -> {
        _terminalLines.value = _terminalLines.value + getOmafetchLines()
      }
      "clear" -> {
        _terminalLines.value = emptyList()
      }
      "theme" -> {
        val query = args.firstOrNull()?.lowercase()
        when {
          query == null || query == "list" -> {
            addTerminalLine("Available Hyprland Themes (${ThemePreset.entries.size}):", TerminalLineType.OUTPUT)
            ThemePreset.entries.forEach { p ->
              val activeTag = if (p == _themeConfig.value.preset) " [ACTIVE]" else ""
              addTerminalLine("  • ${p.name.lowercase()} - ${p.displayName} (${p.authorOrOrigin})$activeTag", TerminalLineType.OUTPUT)
            }
          }
          query == "next" -> {
            cycleTheme(forward = true)
          }
          query == "prev" || query == "previous" -> {
            cycleTheme(forward = false)
          }
          else -> {
            val matched = ThemePreset.entries.firstOrNull { it.name.lowercase().contains(query) || it.displayName.lowercase().contains(query) }
            if (matched != null) {
              setTheme(matched)
            } else {
              addTerminalLine("Unknown theme '$query'. Type 'theme list' for options.", TerminalLineType.ERROR)
            }
          }
        }
      }
      "icons", "iconset" -> {
        val query = args.firstOrNull()?.lowercase()
        when {
          query == null || query == "list" -> {
            addTerminalLine("Available Hyprland Icon Sets (${IconSetPreset.entries.size}):", TerminalLineType.OUTPUT)
            IconSetPreset.entries.forEach { s ->
              val activeTag = if (s == _iconSet.value) " [ACTIVE]" else ""
              addTerminalLine("  • ${s.name.lowercase()} - ${s.displayName} [${s.sampleBadges}]$activeTag", TerminalLineType.OUTPUT)
            }
          }
          query == "next" -> {
            cycleIconSet(forward = true)
          }
          query == "prev" || query == "previous" -> {
            cycleIconSet(forward = false)
          }
          else -> {
            val matched = IconSetPreset.entries.firstOrNull { it.name.lowercase().contains(query) || it.displayName.lowercase().contains(query) }
            if (matched != null) {
              setIconSet(matched)
            } else {
              addTerminalLine("Unknown icon set '$query'. Type 'icons list' for options.", TerminalLineType.ERROR)
            }
          }
        }
      }
      "whoami" -> addTerminalLine("omarchy", TerminalLineType.OUTPUT)
      "pwd" -> addTerminalLine("/home/omarchy", TerminalLineType.OUTPUT)
      "date" -> addTerminalLine(SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault()).format(Date()), TerminalLineType.OUTPUT)
      "uname" -> {
        if (args.contains("-a")) {
          addTerminalLine("Linux archlinux 6.10.8-omarchy1-1-zen #1 SMP PREEMPT_DYNAMIC SMP Sat Sep 12 12:00:00 UTC 2026 x86_64 GNU/Linux", TerminalLineType.OUTPUT)
        } else {
          addTerminalLine("Linux", TerminalLineType.OUTPUT)
        }
      }
      "ls" -> {
        addTerminalLine("drwxr-xr-x 4 omarchy users 4096 Sep 12 11:00 Desktop", TerminalLineType.OUTPUT)
        addTerminalLine("drwxr-xr-x 8 omarchy users 4096 Sep 12 11:15 Documents", TerminalLineType.OUTPUT)
        addTerminalLine("drwxr-xr-x 6 omarchy users 4096 Sep 12 10:45 Projects", TerminalLineType.OUTPUT)
        addTerminalLine("drwxr-xr-x 3 omarchy users 4096 Sep 12 09:20 .config", TerminalLineType.OUTPUT)
        addTerminalLine("-rw-r--r-- 1 omarchy users 2048 Sep 12 09:30 Gemfile", TerminalLineType.OUTPUT)
        addTerminalLine("-rw-r--r-- 1 omarchy users  480 Sep 12 10:12 README.md", TerminalLineType.OUTPUT)
      }
      "cat" -> {
        if (args.isEmpty()) {
          addTerminalLine("usage: cat <filename>", TerminalLineType.ERROR)
        } else {
          val fname = args.first()
          if (fname.contains("gemfile", ignoreCase = true)) {
            addTerminalLine("source 'https://rubygems.org'\ngem 'rails', '~> 8.0.0'\ngem 'propshaft'\ngem 'solid_cache'\ngem 'sqlite3'", TerminalLineType.OUTPUT)
          } else if (fname.contains("readme", ignoreCase = true)) {
            addTerminalLine("# Omarchy: Opinionated Linux\nBeautiful, minimal, agentic desktop for developers.", TerminalLineType.OUTPUT)
          } else {
            addTerminalLine("cat: $fname: No such file or directory", TerminalLineType.ERROR)
          }
        }
      }
      "rails" -> {
        val sub = args.firstOrNull()
        if (sub == "new") {
          val appName = args.getOrNull(1) ?: "my_omarchy_app"
          addTerminalLine("       create  $appName", TerminalLineType.SUCCESS)
          addTerminalLine("       create  README.md", TerminalLineType.OUTPUT)
          addTerminalLine("       create  app/controllers/application_controller.rb", TerminalLineType.OUTPUT)
          addTerminalLine("       create  config/routes.rb", TerminalLineType.OUTPUT)
          addTerminalLine("       create  config/databases/sqlite3.yml", TerminalLineType.OUTPUT)
          addTerminalLine("         run   bundle install using Omarchy Ruby 3.3.4", TerminalLineType.ACCENT)
          addTerminalLine("Rails $appName initialized successfully with SQLite & Solid Cache!", TerminalLineType.SUCCESS)
        } else {
          addTerminalLine("Rails 8.0.0 (Omarchy Edition). Try 'rails new <name>'", TerminalLineType.OUTPUT)
        }
      }
      "pacman" -> {
        if (args.contains("-S") || args.contains("-Syu")) {
          val pkg = args.lastOrNull() ?: "base-devel"
          addTerminalLine(":: Synchronizing package databases...", TerminalLineType.OUTPUT)
          addTerminalLine(" omarchy-core [######################] 100% 4.2 MB", TerminalLineType.SUCCESS)
          addTerminalLine(" extra        [######################] 100% 9.1 MB", TerminalLineType.SUCCESS)
          addTerminalLine(":: Resolving dependencies for '$pkg'...", TerminalLineType.OUTPUT)
          addTerminalLine("Packages (1) $pkg-1.0.0-1", TerminalLineType.ACCENT)
          addTerminalLine("Total Installed Size: 14.82 MiB", TerminalLineType.OUTPUT)
          addTerminalLine(":: Proceed with installation? [Y/n] Y", TerminalLineType.OUTPUT)
          addTerminalLine("(1/1) checking keys in keyring      [######################] 100%", TerminalLineType.OUTPUT)
          addTerminalLine("(1/1) installing $pkg               [######################] 100%", TerminalLineType.SUCCESS)
          addTerminalLine("Installed $pkg cleanly via Omarchy arch mirrors.", TerminalLineType.SUCCESS)
        } else {
          addTerminalLine("Pacman v6.1.0 - libalpm v14.0.0. Try 'pacman -S <package>'", TerminalLineType.OUTPUT)
        }
      }
      "agent" -> {
        val query = args.joinToString(" ")
        if (query.isBlank()) {
          openApp(AppType.AGENT)
        } else {
          openApp(AppType.AGENT)
          sendAgentPrompt(query)
        }
      }
      "session", "workspace", "snapshot" -> {
        val sub = args.firstOrNull()?.lowercase() ?: "status"
        when (sub) {
          "save" -> {
            saveWorkspaceToDb(manual = true)
          }
          "restore", "load" -> {
            restoreWorkspaceFromDb(manual = true)
          }
          "clear", "reset" -> {
            clearWorkspaceDb(manual = true)
          }
          "status" -> {
            addTerminalLine("Room SQLite Workspace Persistence Status:", TerminalLineType.ACCENT)
            addTerminalLine("  Database: omarchy_workspace.db (Room v2.6+)", TerminalLineType.OUTPUT)
            addTerminalLine("  Status: ${_persistenceStatus.value}", TerminalLineType.OUTPUT)
            addTerminalLine("  Last Saved: ${_lastSavedTime.value ?: "Never"}", TerminalLineType.OUTPUT)
            addTerminalLine("  Active Windows: ${_windows.value.size} | Buffers: ${_neovimBuffers.value.size} | Notes: ${_notes.value.size}", TerminalLineType.OUTPUT)
            addTerminalLine("  Current Layout: ${_layoutMode.value.displayName} on Workspace ${_currentWorkspace.value}", TerminalLineType.OUTPUT)
          }
          else -> {
            addTerminalLine("Usage: session [save|restore|status|clear]", TerminalLineType.OUTPUT)
            addTerminalLine("  save    - Save current windows, layouts, and open buffers to Room SQLite", TerminalLineType.OUTPUT)
            addTerminalLine("  restore - Restore saved workspace state from Room SQLite", TerminalLineType.OUTPUT)
            addTerminalLine("  status  - Show persistence database metrics and last snapshot timestamp", TerminalLineType.OUTPUT)
            addTerminalLine("  clear   - Clear persisted workspace tables", TerminalLineType.OUTPUT)
          }
        }
      }
      "macro", "macros" -> {
        handleMacroCommand(args)
      }
      "nvim", "vim", "vi" -> {
        val fileArg = args.firstOrNull()
        if (fileArg != null) {
          openFileInNeovim(fileArg)
          addTerminalLine("Opening '$fileArg' in Neovim workspace...", TerminalLineType.SUCCESS)
        }
        openApp(AppType.NEOVIM)
      }
      "telescope", "fzf" -> {
        val fileArg = args.firstOrNull()
        if (fileArg != null) {
          openFileInNeovim(fileArg)
        }
        openApp(AppType.NEOVIM)
        addTerminalLine("Launched Telescope in Neovim workspace.", TerminalLineType.ACCENT)
      }
      "obsidian" -> openApp(AppType.OBSIDIAN)
      "btop" -> openApp(AppType.BTOP)
      "browser", "chromium" -> openApp(AppType.BROWSER)
      else -> {
        addTerminalLine("zsh: command not found: $base. Type 'help' for command list.", TerminalLineType.ERROR)
      }
    }
  }

  private fun addTerminalLine(text: String, type: TerminalLineType = TerminalLineType.OUTPUT) {
    _terminalLines.value = _terminalLines.value + TerminalLine(text, type)
  }

  // Neovim Actions
  fun selectBuffer(index: Int) {
    if (index in _neovimBuffers.value.indices) {
      _activeBufferIndex.value = index
      scheduleAutoSave()
    }
  }

  fun updateNeovimContent(newContent: String) {
    val idx = _activeBufferIndex.value
    if (idx in _neovimBuffers.value.indices) {
      _neovimBuffers.value = _neovimBuffers.value.mapIndexed { i, buf ->
        if (i == idx) buf.copy(content = newContent, isModified = true) else buf
      }
      scheduleAutoSave()
    }
  }

  fun setNeovimMode(mode: String) {
    _neovimMode.value = mode
  }

  fun openFileInNeovim(filePath: String) {
    val cleanPath = filePath.trim()
    val existingIndex = _neovimBuffers.value.indexOfFirst {
      it.filename.equals(cleanPath, ignoreCase = true) ||
      it.filename.endsWith("/$cleanPath", ignoreCase = true) ||
      cleanPath.endsWith("/${it.filename}", ignoreCase = true)
    }

    if (existingIndex >= 0) {
      _activeBufferIndex.value = existingIndex
    } else {
      val catalogFile = WorkspaceFileCatalog.getFileByPath(cleanPath)
      val newBuffer = if (catalogFile != null) {
        NeovimBuffer(
          filename = catalogFile.path,
          language = catalogFile.language,
          content = catalogFile.content,
          isModified = false
        )
      } else {
        val detectedLang = when {
          cleanPath.endsWith(".rb") -> "ruby"
          cleanPath.endsWith(".lua") -> "lua"
          cleanPath.endsWith(".conf") -> "conf"
          cleanPath.endsWith(".toml") -> "toml"
          cleanPath.endsWith(".yml") || cleanPath.endsWith(".yaml") -> "yaml"
          cleanPath.endsWith(".html") || cleanPath.endsWith(".erb") -> "html"
          cleanPath.endsWith(".md") -> "markdown"
          cleanPath.endsWith(".sh") || cleanPath.endsWith(".zsh") -> "sh"
          else -> "txt"
        }
        NeovimBuffer(
          filename = cleanPath,
          language = detectedLang,
          content = "# $cleanPath\n# Created in Omarchy workspace\n",
          isModified = false
        )
      }
      _neovimBuffers.value = _neovimBuffers.value + newBuffer
      _activeBufferIndex.value = _neovimBuffers.value.lastIndex
    }
    scheduleAutoSave()
  }

  fun closeBuffer(index: Int) {
    val current = _neovimBuffers.value
    if (index in current.indices) {
      if (current.size > 1) {
        _neovimBuffers.value = current.filterIndexed { i, _ -> i != index }
        _activeBufferIndex.value = _activeBufferIndex.value.coerceIn(0, _neovimBuffers.value.lastIndex)
      } else {
        _neovimBuffers.value = listOf(NeovimBuffer("[No Name]", "txt", ""))
        _activeBufferIndex.value = 0
      }
      scheduleAutoSave()
    }
  }

  fun executeNeovimCommand(cmd: String) {
    val trimmed = cmd.trim()
    if (trimmed.isNotEmpty()) {
      recordAction(MacroAction(MacroActionType.EDITOR_COMMAND, trimmed))
    }
    when {
      trimmed == ":w" || trimmed.startsWith(":w ") -> {
        val idx = _activeBufferIndex.value
        if (idx in _neovimBuffers.value.indices) {
          _neovimBuffers.value = _neovimBuffers.value.mapIndexed { i, b ->
            if (i == idx) b.copy(isModified = false) else b
          }
          scheduleAutoSave()
        }
      }
      trimmed == ":q" || trimmed == ":q!" -> {
        closeBuffer(_activeBufferIndex.value)
      }
      trimmed == ":wq" -> {
        val idx = _activeBufferIndex.value
        if (idx in _neovimBuffers.value.indices) {
          _neovimBuffers.value = _neovimBuffers.value.mapIndexed { i, b ->
            if (i == idx) b.copy(isModified = false) else b
          }
        }
        closeBuffer(idx)
      }
      trimmed.startsWith(":e ") -> {
        val path = trimmed.substring(3).trim()
        if (path.isNotEmpty()) {
          openFileInNeovim(path)
        }
      }
      trimmed == ":split" || trimmed == ":vsplit" -> {
        toggleLayoutMode()
      }
    }
  }

  // Obsidian Actions
  fun selectNote(id: String) {
    _activeNoteId.value = id
    scheduleAutoSave()
  }

  fun updateNoteContent(newContent: String) {
    val id = _activeNoteId.value ?: return
    _notes.value = _notes.value.map {
      if (it.id == id) it.copy(content = newContent, updatedAt = "Just now") else it
    }
    scheduleAutoSave()
  }

  fun createNewNote() {
    val newId = "n-${UUID.randomUUID().toString().take(4)}"
    val note = NoteItem(
      id = newId,
      title = "Untitled Note ${_notes.value.size + 1}",
      content = "# New Note\n\nWrite your markdown thoughts here...",
      tag = "#draft",
      updatedAt = "Just now"
    )
    _notes.value = listOf(note) + _notes.value
    _activeNoteId.value = newId
    scheduleAutoSave()
  }

  fun toggleObsidianPreview() {
    _isObsidianPreview.value = !_isObsidianPreview.value
  }

  // Agent Actions
  fun sendAgentPrompt(prompt: String) {
    if (prompt.isBlank()) return

    val userMsg = AgentMessage(
      sender = AgentSender.USER,
      text = prompt
    )
    _agentMessages.value = _agentMessages.value + userMsg
    _isAgentThinking.value = true

    viewModelScope.launch {
      delay(1200)
      _isAgentThinking.value = false

      val lower = prompt.lowercase()
      when {
        lower.contains("theme") -> {
          val preset = if (lower.contains("catppuccin")) ThemePreset.CATPPUCCIN
          else if (lower.contains("lumon")) ThemePreset.LUMON
          else if (lower.contains("cyber")) ThemePreset.CYBERPUNK
          else if (lower.contains("everforest")) ThemePreset.EVERFOREST
          else if (lower.contains("rose")) ThemePreset.ROSE_PINE
          else ThemePreset.TOKYO_NIGHT

          setTheme(preset)
          _agentMessages.value = _agentMessages.value + AgentMessage(
            sender = AgentSender.TOOL,
            text = "Executing: `omarchy-theme set ${preset.name.lowercase()}`",
            commandPreview = "hyprctl reload && quickshell --theme ${preset.name.lowercase()}"
          ) + AgentMessage(
            sender = AgentSender.AGENT,
            text = "I've reconfigured your Omarchy desktop theme to ${preset.displayName}. Borders, status bar, and terminal colors are synchronized."
          )
        }
        lower.contains("rails") || lower.contains("model") || lower.contains("controller") -> {
          _agentMessages.value = _agentMessages.value + AgentMessage(
            sender = AgentSender.TOOL,
            text = "Executing: `bin/rails generate model Post title:string content:text published:boolean`",
            commandPreview = "db/migrate/20260912_create_posts.rb created."
          ) + AgentMessage(
            sender = AgentSender.AGENT,
            text = "Scaffolded Post model with SQLite solid cache compatibility. Migration is ready to execute with `bin/rails db:migrate`."
          )
        }
        lower.contains("btop") || lower.contains("cpu") || lower.contains("memory") || lower.contains("process") -> {
          _agentMessages.value = _agentMessages.value + AgentMessage(
            sender = AgentSender.AGENT,
            text = "Current CPU load is ${_cpuPercent.value.toInt()}% across 8 virtual cores. Memory consumption is at ${_memPercent.value.toInt()}% (4.8 GB / 16 GB). System is operating smoothly."
          )
        }
        lower.contains("tile") || lower.contains("layout") || lower.contains("window") -> {
          toggleLayoutMode()
          _agentMessages.value = _agentMessages.value + AgentMessage(
            sender = AgentSender.TOOL,
            text = "Executing: `hyprctl dispatch layoutmsg ${_layoutMode.value.name.lowercase()}`"
          ) + AgentMessage(
            sender = AgentSender.AGENT,
            text = "Switched tiling compositor layout to ${_layoutMode.value.displayName}."
          )
        }
        else -> {
          _agentMessages.value = _agentMessages.value + AgentMessage(
            sender = AgentSender.AGENT,
            text = "Task analyzed. Running Omarchy agent script for: '$prompt'. Checked system dependencies and all services are running green."
          )
        }
      }
    }
  }

  // Browser Actions
  fun setBrowserUrl(url: String) {
    _browserUrl.value = url
  }

  // Kill Process
  fun killProcess(pid: Int) {
    _processes.value = _processes.value.filterNot { it.pid == pid }
    addTerminalLine("Killed process PID $pid cleanly via SIGTERM", TerminalLineType.ACCENT)
  }

  // ==========================================
  // Room Database Workspace Persistence Engine
  // ==========================================

  fun scheduleAutoSave() {
    autoSaveJob?.cancel()
    autoSaveJob = viewModelScope.launch {
      delay(1200) // 1.2-second debounce for typing/rapid layout shifts
      saveWorkspaceToDb(manual = false)
    }
  }

  fun saveWorkspaceToDb(manual: Boolean = false) {
    viewModelScope.launch {
      val repo = workspaceRepository ?: return@launch
      try {
        val now = System.currentTimeMillis()
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))
        val currentTiling = _tilingControllerState.value

        val sessionEntity = WorkspaceSessionEntity(
          id = 1,
          currentWorkspace = _currentWorkspace.value,
          layoutMode = _layoutMode.value.name,
          focusedWindowId = _focusedWindowId.value,
          isFullscreen = _isFullscreen.value,
          isHudOpen = _isHudOpen.value,
          isHudCollapsed = _isHudCollapsed.value,
          themePreset = _themeConfig.value.preset.name,
          iconSetPreset = _iconSet.value.name,
          tilingPreset = currentTiling.preset.name,
          tilingRatioPreset = currentTiling.ratioPreset.name,
          tilingVisibleZones = currentTiling.visibleZones.joinToString(",") { it.name },
          tilingMaximizedZone = currentTiling.maximizedZone?.name,
          tilingZoneOrder = currentTiling.zoneOrder.joinToString(",") { it.name },
          activeBufferIndex = _activeBufferIndex.value,
          activeNoteId = _activeNoteId.value,
          lastSavedAt = now,
          sessionName = "Omarchy Session ($timeStr)"
        )

        val windowEntities = _windows.value.mapIndexed { idx, win ->
          WindowEntity.fromWindowInstance(win, idx)
        }

        val bufferEntities = _neovimBuffers.value.mapIndexed { idx, buf ->
          BufferEntity.fromNeovimBuffer(buf, idx)
        }

        val noteEntities = _notes.value.mapIndexed { idx, note ->
          NoteEntity.fromNoteItem(note, idx)
        }

        val macroEntities = _macros.value.map { MacroEntity.fromCommandMacro(it) }

        val snapshot = WorkspaceSnapshot(
          session = sessionEntity,
          windows = windowEntities,
          buffers = bufferEntities,
          notes = noteEntities,
          macros = macroEntities
        )

        repo.saveWorkspaceSnapshot(snapshot)
        _isPersisted.value = true
        _lastSavedTime.value = timeStr
        _persistenceStatus.value = "Saved at $timeStr"

        if (manual) {
          addTerminalLine("[ROOM PERSISTENCE] Workspace snapshot saved to SQLite database.", TerminalLineType.SUCCESS)
          addTerminalLine("   - Windows saved: ${windowEntities.size} across workspaces", TerminalLineType.OUTPUT)
          addTerminalLine("   - Neovim buffers: ${bufferEntities.size} persisted", TerminalLineType.OUTPUT)
          addTerminalLine("   - Obsidian notes: ${noteEntities.size} persisted", TerminalLineType.OUTPUT)
          addTerminalLine("   - Command macros: ${macroEntities.size} persisted", TerminalLineType.OUTPUT)
          addTerminalLine("   - Active layout: ${_layoutMode.value.displayName} | Theme: ${_themeConfig.value.preset.displayName}", TerminalLineType.OUTPUT)
        }
      } catch (e: Exception) {
        _persistenceStatus.value = "Save error: ${e.localizedMessage}"
        if (manual) {
          addTerminalLine("[ROOM PERSISTENCE] Failed to save: ${e.localizedMessage}", TerminalLineType.ERROR)
        }
      }
    }
  }

  fun restoreWorkspaceFromDb(manual: Boolean = true) {
    viewModelScope.launch {
      val repo = workspaceRepository
      if (repo == null) {
        if (manual) addTerminalLine("[ROOM PERSISTENCE] Database repository not initialized.", TerminalLineType.ERROR)
        return@launch
      }
      try {
        val snapshot = repo.loadWorkspaceSnapshot()
        if (snapshot != null) {
          applySnapshot(snapshot)
          val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(snapshot.session.lastSavedAt))
          _isPersisted.value = true
          _lastSavedTime.value = timeStr
          _persistenceStatus.value = "Restored ($timeStr)"
          if (manual) {
            addTerminalLine("[ROOM PERSISTENCE] Workspace restored from snapshot ($timeStr).", TerminalLineType.SUCCESS)
            addTerminalLine("   - ${snapshot.windows.size} windows re-opened", TerminalLineType.OUTPUT)
            addTerminalLine("   - ${snapshot.buffers.size} Neovim buffers & ${snapshot.notes.size} notes restored", TerminalLineType.OUTPUT)
            addTerminalLine("   - Switched to workspace ${snapshot.session.currentWorkspace} (${snapshot.session.layoutMode})", TerminalLineType.OUTPUT)
          }
        } else {
          if (manual) addTerminalLine("[ROOM PERSISTENCE] No saved workspace snapshot found in SQLite database.", TerminalLineType.ERROR)
        }
      } catch (e: Exception) {
        if (manual) addTerminalLine("[ROOM PERSISTENCE] Failed to restore: ${e.localizedMessage}", TerminalLineType.ERROR)
      }
    }
  }

  fun clearWorkspaceDb(manual: Boolean = true) {
    viewModelScope.launch {
      val repo = workspaceRepository ?: return@launch
      try {
        repo.clearSavedWorkspace()
        _isPersisted.value = false
        _lastSavedTime.value = null
        _persistenceStatus.value = "Database Cleared"
        if (manual) {
          addTerminalLine("[ROOM PERSISTENCE] Database tables cleared. Default layout will load on next launch.", TerminalLineType.SUCCESS)
        }
      } catch (e: Exception) {
        if (manual) addTerminalLine("[ROOM PERSISTENCE] Clear failed: ${e.localizedMessage}", TerminalLineType.ERROR)
      }
    }
  }

  private fun checkAndRestorePersistedWorkspace() {
    viewModelScope.launch {
      try {
        val repo = workspaceRepository ?: return@launch
        val snapshot = repo.loadWorkspaceSnapshot()
        if (snapshot != null && snapshot.windows.isNotEmpty()) {
          applySnapshot(snapshot)
          _isPersisted.value = true
          val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(snapshot.session.lastSavedAt))
          _lastSavedTime.value = timeStr
          _persistenceStatus.value = "Restored ($timeStr)"
          addTerminalLine("[ROOM PERSISTENCE] Restored previous workspace session from omarchy_workspace.db", TerminalLineType.SUCCESS)
          addTerminalLine("   - ${snapshot.windows.size} windows, ${snapshot.buffers.size} buffers, ${snapshot.notes.size} notes recovered", TerminalLineType.OUTPUT)
        }
      } catch (e: Exception) {
        _persistenceStatus.value = "DB Init: ${e.localizedMessage}"
      }
    }
  }

  private fun applySnapshot(snapshot: WorkspaceSnapshot) {
    val session = snapshot.session
    _currentWorkspace.value = session.currentWorkspace
    _layoutMode.value = session.getParsedLayoutMode()
    _isFullscreen.value = session.isFullscreen
    _isHudOpen.value = session.isHudOpen
    _isHudCollapsed.value = session.isHudCollapsed
    val parsedTheme = session.getParsedThemePreset()
    val parsedIconSet = session.getParsedIconSetPreset()
    _iconSet.value = parsedIconSet
    _themeConfig.value = OmarchyThemeConfig.fromPreset(parsedTheme, parsedIconSet)
    _tilingControllerState.value = session.toTilingControllerState()

    if (snapshot.windows.isNotEmpty()) {
      _windows.value = snapshot.windows.map { it.toWindowInstance() }
      _focusedWindowId.value = session.focusedWindowId ?: _windows.value.firstOrNull()?.id
    }

    if (snapshot.buffers.isNotEmpty()) {
      _neovimBuffers.value = snapshot.buffers.map { it.toNeovimBuffer() }
      _activeBufferIndex.value = session.activeBufferIndex.coerceIn(0, (snapshot.buffers.size - 1).coerceAtLeast(0))
    }

    if (snapshot.notes.isNotEmpty()) {
      _notes.value = snapshot.notes.map { it.toNoteItem() }
      _activeNoteId.value = session.activeNoteId ?: _notes.value.firstOrNull()?.id
    }

    if (snapshot.macros.isNotEmpty()) {
      _macros.value = snapshot.macros.map { it.toCommandMacro() }
    }
  }

  // ==========================================
  // COMMAND MACRO RECORDING & EXECUTION ENGINE
  // ==========================================

  fun toggleMacroManager() {
    _isMacroManagerOpen.value = !_isMacroManagerOpen.value
  }

  fun openMacroManager() {
    _isMacroManagerOpen.value = true
  }

  fun closeMacroManager() {
    _isMacroManagerOpen.value = false
  }

  fun startRecordingMacro(name: String = "Macro_${System.currentTimeMillis() % 10000}") {
    _recordingMacroName.value = name.ifBlank { "Macro_${System.currentTimeMillis() % 10000}" }
    _recordedActions.value = emptyList()
    _recordingSeconds.value = 0
    _isRecordingMacro.value = true

    recordingJob?.cancel()
    recordingJob = viewModelScope.launch {
      while (_isRecordingMacro.value) {
        delay(1000)
        _recordingSeconds.value += 1
      }
    }

    addTerminalLine("[MACRO RECORDER] ● Recording started: '${_recordingMacroName.value}'.", TerminalLineType.ACCENT)
    addTerminalLine("   Commands, Neovim actions, and workspace switches are being captured.", TerminalLineType.OUTPUT)
    addTerminalLine("   Type 'macro stop [name] [shortcut]' or use Macro Studio to finish.", TerminalLineType.OUTPUT)
  }

  fun recordAction(action: MacroAction) {
    if (_isRecordingMacro.value && !isExecutingMacroInternal) {
      _recordedActions.value = _recordedActions.value + action
    }
  }

  fun deleteRecordedAction(index: Int) {
    if (index in _recordedActions.value.indices) {
      _recordedActions.value = _recordedActions.value.filterIndexed { i, _ -> i != index }
    }
  }

  fun addManualActionToRecording(action: MacroAction) {
    _recordedActions.value = _recordedActions.value + action
  }

  fun stopRecordingMacro(
    name: String = _recordingMacroName.value,
    shortcutLabel: String = "",
    keyCode: Int = KeyEvent.KEYCODE_UNKNOWN,
    requiresSuper: Boolean = true,
    requiresShift: Boolean = false,
    requiresCtrl: Boolean = false,
    requiresAlt: Boolean = false,
    description: String = ""
  ): CommandMacro? {
    if (!_isRecordingMacro.value) return null
    _isRecordingMacro.value = false
    recordingJob?.cancel()
    recordingJob = null

    val actions = _recordedActions.value
    if (actions.isEmpty()) {
      addTerminalLine("[MACRO RECORDER] Recording stopped with 0 actions. Macro was discarded.", TerminalLineType.ERROR)
      return null
    }

    val finalName = name.ifBlank { "Macro_${System.currentTimeMillis() % 10000}" }
    val finalShortcut = if (shortcutLabel.isBlank() && keyCode != KeyEvent.KEYCODE_UNKNOWN) {
      buildShortcutLabel(keyCode, requiresSuper, requiresShift, requiresCtrl, requiresAlt)
    } else shortcutLabel

    val desc = description.ifBlank { "Sequence of ${actions.size} actions" }

    val newMacro = CommandMacro(
      id = "macro-${UUID.randomUUID().toString().take(8)}",
      name = finalName,
      description = desc,
      shortcutLabel = finalShortcut,
      keyCode = keyCode,
      requiresSuper = requiresSuper,
      requiresShift = requiresShift,
      requiresCtrl = requiresCtrl,
      requiresAlt = requiresAlt,
      actions = actions,
      isBuiltIn = false,
      executionCount = 0,
      lastExecutedAt = null,
      createdAt = System.currentTimeMillis()
    )

    saveMacro(newMacro)

    addTerminalLine("[MACRO RECORDER] ✓ Saved macro '${newMacro.name}' with ${actions.size} action(s).", TerminalLineType.SUCCESS)
    if (newMacro.shortcutLabel.isNotBlank()) {
      addTerminalLine("   Bound to keybinding: [${newMacro.shortcutLabel}]", TerminalLineType.OUTPUT)
    }

    return newMacro
  }

  fun cancelRecordingMacro() {
    _isRecordingMacro.value = false
    recordingJob?.cancel()
    recordingJob = null
    _recordedActions.value = emptyList()
    addTerminalLine("[MACRO RECORDER] Recording cancelled.", TerminalLineType.OUTPUT)
  }

  fun saveMacro(macro: CommandMacro) {
    val existing = _macros.value.indexOfFirst { it.id == macro.id }
    if (existing >= 0) {
      _macros.value = _macros.value.mapIndexed { i, m -> if (i == existing) macro else m }
    } else {
      _macros.value = _macros.value + macro
    }

    viewModelScope.launch {
      workspaceRepository?.saveMacro(MacroEntity.fromCommandMacro(macro))
    }
  }

  fun deleteMacro(id: String) {
    val macro = _macros.value.firstOrNull { it.id == id }
    if (macro?.isBuiltIn == true) {
      addTerminalLine("Cannot delete built-in preset macro '${macro.name}'.", TerminalLineType.ERROR)
      return
    }

    _macros.value = _macros.value.filterNot { it.id == id }
    viewModelScope.launch {
      workspaceRepository?.deleteMacro(id)
    }
    if (macro != null) {
      addTerminalLine("[MACRO] Deleted macro '${macro.name}'.", TerminalLineType.SUCCESS)
    }
  }

  fun updateMacroShortcut(
    macroId: String,
    shortcutLabel: String,
    keyCode: Int,
    requiresSuper: Boolean = true,
    requiresShift: Boolean = false,
    requiresCtrl: Boolean = false,
    requiresAlt: Boolean = false
  ) {
    val target = _macros.value.firstOrNull { it.id == macroId } ?: return
    val updated = target.copy(
      shortcutLabel = shortcutLabel,
      keyCode = keyCode,
      requiresSuper = requiresSuper,
      requiresShift = requiresShift,
      requiresCtrl = requiresCtrl,
      requiresAlt = requiresAlt
    )
    saveMacro(updated)
    addTerminalLine("[MACRO] Updated shortcut for '${target.name}' to [$shortcutLabel].", TerminalLineType.SUCCESS)
  }

  fun playMacroById(id: String) {
    val macro = _macros.value.firstOrNull { it.id == id }
    if (macro != null) {
      playMacro(macro)
    } else {
      addTerminalLine("[MACRO] No macro found with ID '$id'.", TerminalLineType.ERROR)
    }
  }

  fun playMacro(macro: CommandMacro) {
    if (macro.actions.isEmpty()) {
      addTerminalLine("[MACRO] Macro '${macro.name}' contains no actions.", TerminalLineType.ERROR)
      return
    }

    viewModelScope.launch {
      isExecutingMacroInternal = true
      _macroNotification.value = "⚡ Running: ${macro.name}"
      addTerminalLine("[MACRO] ⚡ Executing '${macro.name}' (${macro.actions.size} steps)...", TerminalLineType.ACCENT)

      try {
        macro.actions.forEachIndexed { index, action ->
          _macroExecutionProgress.value = MacroExecutionProgress(
            macroId = macro.id,
            macroName = macro.name,
            currentStep = index + 1,
            totalSteps = macro.actions.size,
            currentAction = action
          )

          executeSingleMacroAction(action)
          if (action.delayMs > 0) {
            delay(action.delayMs)
          }
        }

        val updatedMacro = macro.copy(
          executionCount = macro.executionCount + 1,
          lastExecutedAt = System.currentTimeMillis()
        )
        saveMacro(updatedMacro)

        _macroNotification.value = "✓ Macro '${macro.name}' completed"
        addTerminalLine("[MACRO] ✓ '${macro.name}' finished successfully.", TerminalLineType.SUCCESS)
      } catch (e: Exception) {
        _macroNotification.value = "Error running '${macro.name}'"
        addTerminalLine("[MACRO] Execution failed: ${e.localizedMessage}", TerminalLineType.ERROR)
      } finally {
        isExecutingMacroInternal = false
        delay(1200)
        _macroExecutionProgress.value = null
        _macroNotification.value = null
      }
    }
  }

  private fun executeSingleMacroAction(action: MacroAction) {
    when (action.type) {
      MacroActionType.TERMINAL_COMMAND -> {
        executeTerminalCommand(action.payload)
      }
      MacroActionType.EDITOR_COMMAND -> {
        executeNeovimCommand(action.payload)
      }
      MacroActionType.EDITOR_INSERT -> {
        val idx = _activeBufferIndex.value
        if (idx in _neovimBuffers.value.indices) {
          val current = _neovimBuffers.value[idx]
          val updated = current.content + "\n" + action.payload
          _neovimBuffers.value = _neovimBuffers.value.mapIndexed { i, b ->
            if (i == idx) b.copy(content = updated, isModified = true) else b
          }
        }
      }
      MacroActionType.SWITCH_WORKSPACE -> {
        val ws = action.payload.toIntOrNull() ?: 1
        switchWorkspace(ws)
      }
      MacroActionType.LAUNCH_APP -> {
        val app = runCatching { AppType.valueOf(action.payload) }.getOrDefault(AppType.TERMINAL)
        openApp(app)
      }
      MacroActionType.LAYOUT_ACTION -> {
        when (action.payload) {
          "CYCLE_THEME" -> cycleTheme(true)
          "CYCLE_ICONSET" -> cycleIconSet(true)
          "TOGGLE_LAYOUT" -> toggleLayoutMode()
          "TOGGLE_FULLSCREEN" -> toggleFullscreen()
          "CLEAR_TERMINAL" -> _terminalLines.value = emptyList()
          else -> {}
        }
      }
    }
  }

  private fun handleMacroCommand(args: List<String>) {
    val sub = args.firstOrNull()?.lowercase() ?: "help"
    when (sub) {
      "help" -> {
        addTerminalLine("Omarchy Command Macro System (Hyprland Keyboard Automation):", TerminalLineType.ACCENT)
        addTerminalLine("  macro list                  - List all macros and keyboard shortcuts", TerminalLineType.OUTPUT)
        addTerminalLine("  macro record [name]         - Start recording terminal & editor actions", TerminalLineType.OUTPUT)
        addTerminalLine("  macro stop [name] [key]     - Stop recording and save macro", TerminalLineType.OUTPUT)
        addTerminalLine("  macro cancel                - Cancel current recording without saving", TerminalLineType.OUTPUT)
        addTerminalLine("  macro play <name|id>        - Run a macro by name or ID", TerminalLineType.OUTPUT)
        addTerminalLine("  macro run <name|id>         - Alias for 'macro play'", TerminalLineType.OUTPUT)
        addTerminalLine("  macro bind <name> <key>     - Bind or rebind shortcut (e.g. Super+F6)", TerminalLineType.OUTPUT)
        addTerminalLine("  macro delete <name|id>      - Delete a custom macro", TerminalLineType.OUTPUT)
        addTerminalLine("  macro gui / macro ui        - Open the visual Macro Studio modal", TerminalLineType.OUTPUT)
      }
      "list", "ls" -> {
        val list = _macros.value
        addTerminalLine("Registered Command Macros (${list.size}):", TerminalLineType.ACCENT)
        addTerminalLine("  NAME                     KEYBINDING       ACTIONS  RUNS", TerminalLineType.OUTPUT)
        addTerminalLine("  ---------------------------------------------------------", TerminalLineType.OUTPUT)
        list.forEach { m ->
          val padName = m.name.padEnd(24).take(24)
          val padKey = (if (m.shortcutLabel.isNotBlank()) "[${m.shortcutLabel}]" else "[None]").padEnd(16).take(16)
          val padActions = "${m.actions.size} steps".padEnd(9)
          val runs = "${m.executionCount}"
          val builtInTag = if (m.isBuiltIn) " (built-in)" else ""
          addTerminalLine("  $padName $padKey $padActions $runs$builtInTag", TerminalLineType.OUTPUT)
        }
      }
      "record", "rec" -> {
        val name = args.drop(1).joinToString(" ")
        startRecordingMacro(name)
      }
      "stop" -> {
        if (!_isRecordingMacro.value) {
          addTerminalLine("No macro recording is currently active. Use 'macro record' to begin.", TerminalLineType.ERROR)
          return
        }
        val rest = args.drop(1)
        val name = rest.firstOrNull() ?: _recordingMacroName.value
        val shortcut = rest.getOrNull(1) ?: ""
        val (keyCode, isSuper, isShift, isCtrl, isAlt) = parseShortcutCombo(shortcut)
        stopRecordingMacro(
          name = name,
          shortcutLabel = shortcut,
          keyCode = keyCode,
          requiresSuper = isSuper,
          requiresShift = isShift,
          requiresCtrl = isCtrl,
          requiresAlt = isAlt
        )
      }
      "cancel", "abort" -> {
        cancelRecordingMacro()
      }
      "play", "run", "exec" -> {
        val query = args.drop(1).joinToString(" ").trim()
        if (query.isBlank()) {
          addTerminalLine("Usage: macro play <name|id>", TerminalLineType.ERROR)
          return
        }
        val target = _macros.value.firstOrNull {
          it.id.equals(query, ignoreCase = true) ||
          it.name.equals(query, ignoreCase = true) ||
          it.name.contains(query, ignoreCase = true)
        }
        if (target != null) {
          playMacro(target)
        } else {
          addTerminalLine("No macro matched '$query'. Type 'macro list' to view available macros.", TerminalLineType.ERROR)
        }
      }
      "bind" -> {
        val namePart = args.getOrNull(1) ?: ""
        val keyPart = args.getOrNull(2) ?: ""
        if (namePart.isBlank() || keyPart.isBlank()) {
          addTerminalLine("Usage: macro bind <name> <shortcut> (e.g. macro bind \"Dev Cockpit\" Super+F6)", TerminalLineType.ERROR)
          return
        }
        val target = _macros.value.firstOrNull {
          it.name.contains(namePart, ignoreCase = true) || it.id.equals(namePart, ignoreCase = true)
        }
        if (target != null) {
          val (keyCode, isSuper, isShift, isCtrl, isAlt) = parseShortcutCombo(keyPart)
          updateMacroShortcut(target.id, keyPart, keyCode, isSuper, isShift, isCtrl, isAlt)
        } else {
          addTerminalLine("Macro '$namePart' not found.", TerminalLineType.ERROR)
        }
      }
      "delete", "rm" -> {
        val query = args.drop(1).joinToString(" ").trim()
        if (query.isBlank()) {
          addTerminalLine("Usage: macro delete <name|id>", TerminalLineType.ERROR)
          return
        }
        val target = _macros.value.firstOrNull {
          it.id.equals(query, ignoreCase = true) || it.name.equals(query, ignoreCase = true)
        }
        if (target != null) {
          deleteMacro(target.id)
        } else {
          addTerminalLine("No macro matched '$query'.", TerminalLineType.ERROR)
        }
      }
      "gui", "ui", "studio", "open" -> {
        openMacroManager()
        addTerminalLine("Opened Omarchy Macro Studio modal.", TerminalLineType.SUCCESS)
      }
      else -> {
        addTerminalLine("Unknown macro subcommand '$sub'. Type 'macro help' for options.", TerminalLineType.ERROR)
      }
    }
  }

  private fun parseShortcutCombo(combo: String): ShortcutParseResult {
    if (combo.isBlank()) return ShortcutParseResult(KeyEvent.KEYCODE_UNKNOWN, requiresSuper = true, requiresShift = false, requiresCtrl = false, requiresAlt = false)

    val lower = combo.lowercase()
    val isSuper = lower.contains("super") || lower.contains("meta") || lower.contains("mod")
    val isShift = lower.contains("shift")
    val isCtrl = lower.contains("ctrl")
    val isAlt = lower.contains("alt")

    val keyCode = when {
      lower.contains("f12") -> KeyEvent.KEYCODE_F12
      lower.contains("f11") -> KeyEvent.KEYCODE_F11
      lower.contains("f10") -> KeyEvent.KEYCODE_F10
      lower.contains("f9") -> KeyEvent.KEYCODE_F9
      lower.contains("f8") -> KeyEvent.KEYCODE_F8
      lower.contains("f7") -> KeyEvent.KEYCODE_F7
      lower.contains("f6") -> KeyEvent.KEYCODE_F6
      lower.contains("f5") -> KeyEvent.KEYCODE_F5
      lower.contains("f4") -> KeyEvent.KEYCODE_F4
      lower.contains("f3") -> KeyEvent.KEYCODE_F3
      lower.contains("f2") -> KeyEvent.KEYCODE_F2
      lower.contains("f1") -> KeyEvent.KEYCODE_F1
      lower.endsWith("t") -> KeyEvent.KEYCODE_T
      lower.endsWith("m") -> KeyEvent.KEYCODE_M
      lower.endsWith("r") -> KeyEvent.KEYCODE_R
      lower.endsWith("p") -> KeyEvent.KEYCODE_P
      lower.endsWith("b") -> KeyEvent.KEYCODE_B
      lower.endsWith("d") -> KeyEvent.KEYCODE_D
      lower.endsWith("k") -> KeyEvent.KEYCODE_K
      lower.endsWith("1") -> KeyEvent.KEYCODE_1
      lower.endsWith("2") -> KeyEvent.KEYCODE_2
      lower.endsWith("3") -> KeyEvent.KEYCODE_3
      lower.endsWith("4") -> KeyEvent.KEYCODE_4
      lower.endsWith("5") -> KeyEvent.KEYCODE_5
      else -> KeyEvent.KEYCODE_UNKNOWN
    }
    return ShortcutParseResult(keyCode, isSuper, isShift, isCtrl, isAlt)
  }

  private fun buildShortcutLabel(
    keyCode: Int,
    requiresSuper: Boolean,
    requiresShift: Boolean,
    requiresCtrl: Boolean,
    requiresAlt: Boolean
  ): String {
    val parts = mutableListOf<String>()
    if (requiresSuper) parts.add("Super")
    if (requiresCtrl) parts.add("Ctrl")
    if (requiresAlt) parts.add("Alt")
    if (requiresShift) parts.add("Shift")

    val keyName = when (keyCode) {
      KeyEvent.KEYCODE_F1 -> "F1"
      KeyEvent.KEYCODE_F2 -> "F2"
      KeyEvent.KEYCODE_F3 -> "F3"
      KeyEvent.KEYCODE_F4 -> "F4"
      KeyEvent.KEYCODE_F5 -> "F5"
      KeyEvent.KEYCODE_F6 -> "F6"
      KeyEvent.KEYCODE_F7 -> "F7"
      KeyEvent.KEYCODE_F8 -> "F8"
      KeyEvent.KEYCODE_F9 -> "F9"
      KeyEvent.KEYCODE_F10 -> "F10"
      KeyEvent.KEYCODE_F11 -> "F11"
      KeyEvent.KEYCODE_F12 -> "F12"
      KeyEvent.KEYCODE_A -> "A"
      KeyEvent.KEYCODE_B -> "B"
      KeyEvent.KEYCODE_C -> "C"
      KeyEvent.KEYCODE_D -> "D"
      KeyEvent.KEYCODE_E -> "E"
      KeyEvent.KEYCODE_F -> "F"
      KeyEvent.KEYCODE_G -> "G"
      KeyEvent.KEYCODE_H -> "H"
      KeyEvent.KEYCODE_I -> "I"
      KeyEvent.KEYCODE_J -> "J"
      KeyEvent.KEYCODE_K -> "K"
      KeyEvent.KEYCODE_L -> "L"
      KeyEvent.KEYCODE_M -> "M"
      KeyEvent.KEYCODE_N -> "N"
      KeyEvent.KEYCODE_O -> "O"
      KeyEvent.KEYCODE_P -> "P"
      KeyEvent.KEYCODE_Q -> "Q"
      KeyEvent.KEYCODE_R -> "R"
      KeyEvent.KEYCODE_S -> "S"
      KeyEvent.KEYCODE_T -> "T"
      KeyEvent.KEYCODE_U -> "U"
      KeyEvent.KEYCODE_V -> "V"
      KeyEvent.KEYCODE_W -> "W"
      KeyEvent.KEYCODE_X -> "X"
      KeyEvent.KEYCODE_Y -> "Y"
      KeyEvent.KEYCODE_Z -> "Z"
      KeyEvent.KEYCODE_1 -> "1"
      KeyEvent.KEYCODE_2 -> "2"
      KeyEvent.KEYCODE_3 -> "3"
      KeyEvent.KEYCODE_4 -> "4"
      KeyEvent.KEYCODE_5 -> "5"
      else -> "Key_$keyCode"
    }
    parts.add(keyName)
    return parts.joinToString(" + ")
  }

  fun dismissSplashScreen() {
    _isSplashScreenVisible.value = false
  }

  fun toggleMascotAssistant() {
    _isMascotAssistantVisible.value = !_isMascotAssistantVisible.value
  }

  fun dismissMascotAssistant() {
    _isMascotAssistantVisible.value = false
  }

  data class ShortcutParseResult(
    val keyCode: Int,
    val requiresSuper: Boolean,
    val requiresShift: Boolean,
    val requiresCtrl: Boolean,
    val requiresAlt: Boolean
  )
}


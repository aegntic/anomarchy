package com.example.omarchy.model

import android.view.KeyEvent
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Types of discrete user or system actions that can be recorded and replayed in a macro.
 */
enum class MacroActionType(
  val displayName: String,
  val defaultGlyph: String,
  val category: String
) {
  TERMINAL_COMMAND("Terminal Command", "󰆍", "Terminal"),
  EDITOR_COMMAND("Neovim Command", "", "Editor"),
  EDITOR_INSERT("Editor Text Insert", "󰏫", "Editor"),
  SWITCH_WORKSPACE("Switch Workspace", "󰒓", "Hyprland"),
  LAUNCH_APP("Launch Application", "󰍉", "Desktop"),
  LAYOUT_ACTION("Desktop Action", "󰕰", "Window Manager")
}

/**
 * An individual action step inside a command macro sequence.
 */
data class MacroAction(
  val type: MacroActionType,
  val payload: String,
  val delayMs: Long = 120L
) {
  fun displayTitle(): String {
    return when (type) {
      MacroActionType.TERMINAL_COMMAND -> "$ $payload"
      MacroActionType.EDITOR_COMMAND -> payload
      MacroActionType.EDITOR_INSERT -> "Insert \"${payload.take(24).replace("\n", "\\n")}${if (payload.length > 24) "..." else ""}\""
      MacroActionType.SWITCH_WORKSPACE -> "Workspace $payload"
      MacroActionType.LAUNCH_APP -> "Launch ${runCatching { AppType.valueOf(payload).displayName }.getOrDefault(payload)}"
      MacroActionType.LAYOUT_ACTION -> when (payload) {
        "CYCLE_THEME" -> "Cycle Color Scheme"
        "CYCLE_ICONSET" -> "Cycle Hyprland Icons"
        "TOGGLE_LAYOUT" -> "Toggle Split Layout"
        "TOGGLE_FULLSCREEN" -> "Toggle Fullscreen"
        "CLEAR_TERMINAL" -> "Clear Terminal"
        else -> payload
      }
    }
  }

  fun toJson(): JSONObject {
    return JSONObject().apply {
      put("type", type.name)
      put("payload", payload)
      put("delayMs", delayMs)
    }
  }

  companion object {
    fun fromJson(obj: JSONObject): MacroAction {
      val type = runCatching { MacroActionType.valueOf(obj.getString("type")) }.getOrDefault(MacroActionType.TERMINAL_COMMAND)
      val payload = obj.optString("payload", "")
      val delayMs = obj.optLong("delayMs", 120L)
      return MacroAction(type, payload, delayMs)
    }
  }
}

/**
 * A named macro sequence bound to an optional keyboard shortcut.
 */
data class CommandMacro(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val description: String = "",
  val shortcutLabel: String = "",
  val keyCode: Int = KeyEvent.KEYCODE_UNKNOWN,
  val requiresSuper: Boolean = true,
  val requiresShift: Boolean = false,
  val requiresCtrl: Boolean = false,
  val requiresAlt: Boolean = false,
  val actions: List<MacroAction> = emptyList(),
  val isBuiltIn: Boolean = false,
  val executionCount: Int = 0,
  val lastExecutedAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis()
) {

  fun matchesKeyEvent(
    code: Int,
    isMeta: Boolean,
    isShift: Boolean,
    isCtrl: Boolean,
    isAlt: Boolean
  ): Boolean {
    if (keyCode == KeyEvent.KEYCODE_UNKNOWN || keyCode != code) return false
    if (requiresSuper && !isMeta) return false
    if (!requiresSuper && isMeta) return false
    if (requiresShift && !isShift) return false
    if (requiresCtrl && !isCtrl) return false
    if (requiresAlt && !isAlt) return false
    return true
  }

  fun toJson(): JSONObject {
    val arr = JSONArray()
    actions.forEach { arr.put(it.toJson()) }

    return JSONObject().apply {
      put("id", id)
      put("name", name)
      put("description", description)
      put("shortcutLabel", shortcutLabel)
      put("keyCode", keyCode)
      put("requiresSuper", requiresSuper)
      put("requiresShift", requiresShift)
      put("requiresCtrl", requiresCtrl)
      put("requiresAlt", requiresAlt)
      put("isBuiltIn", isBuiltIn)
      put("executionCount", executionCount)
      put("lastExecutedAt", lastExecutedAt ?: 0L)
      put("createdAt", createdAt)
      put("actions", arr)
    }
  }

  companion object {
    fun fromJson(jsonStr: String): CommandMacro? {
      return runCatching {
        val obj = JSONObject(jsonStr)
        val actionsArray = obj.optJSONArray("actions") ?: JSONArray()
        val actionsList = mutableListOf<MacroAction>()
        for (i in 0 until actionsArray.length()) {
          val actObj = actionsArray.getJSONObject(i)
          actionsList.add(MacroAction.fromJson(actObj))
        }

        val lastExec = obj.optLong("lastExecutedAt", 0L)

        CommandMacro(
          id = obj.optString("id", UUID.randomUUID().toString()),
          name = obj.optString("name", "Untitled Macro"),
          description = obj.optString("description", ""),
          shortcutLabel = obj.optString("shortcutLabel", ""),
          keyCode = obj.optInt("keyCode", KeyEvent.KEYCODE_UNKNOWN),
          requiresSuper = obj.optBoolean("requiresSuper", true),
          requiresShift = obj.optBoolean("requiresShift", false),
          requiresCtrl = obj.optBoolean("requiresCtrl", false),
          requiresAlt = obj.optBoolean("requiresAlt", false),
          actions = actionsList,
          isBuiltIn = obj.optBoolean("isBuiltIn", false),
          executionCount = obj.optInt("executionCount", 0),
          lastExecutedAt = if (lastExec > 0L) lastExec else null,
          createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        )
      }.getOrNull()
    }
  }
}

/**
 * Status snapshot of a currently running macro execution.
 */
data class MacroExecutionProgress(
  val macroId: String,
  val macroName: String,
  val currentStep: Int,
  val totalSteps: Int,
  val currentAction: MacroAction?
)

/**
 * Stock out-of-the-box Hyprland macros adhering to keyboard-first workflow.
 */
object DefaultCommandMacros {
  fun getPresets(): List<CommandMacro> = listOf(
    CommandMacro(
      id = "macro-dev-cockpit",
      name = "Dev Cockpit",
      description = "Launch Terminal & omafetch, switch to Neovim and save buffer",
      shortcutLabel = "Super + F1",
      keyCode = KeyEvent.KEYCODE_F1,
      requiresSuper = true,
      isBuiltIn = true,
      actions = listOf(
        MacroAction(MacroActionType.LAUNCH_APP, AppType.TERMINAL.name),
        MacroAction(MacroActionType.TERMINAL_COMMAND, "omafetch", delayMs = 150L),
        MacroAction(MacroActionType.LAUNCH_APP, AppType.NEOVIM.name),
        MacroAction(MacroActionType.EDITOR_COMMAND, ":w", delayMs = 120L)
      )
    ),
    CommandMacro(
      id = "macro-rails-workflow",
      name = "Rails Scaffold & Test",
      description = "Scaffold a new Rails microservice and execute the test runner",
      shortcutLabel = "Super + F2",
      keyCode = KeyEvent.KEYCODE_F2,
      requiresSuper = true,
      isBuiltIn = true,
      actions = listOf(
        MacroAction(MacroActionType.LAUNCH_APP, AppType.TERMINAL.name),
        MacroAction(MacroActionType.TERMINAL_COMMAND, "rails new api_microservice", delayMs = 150L),
        MacroAction(MacroActionType.TERMINAL_COMMAND, "rails test", delayMs = 200L)
      )
    ),
    CommandMacro(
      id = "macro-save-and-status",
      name = "Quick Save & Git Status",
      description = "Save current active Neovim buffer and inspect git working tree",
      shortcutLabel = "Super + F3",
      keyCode = KeyEvent.KEYCODE_F3,
      requiresSuper = true,
      isBuiltIn = true,
      actions = listOf(
        MacroAction(MacroActionType.LAUNCH_APP, AppType.NEOVIM.name),
        MacroAction(MacroActionType.EDITOR_COMMAND, ":w", delayMs = 100L),
        MacroAction(MacroActionType.LAUNCH_APP, AppType.TERMINAL.name),
        MacroAction(MacroActionType.TERMINAL_COMMAND, "git status", delayMs = 150L)
      )
    ),
    CommandMacro(
      id = "macro-sys-diag",
      name = "System Diagnostics",
      description = "Switch to Workspace 4, focus btop monitor and display system kernel",
      shortcutLabel = "Super + F4",
      keyCode = KeyEvent.KEYCODE_F4,
      requiresSuper = true,
      isBuiltIn = true,
      actions = listOf(
        MacroAction(MacroActionType.SWITCH_WORKSPACE, "4"),
        MacroAction(MacroActionType.LAUNCH_APP, AppType.BTOP.name),
        MacroAction(MacroActionType.LAUNCH_APP, AppType.TERMINAL.name),
        MacroAction(MacroActionType.TERMINAL_COMMAND, "uname -a", delayMs = 150L)
      )
    ),
    CommandMacro(
      id = "macro-rice-zen",
      name = "Zen Rice Cycle",
      description = "Cycle to next color theme and icon set, clear terminal buffer",
      shortcutLabel = "Super + F5",
      keyCode = KeyEvent.KEYCODE_F5,
      requiresSuper = true,
      isBuiltIn = true,
      actions = listOf(
        MacroAction(MacroActionType.LAYOUT_ACTION, "CYCLE_THEME", delayMs = 100L),
        MacroAction(MacroActionType.LAYOUT_ACTION, "CYCLE_ICONSET", delayMs = 100L),
        MacroAction(MacroActionType.LAUNCH_APP, AppType.TERMINAL.name),
        MacroAction(MacroActionType.TERMINAL_COMMAND, "clear", delayMs = 80L)
      )
    )
  )
}

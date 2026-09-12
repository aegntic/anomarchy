package com.example.omarchy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.omarchy.model.AppType
import com.example.omarchy.model.CommandMacro
import com.example.omarchy.model.IconSetPreset
import com.example.omarchy.model.MacroAction
import com.example.omarchy.model.NoteItem
import com.example.omarchy.model.SplitRatioPreset
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.model.TilingControllerState
import com.example.omarchy.model.TilingSplitPreset
import com.example.omarchy.model.TilingZoneType
import com.example.omarchy.model.WindowInstance
import com.example.omarchy.model.WindowLayoutMode
import com.example.omarchy.state.NeovimBuffer
import org.json.JSONArray

/**
 * Top-level session state capturing the desktop layout mode,
 * active workspace, tiling controller settings, and theme.
 */
@Entity(tableName = "workspace_session")
data class WorkspaceSessionEntity(
  @PrimaryKey val id: Int = 1,
  val currentWorkspace: Int = 1,
  val layoutMode: String = WindowLayoutMode.SPLIT_VERTICAL.name,
  val focusedWindowId: String? = null,
  val isFullscreen: Boolean = false,
  val isHudOpen: Boolean = true,
  val isHudCollapsed: Boolean = false,
  val themePreset: String = ThemePreset.TOKYO_NIGHT.name,
  val iconSetPreset: String = IconSetPreset.NERD_FONTS.name,
  val tilingPreset: String = TilingSplitPreset.COCKPIT.name,
  val tilingRatioPreset: String = SplitRatioPreset.BALANCED.name,
  val tilingVisibleZones: String = "EDITOR,TERMINAL,ASSISTANT",
  val tilingMaximizedZone: String? = null,
  val tilingZoneOrder: String = "EDITOR,TERMINAL,ASSISTANT",
  val activeBufferIndex: Int = 0,
  val activeNoteId: String? = null,
  val lastSavedAt: Long = System.currentTimeMillis(),
  val sessionName: String = "Default Session"
) {
  fun getParsedLayoutMode(): WindowLayoutMode {
    return runCatching { WindowLayoutMode.valueOf(layoutMode) }.getOrDefault(WindowLayoutMode.SPLIT_VERTICAL)
  }

  fun getParsedThemePreset(): ThemePreset {
    return runCatching { ThemePreset.valueOf(themePreset) }.getOrDefault(ThemePreset.TOKYO_NIGHT)
  }

  fun getParsedIconSetPreset(): IconSetPreset {
    return runCatching { IconSetPreset.valueOf(iconSetPreset) }.getOrDefault(IconSetPreset.NERD_FONTS)
  }

  fun toTilingControllerState(): TilingControllerState {
    val preset = runCatching { TilingSplitPreset.valueOf(tilingPreset) }.getOrDefault(TilingSplitPreset.COCKPIT)
    val ratio = runCatching { SplitRatioPreset.valueOf(tilingRatioPreset) }.getOrDefault(SplitRatioPreset.BALANCED)
    val visible = tilingVisibleZones.split(",")
      .mapNotNull { name -> runCatching { TilingZoneType.valueOf(name.trim()) }.getOrNull() }
      .toSet().ifEmpty { setOf(TilingZoneType.EDITOR, TilingZoneType.TERMINAL, TilingZoneType.ASSISTANT) }
    val maxZone = tilingMaximizedZone?.let { name -> runCatching { TilingZoneType.valueOf(name.trim()) }.getOrNull() }
    val order = tilingZoneOrder.split(",")
      .mapNotNull { name -> runCatching { TilingZoneType.valueOf(name.trim()) }.getOrNull() }
      .ifEmpty { listOf(TilingZoneType.EDITOR, TilingZoneType.TERMINAL, TilingZoneType.ASSISTANT) }

    return TilingControllerState(
      preset = preset,
      ratioPreset = ratio,
      visibleZones = visible,
      maximizedZone = maxZone,
      zoneOrder = order,
      focusedZone = order.firstOrNull() ?: TilingZoneType.EDITOR
    )
  }
}

/**
 * Persisted window instances across workspaces.
 */
@Entity(tableName = "windows")
data class WindowEntity(
  @PrimaryKey val id: String,
  val appType: String,
  val title: String,
  val workspaceId: Int,
  val isFocused: Boolean,
  val isFloating: Boolean,
  val createdAt: Long,
  val orderIndex: Int
) {
  fun toWindowInstance(): WindowInstance {
    val parsedType = runCatching { AppType.valueOf(appType) }.getOrDefault(AppType.TERMINAL)
    return WindowInstance(
      id = id,
      appType = parsedType,
      title = title,
      workspaceId = workspaceId,
      isFocused = isFocused,
      isFloating = isFloating,
      createdAt = createdAt
    )
  }

  companion object {
    fun fromWindowInstance(instance: WindowInstance, orderIndex: Int): WindowEntity {
      return WindowEntity(
        id = instance.id,
        appType = instance.appType.name,
        title = instance.title,
        workspaceId = instance.workspaceId,
        isFocused = instance.isFocused,
        isFloating = instance.isFloating,
        createdAt = instance.createdAt,
        orderIndex = orderIndex
      )
    }
  }
}

/**
 * Persisted Neovim open file buffers.
 */
@Entity(tableName = "buffers")
data class BufferEntity(
  @PrimaryKey val filename: String,
  val language: String,
  val content: String,
  val isModified: Boolean,
  val bufferOrder: Int
) {
  fun toNeovimBuffer(): NeovimBuffer {
    return NeovimBuffer(
      filename = filename,
      language = language,
      content = content,
      isModified = isModified
    )
  }

  companion object {
    fun fromNeovimBuffer(buf: NeovimBuffer, order: Int): BufferEntity {
      return BufferEntity(
        filename = buf.filename,
        language = buf.language,
        content = buf.content,
        isModified = buf.isModified,
        bufferOrder = order
      )
    }
  }
}

/**
 * Persisted Obsidian notes and documents.
 */
@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey val id: String,
  val title: String,
  val content: String,
  val tag: String,
  val updatedAt: String,
  val noteOrder: Int
) {
  fun toNoteItem(): NoteItem {
    return NoteItem(
      id = id,
      title = title,
      content = content,
      tag = tag,
      updatedAt = updatedAt
    )
  }

  companion object {
    fun fromNoteItem(item: NoteItem, order: Int): NoteEntity {
      return NoteEntity(
        id = item.id,
        title = item.title,
        content = item.content,
        tag = item.tag,
        updatedAt = item.updatedAt,
        noteOrder = order
      )
    }
  }
}

/**
 * Complete snapshot container for atomic workspace persistence.
 */
data class WorkspaceSnapshot(
  val session: WorkspaceSessionEntity,
  val windows: List<WindowEntity>,
  val buffers: List<BufferEntity>,
  val notes: List<NoteEntity>,
  val macros: List<MacroEntity> = emptyList()
)

/**
 * Persisted Command Macro definition with serialized sequence of steps and custom keybinding.
 */
@Entity(tableName = "command_macros")
data class MacroEntity(
  @PrimaryKey val id: String,
  val name: String,
  val description: String,
  val shortcutLabel: String,
  val keyCode: Int,
  val requiresSuper: Boolean,
  val requiresShift: Boolean,
  val requiresCtrl: Boolean,
  val requiresAlt: Boolean,
  val actionsJson: String,
  val isBuiltIn: Boolean,
  val executionCount: Int,
  val lastExecutedAt: Long?,
  val createdAt: Long
) {
  fun toCommandMacro(): CommandMacro {
    val actions = runCatching {
      val arr = JSONArray(actionsJson)
      val list = mutableListOf<MacroAction>()
      for (i in 0 until arr.length()) {
        list.add(MacroAction.fromJson(arr.getJSONObject(i)))
      }
      list
    }.getOrDefault(emptyList())

    return CommandMacro(
      id = id,
      name = name,
      description = description,
      shortcutLabel = shortcutLabel,
      keyCode = keyCode,
      requiresSuper = requiresSuper,
      requiresShift = requiresShift,
      requiresCtrl = requiresCtrl,
      requiresAlt = requiresAlt,
      actions = actions,
      isBuiltIn = isBuiltIn,
      executionCount = executionCount,
      lastExecutedAt = lastExecutedAt,
      createdAt = createdAt
    )
  }

  companion object {
    fun fromCommandMacro(macro: CommandMacro): MacroEntity {
      val arr = JSONArray()
      macro.actions.forEach { arr.put(it.toJson()) }
      return MacroEntity(
        id = macro.id,
        name = macro.name,
        description = macro.description,
        shortcutLabel = macro.shortcutLabel,
        keyCode = macro.keyCode,
        requiresSuper = macro.requiresSuper,
        requiresShift = macro.requiresShift,
        requiresCtrl = macro.requiresCtrl,
        requiresAlt = macro.requiresAlt,
        actionsJson = arr.toString(),
        isBuiltIn = macro.isBuiltIn,
        executionCount = macro.executionCount,
        lastExecutedAt = macro.lastExecutedAt,
        createdAt = macro.createdAt
      )
    }
  }
}


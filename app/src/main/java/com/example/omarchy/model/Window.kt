package com.example.omarchy.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppType(
  val displayName: String,
  val binaryName: String,
  val defaultTitle: String,
  val description: String,
  val category: String
) {
  TERMINAL("Alacritty", "alacritty", "omarchy@archlinux:~", "GPU-accelerated terminal emulator", "System"),
  NEOVIM("Neovim", "nvim", "nvim - init.lua", "Hyperextensible Vim-based text editor", "Development"),
  OBSIDIAN("Obsidian", "obsidian", "Obsidian Vault - Omarchy Notes", "Local markdown knowledge base", "Productivity"),
  BTOP("btop++", "btop", "btop v1.3.2 - System Monitor", "Resource monitor with responsive terminal graphs", "System"),
  AGENT("Agentic Linux", "agentd", "Omarchy AI Coding Agent", "Autonomous agent interface & system copilot", "AI"),
  BROWSER("Chromium", "chromium", "Chromium - Omarchy Portal", "Minimal Wayland web browser", "Internet");

  val icon: ImageVector
    get() = when (this) {
      TERMINAL -> Icons.Default.Terminal
      NEOVIM -> Icons.Default.Code
      OBSIDIAN -> Icons.Default.Description
      BTOP -> Icons.Default.MonitorHeart
      AGENT -> Icons.Default.Psychology
      BROWSER -> Icons.Default.Language
    }
}

enum class WindowLayoutMode(val displayName: String) {
  SPLIT_VERTICAL("Vertical Split"),
  SPLIT_HORIZONTAL("Horizontal Split"),
  MASTER_STACK("Master / Stack"),
  TRI_ZONE_DEV("Dev Tri-Zone"),
  FULLSCREEN("Fullscreen"),
  FLOATING("Floating")
}

data class WindowInstance(
  val id: String,
  val appType: AppType,
  val title: String = appType.defaultTitle,
  val workspaceId: Int = 1,
  val isFocused: Boolean = false,
  val isFloating: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

data class ProcessItem(
  val pid: Int,
  val name: String,
  val user: String,
  val cpuPercent: Float,
  val memPercent: Float,
  val state: String = "S"
)

data class NoteItem(
  val id: String,
  val title: String,
  val content: String,
  val tag: String,
  val updatedAt: String
)

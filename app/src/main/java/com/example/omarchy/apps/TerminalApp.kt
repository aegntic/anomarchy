package com.example.omarchy.apps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.state.TerminalLine
import com.example.omarchy.ui.TerminalEmulator

/**
 * Terminal application for Omarchy, powered by the TerminalEmulator composable
 * providing Xterm.js emulation and native ANSI grid views.
 */
@Composable
fun TerminalApp(
  theme: OmarchyThemeConfig,
  lines: List<TerminalLine>,
  onExecuteCommand: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  TerminalEmulator(
    theme = theme,
    lines = lines,
    onExecuteCommand = onExecuteCommand,
    modifier = modifier
  )
}


package com.example.omarchy.ui

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.CommandMacro
import com.example.omarchy.model.MacroAction
import com.example.omarchy.model.MacroActionType
import com.example.omarchy.model.MacroExecutionProgress
import com.example.omarchy.model.OmarchyThemeConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MacroManagerModal(
  theme: OmarchyThemeConfig,
  macros: List<CommandMacro>,
  isRecording: Boolean,
  recordedActions: List<MacroAction>,
  recordingSeconds: Int,
  recordingName: String,
  executionProgress: MacroExecutionProgress?,
  onStartRecording: (name: String) -> Unit,
  onStopRecording: (name: String, shortcut: String, keyCode: Int, isSuper: Boolean, isShift: Boolean, isCtrl: Boolean, isAlt: Boolean, desc: String) -> Unit,
  onCancelRecording: () -> Unit,
  onDeleteRecordedAction: (Int) -> Unit,
  onPlayMacro: (CommandMacro) -> Unit,
  onDeleteMacro: (String) -> Unit,
  onUpdateShortcut: (id: String, label: String, keyCode: Int, isSuper: Boolean, isShift: Boolean, isCtrl: Boolean, isAlt: Boolean) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var newMacroName by remember(recordingName) { mutableStateOf(recordingName.ifBlank { "Custom Macro" }) }
  var selectedKeySlot by remember { mutableStateOf("F5") }
  var reqSuper by remember { mutableStateOf(true) }
  var reqShift by remember { mutableStateOf(false) }
  var reqCtrl by remember { mutableStateOf(false) }
  var reqAlt by remember { mutableStateOf(false) }

  var macroToRebind by remember { mutableStateOf<CommandMacro?>(null) }
  var expandedMacroId by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.78f))
      .clickable { onClose() }
      .testTag("macro_manager_scrim"),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.88f)
        .clip(RoundedCornerShape(12.dp))
        .background(theme.surfaceColor)
        .border(1.5.dp, theme.accentPrimary.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
        .clickable(enabled = false) {}
        .padding(16.dp)
        .testTag("macro_manager_modal")
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(theme.accentPrimary.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = null,
                tint = theme.accentPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                  text = "COMMAND MACRO STUDIO",
                  color = theme.textPrimary,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                if (isRecording) {
                  RecordingBadge(theme = theme, seconds = recordingSeconds, actionCount = recordedActions.size)
                }
              }
              Text(
                text = "Record sequences of terminal & editor actions • Hyprland keyboard automation",
                color = theme.textSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          IconButton(
            onClick = onClose,
            modifier = Modifier
              .size(32.dp)
              .testTag("close_macro_modal_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = theme.textSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        // Execution progress notification if active
        if (executionProgress != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(theme.accentPrimary.copy(alpha = 0.18f))
              .border(1.dp, theme.accentPrimary, RoundedCornerShape(8.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "⚡ EXECUTING: ${executionProgress.macroName}",
                  color = theme.accentPrimary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "Step ${executionProgress.currentStep}/${executionProgress.totalSteps}: [${executionProgress.currentAction?.type?.displayName ?: ""}] ${executionProgress.currentAction?.payload ?: ""}",
                  color = theme.textPrimary,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        // Active Recording Panel or Start Recording Controls
        if (isRecording) {
          RecordingControllerPanel(
            theme = theme,
            recordedActions = recordedActions,
            recordingName = newMacroName,
            onNameChange = { newMacroName = it },
            selectedKeySlot = selectedKeySlot,
            onKeySlotChange = { selectedKeySlot = it },
            reqSuper = reqSuper,
            onReqSuperChange = { reqSuper = it },
            reqShift = reqShift,
            onReqShiftChange = { reqShift = it },
            reqCtrl = reqCtrl,
            onReqCtrlChange = { reqCtrl = it },
            reqAlt = reqAlt,
            onReqAltChange = { reqAlt = it },
            onDeleteAction = onDeleteRecordedAction,
            onStop = {
              val keyCode = mapKeySlotToKeyCode(selectedKeySlot)
              val label = buildShortcutLabelString(selectedKeySlot, reqSuper, reqShift, reqCtrl, reqAlt)
              onStopRecording(
                newMacroName,
                label,
                keyCode,
                reqSuper,
                reqShift,
                reqCtrl,
                reqAlt,
                "Sequence of ${recordedActions.size} actions"
              )
            },
            onCancel = onCancelRecording
          )
          Spacer(modifier = Modifier.height(12.dp))
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "REGISTERED MACROS (${macros.size})",
              color = theme.textSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )

            Button(
              onClick = {
                onStartRecording("Macro_${System.currentTimeMillis() % 10000}")
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = theme.terminalRed.copy(alpha = 0.9f),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.testTag("start_recording_macro_button")
            ) {
              Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Record New Macro",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        // Macro List
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(macros, key = { it.id }) { macro ->
            val isExpanded = expandedMacroId == macro.id
            MacroCard(
              macro = macro,
              theme = theme,
              isExpanded = isExpanded,
              onToggleExpand = {
                expandedMacroId = if (isExpanded) null else macro.id
              },
              onPlay = { onPlayMacro(macro) },
              onRebind = { macroToRebind = macro },
              onDelete = { onDeleteMacro(macro.id) }
            )
          }
        }
      }
    }

    // Rebind Modal Dialog
    if (macroToRebind != null) {
      RebindShortcutDialog(
        macro = macroToRebind!!,
        theme = theme,
        onConfirm = { label, keyCode, superReq, shiftReq, ctrlReq, altReq ->
          onUpdateShortcut(macroToRebind!!.id, label, keyCode, superReq, shiftReq, ctrlReq, altReq)
          macroToRebind = null
        },
        onDismiss = { macroToRebind = null }
      )
    }
  }
}

@Composable
private fun RecordingBadge(
  theme: OmarchyThemeConfig,
  seconds: Int,
  actionCount: Int
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val dotColor by infiniteTransition.animateColor(
    initialValue = theme.terminalRed,
    targetValue = theme.terminalRed.copy(alpha = 0.3f),
    animationSpec = infiniteRepeatable(
      animation = tween(600),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot_alpha"
  )

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(theme.terminalRed.copy(alpha = 0.15f))
      .border(1.dp, theme.terminalRed.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(dotColor)
    )
    val mins = seconds / 60
    val secs = seconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)
    Text(
      text = "REC $timeFormatted ($actionCount steps)",
      color = theme.terminalRed,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun RecordingControllerPanel(
  theme: OmarchyThemeConfig,
  recordedActions: List<MacroAction>,
  recordingName: String,
  onNameChange: (String) -> Unit,
  selectedKeySlot: String,
  onKeySlotChange: (String) -> Unit,
  reqSuper: Boolean,
  onReqSuperChange: (Boolean) -> Unit,
  reqShift: Boolean,
  onReqShiftChange: (Boolean) -> Unit,
  reqCtrl: Boolean,
  onReqCtrlChange: (Boolean) -> Unit,
  reqAlt: Boolean,
  onReqAltChange: (Boolean) -> Unit,
  onDeleteAction: (Int) -> Unit,
  onStop: () -> Unit,
  onCancel: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(theme.terminalRed.copy(alpha = 0.08f))
      .border(1.dp, theme.terminalRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(12.dp)
  ) {
    Text(
      text = "LIVE CAPTURE IN PROGRESS",
      color = theme.terminalRed,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Text(
      text = "Perform actions anywhere in Omarchy (run commands, open apps, switch workspaces, edit in Neovim) - they will appear below automatically.",
      color = theme.textSecondary,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Recorded action chips
    if (recordedActions.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(theme.surfaceColor.copy(alpha = 0.5f))
          .border(1.dp, theme.surfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Waiting for actions... (0 captured so far)",
          color = theme.textSecondary,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        recordedActions.forEachIndexed { index, action ->
          ActionChip(
            index = index,
            action = action,
            theme = theme,
            onDelete = { onDeleteAction(index) }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Save Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = recordingName,
        onValueChange = onNameChange,
        label = { Text("Macro Name", fontSize = 10.sp) },
        singleLine = true,
        modifier = Modifier.weight(1f),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = theme.textPrimary,
          unfocusedTextColor = theme.textPrimary,
          focusedBorderColor = theme.accentPrimary,
          unfocusedBorderColor = theme.surfaceBorder
        )
      )

      // Key slot picker
      Column {
        Text("Bind Key", fontSize = 10.sp, color = theme.textSecondary, fontFamily = FontFamily.Monospace)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(theme.surfaceColor)
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "T", "R", "P", "B").forEach { slot ->
            val isSelected = selectedKeySlot == slot
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(if (isSelected) theme.accentPrimary else Color.Transparent)
                .clickable { onKeySlotChange(slot) }
                .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Text(
                text = slot,
                color = if (isSelected) theme.surfaceColor else theme.textPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Modifiers checkboxes
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ModifierCheckbox(label = "Super", checked = reqSuper, onCheckedChange = onReqSuperChange, theme = theme)
      ModifierCheckbox(label = "Shift", checked = reqShift, onCheckedChange = onReqShiftChange, theme = theme)
      ModifierCheckbox(label = "Ctrl", checked = reqCtrl, onCheckedChange = onReqCtrlChange, theme = theme)
      ModifierCheckbox(label = "Alt", checked = reqAlt, onCheckedChange = onReqAltChange, theme = theme)

      Spacer(modifier = Modifier.weight(1f))

      Button(
        onClick = onCancel,
        colors = ButtonDefaults.buttonColors(
          containerColor = theme.surfaceColor,
          contentColor = theme.textSecondary
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
      ) {
        Text("Cancel", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }

      Button(
        onClick = onStop,
        enabled = recordedActions.isNotEmpty(),
        colors = ButtonDefaults.buttonColors(
          containerColor = theme.accentPrimary,
          contentColor = theme.surfaceColor
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.testTag("save_recorded_macro_button")
      ) {
        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Save & Bind (${recordedActions.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
    }
  }
}

@Composable
private fun ModifierCheckbox(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  theme: OmarchyThemeConfig
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.clickable { onCheckedChange(!checked) }
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(
        checkedColor = theme.accentPrimary,
        uncheckedColor = theme.surfaceBorder,
        checkmarkColor = theme.surfaceColor
      ),
      modifier = Modifier.size(24.dp)
    )
    Text(
      text = label,
      color = if (checked) theme.accentPrimary else theme.textSecondary,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun ActionChip(
  index: Int,
  action: MacroAction,
  theme: OmarchyThemeConfig,
  onDelete: () -> Unit
) {
  val (color, badge) = when (action.type) {
    MacroActionType.TERMINAL_COMMAND -> Pair(theme.terminalGreen, "TERM")
    MacroActionType.EDITOR_COMMAND -> Pair(theme.accentSecondary, "NVIM")
    MacroActionType.EDITOR_INSERT -> Pair(theme.terminalCyan, "TEXT")
    MacroActionType.SWITCH_WORKSPACE -> Pair(theme.accentPrimary, "WS")
    MacroActionType.LAUNCH_APP -> Pair(theme.terminalYellow, "APP")
    MacroActionType.LAYOUT_ACTION -> Pair(theme.accentPrimary, "LAYOUT")
  }

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(theme.surfaceColor)
      .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = "#${index + 1}",
      color = theme.textSecondary,
      fontSize = 9.sp,
      fontFamily = FontFamily.Monospace
    )
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(2.dp))
        .background(color.copy(alpha = 0.2f))
        .padding(horizontal = 3.dp, vertical = 1.dp)
    ) {
      Text(
        text = badge,
        color = color,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
    Text(
      text = action.payload.take(18) + if (action.payload.length > 18) "..." else "",
      color = theme.textPrimary,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace
    )
    Icon(
      imageVector = Icons.Default.Close,
      contentDescription = "Remove Step",
      tint = theme.textSecondary,
      modifier = Modifier
        .size(12.dp)
        .clickable { onDelete() }
    )
  }
}

@Composable
private fun MacroCard(
  macro: CommandMacro,
  theme: OmarchyThemeConfig,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  onPlay: () -> Unit,
  onRebind: () -> Unit,
  onDelete: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(theme.surfaceColor.copy(alpha = 0.8f))
      .border(1.dp, theme.surfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
      .padding(10.dp)
      .testTag("macro_card_${macro.id}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Keybinding Keycap Badge
        if (macro.shortcutLabel.isNotBlank()) {
          KeycapBadge(label = macro.shortcutLabel, theme = theme)
        } else {
          KeycapBadge(label = "Unbound", theme = theme, isUnbound = true)
        }

        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = macro.name,
              color = theme.textPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            if (macro.isBuiltIn) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(3.dp))
                  .background(theme.accentSecondary.copy(alpha = 0.2f))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text("PRESET", color = theme.accentSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
          }
          Text(
            text = "${macro.description} • ${macro.actions.size} action steps • ${macro.executionCount} runs",
            color = theme.textSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // Actions
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Run Button
        Button(
          onClick = onPlay,
          colors = ButtonDefaults.buttonColors(
            containerColor = theme.accentPrimary,
            contentColor = theme.surfaceColor
          ),
          shape = RoundedCornerShape(4.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.testTag("run_macro_${macro.id}")
        ) {
          Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(2.dp))
          Text("Run", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        // Rebind Button
        IconButton(
          onClick = onRebind,
          modifier = Modifier.size(28.dp).testTag("rebind_macro_${macro.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Rebind shortcut",
            tint = theme.accentSecondary,
            modifier = Modifier.size(14.dp)
          )
        }

        // Expand/Inspect Button
        IconButton(
          onClick = onToggleExpand,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "Inspect Steps",
            tint = if (isExpanded) theme.accentPrimary else theme.textSecondary,
            modifier = Modifier.size(14.dp)
          )
        }

        // Delete (if custom)
        if (!macro.isBuiltIn) {
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp).testTag("delete_macro_${macro.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete macro",
              tint = theme.terminalRed,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }

    // Expanded sequence details
    AnimatedVisibility(visible = isExpanded) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(theme.desktopBackground.copy(alpha = 0.5f))
          .padding(8.dp)
      ) {
        Text(
          text = "MACRO ACTION SEQUENCE:",
          color = theme.textSecondary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        macro.actions.forEachIndexed { i, act ->
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "${i + 1}.",
              color = theme.textSecondary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "[${act.type.displayName}]",
              color = theme.accentPrimary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = act.payload,
              color = theme.textPrimary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            if (act.delayMs > 0) {
              Text(
                text = "(+${act.delayMs}ms)",
                color = theme.textSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun KeycapBadge(
  label: String,
  theme: OmarchyThemeConfig,
  isUnbound: Boolean = false,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(if (isUnbound) theme.surfaceColor else theme.accentPrimary.copy(alpha = 0.15f))
      .border(
        1.dp,
        if (isUnbound) theme.surfaceBorder else theme.accentPrimary.copy(alpha = 0.6f),
        RoundedCornerShape(4.dp)
      )
      .padding(horizontal = 6.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = if (isUnbound) theme.textSecondary else theme.accentPrimary,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun RebindShortcutDialog(
  macro: CommandMacro,
  theme: OmarchyThemeConfig,
  onConfirm: (label: String, keyCode: Int, isSuper: Boolean, isShift: Boolean, isCtrl: Boolean, isAlt: Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedKey by remember { mutableStateOf("F1") }
  var superReq by remember { mutableStateOf(macro.requiresSuper) }
  var shiftReq by remember { mutableStateOf(macro.requiresShift) }
  var ctrlReq by remember { mutableStateOf(macro.requiresCtrl) }
  var altReq by remember { mutableStateOf(macro.requiresAlt) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.5f))
      .clickable { onDismiss() },
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .clip(RoundedCornerShape(10.dp))
        .background(theme.surfaceColor)
        .border(1.dp, theme.accentSecondary, RoundedCornerShape(10.dp))
        .clickable(enabled = false) {}
        .padding(16.dp)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "REBIND SHORTCUT: ${macro.name}",
          color = theme.textPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "Select a shortcut trigger key and modifiers for hardware and touch activation.",
          color = theme.textSecondary,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )

        // Key selector
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "T", "R", "P", "B", "M", "D").forEach { k ->
            val isSelected = selectedKey == k
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSelected) theme.accentSecondary else theme.desktopBackground)
                .border(1.dp, if (isSelected) theme.accentSecondary else theme.surfaceBorder, RoundedCornerShape(4.dp))
                .clickable { selectedKey = k }
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Text(
                text = k,
                color = if (isSelected) theme.surfaceColor else theme.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // Modifiers
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          ModifierCheckbox(label = "Super", checked = superReq, onCheckedChange = { superReq = it }, theme = theme)
          ModifierCheckbox(label = "Shift", checked = shiftReq, onCheckedChange = { shiftReq = it }, theme = theme)
          ModifierCheckbox(label = "Ctrl", checked = ctrlReq, onCheckedChange = { ctrlReq = it }, theme = theme)
          ModifierCheckbox(label = "Alt", checked = altReq, onCheckedChange = { altReq = it }, theme = theme)
        }

        val previewLabel = buildShortcutLabelString(selectedKey, superReq, shiftReq, ctrlReq, altReq)
        Text(
          text = "Preview: [$previewLabel]",
          color = theme.accentPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = theme.textSecondary)
          ) {
            Text("Cancel", fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.width(6.dp))
          Button(
            onClick = {
              val code = mapKeySlotToKeyCode(selectedKey)
              onConfirm(previewLabel, code, superReq, shiftReq, ctrlReq, altReq)
            },
            colors = ButtonDefaults.buttonColors(containerColor = theme.accentSecondary, contentColor = theme.surfaceColor)
          ) {
            Text("Apply Binding", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
        }
      }
    }
  }
}

private fun mapKeySlotToKeyCode(slot: String): Int {
  return when (slot.uppercase()) {
    "F1" -> KeyEvent.KEYCODE_F1
    "F2" -> KeyEvent.KEYCODE_F2
    "F3" -> KeyEvent.KEYCODE_F3
    "F4" -> KeyEvent.KEYCODE_F4
    "F5" -> KeyEvent.KEYCODE_F5
    "F6" -> KeyEvent.KEYCODE_F6
    "F7" -> KeyEvent.KEYCODE_F7
    "F8" -> KeyEvent.KEYCODE_F8
    "F9" -> KeyEvent.KEYCODE_F9
    "F10" -> KeyEvent.KEYCODE_F10
    "F11" -> KeyEvent.KEYCODE_F11
    "F12" -> KeyEvent.KEYCODE_F12
    "T" -> KeyEvent.KEYCODE_T
    "R" -> KeyEvent.KEYCODE_R
    "P" -> KeyEvent.KEYCODE_P
    "B" -> KeyEvent.KEYCODE_B
    "M" -> KeyEvent.KEYCODE_M
    "D" -> KeyEvent.KEYCODE_D
    else -> KeyEvent.KEYCODE_UNKNOWN
  }
}

private fun buildShortcutLabelString(
  key: String,
  reqSuper: Boolean,
  reqShift: Boolean,
  reqCtrl: Boolean,
  reqAlt: Boolean
): String {
  val parts = mutableListOf<String>()
  if (reqSuper) parts.add("Super")
  if (reqCtrl) parts.add("Ctrl")
  if (reqAlt) parts.add("Alt")
  if (reqShift) parts.add("Shift")
  parts.add(key)
  return parts.joinToString(" + ")
}

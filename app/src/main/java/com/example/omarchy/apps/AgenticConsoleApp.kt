package com.example.omarchy.apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.state.AgentMessage
import com.example.omarchy.state.AgentSender

@Composable
fun AgenticConsoleApp(
  theme: OmarchyThemeConfig,
  messages: List<AgentMessage>,
  isThinking: Boolean,
  onSendMessage: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var promptInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size, isThinking) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(theme.surfaceColor)
      .padding(8.dp)
  ) {
    // Header Info Banner
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(theme.surfaceVariant)
        .border(1.dp, theme.accentSecondary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = theme.accentSecondary,
          modifier = Modifier.size(16.dp)
        )
        Column {
          Text(
            text = "Agentic Linux Copilot (agentd)",
            color = theme.textPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Second user of your machine • Hyprland & Shell integration",
            color = theme.textMuted,
            fontSize = 9.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(theme.terminalGreen.copy(alpha = 0.2f))
          .padding(horizontal = 5.dp, vertical = 2.dp)
      ) {
        Text(
          text = "ACTIVE",
          color = theme.terminalGreen,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Suggested Agent Prompts
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      AgentPromptPill("Scaffold Rails model", theme) { onSendMessage("Scaffold Rails model Post") }
      AgentPromptPill("Switch to Catppuccin", theme) { onSendMessage("Switch theme to Catppuccin") }
      AgentPromptPill("Check system CPU", theme) { onSendMessage("Check system resources with btop") }
      AgentPromptPill("Toggle window split", theme) { onSendMessage("Toggle window split layout") }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Message List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(messages) { msg ->
        AgentMessageCard(message = msg, theme = theme)
      }

      if (isThinking) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(14.dp),
              color = theme.accentSecondary,
              strokeWidth = 2.dp
            )
            Text(
              text = "Agent reasoning & executing commands...",
              color = theme.textSecondary,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Prompt Input
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(theme.surfaceVariant)
        .border(1.dp, theme.surfaceBorder, RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      BasicTextField(
        value = promptInput,
        onValueChange = { promptInput = it },
        modifier = Modifier
          .weight(1f)
          .testTag("agent_prompt_input"),
        textStyle = TextStyle(
          color = theme.textPrimary,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace
        ),
        cursorBrush = SolidColor(theme.accentSecondary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
          onSend = {
            if (promptInput.isNotBlank()) {
              onSendMessage(promptInput)
              promptInput = ""
            }
          }
        ),
        decorationBox = { innerTextField ->
          if (promptInput.isEmpty()) {
            Text(
              text = "Instruct Omarchy Agent (e.g. 'theme tokyo', 'generate rails controller')...",
              color = theme.textMuted,
              fontSize = 11.sp
            )
          }
          innerTextField()
        }
      )

      IconButton(
        onClick = {
          if (promptInput.isNotBlank()) {
            onSendMessage(promptInput)
            promptInput = ""
          }
        },
        modifier = Modifier.size(28.dp).testTag("agent_send_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send",
          tint = theme.accentSecondary,
          modifier = Modifier.size(14.dp)
        )
      }
    }
  }
}

@Composable
private fun AgentMessageCard(
  message: AgentMessage,
  theme: OmarchyThemeConfig
) {
  val isUser = message.sender == AgentSender.USER
  val isTool = message.sender == AgentSender.TOOL
  val isSystem = message.sender == AgentSender.SYSTEM

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(
        when {
          isUser -> theme.accentPrimary.copy(alpha = 0.15f)
          isTool -> theme.terminalBackground
          isSystem -> theme.surfaceVariant.copy(alpha = 0.5f)
          else -> theme.surfaceVariant
        }
      )
      .border(
        1.dp,
        when {
          isUser -> theme.accentPrimary.copy(alpha = 0.3f)
          isTool -> theme.terminalGreen.copy(alpha = 0.3f)
          else -> theme.surfaceBorder
        },
        RoundedCornerShape(6.dp)
      )
      .padding(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = when {
            isUser -> Icons.Default.Terminal
            isTool -> Icons.Default.Code
            else -> Icons.Default.AutoAwesome
          },
          contentDescription = null,
          tint = when {
            isUser -> theme.accentPrimary
            isTool -> theme.terminalGreen
            else -> theme.accentSecondary
          },
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = when {
            isUser -> "You"
            isTool -> "System Tool Execution"
            isSystem -> "System"
            else -> "Omarchy Agent"
          },
          color = when {
            isUser -> theme.accentPrimary
            isTool -> theme.terminalGreen
            else -> theme.accentSecondary
          },
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }

      Text(
        text = message.timestamp,
        color = theme.textMuted,
        fontSize = 9.sp
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = message.text,
      color = theme.textPrimary,
      fontSize = 11.5.sp,
      lineHeight = 16.sp
    )

    if (message.commandPreview != null) {
      Spacer(modifier = Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(4.dp))
          .background(theme.terminalBackground)
          .padding(6.dp)
      ) {
        Text(
          text = message.commandPreview,
          color = theme.terminalCyan,
          fontSize = 10.5.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
private fun AgentPromptPill(
  text: String,
  theme: OmarchyThemeConfig,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(theme.surfaceVariant)
      .border(1.dp, theme.surfaceBorder, RoundedCornerShape(4.dp))
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = theme.accentSecondary,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

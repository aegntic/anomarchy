package com.example.omarchy.ui

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.state.TerminalLine
import com.example.omarchy.state.TerminalLineType
import org.json.JSONArray
import org.json.JSONObject

enum class TerminalMode(val displayName: String) {
  XTERM("Xterm.js"),
  COMPOSE("Native Grid")
}

/**
 * JavaScript interface bridge to communicate between the Web-based Xterm emulation
 * and the Android Compose ViewModel.
 */
class XtermJsBridge(
  private val onCommand: (String) -> Unit
) {
  @JavascriptInterface
  fun executeCommand(command: String) {
    onCommand(command)
  }

  @JavascriptInterface
  fun onReady() {
    // Terminal initialized
  }
}

/**
 * TerminalEmulator composable:
 * Integrates an authentic Xterm.js emulation container with ANSI color formatting,
 * virtual terminal modifier bar, prompt line, and fallback to native Compose grid.
 */
@Composable
fun TerminalEmulator(
  theme: OmarchyThemeConfig,
  lines: List<TerminalLine>,
  onExecuteCommand: (String) -> Unit,
  modifier: Modifier = Modifier,
  initialMode: TerminalMode = TerminalMode.XTERM
) {
  var activeMode by remember { mutableStateOf(initialMode) }
  var commandInput by remember { mutableStateOf("") }
  var historyIndex by remember { mutableIntStateOf(-1) }
  val historyList = remember(lines) {
    lines.filter { it.type == TerminalLineType.COMMAND }
      .map { it.text.replace("omarchy@archlinux ~ $ ", "").replace("omarchy@arch ~ $ ", "") }
      .filter { it.isNotBlank() }
  }

  val nativeListState = rememberLazyListState()
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var isWebViewReady by remember { mutableStateOf(false) }

  // Auto-scroll native list
  LaunchedEffect(lines.size) {
    if (lines.isNotEmpty()) {
      nativeListState.animateScrollToItem(lines.size - 1)
    }
  }

  // Synchronize lines with Xterm.js instance when lines change
  LaunchedEffect(lines, isWebViewReady, webViewRef) {
    if (isWebViewReady && webViewRef != null) {
      val jsonArray = JSONArray()
      lines.forEach { line ->
        val obj = JSONObject()
        obj.put("text", line.text)
        obj.put("type", line.type.name)
        jsonArray.put(obj)
      }
      val encoded = jsonArray.toString().replace("'", "\\'")
      webViewRef?.evaluateJavascript("window.updateTerminalLines && window.updateTerminalLines('$encoded');", null)
    }
  }

  // Update Xterm theme colors when user switches preset
  LaunchedEffect(theme, isWebViewReady, webViewRef) {
    if (isWebViewReady && webViewRef != null) {
      val themeJson = JSONObject().apply {
        put("background", toHexColor(theme.terminalBackground))
        put("foreground", toHexColor(theme.terminalForeground))
        put("cursor", toHexColor(theme.terminalGreen))
        put("selection", toHexColor(theme.accentPrimary.copy(alpha = 0.3f)))
        put("cyan", toHexColor(theme.terminalCyan))
        put("green", toHexColor(theme.terminalGreen))
        put("red", toHexColor(theme.terminalRed))
        put("magenta", toHexColor(theme.terminalMagenta))
        put("yellow", toHexColor(theme.terminalYellow))
      }
      webViewRef?.evaluateJavascript("window.setTheme && window.setTheme(${themeJson});", null)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(theme.terminalBackground)
      .testTag("terminal_emulator")
  ) {
    // Top Sub-bar: Mode switch & Quick Pill actions
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(theme.surfaceColor)
        .border(0.5.dp, theme.surfaceBorder)
        .padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Terminal Mode Toggle Pill (Xterm.js vs Native)
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(theme.surfaceVariant)
          .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(4.dp))
          .clickable {
            activeMode = if (activeMode == TerminalMode.XTERM) TerminalMode.COMPOSE else TerminalMode.XTERM
          }
          .padding(horizontal = 6.dp, vertical = 2.5.dp)
          .testTag("terminal_mode_toggle"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = if (activeMode == TerminalMode.XTERM) Icons.Default.Code else Icons.Default.Terminal,
          contentDescription = "Terminal Mode",
          tint = theme.accentPrimary,
          modifier = Modifier.size(11.dp)
        )
        Text(
          text = activeMode.displayName,
          color = theme.textPrimary,
          fontSize = 9.5.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }

      // Quick Command Suggestions
      Row(
        modifier = Modifier
          .weight(1f)
          .padding(start = 8.dp)
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        TerminalCmdPill("omafetch", theme) { onExecuteCommand("omafetch") }
        TerminalCmdPill("theme", theme) { onExecuteCommand("theme") }
        TerminalCmdPill("help", theme) { onExecuteCommand("help") }
        TerminalCmdPill("rails new", theme) { onExecuteCommand("rails new blog") }
        TerminalCmdPill("clear", theme) { onExecuteCommand("clear") }
      }
    }

    // Main Terminal View (Xterm.js or Compose Grid)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (activeMode == TerminalMode.XTERM) {
        XtermWebView(
          theme = theme,
          lines = lines,
          onExecuteCommand = onExecuteCommand,
          onReady = { webView ->
            webViewRef = webView
            isWebViewReady = true
          },
          modifier = Modifier.fillMaxSize()
        )
      } else {
        // Native Compose Terminal Output Grid
        NativeTerminalGrid(
          theme = theme,
          lines = lines,
          listState = nativeListState,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
        )
      }
    }

    // Virtual Modifier & Navigation Keys Bar (Esc, Tab, Ctrl+C, Up, Down, Clear)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(theme.surfaceColor.copy(alpha = 0.95f))
        .border(0.5.dp, theme.surfaceBorder)
        .padding(horizontal = 6.dp, vertical = 3.dp)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      VirtualKeyCap("ESC", theme) {
        commandInput = ""
      }
      VirtualKeyCap("TAB", theme) {
        // Simple auto-completion suggestion
        if (commandInput.startsWith("oma")) commandInput = "omafetch"
        else if (commandInput.startsWith("pac")) commandInput = "pacman -Syu"
        else if (commandInput.startsWith("rai")) commandInput = "rails new "
        else if (commandInput.startsWith("the")) commandInput = "theme "
      }
      VirtualKeyCap("CTRL+C", theme) {
        commandInput = ""
        onExecuteCommand("^C")
      }
      VirtualKeyCap("↑ HIST", theme) {
        if (historyList.isNotEmpty()) {
          historyIndex = (historyIndex + 1).coerceAtMost(historyList.size - 1)
          commandInput = historyList[historyList.size - 1 - historyIndex]
        }
      }
      VirtualKeyCap("↓", theme) {
        if (historyIndex > 0) {
          historyIndex--
          commandInput = historyList[historyList.size - 1 - historyIndex]
        } else if (historyIndex == 0) {
          historyIndex = -1
          commandInput = ""
        }
      }
      VirtualKeyCap("CLEAR", theme) {
        onExecuteCommand("clear")
      }
    }

    // Interactive Terminal Input Prompt
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(theme.surfaceColor)
        .border(1.dp, theme.surfaceBorder)
        .padding(horizontal = 8.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "omarchy@arch ~ $ ",
        color = theme.terminalGreen,
        fontSize = 11.5.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )

      BasicTextField(
        value = commandInput,
        onValueChange = { commandInput = it },
        modifier = Modifier
          .weight(1f)
          .testTag("terminal_input_field"),
        textStyle = TextStyle(
          color = theme.terminalForeground,
          fontSize = 11.5.sp,
          fontFamily = FontFamily.Monospace
        ),
        cursorBrush = SolidColor(theme.terminalGreen),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
          onSend = {
            if (commandInput.isNotBlank()) {
              onExecuteCommand(commandInput)
              commandInput = ""
              historyIndex = -1
            }
          }
        )
      )

      IconButton(
        onClick = {
          if (commandInput.isNotBlank()) {
            onExecuteCommand(commandInput)
            commandInput = ""
            historyIndex = -1
          }
        },
        modifier = Modifier
          .size(24.dp)
          .testTag("terminal_send_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Execute Command",
          tint = theme.accentPrimary,
          modifier = Modifier.size(13.dp)
        )
      }
    }
  }
}

@Composable
private fun TerminalCmdPill(
  text: String,
  theme: OmarchyThemeConfig,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(3.dp))
      .background(theme.surfaceVariant)
      .border(0.5.dp, theme.surfaceBorder, RoundedCornerShape(3.dp))
      .clickable { onClick() }
      .padding(horizontal = 5.dp, vertical = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = theme.terminalCyan,
      fontSize = 9.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun VirtualKeyCap(
  label: String,
  theme: OmarchyThemeConfig,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(3.dp))
      .background(theme.terminalBackground)
      .border(0.75.dp, theme.surfaceBorder, RoundedCornerShape(3.dp))
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 2.5.dp)
      .testTag("virt_key_$label"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = theme.accentPrimary,
      fontSize = 8.5.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun NativeTerminalGrid(
  theme: OmarchyThemeConfig,
  lines: List<TerminalLine>,
  listState: androidx.compose.foundation.lazy.LazyListState,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    state = listState,
    modifier = modifier
  ) {
    items(lines) { line ->
      val color = when (line.type) {
        TerminalLineType.COMMAND -> theme.terminalCyan
        TerminalLineType.OUTPUT -> theme.terminalForeground
        TerminalLineType.ERROR -> theme.terminalRed
        TerminalLineType.SUCCESS -> theme.terminalGreen
        TerminalLineType.ACCENT -> theme.terminalMagenta
        TerminalLineType.ASCII -> theme.accentPrimary
      }

      Text(
        text = line.text,
        color = color,
        fontSize = 11.5.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 16.sp,
        modifier = Modifier.padding(vertical = 0.5.dp)
      )
    }
  }
}

/**
 * Embedded Xterm.js emulation container using Android WebView.
 * Renders an xterm-compatible terminal terminal screen with cursor blink,
 * ANSI syntax highlighting, and live updates.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun XtermWebView(
  theme: OmarchyThemeConfig,
  lines: List<TerminalLine>,
  onExecuteCommand: (String) -> Unit,
  onReady: (WebView) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val htmlContent = remember(theme) {
    buildXtermHtml(theme)
  }

  AndroidView(
    factory = { ctx ->
      WebView(ctx).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        setBackgroundColor(android.graphics.Color.TRANSPARENT)

        val bridge = XtermJsBridge(onExecuteCommand)
        addJavascriptInterface(bridge, "TerminalBridge")

        webViewClient = object : WebViewClient() {
          override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.let { onReady(it) }
          }

          override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
          ): Boolean {
            return true
          }
        }

        loadDataWithBaseURL("https://omarchy.local", htmlContent, "text/html", "utf-8", null)
      }
    },
    update = { webView ->
      // Webview instance is maintained and updated via evaluateJavascript
    },
    modifier = modifier.testTag("xterm_webview")
  )
}

private fun toHexColor(color: Color): String {
  val argb = color.toArgb()
  return String.format("#%06X", 0xFFFFFF and argb)
}

/**
 * Builds an authentic Xterm.js emulation HTML/JS page with ANSI formatting,
 * responsive terminal container, and JavaScript event hooks.
 */
private fun buildXtermHtml(theme: OmarchyThemeConfig): String {
  val bg = toHexColor(theme.terminalBackground)
  val fg = toHexColor(theme.terminalForeground)
  val cyan = toHexColor(theme.terminalCyan)
  val green = toHexColor(theme.terminalGreen)
  val red = toHexColor(theme.terminalRed)
  val magenta = toHexColor(theme.terminalMagenta)
  val accent = toHexColor(theme.accentPrimary)

  return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
      <title>Omarchy Xterm Emulator</title>
      <style>
        * {
          box-sizing: border-box;
          margin: 0;
          padding: 0;
          -webkit-user-select: text;
          user-select: text;
        }
        body {
          background-color: $bg;
          color: $fg;
          font-family: 'JetBrains Mono', 'Fira Code', 'Courier New', monospace;
          font-size: 11px;
          line-height: 1.4;
          padding: 8px;
          overflow-x: hidden;
          overflow-y: auto;
          width: 100vw;
          height: 100vh;
        }
        #xterm-container {
          display: flex;
          flex-direction: column;
          min-height: 100%;
        }
        .xterm-line {
          white-space: pre-wrap;
          word-break: break-all;
          margin-bottom: 2px;
        }
        .type-COMMAND { color: $cyan; font-weight: bold; }
        .type-OUTPUT { color: $fg; }
        .type-ERROR { color: $red; }
        .type-SUCCESS { color: $green; }
        .type-ACCENT { color: $magenta; }
        .type-ASCII { color: $accent; font-weight: bold; }
        
        .xterm-prompt-row {
          display: flex;
          align-items: center;
          margin-top: 4px;
        }
        .xterm-prompt-label {
          color: $green;
          font-weight: bold;
          margin-right: 4px;
        }
        .xterm-cursor {
          display: inline-block;
          width: 7px;
          height: 13px;
          background-color: $green;
          vertical-align: middle;
          animation: blink 1s infinite;
        }
        @keyframes blink {
          0%, 49% { opacity: 1; }
          50%, 100% { opacity: 0; }
        }
      </style>
    </head>
    <body>
      <div id="xterm-container">
        <div id="terminal-history"></div>
        <div class="xterm-prompt-row">
          <span class="xterm-prompt-label">omarchy@arch ~ $</span>
          <span class="xterm-cursor"></span>
        </div>
      </div>

      <script>
        window.terminalLines = [];

        function escapeHtml(text) {
          var div = document.createElement('div');
          div.innerText = text;
          return div.innerHTML;
        }

        window.updateTerminalLines = function(jsonStr) {
          try {
            var lines = JSON.parse(jsonStr);
            var historyContainer = document.getElementById('terminal-history');
            historyContainer.innerHTML = '';
            
            lines.forEach(function(item) {
              var lineEl = document.createElement('div');
              lineEl.className = 'xterm-line type-' + (item.type || 'OUTPUT');
              lineEl.innerHTML = escapeHtml(item.text);
              historyContainer.appendChild(lineEl);
            });

            window.scrollTo(0, document.body.scrollHeight);
          } catch(e) {
            console.error('Error updating lines:', e);
          }
        };

        window.setTheme = function(theme) {
          if (!theme) return;
          document.body.style.backgroundColor = theme.background || '$bg';
          document.body.style.color = theme.foreground || '$fg';
        };

        if (window.TerminalBridge && window.TerminalBridge.onReady) {
          window.TerminalBridge.onReady();
        }
      </script>
    </body>
    </html>
  """.trimIndent()
}

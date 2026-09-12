package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.state.TerminalLine
import com.example.omarchy.state.TerminalLineType
import com.example.omarchy.ui.TerminalEmulator
import com.example.omarchy.ui.TerminalMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TerminalEmulatorTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val testTheme = OmarchyThemeConfig.fromPreset(ThemePreset.TOKYO_NIGHT)

  @Test
  fun testTerminalEmulatorInteractions() {
    var executedCmd = ""

    val testLines = listOf(
      TerminalLine("Welcome to Omarchy Linux", TerminalLineType.ACCENT),
      TerminalLine("omarchy@archlinux ~ $ help", TerminalLineType.COMMAND),
      TerminalLine("Available commands: omafetch, theme, rails", TerminalLineType.OUTPUT)
    )

    composeTestRule.setContent {
      TerminalEmulator(
        theme = testTheme,
        lines = testLines,
        onExecuteCommand = { executedCmd = it },
        initialMode = TerminalMode.COMPOSE
      )
    }

    // Verify main container is displayed
    composeTestRule.onNodeWithTag("terminal_emulator").assertIsDisplayed()

    // Verify input field exists and accepts text
    composeTestRule.onNodeWithTag("terminal_input_field").performTextInput("omafetch")
    composeTestRule.onNodeWithTag("terminal_send_button").performClick()

    assertEquals("omafetch", executedCmd)

    // Test mode toggle
    composeTestRule.onNodeWithTag("terminal_mode_toggle").performClick()
  }
}

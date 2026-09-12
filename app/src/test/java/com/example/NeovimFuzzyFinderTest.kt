package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.omarchy.apps.NeovimApp
import com.example.omarchy.model.FileCategory
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.TelescopeMode
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.model.WorkspaceFileCatalog
import com.example.omarchy.state.NeovimBuffer
import com.example.omarchy.ui.NeovimCommandPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w800dp-h1280dp")
class NeovimFuzzyFinderTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val theme = OmarchyThemeConfig.fromPreset(ThemePreset.TOKYO_NIGHT)

  @Test
  fun testFuzzyMatchAlgorithm() {
    // Exact match
    val exactMatch = WorkspaceFileCatalog.fuzzyMatch("init.lua", "init.lua")
    assertTrue("Exact match should succeed", exactMatch.isMatch)
    assertTrue("Exact match should have positive score", exactMatch.score > 0)

    // Acronym / Subsequence match
    val subMatch = WorkspaceFileCatalog.fuzzyMatch("hypconf", "hyprland.conf")
    assertTrue("Subsequence should match", subMatch.isMatch)

    val luaMatch = WorkspaceFileCatalog.fuzzyMatch("initlua", "nvim/init.lua")
    assertTrue("initlua should match nvim/init.lua", luaMatch.isMatch)

    // Negative match
    val noMatch = WorkspaceFileCatalog.fuzzyMatch("zzxxqq", "article.rb")
    assertFalse("Non-matching substring should fail", noMatch.isMatch)

    // Empty query matches all
    val emptyMatch = WorkspaceFileCatalog.fuzzyMatch("", "anything.txt")
    assertTrue("Empty query should always match", emptyMatch.isMatch)
  }

  @Test
  fun testCatalogFilesAndCategories() {
    val allFiles = WorkspaceFileCatalog.workspaceFiles
    assertTrue("Catalog should contain files", allFiles.isNotEmpty())

    val configFiles = allFiles.filter { it.category == FileCategory.CONFIG }
    assertTrue("Config files should be present", configFiles.isNotEmpty())

    val nvimInit = WorkspaceFileCatalog.getFileByPath("init.lua")
    assertNotNull("nvim init.lua should exist in catalog", nvimInit)
    assertEquals("lua", nvimInit?.language)

    val commands = WorkspaceFileCatalog.paletteCommands
    assertTrue("Palette commands should contain Telescope", commands.any { it.command.contains("Telescope") })
  }

  @Test
  fun testNeovimAppRendersTelescopeTriggers() {
    val testBuffers = listOf(
      NeovimBuffer(filename = "main.rb", language = "ruby", content = "puts 'hello'", isModified = false),
      NeovimBuffer(filename = "init.lua", language = "lua", content = "vim.opt.number = true", isModified = true)
    )

    composeTestRule.setContent {
      NeovimApp(
        theme = theme,
        buffers = testBuffers,
        activeBufferIndex = 0,
        neovimMode = "NORMAL",
        onSelectBuffer = {},
        onContentChange = {},
        onModeChange = {}
      )
    }

    // Check tabs and buttons exist
    composeTestRule.onNodeWithTag("nvim_telescope_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nvim_buffers_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nvim_commands_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nvim_status_telescope").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nvim_buffer_tab_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nvim_buffer_tab_1").assertIsDisplayed()
  }

  @Test
  fun testNeovimCommandPaletteUIInteraction() {
    val testBuffers = listOf(
      NeovimBuffer(filename = "app/models/article.rb", language = "ruby", content = "class Article < ApplicationRecord\nend")
    )

    var selectedFile: String? = null
    var closed = false

    composeTestRule.setContent {
      NeovimCommandPalette(
        theme = theme,
        buffers = testBuffers,
        activeBufferIndex = 0,
        initialMode = TelescopeMode.FIND_FILES,
        onOpenFile = { selectedFile = it },
        onSelectBuffer = {},
        onCloseBuffer = {},
        onExecuteCommand = {},
        onClose = { closed = true }
      )
    }

    // Modal should be visible
    composeTestRule.onNodeWithTag("telescope_modal").assertIsDisplayed()
    composeTestRule.onNodeWithTag("telescope_input").assertIsDisplayed()

    // Type a query in search
    composeTestRule.onNodeWithTag("telescope_input").performTextInput("hyprland")

    // Results should show item 0 which matches hyprland
    composeTestRule.onNodeWithTag("telescope_item_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("telescope_item_0").performClick()

    assertNotNull("Selected file should not be null", selectedFile)
    assertTrue("Selected file should contain hyprland", selectedFile?.contains("hyprland") == true)
  }

  @Test
  fun testNeovimAppLaunchesAndDismissesTelescope() {
    val testBuffers = listOf(
      NeovimBuffer(filename = "main.rb", language = "ruby", content = "puts 'hello'")
    )

    var openedFile: String? = null

    composeTestRule.setContent {
      NeovimApp(
        theme = theme,
        buffers = testBuffers,
        activeBufferIndex = 0,
        neovimMode = "NORMAL",
        onSelectBuffer = {},
        onContentChange = {},
        onModeChange = {},
        onOpenFile = { openedFile = it }
      )
    }

    // Click Telescope button
    composeTestRule.onNodeWithTag("nvim_telescope_btn").performClick()

    // Palette modal should now be shown
    composeTestRule.onNodeWithTag("telescope_modal").assertIsDisplayed()

    // Close button dismisses modal
    composeTestRule.onNodeWithTag("telescope_close_button").performClick()

    // Editor should be displayed again
    composeTestRule.onNodeWithTag("neovim_editor_field").assertIsDisplayed()
  }
}

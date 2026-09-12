package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.omarchy.data.BufferEntity
import com.example.omarchy.data.NoteEntity
import com.example.omarchy.data.OmarchyDatabase
import com.example.omarchy.data.OmarchyWorkspaceRepository
import com.example.omarchy.data.WindowEntity
import com.example.omarchy.data.WorkspaceSessionEntity
import com.example.omarchy.data.WorkspaceSnapshot
import com.example.omarchy.model.AppType
import com.example.omarchy.model.IconSetPreset
import com.example.omarchy.model.OmarchyThemeConfig
import com.example.omarchy.model.ThemePreset
import com.example.omarchy.model.WindowLayoutMode
import com.example.omarchy.ui.ControlCenterModal
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w800dp-h1280dp")
class WorkspacePersistenceTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private lateinit var db: OmarchyDatabase
  private lateinit var repository: OmarchyWorkspaceRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, OmarchyDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = OmarchyWorkspaceRepository(db.workspaceDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun testRoomDatabaseSavesAndRestoresWorkspaceSnapshot() = runBlocking {
    val session = WorkspaceSessionEntity(
      id = 1,
      currentWorkspace = 2,
      layoutMode = WindowLayoutMode.TRI_ZONE_DEV.name,
      focusedWindowId = "win-editor-1",
      isFullscreen = false,
      isHudOpen = true,
      isHudCollapsed = false,
      themePreset = ThemePreset.TOKYO_NIGHT.name,
      iconSetPreset = IconSetPreset.MINIMAL_GEOMETRIC.name,
      tilingPreset = "DEV_TRIAD",
      tilingRatioPreset = "DEV_GOLDEN",
      tilingVisibleZones = "EDITOR,TERMINAL,ASSISTANT",
      tilingMaximizedZone = null,
      tilingZoneOrder = "EDITOR,TERMINAL,ASSISTANT",
      activeBufferIndex = 1,
      activeNoteId = "note-omarchy-1",
      lastSavedAt = System.currentTimeMillis(),
      sessionName = "Omarchy Test Session"
    )

    val windows = listOf(
      WindowEntity(
        id = "win-term-1",
        appType = AppType.TERMINAL.name,
        title = "omarchy@archlinux:~ (zsh)",
        workspaceId = 2,
        isFocused = false,
        isFloating = false,
        createdAt = 1000L,
        orderIndex = 0
      ),
      WindowEntity(
        id = "win-editor-1",
        appType = AppType.NEOVIM.name,
        title = "main.rs - Neovim",
        workspaceId = 2,
        isFocused = true,
        isFloating = false,
        createdAt = 1001L,
        orderIndex = 1
      )
    )

    val buffers = listOf(
      BufferEntity(
        filename = "main.rs",
        language = "rust",
        content = "fn main() { println!(\"Hello Omarchy!\"); }",
        isModified = true,
        bufferOrder = 0
      ),
      BufferEntity(
        filename = "config.lua",
        language = "lua",
        content = "vim.opt.number = true\nvim.opt.relativenumber = true",
        isModified = false,
        bufferOrder = 1
      )
    )

    val notes = listOf(
      NoteEntity(
        id = "note-omarchy-1",
        title = "Omarchy Architecture",
        content = "# Omarchy Architecture\nRoom SQLite persistence integrated.",
        tag = "#linux",
        updatedAt = "Today",
        noteOrder = 0
      )
    )

    val snapshot = WorkspaceSnapshot(
      session = session,
      windows = windows,
      buffers = buffers,
      notes = notes
    )

    // Save snapshot
    repository.saveWorkspaceSnapshot(snapshot)

    // Verify retrieval
    val restored = repository.loadWorkspaceSnapshot()
    assertNotNull("Restored snapshot must not be null", restored)
    assertEquals(2, restored!!.session.currentWorkspace)
    assertEquals(WindowLayoutMode.TRI_ZONE_DEV.name, restored.session.layoutMode)
    assertEquals("win-editor-1", restored.session.focusedWindowId)
    assertEquals(IconSetPreset.MINIMAL_GEOMETRIC, restored.session.getParsedIconSetPreset())

    // Assert windows
    assertEquals(2, restored.windows.size)
    assertEquals("win-term-1", restored.windows[0].id)
    assertEquals(AppType.TERMINAL.name, restored.windows[0].appType)
    assertEquals("win-editor-1", restored.windows[1].id)
    assertEquals(AppType.NEOVIM.name, restored.windows[1].appType)

    // Assert Neovim buffers
    assertEquals(2, restored.buffers.size)
    assertEquals("main.rs", restored.buffers[0].filename)
    assertEquals("config.lua", restored.buffers[1].filename)
    assertTrue(restored.buffers[0].content.contains("Hello Omarchy!"))

    // Assert Obsidian notes
    assertEquals(1, restored.notes.size)
    assertEquals("note-omarchy-1", restored.notes[0].id)
    assertEquals("Omarchy Architecture", restored.notes[0].title)

    // Clear database
    repository.clearSavedWorkspace()
    val cleared = repository.loadWorkspaceSnapshot()
    assertNull("After clearing, snapshot must be null", cleared)
  }

  @Test
  fun testControlCenterModalPersistenceUI() {
    var saveClicked = false
    var restoreClicked = false
    var clearClicked = false
    val theme = OmarchyThemeConfig.fromPreset(ThemePreset.TOKYO_NIGHT)

    composeTestRule.setContent {
      ControlCenterModal(
        theme = theme,
        currentLayoutMode = WindowLayoutMode.TRI_ZONE_DEV,
        isFullscreen = false,
        isPersisted = true,
        lastSavedTime = "12:45:00",
        persistenceStatus = "Synced (12:45:00)",
        onSelectTheme = {},
        onToggleLayoutMode = {},
        onToggleFullscreen = {},
        onSaveWorkspace = { saveClicked = true },
        onRestoreWorkspace = { restoreClicked = true },
        onClearWorkspace = { clearClicked = true },
        onClose = {}
      )
    }

    composeTestRule.onNodeWithTag("save_workspace_button").assertIsDisplayed().performClick()
    assertTrue("Save button should trigger callback", saveClicked)

    composeTestRule.onNodeWithTag("restore_workspace_button").assertIsDisplayed().performClick()
    assertTrue("Restore button should trigger callback", restoreClicked)

    composeTestRule.onNodeWithTag("clear_workspace_button").assertIsDisplayed().performClick()
    assertTrue("Clear button should trigger callback", clearClicked)
  }

  @Test
  fun testThemingAndIconSetUI() {
    var selectedTheme: ThemePreset? = null
    var selectedIconSet: IconSetPreset? = null
    var cycleThemeCalled = false
    var cycleIconSetCalled = false

    val theme = OmarchyThemeConfig.fromPreset(ThemePreset.TOKYO_NIGHT, IconSetPreset.NERD_FONTS)

    composeTestRule.setContent {
      ControlCenterModal(
        theme = theme,
        currentLayoutMode = WindowLayoutMode.SPLIT_VERTICAL,
        isFullscreen = false,
        onSelectTheme = { selectedTheme = it },
        onSelectIconSet = { selectedIconSet = it },
        onCycleTheme = { cycleThemeCalled = true },
        onCycleIconSet = { cycleIconSetCalled = true },
        onToggleLayoutMode = {},
        onToggleFullscreen = {},
        onClose = {}
      )
    }

    composeTestRule.onNodeWithTag("theme_preset_catppuccin").assertIsDisplayed().performClick()
    assertEquals("Should select Catppuccin theme", ThemePreset.CATPPUCCIN, selectedTheme)

    composeTestRule.onNodeWithTag("icon_set_preset_minimal_geometric").assertIsDisplayed().performClick()
    assertEquals("Should select Hypr Geometric icon set", IconSetPreset.MINIMAL_GEOMETRIC, selectedIconSet)

    composeTestRule.onNodeWithTag("cycle_theme_button").assertIsDisplayed().performClick()
    assertTrue("Cycle theme button should be clicked", cycleThemeCalled)

    composeTestRule.onNodeWithTag("cycle_icon_set_button").assertIsDisplayed().performClick()
    assertTrue("Cycle icon set button should be clicked", cycleIconSetCalled)
  }
}

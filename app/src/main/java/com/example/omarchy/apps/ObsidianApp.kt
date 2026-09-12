package com.example.omarchy.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omarchy.model.NoteItem
import com.example.omarchy.model.OmarchyThemeConfig

@Composable
fun ObsidianApp(
  theme: OmarchyThemeConfig,
  notes: List<NoteItem>,
  activeNoteId: String?,
  isPreviewMode: Boolean,
  onSelectNote: (String) -> Unit,
  onContentChange: (String) -> Unit,
  onCreateNote: () -> Unit,
  onTogglePreview: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showSidebar by remember { mutableStateOf(false) }
  val currentNote = notes.firstOrNull { it.id == activeNoteId } ?: notes.firstOrNull()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(theme.surfaceColor)
  ) {
    // Top Obsidian App Toolbar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
        .background(theme.surfaceVariant)
        .border(0.5.dp, theme.surfaceBorder)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        IconButton(
          onClick = { showSidebar = !showSidebar },
          modifier = Modifier.size(26.dp).testTag("obsidian_sidebar_toggle")
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Vault Sidebar",
            tint = theme.accentPrimary,
            modifier = Modifier.size(16.dp)
          )
        }

        Text(
          text = currentNote?.title ?: "No Note Selected",
          color = theme.textPrimary,
          fontSize = 12.sp,
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Bold
        )

        currentNote?.let {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(theme.accentSecondary.copy(alpha = 0.2f))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = it.tag,
              color = theme.accentSecondary,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Toggle Edit/Preview
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPreviewMode) theme.accentPrimary else theme.surfaceColor)
            .clickable { onTogglePreview() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("obsidian_preview_toggle"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Icon(
              imageVector = if (isPreviewMode) Icons.Default.Visibility else Icons.Default.Edit,
              contentDescription = null,
              tint = if (isPreviewMode) Color.White else theme.textSecondary,
              modifier = Modifier.size(11.dp)
            )
            Text(
              text = if (isPreviewMode) "Preview" else "Edit",
              color = if (isPreviewMode) Color.White else theme.textSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        // New Note Button
        IconButton(
          onClick = { onCreateNote() },
          modifier = Modifier.size(26.dp).testTag("obsidian_new_note_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New Note",
            tint = theme.terminalGreen,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Main Body: Optional Drawer + Note Content
    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      // Sidebar if open
      if (showSidebar) {
        Column(
          modifier = Modifier
            .width(180.dp)
            .fillMaxSize()
            .background(theme.surfaceVariant.copy(alpha = 0.95f))
            .border(0.5.dp, theme.surfaceBorder)
            .padding(6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 6.dp)
          ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(14.dp))
            Text("Omarchy Vault", color = theme.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            items(notes) { note ->
              val isSelected = note.id == activeNoteId
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (isSelected) theme.accentPrimary.copy(alpha = 0.2f) else Color.Transparent)
                  .clickable {
                    onSelectNote(note.id)
                    showSidebar = false
                  }
                  .padding(6.dp)
                  .testTag("obsidian_note_${note.id}")
              ) {
                Column {
                  Text(
                    text = note.title,
                    color = if (isSelected) theme.accentPrimary else theme.textPrimary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                  Text(
                    text = note.updatedAt,
                    color = theme.textMuted,
                    fontSize = 9.sp
                  )
                }
              }
            }
          }
        }
      }

      // Note Editor / Markdown Preview
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
          .padding(12.dp)
      ) {
        if (currentNote != null) {
          if (isPreviewMode) {
            // Rendered Markdown preview
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              currentNote.content.split("\n").forEach { line ->
                when {
                  line.startsWith("# ") -> {
                    Text(
                      text = line.removePrefix("# "),
                      color = theme.accentPrimary,
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.SansSerif
                    )
                  }
                  line.startsWith("## ") -> {
                    Text(
                      text = line.removePrefix("## "),
                      color = theme.accentSecondary,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.SemiBold,
                      fontFamily = FontFamily.SansSerif
                    )
                  }
                  line.startsWith("> ") -> {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.surfaceVariant)
                        .border(
                          width = 2.dp,
                          color = theme.accentPrimary,
                          shape = RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                    ) {
                      Text(
                        text = line.removePrefix("> "),
                        color = theme.textSecondary,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                      )
                    }
                  }
                  line.startsWith("- ") -> {
                    Row(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      verticalAlignment = Alignment.Top
                    ) {
                      Text("•", color = theme.accentPrimary, fontSize = 12.sp)
                      Text(
                        text = line.removePrefix("- "),
                        color = theme.textPrimary,
                        fontSize = 11.5.sp
                      )
                    }
                  }
                  line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                  }
                  else -> {
                    Text(
                      text = line,
                      color = theme.textPrimary,
                      fontSize = 11.5.sp,
                      lineHeight = 17.sp
                    )
                  }
                }
              }
            }
          } else {
            // Raw Markdown editor
            BasicTextField(
              value = currentNote.content,
              onValueChange = { onContentChange(it) },
              modifier = Modifier
                .fillMaxSize()
                .testTag("obsidian_content_editor"),
              textStyle = TextStyle(
                color = theme.textPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
              ),
              cursorBrush = SolidColor(theme.accentPrimary)
            )
          }
        }
      }
    }
  }
}

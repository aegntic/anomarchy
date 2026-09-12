package com.example.omarchy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.omarchy.model.CyberMascot
import com.example.omarchy.model.MascotCatalog
import com.example.omarchy.model.MascotId
import com.example.omarchy.model.OmarchyThemeConfig

@Composable
fun FloatingMascotAssistant(
  theme: OmarchyThemeConfig,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var activeMascotIndex by remember { mutableIntStateOf(0) }
  var activeTipIndex by remember { mutableIntStateOf(0) }
  var isMinimized by remember { mutableStateOf(false) }

  val mascots = MascotCatalog.all
  val currentMascot = mascots[activeMascotIndex % mascots.size]
  val currentTip = currentMascot.tips[activeTipIndex % currentMascot.tips.size]

  val accentColor = when (currentMascot.id) {
    MascotId.CATFACE -> Color(0xFF78F1A0)
    MascotId.LONGNECK -> Color(0xFFF6C177)
    MascotId.PLINKY -> Color(0xFFEBBCBA)
  }

  val mascotIcon = when (currentMascot.id) {
    MascotId.CATFACE -> Icons.Default.Code
    MascotId.LONGNECK -> Icons.Default.Memory
    MascotId.PLINKY -> Icons.Default.Palette
  }

  Box(
    modifier = modifier.testTag("floating_mascot_assistant")
  ) {
    if (isMinimized) {
      // -------------------------------------------------------------
      // MINIMIZED FLOATING PILL / BUBBLE
      // -------------------------------------------------------------
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF0E1322))
          .border(1.5.dp, accentColor, RoundedCornerShape(20.dp))
          .clickable { isMinimized = false }
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .testTag("mascot_minimized_bubble"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = mascotIcon,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(12.dp)
            )
          }

          Text(
            text = currentMascot.name,
            color = accentColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )

          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(accentColor)
          )
        }
      }
    } else {
      // -------------------------------------------------------------
      // EXPANDED CYBER ASSISTANT CARD
      // -------------------------------------------------------------
      Box(
        modifier = Modifier
          .widthIn(min = 260.dp, max = 310.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0A0E1A).copy(alpha = 0.95f))
          .border(1.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
          .padding(12.dp)
          .testTag("mascot_expanded_card")
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Card Header: Mascot identity + window controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                  // Tap identity to cycle between Catface, Longneck, Plinky
                  activeMascotIndex = (activeMascotIndex + 1) % mascots.size
                  activeTipIndex = 0
                }
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(accentColor.copy(alpha = 0.2f))
                  .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = mascotIcon,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(14.dp)
                )
              }

              Column {
                Text(
                  text = "${currentMascot.name} ▾",
                  color = accentColor,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = currentMascot.subtitle,
                  color = Color(0xFF94A3B8),
                  fontSize = 7.5.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            // Window action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
              IconButton(
                onClick = { isMinimized = true },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Remove,
                  contentDescription = "Minimize",
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(12.dp)
                )
              }

              IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(20.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close Assistant",
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(12.dp)
                )
              }
            }
          }

          // Tip / Message Bubble
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF0F1528))
              .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.Top
            ) {
              Icon(
                imageVector = Icons.Default.TipsAndUpdates,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier
                  .size(14.dp)
                  .padding(top = 1.dp)
              )
              Text(
                text = currentTip,
                color = Color(0xFFE2E8F0),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 13.sp
              )
            }
          }

          // Bottom Bar: Next tip & mascot switcher
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Tip ${activeTipIndex + 1}/${currentMascot.tips.size}",
              color = Color(0xFF64748B),
              fontSize = 8.sp,
              fontFamily = FontFamily.Monospace
            )

            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Cycle Mascot button
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFF1E293B))
                  .clickable {
                    activeMascotIndex = (activeMascotIndex + 1) % mascots.size
                    activeTipIndex = 0
                  }
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "Switch Hero",
                  color = Color(0xFF94A3B8),
                  fontSize = 8.sp,
                  fontFamily = FontFamily.Monospace
                )
              }

              // Next Tip Button
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(accentColor)
                  .clickable {
                    activeTipIndex = (activeTipIndex + 1) % currentMascot.tips.size
                  }
                  .padding(horizontal = 7.dp, vertical = 3.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                  Text(
                    text = "Next",
                    color = Color.Black,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                  Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(10.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

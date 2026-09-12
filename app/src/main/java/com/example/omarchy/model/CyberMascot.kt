package com.example.omarchy.model

enum class MascotId {
  CATFACE,
  LONGNECK,
  PLINKY
}

data class CyberMascot(
  val id: MascotId,
  val name: String,
  val subtitle: String,
  val quote: String,
  val accentHex: String,
  val tips: List<String>
)

object MascotCatalog {
  val Catface = CyberMascot(
    id = MascotId.CATFACE,
    name = "CATFACE",
    subtitle = "Terminal & Neovim Specialist",
    quote = "Meow. Arch with Hyprland is pure developer joy.",
    accentHex = "#78f1a0",
    tips = listOf(
      "Press Super + Enter anytime to spawn an Alacritty terminal.",
      "Press Super + Space to open the Walker / Telescope fuzzy launcher.",
      "In Neovim, use :w to save your code buffers locally.",
      "Run Super + F1 to execute the Dev Cockpit macro in one keystroke."
    )
  )

  val Longneck = CyberMascot(
    id = MascotId.LONGNECK,
    name = "LONGNECK",
    subtitle = "System & Kernel Architect",
    quote = "I eat lions like you for breakfast. Keep computing sovereign.",
    accentHex = "#f6c177",
    tips = listOf(
      "Original desktop OS & vision by David Heinemeier Hansson (DHH).",
      "Mobile architecture natively engineered by @aegntic.",
      "Omarchy runs 100% offline with zero cloud phone-home telemetry.",
      "Double-tap Quickshell bar or press Super + V to toggle dwindle vs master-stack layouts."
    )
  )

  val Plinky = CyberMascot(
    id = MascotId.PLINKY,
    name = "PLINKY",
    subtitle = "Cyber Rice & Ergonomics Guide",
    quote = "Peace! Digital sovereignty meets cyberpunk aesthetic.",
    accentHex = "#ebbcba",
    tips = listOf(
      "Tap the OMA pill on the top Quickshell bar to open the Control Center.",
      "Press Super + F5 to cycle through 10+ official Hyprland colorways.",
      "Swipe across workspaces 1 through 5 to keep your workflows isolated.",
      "You own this device. No subscription rent, no spyware."
    )
  )

  val all: List<CyberMascot> = listOf(Catface, Longneck, Plinky)
}

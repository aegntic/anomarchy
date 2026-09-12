package com.example.omarchy.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemePreset(val displayName: String, val authorOrOrigin: String) {
  TOKYO_NIGHT("Tokyo Night", "Omarchy Official"),
  CATPPUCCIN("Catppuccin Macchiato", "Community"),
  LUMON("Lumon", "DHH Signature"),
  ROSE_PINE("Rosé Pine", "Community"),
  EVERFOREST("Everforest", "Nature"),
  CYBERPUNK("Cyberpunk", "Neon High-Tech"),
  GRUVBOX_DARK("Gruvbox Dark", "Retro Groove"),
  NORD_FROST("Nord Frost", "Arctic Ice"),
  DRACULA("Dracula Pro", "Vampire Dark"),
  HYPR_NEON("Hyprland Neon", "Wayland Glaze"),
  SOLARIZED_DARK("Solarized Dark", "Teal Precision"),
  MONOKAI_PRO("Monokai Pro", "Sunset Octagon")
}

enum class IconSetPreset(
  val displayName: String,
  val subtitle: String,
  val sampleBadges: String,
  val terminalGlyph: String,
  val neovimGlyph: String,
  val obsidianGlyph: String,
  val btopGlyph: String,
  val agentGlyph: String,
  val browserGlyph: String,
  val distroGlyph: String = "󰣇",
  val closeGlyph: String = "✕",
  val floatGlyph: String = "󰉦",
  val fullscreenGlyph: String = "󰊓",
  val splitGlyph: String = "󰕰"
) {
  NERD_FONTS(
    displayName = "Nerd Font Glyphs",
    subtitle = "Waybar & Alacritty icons (󰆍  󱓞 󰍛 󰚩 󰈹)",
    sampleBadges = "󰆍  󱓞 󰍛 󰚩",
    terminalGlyph = "󰆍",
    neovimGlyph = "",
    obsidianGlyph = "󱓞",
    btopGlyph = "󰍛",
    agentGlyph = "󰚩",
    browserGlyph = "󰈹",
    distroGlyph = "󰣇",
    closeGlyph = "󰅖",
    floatGlyph = "󰉦",
    fullscreenGlyph = "󰊓",
    splitGlyph = "󰕰"
  ),
  MINIMAL_GEOMETRIC(
    displayName = "Hypr Geometric",
    subtitle = "Crisp outlined geometric shapes (Aylur's AGS style)",
    sampleBadges = "▤ ◈ ◫ ☵ ✦",
    terminalGlyph = "▤",
    neovimGlyph = "◈",
    obsidianGlyph = "◫",
    btopGlyph = "☵",
    agentGlyph = "✦",
    browserGlyph = "◎",
    distroGlyph = "▲",
    closeGlyph = "✕",
    floatGlyph = "◱",
    fullscreenGlyph = "⛶",
    splitGlyph = "◫"
  ),
  PAPIRUS_VIBRANT(
    displayName = "Papirus Distro",
    subtitle = "Vibrant Linux desktop icons with tinted badges",
    sampleBadges = "◆ ❖ ◈ ⬢ ✦",
    terminalGlyph = "◆",
    neovimGlyph = "❖",
    obsidianGlyph = "◈",
    btopGlyph = "⬢",
    agentGlyph = "✦",
    browserGlyph = "●",
    distroGlyph = "󰣇",
    closeGlyph = "✕",
    floatGlyph = "◻",
    fullscreenGlyph = "⛶",
    splitGlyph = "◫"
  ),
  ASCII_BRACKETS(
    displayName = "Retro Hacker CLI",
    subtitle = "Minimalist ASCII bracket badges (dwm / ratpoison)",
    sampleBadges = "[sh] [vi] [md] [top]",
    terminalGlyph = "[sh]",
    neovimGlyph = "[vi]",
    obsidianGlyph = "[md]",
    btopGlyph = "[top]",
    agentGlyph = "[ai]",
    browserGlyph = "[www]",
    distroGlyph = "[arch]",
    closeGlyph = "[x]",
    floatGlyph = "[^]",
    fullscreenGlyph = "[+]",
    splitGlyph = "[|]"
  ),
  KANJI_ZEN(
    displayName = "Kanji Minimalist",
    subtitle = "Zen Kanji glyphs (r/unixporn aesthetic rices)",
    sampleBadges = "端 編 記 監 知",
    terminalGlyph = "端",
    neovimGlyph = "編",
    obsidianGlyph = "記",
    btopGlyph = "監",
    agentGlyph = "知",
    browserGlyph = "網",
    distroGlyph = "鳥",
    closeGlyph = "閉",
    floatGlyph = "浮",
    fullscreenGlyph = "全",
    splitGlyph = "分"
  )
}

data class OmarchyThemeConfig(
  val preset: ThemePreset,
  val desktopBackground: Color,
  val desktopSecondary: Color,
  val surfaceColor: Color,
  val surfaceVariant: Color,
  val surfaceBorder: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val accentPrimary: Color,
  val accentSecondary: Color,
  val activeBorderGradient: List<Color>,
  val inactiveBorderColor: Color,
  val barBackground: Color,
  val barText: Color,
  val barActiveWorkspace: Color,
  val terminalBackground: Color,
  val terminalForeground: Color,
  val terminalGreen: Color,
  val terminalCyan: Color,
  val terminalYellow: Color,
  val terminalRed: Color,
  val terminalMagenta: Color,
  val isDark: Boolean = true,
  val iconSet: IconSetPreset = IconSetPreset.NERD_FONTS,
  val borderGlowEffect: Boolean = true
) {
  val activeBorderBrush: Brush
    get() = Brush.linearGradient(activeBorderGradient)

  fun getAppGlyph(app: AppType): String = when (app) {
    AppType.TERMINAL -> iconSet.terminalGlyph
    AppType.NEOVIM -> iconSet.neovimGlyph
    AppType.OBSIDIAN -> iconSet.obsidianGlyph
    AppType.BTOP -> iconSet.btopGlyph
    AppType.AGENT -> iconSet.agentGlyph
    AppType.BROWSER -> iconSet.browserGlyph
  }

  fun getWorkspaceGlyph(ws: Int): String = when (iconSet) {
    IconSetPreset.NERD_FONTS -> when (ws) {
      1 -> "󰮯"
      2 -> "󰮰"
      3 -> "󰮱"
      4 -> "󰮲"
      5 -> "󰮳"
      else -> "$ws"
    }
    IconSetPreset.KANJI_ZEN -> when (ws) {
      1 -> "一"
      2 -> "二"
      3 -> "三"
      4 -> "四"
      5 -> "五"
      else -> "$ws"
    }
    IconSetPreset.ASCII_BRACKETS -> "[$ws]"
    IconSetPreset.MINIMAL_GEOMETRIC -> "•$ws"
    IconSetPreset.PAPIRUS_VIBRANT -> "$ws"
  }

  fun copyWithIconSet(newIconSet: IconSetPreset): OmarchyThemeConfig = this.copy(iconSet = newIconSet)

  companion object {
    fun mocha(): OmarchyThemeConfig = fromPreset(ThemePreset.CATPPUCCIN)

    fun fromPreset(preset: ThemePreset, iconSet: IconSetPreset = IconSetPreset.NERD_FONTS): OmarchyThemeConfig = when (preset) {
      ThemePreset.TOKYO_NIGHT -> OmarchyThemeConfig(
        preset = ThemePreset.TOKYO_NIGHT,
        iconSet = iconSet,
        desktopBackground = Color(0xFF16161E),
        desktopSecondary = Color(0xFF1A1B26),
        surfaceColor = Color(0xFF1F2335),
        surfaceVariant = Color(0xFF24283B),
        surfaceBorder = Color(0xFF292E42),
        textPrimary = Color(0xFFC0CAF5),
        textSecondary = Color(0xFF9AA5CE),
        textMuted = Color(0xFF565F89),
        accentPrimary = Color(0xFF7AA2F7),
        accentSecondary = Color(0xFFBB9AF7),
        activeBorderGradient = listOf(Color(0xFF7AA2F7), Color(0xFFBB9AF7), Color(0xFF7DCFFF)),
        inactiveBorderColor = Color(0xFF292E42),
        barBackground = Color(0xEE16161E),
        barText = Color(0xFFC0CAF5),
        barActiveWorkspace = Color(0xFF7AA2F7),
        terminalBackground = Color(0xFF1A1B26),
        terminalForeground = Color(0xFFC0CAF5),
        terminalGreen = Color(0xFF9ECE6A),
        terminalCyan = Color(0xFF7DCFFF),
        terminalYellow = Color(0xFFE0AF68),
        terminalRed = Color(0xFFF7768E),
        terminalMagenta = Color(0xFFBB9AF7)
      )

      ThemePreset.CATPPUCCIN -> OmarchyThemeConfig(
        preset = ThemePreset.CATPPUCCIN,
        iconSet = iconSet,
        desktopBackground = Color(0xFF1E2030),
        desktopSecondary = Color(0xFF24273A),
        surfaceColor = Color(0xFF24273A),
        surfaceVariant = Color(0xFF363A4F),
        surfaceBorder = Color(0xFF494D64),
        textPrimary = Color(0xFFCAD3F5),
        textSecondary = Color(0xFFA5ADCB),
        textMuted = Color(0xFF6E738D),
        accentPrimary = Color(0xFFC6A0F6),
        accentSecondary = Color(0xFFF5A97F),
        activeBorderGradient = listOf(Color(0xFFC6A0F6), Color(0xFFF5A97F), Color(0xFF8AADF4)),
        inactiveBorderColor = Color(0xFF363A4F),
        barBackground = Color(0xEE1E2030),
        barText = Color(0xFFCAD3F5),
        barActiveWorkspace = Color(0xFFC6A0F6),
        terminalBackground = Color(0xFF24273A),
        terminalForeground = Color(0xFFCAD3F5),
        terminalGreen = Color(0xFFA6DA95),
        terminalCyan = Color(0xFF8BD5CA),
        terminalYellow = Color(0xFFEED49F),
        terminalRed = Color(0xFFED8796),
        terminalMagenta = Color(0xFFF5BDE6)
      )

      ThemePreset.LUMON -> OmarchyThemeConfig(
        preset = ThemePreset.LUMON,
        iconSet = iconSet,
        desktopBackground = Color(0xFF0F1115),
        desktopSecondary = Color(0xFF16181D),
        surfaceColor = Color(0xFF181B20),
        surfaceVariant = Color(0xFF22262E),
        surfaceBorder = Color(0xFF2D333D),
        textPrimary = Color(0xFFE6EDF3),
        textSecondary = Color(0xFF8B949E),
        textMuted = Color(0xFF6E7681),
        accentPrimary = Color(0xFF58A6FF),
        accentSecondary = Color(0xFF3FB950),
        activeBorderGradient = listOf(Color(0xFF58A6FF), Color(0xFF79C0FF), Color(0xFF3FB950)),
        inactiveBorderColor = Color(0xFF2D333D),
        barBackground = Color(0xEE0F1115),
        barText = Color(0xFFE6EDF3),
        barActiveWorkspace = Color(0xFF58A6FF),
        terminalBackground = Color(0xFF0D1117),
        terminalForeground = Color(0xFFE6EDF3),
        terminalGreen = Color(0xFF3FB950),
        terminalCyan = Color(0xFF58A6FF),
        terminalYellow = Color(0xFFD29922),
        terminalRed = Color(0xFFF85149),
        terminalMagenta = Color(0xFFBC8CFF)
      )

      ThemePreset.ROSE_PINE -> OmarchyThemeConfig(
        preset = ThemePreset.ROSE_PINE,
        iconSet = iconSet,
        desktopBackground = Color(0xFF191724),
        desktopSecondary = Color(0xFF1F1D2E),
        surfaceColor = Color(0xFF1F1D2E),
        surfaceVariant = Color(0xFF26233A),
        surfaceBorder = Color(0xFF403D52),
        textPrimary = Color(0xFFE0DEF4),
        textSecondary = Color(0xFF908CAA),
        textMuted = Color(0xFF6E6A86),
        accentPrimary = Color(0xFFEBBCBA),
        accentSecondary = Color(0xFFC4A7E7),
        activeBorderGradient = listOf(Color(0xFFEBBCBA), Color(0xFFC4A7E7), Color(0xFFF6C177)),
        inactiveBorderColor = Color(0xFF26233A),
        barBackground = Color(0xEE191724),
        barText = Color(0xFFE0DEF4),
        barActiveWorkspace = Color(0xFFEBBCBA),
        terminalBackground = Color(0xFF191724),
        terminalForeground = Color(0xFFE0DEF4),
        terminalGreen = Color(0xFF9CCFD8),
        terminalCyan = Color(0xFF31748F),
        terminalYellow = Color(0xFFF6C177),
        terminalRed = Color(0xFFEB6F92),
        terminalMagenta = Color(0xFFC4A7E7)
      )

      ThemePreset.EVERFOREST -> OmarchyThemeConfig(
        preset = ThemePreset.EVERFOREST,
        iconSet = iconSet,
        desktopBackground = Color(0xFF272E33),
        desktopSecondary = Color(0xFF2D353B),
        surfaceColor = Color(0xFF2D353B),
        surfaceVariant = Color(0xFF374145),
        surfaceBorder = Color(0xFF475258),
        textPrimary = Color(0xFFD3C6AA),
        textSecondary = Color(0xFF9DA9A0),
        textMuted = Color(0xFF7A8478),
        accentPrimary = Color(0xFFA7C080),
        accentSecondary = Color(0xFFDBBC7F),
        activeBorderGradient = listOf(Color(0xFFA7C080), Color(0xFF83C092), Color(0xFFDBBC7F)),
        inactiveBorderColor = Color(0xFF374145),
        barBackground = Color(0xEE272E33),
        barText = Color(0xFFD3C6AA),
        barActiveWorkspace = Color(0xFFA7C080),
        terminalBackground = Color(0xFF2B3339),
        terminalForeground = Color(0xFFD3C6AA),
        terminalGreen = Color(0xFFA7C080),
        terminalCyan = Color(0xFF87C095),
        terminalYellow = Color(0xFFDBBC7F),
        terminalRed = Color(0xFFE67E80),
        terminalMagenta = Color(0xFFD699B6)
      )

      ThemePreset.CYBERPUNK -> OmarchyThemeConfig(
        preset = ThemePreset.CYBERPUNK,
        iconSet = iconSet,
        desktopBackground = Color(0xFF0A0C14),
        desktopSecondary = Color(0xFF111422),
        surfaceColor = Color(0xFF121629),
        surfaceVariant = Color(0xFF1E243E),
        surfaceBorder = Color(0xFF2B3356),
        textPrimary = Color(0xFFF0F6FC),
        textSecondary = Color(0xFF79E2FB),
        textMuted = Color(0xFF5D6B8C),
        accentPrimary = Color(0xFF00F0FF),
        accentSecondary = Color(0xFFFF007F),
        activeBorderGradient = listOf(Color(0xFF00F0FF), Color(0xFFFF007F), Color(0xFFFFE600)),
        inactiveBorderColor = Color(0xFF1E243E),
        barBackground = Color(0xEE0A0C14),
        barText = Color(0xFF00F0FF),
        barActiveWorkspace = Color(0xFFFF007F),
        terminalBackground = Color(0xFF0A0C14),
        terminalForeground = Color(0xFF00F0FF),
        terminalGreen = Color(0xFF00FF66),
        terminalCyan = Color(0xFF00F0FF),
        terminalYellow = Color(0xFFFFE600),
        terminalRed = Color(0xFFFF007F),
        terminalMagenta = Color(0xFFCC00FF)
      )

      ThemePreset.GRUVBOX_DARK -> OmarchyThemeConfig(
        preset = ThemePreset.GRUVBOX_DARK,
        iconSet = iconSet,
        desktopBackground = Color(0xFF1D2021),
        desktopSecondary = Color(0xFF282828),
        surfaceColor = Color(0xFF282828),
        surfaceVariant = Color(0xFF3C3836),
        surfaceBorder = Color(0xFF504945),
        textPrimary = Color(0xFFEBDBB2),
        textSecondary = Color(0xFFD5C4A1),
        textMuted = Color(0xFF928374),
        accentPrimary = Color(0xFFFE8019),
        accentSecondary = Color(0xFF8EC07C),
        activeBorderGradient = listOf(Color(0xFFFE8019), Color(0xFFFABD2F), Color(0xFF8EC07C)),
        inactiveBorderColor = Color(0xFF3C3836),
        barBackground = Color(0xEE1D2021),
        barText = Color(0xFFEBDBB2),
        barActiveWorkspace = Color(0xFFFE8019),
        terminalBackground = Color(0xFF282828),
        terminalForeground = Color(0xFFEBDBB2),
        terminalGreen = Color(0xFFB8BB26),
        terminalCyan = Color(0xFF8EC07C),
        terminalYellow = Color(0xFFFABD2F),
        terminalRed = Color(0xFFFB4934),
        terminalMagenta = Color(0xFFD3869B)
      )

      ThemePreset.NORD_FROST -> OmarchyThemeConfig(
        preset = ThemePreset.NORD_FROST,
        iconSet = iconSet,
        desktopBackground = Color(0xFF242933),
        desktopSecondary = Color(0xFF2E3440),
        surfaceColor = Color(0xFF2E3440),
        surfaceVariant = Color(0xFF3B4252),
        surfaceBorder = Color(0xFF434C5E),
        textPrimary = Color(0xFFECEFF4),
        textSecondary = Color(0xFFD8DEE9),
        textMuted = Color(0xFF4C566A),
        accentPrimary = Color(0xFF88C0D0),
        accentSecondary = Color(0xFF81A1C1),
        activeBorderGradient = listOf(Color(0xFF88C0D0), Color(0xFF81A1C1), Color(0xFF8FBCBB)),
        inactiveBorderColor = Color(0xFF3B4252),
        barBackground = Color(0xEE242933),
        barText = Color(0xFFECEFF4),
        barActiveWorkspace = Color(0xFF88C0D0),
        terminalBackground = Color(0xFF2E3440),
        terminalForeground = Color(0xFFECEFF4),
        terminalGreen = Color(0xFFA3BE8C),
        terminalCyan = Color(0xFF88C0D0),
        terminalYellow = Color(0xFFEBCB8B),
        terminalRed = Color(0xFFBF616A),
        terminalMagenta = Color(0xFFB48EAD)
      )

      ThemePreset.DRACULA -> OmarchyThemeConfig(
        preset = ThemePreset.DRACULA,
        iconSet = iconSet,
        desktopBackground = Color(0xFF21222C),
        desktopSecondary = Color(0xFF282A36),
        surfaceColor = Color(0xFF282A36),
        surfaceVariant = Color(0xFF343746),
        surfaceBorder = Color(0xFF44475A),
        textPrimary = Color(0xFFF8F8F2),
        textSecondary = Color(0xFFBFBFBF),
        textMuted = Color(0xFF6272A4),
        accentPrimary = Color(0xFFBD93F9),
        accentSecondary = Color(0xFFFF79C6),
        activeBorderGradient = listOf(Color(0xFFBD93F9), Color(0xFFFF79C6), Color(0xFF8BE9FD)),
        inactiveBorderColor = Color(0xFF343746),
        barBackground = Color(0xEE21222C),
        barText = Color(0xFFF8F8F2),
        barActiveWorkspace = Color(0xFFBD93F9),
        terminalBackground = Color(0xFF282A36),
        terminalForeground = Color(0xFFF8F8F2),
        terminalGreen = Color(0xFF50FA7B),
        terminalCyan = Color(0xFF8BE9FD),
        terminalYellow = Color(0xFFF1FA8C),
        terminalRed = Color(0xFFFF5555),
        terminalMagenta = Color(0xFFFF79C6)
      )

      ThemePreset.HYPR_NEON -> OmarchyThemeConfig(
        preset = ThemePreset.HYPR_NEON,
        iconSet = iconSet,
        desktopBackground = Color(0xFF07080E),
        desktopSecondary = Color(0xFF0E101B),
        surfaceColor = Color(0xFF101322),
        surfaceVariant = Color(0xFF191E34),
        surfaceBorder = Color(0xFF262E4F),
        textPrimary = Color(0xFFF2F6FF),
        textSecondary = Color(0xFF8FA5E2),
        textMuted = Color(0xFF4B577D),
        accentPrimary = Color(0xFF00F5FF),
        accentSecondary = Color(0xFFC300FF),
        activeBorderGradient = listOf(Color(0xFF00F5FF), Color(0xFFC300FF), Color(0xFFFF0080)),
        inactiveBorderColor = Color(0xFF191E34),
        barBackground = Color(0xEE07080E),
        barText = Color(0xFF00F5FF),
        barActiveWorkspace = Color(0xFF00F5FF),
        terminalBackground = Color(0xFF090A12),
        terminalForeground = Color(0xFFF2F6FF),
        terminalGreen = Color(0xFF00FF9D),
        terminalCyan = Color(0xFF00F5FF),
        terminalYellow = Color(0xFFFFE600),
        terminalRed = Color(0xFFFF0055),
        terminalMagenta = Color(0xFFC300FF)
      )

      ThemePreset.SOLARIZED_DARK -> OmarchyThemeConfig(
        preset = ThemePreset.SOLARIZED_DARK,
        iconSet = iconSet,
        desktopBackground = Color(0xFF00212B),
        desktopSecondary = Color(0xFF002B36),
        surfaceColor = Color(0xFF073642),
        surfaceVariant = Color(0xFF0D4452),
        surfaceBorder = Color(0xFF165666),
        textPrimary = Color(0xFF93A1A1),
        textSecondary = Color(0xFF839496),
        textMuted = Color(0xFF586E75),
        accentPrimary = Color(0xFF2AA198),
        accentSecondary = Color(0xFF268BD2),
        activeBorderGradient = listOf(Color(0xFF2AA198), Color(0xFF268BD2), Color(0xFF859900)),
        inactiveBorderColor = Color(0xFF0D4452),
        barBackground = Color(0xEE00212B),
        barText = Color(0xFF93A1A1),
        barActiveWorkspace = Color(0xFF2AA198),
        terminalBackground = Color(0xFF002B36),
        terminalForeground = Color(0xFF839496),
        terminalGreen = Color(0xFF859900),
        terminalCyan = Color(0xFF2AA198),
        terminalYellow = Color(0xFFB58900),
        terminalRed = Color(0xFFDC322F),
        terminalMagenta = Color(0xFFD33682)
      )

      ThemePreset.MONOKAI_PRO -> OmarchyThemeConfig(
        preset = ThemePreset.MONOKAI_PRO,
        iconSet = iconSet,
        desktopBackground = Color(0xFF221F22),
        desktopSecondary = Color(0xFF2D2A2E),
        surfaceColor = Color(0xFF2D2A2E),
        surfaceVariant = Color(0xFF3B373C),
        surfaceBorder = Color(0xFF4A444B),
        textPrimary = Color(0xFFFCFCFA),
        textSecondary = Color(0xFFC1C0C0),
        textMuted = Color(0xFF727072),
        accentPrimary = Color(0xFFFFD866),
        accentSecondary = Color(0xFFFF6188),
        activeBorderGradient = listOf(Color(0xFFFFD866), Color(0xFFFF6188), Color(0xFF78DCE8)),
        inactiveBorderColor = Color(0xFF3B373C),
        barBackground = Color(0xEE221F22),
        barText = Color(0xFFFCFCFA),
        barActiveWorkspace = Color(0xFFFFD866),
        terminalBackground = Color(0xFF2D2A2E),
        terminalForeground = Color(0xFFFCFCFA),
        terminalGreen = Color(0xFFA9DC76),
        terminalCyan = Color(0xFF78DCE8),
        terminalYellow = Color(0xFFFFD866),
        terminalRed = Color(0xFFFF6188),
        terminalMagenta = Color(0xFFAB9DF2)
      )
    }
  }
}


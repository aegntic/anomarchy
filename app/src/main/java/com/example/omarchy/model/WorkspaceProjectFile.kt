package com.example.omarchy.model

/**
 * Model representing a file in the Omarchy project workspace.
 */
data class WorkspaceProjectFile(
  val path: String,
  val filename: String,
  val directory: String,
  val language: String,
  val category: FileCategory,
  val content: String,
  val lineCount: Int = content.lines().size,
  val description: String = ""
)

enum class FileCategory(val displayName: String, val badge: String) {
  ALL("All Files", "ALL"),
  RAILS("Rails App", "RAILS"),
  CONFIG("Config & DB", "CONF"),
  DOTFILES("Dotfiles", "DOT"),
  DOCS("Docs & Notes", "DOC")
}

enum class TelescopeMode(val displayName: String, val commandPrompt: String, val icon: String) {
  FIND_FILES("find_files", "❯ Find Files", "󰍉"),
  BUFFERS("buffers", "❯ Active Buffers", "󰈙"),
  COMMANDS("commands", "❯ Commands (:)", "󰘳"),
  LIVE_GREP("live_grep", "❯ Live Grep", "󰈞")
}

data class FuzzyMatchResult(
  val isMatch: Boolean,
  val score: Int,
  val matchedIndicesInFilename: List<Int> = emptyList(),
  val matchedIndicesInPath: List<Int> = emptyList()
)

data class NeovimPaletteCommand(
  val command: String,
  val description: String,
  val category: String,
  val shortcut: String? = null
)

object WorkspaceFileCatalog {

  val workspaceFiles: List<WorkspaceProjectFile> = listOf(
    WorkspaceProjectFile(
      path = "app/models/article.rb",
      filename = "article.rb",
      directory = "app/models",
      language = "ruby",
      category = FileCategory.RAILS,
      description = "ActiveRecord Article model with validations and associations",
      content = """class Article < ApplicationRecord
  include Visible

  has_many :comments, dependent: :destroy

  validates :title, presence: true, length: { minimum: 5 }
  validates :body, presence: true, length: { minimum: 10 }

  scope :published, -> { where(status: 'public') }
  scope :recent, -> { order(created_at: :desc) }

  def summary
    body.truncate(140)
  end

  def reading_time_minutes
    (body.split.size / 200.0).ceil
  end
end"""
    ),
    WorkspaceProjectFile(
      path = "app/models/comment.rb",
      filename = "comment.rb",
      directory = "app/models",
      language = "ruby",
      category = FileCategory.RAILS,
      description = "ActiveRecord Comment model belonging to Article",
      content = """class Comment < ApplicationRecord
  include Visible

  belongs_to :article, touch: true

  validates :commenter, presence: true
  validates :body, presence: true, length: { maximum: 1000 }

  after_create_commit :notify_author

  private

  def notify_author
    CommentNotificationJob.perform_later(self)
  end
end"""
    ),
    WorkspaceProjectFile(
      path = "app/models/user.rb",
      filename = "user.rb",
      directory = "app/models",
      language = "ruby",
      category = FileCategory.RAILS,
      description = "User authentication and workspace session owner",
      content = """class User < ApplicationRecord
  has_secure_password
  has_many :articles, dependent: :nullify
  has_many :sessions, dependent: :destroy

  validates :email, presence: true, uniqueness: true, format: { with: URI::MailTo::EMAIL_REGEXP }
  validates :handle, presence: true, uniqueness: true, length: { in: 3..24 }

  enum :role, { standard: 0, admin: 1 }, default: :standard
end"""
    ),
    WorkspaceProjectFile(
      path = "app/controllers/articles_controller.rb",
      filename = "articles_controller.rb",
      directory = "app/controllers",
      language = "ruby",
      category = FileCategory.RAILS,
      description = "RESTful controller for browsing, reading, and publishing articles",
      content = """class ArticlesController < ApplicationController
  before_action :set_article, only: %i[show edit update destroy]
  before_action :authenticate_user!, except: %i[index show]

  def index
    @articles = Article.published.recent.includes(:comments)
  end

  def show
    @comment = @article.comments.build
  end

  def new
    @article = Article.new
  end

  def create
    @article = Article.new(article_params)
    if @article.save
      redirect_to @article, notice: "Article published cleanly to Omarchy."
    else
      render :new, status: :unprocessable_entity
    end
  end

  private

  def set_article
    @article = Article.find(params[:id])
  end

  def article_params
    params.require(:article).permit(:title, :body, :status)
  end
end"""
    ),
    WorkspaceProjectFile(
      path = "app/controllers/comments_controller.rb",
      filename = "comments_controller.rb",
      directory = "app/controllers",
      language = "ruby",
      category = FileCategory.RAILS,
      description = "Nested controller for creating and moderating comments",
      content = """class CommentsController < ApplicationController
  before_action :set_article

  def create
    @comment = @article.comments.create(comment_params)
    redirect_to article_path(@article), notice: "Comment appended."
  end

  def destroy
    @comment = @article.comments.find(params[:id])
    @comment.destroy
    redirect_to article_path(@article), status: :see_other
  end

  private

  def set_article
    @article = Article.find(params[:article_id])
  end

  def comment_params
    params.require(:comment).permit(:commenter, :body, :status)
  end
end"""
    ),
    WorkspaceProjectFile(
      path = "app/views/articles/index.html.erb",
      filename = "index.html.erb",
      directory = "app/views/articles",
      language = "html",
      category = FileCategory.RAILS,
      description = "Articles feed template with responsive cards and metadata",
      content = """<div class="omarchy-container max-w-4xl mx-auto py-8">
  <header class="flex items-center justify-between pb-6 border-b border-surface">
    <h1 class="text-3xl font-mono font-bold text-accent">Omarchy Dispatch</h1>
    <%= link_to "New Article", new_article_path, class: "btn-primary font-mono text-sm px-4 py-2" %>
  </header>

  <div class="grid gap-6 mt-8">
    <% @articles.each do |article| %>
      <article class="p-6 rounded-lg bg-surface border border-surface-border">
        <h2 class="text-xl font-bold hover:text-accent">
          <%= link_to article.title, article %>
        </h2>
        <p class="text-muted text-sm mt-2"><%= article.summary %></p>
        <footer class="flex items-center gap-4 mt-4 text-xs font-mono text-secondary">
          <span>󰅐 <%= article.created_at.strftime("%b %d, %Y") %></span>
          <span>󰆈 <%= article.comments.size %> comments</span>
          <span>󰌌 <%= article.reading_time_minutes %> min read</span>
        </footer>
      </article>
    <% end %>
  </div>
</div>"""
    ),
    WorkspaceProjectFile(
      path = "config/routes.rb",
      filename = "routes.rb",
      directory = "config",
      language = "ruby",
      category = FileCategory.CONFIG,
      description = "Application routing endpoints and healthcheck",
      content = """Rails.application.routes.draw do
  root "articles#index"

  resources :articles do
    resources :comments
  end

  resource :session, only: %i[new create destroy]
  resources :users, only: %i[new create]

  # Solid Queue & Cache telemetry dashboard
  mount MissionControl::Jobs::Engine, at: "/jobs"

  # Reveal health status on /up that returns 200 if the app boots with no exceptions
  get "up" => "rails/health#show", as: :rails_health_check
end"""
    ),
    WorkspaceProjectFile(
      path = "config/database.yml",
      filename = "database.yml",
      directory = "config",
      language = "yaml",
      category = FileCategory.CONFIG,
      description = "SQLite 3 connection pool and Solid Cache configuration",
      content = """default: &default
  adapter: sqlite3
  pool: <%= ENV.fetch("RAILS_MAX_THREADS") { 5 } %>
  timeout: 5000

development:
  <<: *default
  database: storage/development.sqlite3

test:
  <<: *default
  database: storage/test.sqlite3

production:
  primary:
    <<: *default
    database: storage/production.sqlite3
  cache:
    <<: *default
    database: storage/production_cache.sqlite3
    migrations_paths: db/cache_migrate"""
    ),
    WorkspaceProjectFile(
      path = "db/schema.rb",
      filename = "schema.rb",
      directory = "db",
      language = "ruby",
      category = FileCategory.CONFIG,
      description = "ActiveRecord database schema snapshot",
      content = """ActiveRecord::Schema[7.2].define(version: 2024_08_12_180422) do
  create_table "articles", force: :cascade do |t|
    t.string "title", null: false
    t.text "body", null: false
    t.string "status", default: "public", null: false
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["created_at"], name: "index_articles_on_created_at"
  end

  create_table "comments", force: :cascade do |t|
    t.string "commenter", null: false
    t.text "body", null: false
    t.string "status", default: "public"
    t.integer "article_id", null: false
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["article_id"], name: "index_comments_on_article_id"
  end
end"""
    ),
    WorkspaceProjectFile(
      path = "Gemfile",
      filename = "Gemfile",
      directory = ".",
      language = "ruby",
      category = FileCategory.CONFIG,
      description = "Ruby gem dependencies for Omarchy backend",
      content = """source "https://rubygems.org"
ruby ">= 3.3.0"

gem "rails", "~> 7.2.0"
gem "sqlite3", ">= 2.1"
gem "puma", ">= 6.4"
gem "importmap-rails"
gem "turbo-rails"
gem "stimulus-rails"
gem "tailwind-rails"

# Solid Stack
gem "solid_cache"
gem "solid_queue"
gem "solid_cable"
gem "kamal", require: false

group :development, :test do
  gem "debug", platforms: %i[mri windows]
  gem "brakeman", require: false
  gem "rubocop-rails-omakase", require: false
end"""
    ),
    WorkspaceProjectFile(
      path = "~/.config/hypr/hyprland.conf",
      filename = "hyprland.conf",
      directory = "~/.config/hypr",
      language = "conf",
      category = FileCategory.DOTFILES,
      description = "Wayland dynamic tiling compositor keybinds and animation rules",
      content = """# Omarchy Hyprland Configuration
monitor=,preferred,auto,1

general {
    gaps_in = 6
    gaps_out = 12
    border_size = 2
    col.active_border = rgba(7aa2f7ee) rgba(bb9af7ee) 45deg
    col.inactive_border = rgba(292e42aa)
    layout = dwindle
}

decoration {
    rounding = 10
    blur {
        enabled = true
        size = 8
        passes = 3
    }
}

# Keybindings
bind = SUPER, RETURN, exec, alacritty
bind = SUPER, Q, killactive,
bind = SUPER, SPACE, exec, walker
bind = SUPER, 1, workspace, 1
bind = SUPER, 2, workspace, 2
bind = SUPER, 3, workspace, 3
bind = SUPER, F, fullscreen, 1"""
    ),
    WorkspaceProjectFile(
      path = "~/.config/nvim/init.lua",
      filename = "init.lua",
      directory = "~/.config/nvim",
      language = "lua",
      category = FileCategory.DOTFILES,
      description = "Neovim 0.10 config with Telescope, Lazy.nvim, and Treesitter",
      content = """-- Omarchy Neovim Lua Configuration
vim.g.mapleader = " "
vim.g.maplocalleader = " "

local opt = vim.opt
opt.number = true
opt.relativenumber = true
opt.tabstop = 2
opt.shiftwidth = 2
opt.expandtab = true
opt.smartindent = true
opt.termguicolors = true
opt.signcolumn = "yes"
opt.clipboard = "unnamedplus"

-- Telescope Keybindings
local builtin = require('telescope.builtin')
vim.keymap.set('n', '<C-p>', builtin.find_files, { desc = 'Telescope Find Files' })
vim.keymap.set('n', '<leader>ff', builtin.find_files, { desc = 'Find Files' })
vim.keymap.set('n', '<leader>fb', builtin.buffers, { desc = 'Find Buffers' })
vim.keymap.set('n', '<leader>fg', builtin.live_grep, { desc = 'Live Grep' })
vim.keymap.set('n', '<leader>fc', builtin.commands, { desc = 'Commands Palette' })

-- Theme
vim.cmd.colorscheme("tokyonight-night")"""
    ),
    WorkspaceProjectFile(
      path = "~/.config/alacritty/alacritty.toml",
      filename = "alacritty.toml",
      directory = "~/.config/alacritty",
      language = "toml",
      category = FileCategory.DOTFILES,
      description = "GPU-accelerated terminal emulator profile and Tokyo Night palette",
      content = """[window]
padding = { x = 12, y = 12 }
opacity = 0.95
blur = true
decorations = "none"

[font]
size = 12.0
normal = { family = "JetBrains Mono Nerd Font", style = "Regular" }
bold = { family = "JetBrains Mono Nerd Font", style = "Bold" }

[colors.primary]
background = "#1a1b26"
foreground = "#c0caf5"

[colors.normal]
black = "#15161e"
red = "#f7768e"
green = "#9ece6a"
yellow = "#e0af68"
blue = "#7aa2f7"
magenta = "#bb9af7"
cyan = "#7dcfff"
white = "#a9b1d6""""
    ),
    WorkspaceProjectFile(
      path = "~/.zshrc",
      filename = ".zshrc",
      directory = "~",
      language = "sh",
      category = FileCategory.DOTFILES,
      description = "ZSH shell aliases, Powerlevel10k prompt, and tool exports",
      content = """# Omarchy Zsh Configuration
export ZSH="${'$'}HOME/.oh-my-zsh"
export EDITOR="nvim"
export VISUAL="nvim"

# Omarchy Aliases
alias nvim="nvim"
alias vi="nvim"
alias oma="omafetch"
alias ll="eza -la --icons"
alias g="git"
alias dc="docker compose"

# Powerlevel10k theme initialization
[[ ! -f ~/.p10k.zsh ]] || source ~/.p10k.zsh"""
    ),
    WorkspaceProjectFile(
      path = "README.md",
      filename = "README.md",
      directory = ".",
      language = "markdown",
      category = FileCategory.DOCS,
      description = "Project overview, philosophy, and keyboard shortcut reference",
      content = """# Omarchy: Opinionated Linux Desktop

Omarchy is a curated, keyboard-driven Linux developer experience built with:
- **Hyprland**: Smooth Wayland dynamic tiling compositor.
- **Neovim**: Modal editor featuring Telescope fuzzy file finder.
- **Agentic Linux Copilot**: Integrated AI development agent.
- **Ruby on Rails 7.2**: Modern web application backend with Solid stack.

## Neovim Telescope Keybindings
- `<Ctrl+P>` or `<leader>ff`: Open Fuzzy File Finder
- `<leader>fb`: Open Buffer Switcher
- `<leader>fc`: Open Command Palette
- `<leader>fg`: Live Grep across project"""
    ),
    WorkspaceProjectFile(
      path = "docs/ARCHITECTURE.md",
      filename = "ARCHITECTURE.md",
      directory = "docs",
      language = "markdown",
      category = FileCategory.DOCS,
      description = "Architectural diagram, Room persistence schema, and component layout",
      content = """# Omarchy Architecture

## 1. Compositor Layer (Hyprland + Quickshell)
Manages window layout algorithms (Single, Vertical Split, Dual Horizontal, Tri-Zone Dev).

## 2. Editor & Tools
- **Neovim**: Multi-buffer modal editing with live fuzzy file finder command palette.
- **Obsidian**: Markdown knowledge base and notes repository.
- **Alacritty / Zsh**: System CLI with Arch Linux pacman & omafetch.

## 3. Persistence Engine
- **Room SQLite**: Real-time snapshot saves to `omarchy_workspace.db`."""
    )
  )

  val paletteCommands: List<NeovimPaletteCommand> = listOf(
    NeovimPaletteCommand(":w", "Save current buffer to disk", "File / Write", ":w"),
    NeovimPaletteCommand(":q", "Close active buffer / quit window", "File / Quit", ":q"),
    NeovimPaletteCommand(":wq", "Write buffer and quit", "File", ":wq"),
    NeovimPaletteCommand(":Telescope find_files", "Open fuzzy file finder across workspace", "Telescope", "<C-p>"),
    NeovimPaletteCommand(":Telescope buffers", "List and switch active buffers", "Telescope", "<leader>fb"),
    NeovimPaletteCommand(":Telescope live_grep", "Search text across all files", "Telescope", "<leader>fg"),
    NeovimPaletteCommand(":Telescope commands", "Open command palette", "Telescope", "<leader>fc"),
    NeovimPaletteCommand(":split", "Horizontal window split", "Window", "<C-w>s"),
    NeovimPaletteCommand(":vsplit", "Vertical window split", "Window", "<C-w>v"),
    NeovimPaletteCommand(":set number", "Show line numbers", "Editor Options"),
    NeovimPaletteCommand(":set nonumber", "Hide line numbers", "Editor Options"),
    NeovimPaletteCommand(":noh", "Clear search highlight matches", "Search", "<Esc>"),
    NeovimPaletteCommand(":checkhealth", "Run Neovim system and plugin diagnostic check", "Diagnostics"),
    NeovimPaletteCommand(":Format", "Format document via LSP / RuboCop", "Code Formatting", "<leader>fm"),
    NeovimPaletteCommand(":terminal", "Open embedded terminal split", "Terminal")
  )

  /**
   * Fast, intuitive fuzzy search algorithm matching characters sequentially
   * with bonuses for filename matches, word boundaries, and consecutive characters.
   */
  fun fuzzyMatch(query: String, target: String): FuzzyMatchResult {
    if (query.isEmpty()) {
      return FuzzyMatchResult(isMatch = true, score = 0)
    }

    val q = query.lowercase()
    val t = target.lowercase()

    var qIdx = 0
    var tIdx = 0
    var score = 0
    var consecutiveCount = 0
    val matchedIndices = mutableListOf<Int>()

    while (qIdx < q.length && tIdx < t.length) {
      val qc = q[qIdx]
      val tc = t[tIdx]

      if (qc == tc) {
        matchedIndices.add(tIdx)
        var matchScore = 10

        // Bonus for consecutive matches
        consecutiveCount++
        matchScore += consecutiveCount * 5

        // Bonus for word boundaries (after '/', '_', '-', '.')
        if (tIdx == 0 || t[tIdx - 1] in "/_-. ") {
          matchScore += 20
        }

        score += matchScore
        qIdx++
      } else {
        consecutiveCount = 0
      }
      tIdx++
    }

    val isMatch = qIdx == q.length
    return FuzzyMatchResult(
      isMatch = isMatch,
      score = if (isMatch) score else 0,
      matchedIndicesInPath = matchedIndices
    )
  }

  fun getFileByPath(path: String): WorkspaceProjectFile? {
    return workspaceFiles.find { it.path.equals(path, ignoreCase = true) || it.filename.equals(path, ignoreCase = true) }
  }
}

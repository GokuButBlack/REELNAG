# ReelNag — AI-Powered Reel Companion

ReelNag is a witty, no-nonsense native Android companion for people who save Instagram Reels and forget them, and for creators who turn saved inspiration into original content. Built around the motto *"Save it. Watch it. Create it. Don't get nagged about it."*, it combines a fast Reel Locker, personality-driven Smart Nag reminders, a Trend Radar, an AI Content Studio powered by Gemini High Thinking, and a full Content Planner.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product and scope decisions were confirmed during our planning interview and govern this build:

- **Confirmed Decision 1 (Full Phase 1 + Phase 2 Scope)**: We are building **both Phase 1 and Phase 2** right now—meaning the **Discover** tab will feature the live **Trend Radar** & **"What Should I Reel Today"** helper, and the **Create** tab will feature the **AI Content Studio** (powered by `gemini-4-argon` with `ThinkingLevel.HIGH`) and the **Content Planner**.
- **Confirmed Decision 2 (Pre-loaded Sample Data)**: The app will launch with **3–4 clearly labeled sample saved Reels, sample reminders, and curated sample trends** so you can immediately explore, filter, snooze, and generate AI concepts without typing links first.
- **Confirmed Decision 3 (Android Share Sheet + Deep Links)**: In addition to manual URL pasting and deep-link query support (`?url=...`), ReelNag registers as an Android system Share target (`ACTION_SEND` for `text/plain`), allowing you to share any Reel directly from the Instagram app into ReelNag's Save Sheet.
- **Confirmed Decision 4 (Local-Only Privacy & Honesty Rules)**: Per your specification, all data is persisted strictly on-device using an Android Room SQLite database + DataStore preferences (surviving app restarts with full JSON Export/Import and Reset). We never fabricate live Instagram metadata, engagement metrics, or direct Instagram publishing.

---

## 1. Overview & Core Concept

- **What It Does**: ReelNag rescues your saved Instagram Reels from digital purgatory. It lets you save Reel links in seconds, organizes them into customizable categories, nags you with rotating personality-driven reminders when they collect dust, and helps you brainstorm and schedule original video concepts using AI.
- **Target Audience / Persona**:
  - **Reel Viewers / Collectors** who save tutorials, recipes, workouts, and memes intending to watch them later.
  - **Content Creators** who collect audio/editing inspiration and want structured scene-by-scene scripts, hooks, and a publishing calendar.
- **Key Value**: Feels like a funny, smart friend rather than a sterile spreadsheet—while adhering to strict honesty rules (no fake metadata scraping, no fake follower stats, and transparent local storage & notification behavior).

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Skippable Onboarding**: A breezy 4-step welcome sheet (or instant 1-tap *Skip with Defaults*) to pick Interests, Role (*Viewer*, *Creator*, or *Both*), Reminder Personality (*Sarcastic Bestie*, *Chill Friend*, *Drama Queen*, *Motivator*, *Unhinged Friend*), and Quiet Hours.
2. **Home Dashboard**: Opens with a personalized greeting and a rotating daily quip in your chosen personality. Displays real live counters (*Saved*, *Watched*, *Unwatched*, *Completed Nags*), an **Overdue / Due Now Nags** banner with 1-tap *Snooze (10m, 1h, Tomorrow)* and *Done*, a **"What Should I Reel Today?"** smart card (surfacing your oldest unwatched Reel or unfinished planner draft), and quick actions.
3. **Reel Locker (Saved Tab)**: Fast search (title, description, notes), category & watch-status filter chips, and sorting (Date Saved, Reminder Date). Tapping **+ Save Reel** (or sharing from Instagram) opens an auto-focused Bottom Sheet that normalizes URLs, strips tracking params (`igsh`, `utm_*`), blocks duplicates by pointing out the existing item, and lets you assign 1-tap category chips, creator name, notes, and a Smart Nag reminder. Each card displays a clean fallback aesthetic with your title, category badge, age badge, and an **Open Original** button that launches Instagram/browser.
4. **Smart Nag Reminders**: Accessible from Home, Reel cards, or the dedicated Nag Manager modal. Includes a live **Message Preview** generator showing how your chosen personality will roast or encourage you, with at least 8 distinct templates per personality (40+ total messages) that never repeat back-to-back and dynamically inject item title, days since saved, snooze count, and unwatched count.
5. **Discover Tab (Trend Radar + Daily Helper)**: Browse curated creative format trends across *Comedy, Fashion, Fitness, Gaming, Food, Lifestyle, Editing Styles,* and *AI Content* (clearly labeled as curated/user-added inspiration), add your own trend links, save trends, or tap **Generate Ideas** to jump straight into the AI Studio with that trend pre-filled.
6. **Create Tab (AI Content Studio + Content Planner)**:
   - **AI Content Studio**: Pick an input source (Topic, Trend, Own Idea, Saved Reel, or Reference URL) and tune selectors (*Niche, Audience, Video Length, Tone, Goal*). Uses `gemini-4-argon` with High Thinking mode (`thinkingLevel = "HIGH"`) to generate **3 distinct video concepts**, each complete with Title, Opening Hook, Concept Explanation, Scene-by-Scene Outline, Suggested Shots, On-Screen Text, Caption, Hashtags, Call to Action, and Editing Suggestions. If no API key is configured or offline, displays a clear, friendly fallback notice while keeping manual concept creation and the rest of the app 100% functional.
   - **Content Planner**: Switch between **List View** and **Calendar / Status Board View** across 5 stages (*Idea, Planned, In Progress, Ready to Publish, Published*), track weekly content goals, and attach reminders.
7. **Profile & Settings**: Edit personality, interests, role, quiet hours, and notification permissions; read plain-English guides on Android Share Sheet / Deep-Link shortcuts and local privacy; and run **Export Data (JSON)**, **Import Data (JSON)**, or **Reset App**.

### Visual Identity & Theme
- **Aesthetic Direction**: *Midnight Velvet Studio* — sleek dark charcoal surfaces paired with electric purple and soft lavender accents, editorial display typography, tactile pill chips, and restrained top-card radial glows.
- **Color Palette & Mood**:
  - `--background`: Deep Charcoal (`#0E0E12`)
  - `--surface`: Elevated Slate (`#17171F`) & Card Surface (`#1F1F2B`)
  - `--primary`: Electric Lavender-Purple (`#A855F7` / `#C084FC`)
  - `--secondary`: Soft Periwinkle (`#818CF8`)
  - `--accent-warning`: Amber Glow (`#FBBF24` for overdue nags)
  - `--accent-success`: Mint Emerald (`#34D399` for watched / completed)
- **Typography & Hierarchy**:
  - **Display / Headlines**: *Space Grotesk* (bundled locally via `res/font`) for punchy personality quotes, screen headers, and stat counters.
  - **Body & UI Labels**: *Plus Jakarta Sans* (bundled locally) for crisp readability across notes, AI scripts, and filter chips.
- **Component Styling & Layout**:
  - Adaptive Material 3 navigation: Bottom `NavigationBar` (`HOME | DISCOVER | CREATE | SAVED | PROFILE`) on handheld portrait screens, automatically switching to a side `NavigationRail` on wide/tablet screens.
  - Rounded 20dp cards with subtle 1dp lavender-tinted borders (`#2E2E3E`) and spring-animated status toggles.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: URL Normalization & Duplicate Detection Engine**
  - *Chosen Approach*: Before saving any Reel URL, a dedicated normalizer parses the URI, validates that it is a well-formed HTTP/HTTPS link, canonicalizes Instagram `/reel/` and `/p/` paths, and strips tracking query parameters (`igsh`, `igshid`, `utm_source`, `utm_medium`, etc.).
  - *Why*: Instagram share links append unique tracking hashes every time you copy them. Stripping tracking params ensures true duplicate detection so we can immediately show the user which saved Reel already matches.
- **Decision 2: Separate Deterministic Smart Nag Message Engine + Gemini High Thinking Studio**
  - *Chosen Approach*: Keep the Smart Nag reminder message generator 100% local and deterministic (40+ rich templates across the 5 personalities with anti-repeat state and dynamic placeholders), while powering the **AI Content Studio** with `gemini-4-argon` and `ThinkingLevel.HIGH`.
  - *Why*: Guarantees that core Reel saving and funny reminders work instantaneously with zero API latency or key requirement, while leveraging deep AI reasoning where it shines most—crafting three distinct, scene-by-scene creator scripts.
- **Decision 3: Honest Multi-Layer Reminder Delivery**
  - *Chosen Approach*: Combine an in-app **Due Now / Overdue Banner** with Android system notifications (`NotificationCompat` + runtime `POST_NOTIFICATIONS` permission flow) that respect user-configured Quiet Hours and clearly explain delivery behavior in plain English.
  - *Why*: Ensures reminders are never lost even if system notifications are denied or silenced during quiet hours.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram
```
┌────────────────────────────────────────────────────────────────────────────┐
│                   MainActivity (Intent & Deep-Link Router)                 │
│     Handles ACTION_SEND (Share Sheet) & ?url= Deep Links -> Save Sheet     │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼──────────────────────────────────────┐
│                  Adaptive App Shell (BottomBar / NavRail)                  │
├─────────────┬───────────────┬───────────────┬──────────────┬───────────────┤
│  HomeScreen │ DiscoverScreen│  CreateScreen │  SavedScreen │ ProfileScreen │
│  - Greeting │ - Trend Radar │ - AI Content  │ - ReelLocker │ - Personality │
│  - Due Nags │ - Category    │   Studio      │ - URL Norm & │ - Quiet Hours │
│  - Daily    │   Filters     │ - 3-Concept   │   Dedup      │ - JSON Export │
│    Helper   │ - Add Trend   │   Generator   │ - Category   │   & Import    │
│  - LiveStat │ - "Reel Today"│ - Planner     │   CRUD       │ - Privacy &   │
│    Counters │   Advisor     │   List/Cal    │ - Search/Sort│   Reset App   │
└──────┬──────┴───────┬───────┴───────┬───────┴──────┬───────┴───────┬───────┘
       │              │               │              │               │
       └──────────────┴───────────────┼──────────────┴───────────────┘
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     ReelNagViewModel (StateFlow + MVVM)                    │
├─────────────────────────┬──────────────────────────┬───────────────────────┤
│   SmartNagMessageEngine │   UrlNormalizer &        │   GeminiStudioService │
│   - 5 Personalities     │   DuplicateValidator     │   - gemini-4-argon    │
│   - 40+ Templates       │   - Strips igsh/utm_*    │   - thinkingLevel:    │
│   - Anti-Repeat Tracker │   - Matches Existing     │     HIGH              │
│   - Quiet Hours Filter  │     Saved Reels          │   - Structured JSON   │
└─────────────────────────┴─────────────┬────────────┴───────────────────────┘
                                        ▼
┌────────────────────────────────────────────────────────────────────────────┐
│             ReelNagRepository & Room Database (Versioned v1)               │
│  Entities: SavedReel | Category | NagReminder | TrendItem |                │
│            GeneratedIdeaBatch | PlannerItem | UserSettings (DataStore/JSON)│
└────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **`SavedReel`**: `id`, `rawUrl`, `normalizedUrl` (indexed for duplicate check), `title`, `description`, `categoryId`, `personalNotes`, `creatorName`, `savedAt`, `isWatched`, `watchedAt`, `isSample`.
- **`Category`**: `id`, `name`, `isDefault`, `colorHex`, `sortOrder` (Defaults: *Funny, Fashion, Fitness, Learn Later, Inspiration, Editing Tutorials, Content Ideas, Favorites*).
- **`NagReminder`**: `id`, `reelId` (nullable for general reminders), `title`, `scheduledAt`, `recurrence` (*ONCE, DAILY, WEEKLY*), `snoozeCount`, `isCompleted`, `isEnabled`, `previewMessage`, `lastDeliveredMessage`.
- **`TrendItem`**: `id`, `name`, `description`, `category`, `sourceUrl`, `dateVerified`, `creativeInspiration`, `isCuratedSample`.
- **`PlannerItem`**: `id`, `title`, `description`, `category`, `plannedDate`, `caption`, `relatedReelId`, `relatedTrendId`, `notes`, `status` (*IDEA, PLANNED, IN_PROGRESS, READY_TO_PUBLISH, PUBLISHED*).
- **`UserSettings`**: `onboardingCompleted`, `interests` (list), `role` (*VIEWER, CREATOR, BOTH*), `personality` (*SARCASTIC_BESTIE, CHILL_FRIEND, DRAMA_QUEEN, MOTIVATOR, UNHINGED_FRIEND*), `preferredReminderTime`, `quietHoursStart`, `quietHoursEnd`, `weeklyContentGoal`, `lastMessageId`.

### Interactive Component & State Mapping
- **Quick-Save & Duplicate Guard**: Typing or sharing a URL runs `UrlNormalizer.normalize(url)` in real time. If invalid, shows an inline error message. If it matches an existing `SavedReel.normalizedUrl`, highlights the existing Reel card with a 1-tap *View Existing Item* action instead of creating a duplicate.
- **Smart Nag Lifecycle**: Creating or editing a reminder updates the live personality preview box immediately. Triggering *Snooze (10m / 1h / Tomorrow)* increments `snoozeCount` (which makes subsequent Sarcastic/Unhinged messages wittier) and shifts `scheduledAt`.
- **AI Content Studio**: Clicking *Generate 3 Concepts* validates inputs, invokes `gemini-4-argon` with `ThinkingConfig(thinkingLevel = "HIGH")` and a structured JSON schema, and renders 3 expandable concept cards with 1-tap *Copy Script*, *Edit*, *Save*, and *Send to Content Planner* handlers.
- **JSON Backup & Restore**: *Export My Data* serializes the entire Room database and settings into a formatted JSON string with system share/clipboard/file export, while *Import Data* validates the JSON schema and restores all tables atomically.

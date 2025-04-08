# Second Brain App - Development To-Do List

**Version:** 1.0
**Date:** April 8, 2025
**Goal:** Build MVP focused on core features, user-controlled AI/Storage, and linking.

---

## Phase 0: Project Setup & Foundation (Priority: High)

* `[ ]` **Environment Setup:**
    * `[ ]` Install/Update Android Studio.
    * `[ ]` Setup Kotlin environment.
    * `[ ]` Setup Emulator/Test Device.
* `[ ]` **Project Initialization:**
    * `[ ]` Create new Android Project (Empty Compose Activity).
    * `[ ]` Configure `build.gradle` (Kotlin version, dependencies placeholder).
    * `[ ]` Setup package structure (e.g., `ui`, `data`, `domain`, `di`, `util`).
* `[ ]` **Version Control:**
    * `[ ]` Initialize Git repository.
    * `[ ]` Create initial commit.
    * `[ ]` Setup remote repository (e.g., GitHub, GitLab).
    * `[ ]` Define branching strategy (e.g., Gitflow).
* `[ ]` **Core Data Model:**
    * `[ ]` Define `Note` data class/entity (ID, title, content, createdAt, updatedAt, metadata placeholders).
* `[ ]` **Database Setup:**
    * `[ ]` Add Room Persistence Library dependencies.
    * `[ ]` Create Room `NoteDatabase`.
    * `[ ]` Create `NoteDao` interface with basic CRUD operations (insert, update, delete, getById, getAll).
    * `[ ]` Implement basic database migrations setup (if needed later).
* `[ ]` **Dependency Injection:**
    * `[ ]` Choose DI framework (e.g., Hilt, Koin).
    * `[ ]` Add DI framework dependencies.
    * `[ ]` Setup basic modules (AppModule, DatabaseModule).
* `[ ]` **Basic Navigation:**
    * `[ ]` Add Jetpack Navigation Compose dependency.
    * `[ ]` Define basic navigation graph (e.g., NoteListScreen -> NoteDetailScreen -> NoteEditScreen).
    * `[ ]` Implement basic navigation controller setup.
* `[ ]` **Open Source:**
    * `[ ]` Choose Open Source License (e.g., MIT, Apache 2.0).
    * `[ ]` Add LICENSE file to repository root.

---

## Phase 1: Core Note Management (MVP - Priority: High)

* `[ ]` **Note List Screen:**
    * `[ ]` Design UI for displaying a list of notes (e.g., Card per note with title/snippet).
    * `[ ]` Implement ViewModel for NoteListScreen.
    * `[ ]` Implement fetching all notes from DB via DAO in ViewModel.
    * `[ ]` Implement observing note list LiveData/Flow in Composable.
    * `[ ]` Implement navigation to NoteDetailScreen on item tap.
    * `[ ]` Implement "New Note" Floating Action Button (FAB) or equivalent.
* `[ ]` **Note Detail/View Screen:**
    * `[ ]` Design UI for displaying a single note's content.
    * `[ ]` Implement ViewModel for NoteDetailScreen.
    * `[ ]` Implement fetching specific note by ID from DB in ViewModel.
    * `[ ]` Implement Markdown rendering for note content (basic initial rendering).
    * `[ ]` Add "Edit" button/icon navigating to NoteEditScreen.
    * `[ ]` Add "Delete" action/button.
* `[ ]` **Note Editor Screen:**
    * `[ ]` Design UI for editing note content (TextField for Markdown).
    * `[ ]` Implement ViewModel for NoteEditScreen.
    * `[ ]` Load existing note content into editor (if editing).
    * `[ ]` Implement saving changes (update DB via DAO/ViewModel).
    * `[ ]` Implement creating new note (insert into DB).
    * `[ ]` Handle navigation back after save/cancel.
* `[ ]` **Deletion Logic:**
    * `[ ]` Implement delete function in NoteDao.
    * `[ ]` Implement delete logic in relevant ViewModel.
    * `[ ]` Add confirmation dialog before deletion.
* `[ ]` **Basic Local Storage (`.md`):**
    * `[ ]` *(Defer initial DB implementation for faster core UI? Alternative: Start with DB)*
    * `[ ]` Refine Note saving logic to write content to a `.md` file in app-specific storage.
    * `[ ]` Refine Note loading logic to read from `.md` file.
    * `[ ]` Link DB entry (metadata) to the `.md` file path.

---

## Phase 2: Content Capturing (MVP - Priority: High)

* `[ ]` **Share Sheet Integration:**
    * `[ ]` Configure `AndroidManifest.xml` to handle `ACTION_SEND` intents for `text/plain` (URLs, text).
    * `[ ]` Create Activity/Composable to process shared intent data.
    * `[ ]` Extract URL or text from intent.
    * `[ ]` Trigger note creation/saving workflow with extracted data.
* `[ ]` **URL Content Fetching:**
    * `[ ]` Add networking library (e.g., Retrofit, Ktor - if not already added) or use built-in HttpURLConnection/OkHttp.
    * `[ ]` Add basic HTML parsing/article extraction library (e.g., Jsoup).
    * `[ ]` Implement background task (Coroutine/WorkManager) to fetch URL content.
    * `[ ]` Extract main article text/title. Handle fetching errors.
* `[ ]` **Paste Handling:**
    * `[ ]` Add "Paste" button/action in Note Editor.
    * `[ ]` Implement logic to get content from clipboard and insert into editor.

---

## Phase 3: AI Integration Framework (MVP - Priority: Medium-High)

* `[ ]` **Settings Screen - AI Section:**
    * `[ ]` Create Settings Screen Composable.
    * `[ ]` Add navigation entry for Settings.
    * `[ ]` Design UI layout for AI Provider list.
    * `[ ]` Design UI for API Key input (masked field) per provider.
    * `[ ]` Design UI for default AI selection.
* `[ ]` **Secure Key Storage:**
    * `[ ]` Implement wrapper/utility class for Android Keystore operations (encrypt/decrypt).
    * `[ ]` Save encrypted API keys in SharedPreferences or Room DB.
    * `[ ]` Load and decrypt API keys when needed for API calls.
* `[ ]` **API Key Input & Guidance:**
    * `[ ]` Implement input fields for each supported provider.
    * `[ ]` Implement saving logic (encrypt & store).
    * `[ ]` Implement clickable `Link` text (using `UriHandler`) for each provider pointing to API key documentation (See PRD Appendix).
* `[ ]` **AI Selection:**
    * `[ ]` Implement saving/loading user's default AI provider choice (SharedPreferences).
    * `[ ]` Use selected provider for summarization tasks.
* `[ ]` **Networking & API Call:**
    * `[ ]` Add/Configure Networking Client (Retrofit/Ktor).
    * `[ ]` Define data classes for API request/response (for one provider, e.g., OpenAI Completion/Chat API).
    * `[ ]` Create API Service interface (Retrofit).
    * `[ ]` Implement Repository/UseCase layer for making the summarization API call (pass content and decrypted key).
    * `[ ]` Handle injecting API key into request headers securely.
* `[ ]` **Integration & Feedback:**
    * `[ ]` Add "Summarize" button/action in Note Editor/Detail screen or trigger automatically after URL capture.
    * `[ ]` Trigger API call logic from ViewModel using Coroutines.
    * `[ ]` Display loading indicator during API call.
    * `[ ]` Update note content with summary on success.
    * `[ ]` Display user-friendly error messages on API failure (invalid key, network error, quota exceeded).

---

## Phase 4: Markdown & Metadata (MVP - Priority: High)

* `[ ]` **Markdown Rendering:**
    * `[ ]` Research and choose a Markdown rendering library for Jetpack Compose (e.g., `markdown-compose`, `commonmark-java` with Compose integration).
    * `[ ]` Add library dependency.
    * `[ ]` Implement Markdown rendering in NoteDetailScreen.
* `[ ]` **YAML Front Matter:**
    * `[ ]` Choose YAML parsing library (e.g., SnakeYAML).
    * `[ ]` Define standard Front Matter keys (`title`, `url`, `date`, `tags: []`).
    * `[ ]` Modify Note saving logic: Prepend Front Matter block to `.md` content before writing file/saving to DB field.
    * `[ ]` Modify Note loading logic: Parse Front Matter on load, store metadata separately or make accessible.
    * `[ ]` Automatically populate `url` and `date` metadata during content capture.

---

## Phase 5: Linking - Basic (MVP - Priority: High)

* `[ ]` **Syntax & Parsing:**
    * `[ ]` Implement regex or parser to detect `[[Note Title]]` syntax in Markdown content.
* `[ ]` **Link Resolution & Navigation:**
    * `[ ]` Implement logic to find target `Note` entity by title (case-insensitive search on Note titles in DB).
    * `[ ]` Make `[[Link]]` text clickable in rendered Markdown view.
    * `[ ]` Implement navigation to the target NoteDetailScreen on link click.
    * `[ ]` Handle case where target note title doesn't exist (e.g., show toast, disable link).
* `[ ]` **Backlinks:**
    * `[ ]` Design DB structure for storing links (e.g., separate `Links` table: `source_note_id`, `target_note_title`, `target_note_id`). *Update:* Maybe just query notes content? Decide on approach.*
    * `[ ]` *If separate table:* Update link table whenever a note is saved/updated/deleted.
    * `[ ]` Implement DB query (DAO method) to find all notes linking *to* a given `note_id` or `note_title`.
    * `[ ]` Implement UI section in NoteDetailScreen to display backlinks (list of clickable source note titles).
    * `[ ]` Fetch and display backlinks in NoteDetailViewModel.
* `[ ]` **Editor Auto-complete:**
    * `[ ]` Implement logic in NoteEditorScreen to detect typing `[[`.
    * `[ ]` Query existing Note titles from DB.
    * `[ ]` Display suggestions in a dropdown/popup near the cursor.
    * `[ ]` Insert selected `[[Note Title]]` on suggestion tap.

---

## Phase 6: Basic Search (MVP - Priority: Medium)

* `[ ]` **Search UI:**
    * `[ ]` Add Search Bar Composable to NoteListScreen (or separate Search Screen).
* `[ ]` **Search Logic:**
    * `[ ]` Implement DAO method for searching notes by keyword (using `LIKE %keyword%` on title and content fields). Ensure it's efficient (FTS4/5 if needed later).
    * `[ ]` Implement search logic in ViewModel, triggering query on search text change (with debouncing).
* `[ ]` **Display Results:**
    * `[ ]` Display search results list dynamically below search bar.
    * `[ ]` Make search results clickable, navigating to the respective NoteDetailScreen.

---

## Phase 7: Storage - Cloud Integration (MVP - Priority: Medium)

* `[ ]` **Settings UI - Storage:**
    * `[ ]` Create UI section in Settings for storage options (Local, GDrive, OneDrive).
    * `[ ]` Add radio buttons/selector for choice.
    * `[ ]` Add button to trigger Auth flow for cloud providers.
    * `[ ]` Add selector for Sync Scope (`.md` only / All Data).
* `[ ]` **Google Drive Integration:**
    * `[ ]` Add Google Drive REST API client library/SDK dependencies.
    * `[ ]` Implement Google Sign-In for authentication & authorization (requesting `drive.file` or `drive.appdata` scope).
    * `[ ]` Securely store refresh/access tokens.
    * `[ ]` Implement Drive API calls: list files, download file, upload file, create folder (within app space).
* `[ ]` **Sync Logic:**
    * `[ ]` Add WorkManager dependency.
    * `[ ]` Create background `Worker` for sync operations.
    * `[ ]` Implement logic to detect local changes since last sync.
    * `[ ]` Implement logic to detect remote changes since last sync (using Drive API).
    * `[ ]` Implement upload/download logic based on changes.
    * `[ ]` Implement basic conflict resolution (e.g., last write wins).
    * `[ ]` Schedule periodic sync and trigger sync after local changes.
* `[ ]` **OneDrive Integration (Post-MVP / Parallel):**
    * `[ ]` Add Microsoft Authentication Library (MSAL) dependency.
    * `[ ]` Add Microsoft Graph SDK dependency.
    * `[ ]` Implement Microsoft Account Auth flow.
    * `[ ]` Implement Graph API calls (similar to Drive: list, download, upload).
    * `[ ]` Integrate into existing Sync Logic.

---

## Phase 8: Storage Management (MVP - Priority: Medium-Low)

* `[ ]` **Settings UI - Management:**
    * `[ ]` Add "Manage Storage" section in Settings.
    * `[ ]` Add toggle for "Delete original after summarization".
    * `[ ]` Add options/buttons for bulk deletion (by age, by type).
* `[ ]` **Implementation:**
    * `[ ]` Store original file paths (if kept) associated with the note metadata.
    * `[ ]` Implement logic for the auto-delete toggle.
    * `[ ]` Implement background task/logic to find and delete original files based on selected criteria (age, MIME type). Requires storing file metadata (path, type, date added).
    * `[ ]` Add robust confirmation dialogs before executing bulk delete actions.

---

## Phase 9: UI/UX Refinement & Polish (Priority: Medium - Interspersed/Post-MVP)

* `[ ]` **Theming:** Implement dynamic Light/Dark theme support based on system settings/user preference.
* `[ ]` **Material Design 3:** Review UI components and layouts against M3 guidelines. Adapt where necessary.
* `[ ]` **Onboarding:** Create simple first-run screens explaining core features and guiding storage/AI setup.
* `[ ]` **Editor Enhancements:** Consider adding a simple Markdown formatting toolbar (Bold, Italic, Link, List).
* `[ ]` **Accessibility Review:** Test with TalkBack, check contrast ratios, ensure adequate touch target sizes.

---

## Phase 10: Testing & Bug Fixing (Priority: High - Ongoing)

* `[ ]` **Unit Tests:** Write tests for ViewModels, Repositories, UseCases, Utility functions. Mock dependencies (DAO, API Services).
* `[ ]` **Integration Tests:** Test Room DB operations. Test interactions between components.
* `[ ]` **Manual Testing:**
    * `[ ]` Test all user flows on different Android versions/devices.
    * `[ ]` Test edge cases (no network, API errors, storage full, invalid input).
    * `[ ]` Test sync logic thoroughly (offline changes, conflicts).
* `[ ]` **Performance Testing:** Monitor app launch time, UI smoothness, battery usage during sync.
* `[ ]` **Security Review:** Verify secure storage of keys/tokens. Check for data leaks.
* `[ ]` **Bug Fixing:** Track bugs using issue tracker (e.g., GitHub Issues). Prioritize and fix bugs based on severity.

---

## Phase 11: Release Preparation (Priority: High - Near End)

* `[ ]` **Assets:** Finalize app icon (adaptive icon support), any other required graphical assets.
* `[ ]` **Play Store Listing:** Write app title, short description, full description. Prepare screenshots and feature graphic. Determine content rating.
* `[ ]` **Build Release Version:**
    * `[ ]` Configure ProGuard/R8 for code shrinking and obfuscation.
    * `[ ]` Increment version code/name.
    * `[ ]` Generate signed release APK/AAB using upload key.
* `[ ]` **Open Source Repository:**
    * `[ ]` Create comprehensive README.md (overview, features, usage, setup, contribution guidelines).
    * `[ ]` Ensure LICENSE file is present and correct.
    * `[ ]` Clean up codebase, add comments where needed.
    * `[ ]` Push final code to public repository.
* `[ ]` **Deployment:**
    * `[ ]` Create new release in Google Play Console.
    * `[ ]` Upload signed AAB.
    * `[ ]` Fill in store listing details.
    * `[ ]` Roll out release (start with internal/alpha/beta testing).

---
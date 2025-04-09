# Second Brain App - Development To-Do List

**Version:** 1.0
**Date:** April 8, 2025
**Goal:** Build MVP focused on core features, user-controlled AI/Storage, and linking.

## Current Status (Updated: May 2025)

### Completed Phases ✅
- Phase 0: Project Setup & Foundation
- Phase 1: Core Note Management
- Phase 6: Basic Search

### Partially Completed Phases 🔄
- Phase 5: Linking - Basic (Editor Auto-complete pending)

### Next Steps (Priority Order) 🚀
1. Complete Phase 2: Content Capturing (Share Sheet Integration, URL Content Fetching)
2. Implement Phase 3: AI Integration Framework
3. Implement Phase 4: Markdown & Metadata
4. Complete remaining parts of Phase 5: Linking - Basic (Editor Auto-complete)
5. Implement Phase 7: Storage - Cloud Integration

---

## Phase 0: Project Setup & Foundation (Priority: High) - COMPLETED ✅

* `[x]` **Environment Setup:**
    * `[x]` Install/Update Android Studio.
    * `[x]` Setup Kotlin environment.
    * `[x]` Setup Emulator/Test Device.
* `[x]` **Project Initialization:**
    * `[x]` Create new Android Project (Empty Compose Activity).
    * `[x]` Configure `build.gradle` (Kotlin version, dependencies placeholder).
    * `[x]` Setup package structure (e.g., `ui`, `data`, `domain`, `di`, `util`).
* `[x]` **Version Control:**
    * `[x]` Initialize Git repository.
    * `[x]` Create initial commit.
    * `[x]` Setup remote repository (e.g., GitHub, GitLab).
    * `[x]` Define branching strategy (e.g., Gitflow).
* `[x]` **Core Data Model:**
    * `[x]` Define `Note` data class/entity (ID, title, content, createdAt, updatedAt, metadata placeholders).
* `[x]` **Database Setup:**
    * `[x]` Add Room Persistence Library dependencies.
    * `[x]` Create Room `NoteDatabase`.
    * `[x]` Create `NoteDao` interface with basic CRUD operations (insert, update, delete, getById, getAll).
    * `[x]` Implement basic database migrations setup (if needed later).
* `[x]` **Dependency Injection:**
    * `[x]` Choose DI framework (Hilt).
    * `[x]` Add DI framework dependencies.
    * `[x]` Setup basic modules (AppModule, DatabaseModule).
* `[x]` **Basic Navigation:**
    * `[x]` Add Jetpack Navigation Compose dependency.
    * `[x]` Define basic navigation graph (e.g., NoteListScreen -> NoteDetailScreen -> NoteEditScreen).
    * `[x]` Implement basic navigation controller setup.
* `[x]` **Open Source:**
    * `[x]` Choose Open Source License (MIT).
    * `[x]` Add LICENSE file to repository root.

---

## Phase 1: Core Note Management (MVP - Priority: High) - COMPLETED ✅

* `[x]` **Note List Screen:**
    * `[x]` Design UI for displaying a list of notes (e.g., Card per note with title/snippet).
    * `[x]` Implement ViewModel for NoteListScreen.
    * `[x]` Implement fetching all notes from DB via DAO in ViewModel.
    * `[x]` Implement observing note list LiveData/Flow in Composable.
    * `[x]` Implement navigation to NoteDetailScreen on item tap.
    * `[x]` Implement "New Note" Floating Action Button (FAB) or equivalent.
* `[x]` **Note Detail/View Screen:**
    * `[x]` Design UI for displaying a single note's content.
    * `[x]` Implement ViewModel for NoteDetailScreen.
    * `[x]` Implement fetching specific note by ID from DB in ViewModel.
    * `[x]` Implement Markdown rendering for note content (basic initial rendering).
    * `[x]` Add "Edit" button/icon navigating to NoteEditScreen.
    * `[x]` Add "Delete" action/button.
* `[x]` **Note Editor Screen:**
    * `[x]` Design UI for editing note content (TextField for Markdown).
    * `[x]` Implement ViewModel for NoteEditScreen.
    * `[x]` Load existing note content into editor (if editing).
    * `[x]` Implement saving changes (update DB via DAO/ViewModel).
    * `[x]` Implement creating new note (insert into DB).
    * `[x]` Handle navigation back after save/cancel.
* `[x]` **Deletion Logic:**
    * `[x]` Implement delete function in NoteDao.
    * `[x]` Implement delete logic in relevant ViewModel.
    * `[x]` Add confirmation dialog before deletion.
* `[x]` **Basic Local Storage (`.md`):**
    * `[x]` Refine Note saving logic to write content to a `.md` file in app-specific storage.
    * `[x]` Refine Note loading logic to read from `.md` file.
    * `[x]` Link DB entry (metadata) to the `.md` file path.

---

## Phase 2: Content Capturing (MVP - Priority: High) - CURRENT FOCUS 💯

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

## Phase 3: AI Integration Framework (MVP - Priority: Medium-High) - UPCOMING 📈

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

## Phase 4: Markdown & Metadata (MVP - Priority: High) - UPCOMING 📈

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

## Phase 5: Linking - Basic (MVP - Priority: High) - PARTIALLY COMPLETED ✅

* `[x]` **Syntax & Parsing:**
    * `[x]` Implement regex or parser to detect `[[Note Title]]` syntax in Markdown content.
* `[x]` **Link Resolution & Navigation:**
    * `[x]` Implement logic to find target `Note` entity by title (case-insensitive search on Note titles in DB).
    * `[x]` Make `[[Link]]` text clickable in rendered Markdown view.
    * `[x]` Implement navigation to the target NoteDetailScreen on link click.
    * `[x]` Handle case where target note title doesn't exist (e.g., show toast, disable link).
* `[x]` **Backlinks:**
    * `[x]` Design DB structure for storing links (using content query approach).
    * `[x]` Implement DB query (DAO method) to find all notes linking *to* a given `note_id` or `note_title`.
    * `[x]` Implement UI section in NoteDetailScreen to display backlinks (list of clickable source note titles).
    * `[x]` Fetch and display backlinks in NoteDetailViewModel.
* `[ ]` **Editor Auto-complete:** - PENDING
    * `[ ]` Implement logic in NoteEditorScreen to detect typing `[[`.
    * `[ ]` Query existing Note titles from DB.
    * `[ ]` Display suggestions in a dropdown/popup near the cursor.
    * `[ ]` Insert selected `[[Note Title]]` on suggestion tap.

---

## Phase 6: Basic Search (MVP - Priority: Medium) - COMPLETED ✅

* `[x]` **Search UI:**
    * `[x]` Add Search Bar Composable to NoteListScreen (or separate Search Screen).
* `[x]` **Search Logic:**
    * `[x]` Implement DAO method for searching notes by keyword (using `LIKE %keyword%` on title and content fields).
    * `[x]` Implement search logic in ViewModel, triggering query on search text change (with debouncing).
* `[x]` **Display Results:**
    * `[x]` Display search results list dynamically below search bar.
    * `[x]` Make search results clickable, navigating to the respective NoteDetailScreen.

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
# Product Requirements Document: Second Brain

**Version:** 1.0
**Date:** April 8, 2025
**Status:** Draft
**Author:** Charles (based on user specifications)
**Location Context:** India

---

## 1. Introduction

### 1.1 Purpose
This document outlines the product requirements for the "Second Brain" Android application (Version 1.0). It details the app's vision, features, functional and non-functional requirements, design considerations, and scope to guide development and ensure alignment with user needs.

### 1.2 App Vision
To empower users with a free, open-source, and highly customizable personal knowledge management (PKM) system on Android. Second Brain aims to be a flexible tool for capturing, connecting, and retrieving information, giving users full control over their AI tools and data storage.

### 1.3 Goals
* Provide a robust platform for capturing thoughts, summaries, and notes.
* Enable fluid connections between pieces of information through bi-directional linking.
* Offer unparalleled flexibility by allowing users to integrate their preferred AI models via API keys.
* Grant users control over their data storage, supporting both local and cloud options (Google Drive, OneDrive).
* Foster a community around an open-source project.
* Deliver a seamless and intuitive user experience specifically tailored for Android.

### 1.4 Target Audience
* Students, researchers, writers, and lifelong learners who actively gather and process information.
* Knowledge workers who need to organize project notes, research, and ideas.
* Privacy-conscious users who prefer control over their data and the AI tools used to process it.
* Users familiar with PKM concepts like Zettelkasten, linking, and markdown.
* Technically inclined users comfortable with obtaining and managing API keys.
* Android users seeking a native, powerful PKM solution.

---

## 2. Scope

### 2.1 In Scope (Version 1.0)
* **Native Android Application:** Development focused solely on the Android platform.
* **Core Note Taking:** Creating, editing, viewing, and deleting notes in Markdown format.
* **Content Capturing:** Importing content via Android Share Sheet (URLs, text), direct text input, and potentially file import (e.g., `.txt`, `.md`).
* **AI Summarization (User Keys):** Integration framework for users to add their own API keys (ChatGPT, Gemini, Deepseek, OpenRouter, Claude, Perplexity, Grok) and use the selected AI for summarizing captured content. Includes guidance links for obtaining keys.
* **Markdown (.md) Focus:** All notes/summaries stored as `.md` files with potential YAML front matter for metadata.
* **Linked Thinking:**
    * `[[Note Title]]` syntax for creating links between notes.
    * Automatic generation and display of backlinks (Linked Mentions).
    * Basic support for identifying Unlinked Mentions.
* **Flexible Storage:**
    * Local device storage.
    * Integration with Google Drive (authentication, sync).
    * Integration with Microsoft OneDrive (authentication, sync).
    * User choice of storage location/sync strategy (.md only or all data).
* **Storage Management:** Settings to manage storage, including deleting original source files (based on age, type, or all).
* **Basic Search:** Keyword search across note titles and content.
* **Open Source:** Codebase available under a permissive open-source license (e.g., MIT or Apache 2.0).
* **Free to Use:** No purchase price, subscriptions, or in-app purchases.

### 2.2 Out of Scope (Version 1.0)
* Web Application or Browser Extension.
* iOS Application.
* Real-time Collaboration Features.
* Advanced AI Features beyond Summarization (e.g., AI chat with notes, automated tagging based on content).
* Transcription of audio/video content (may be added later via AI integration).
* Spaced Repetition System (SRS) or Active Recall features (inspired by Recall, but deferred).
* Augmented Browse (surfacing notes while Browse external web).
* Advanced Search Filters (date ranges, tags - basic tagging via front matter might be v1).
* WYSIWYG Markdown Editor (focus on plain text Markdown editing first).
* Built-in cloud storage provided by the app.
* User accounts or authentication managed by the app itself (relies on device/cloud provider auth).
* Import from other specific PKM tools (e.g., Roam, Obsidian, Logseq).

---

## 3. Functional Requirements

### 3.1 Feature: Core Note Management
* **Description:** Basic functions for creating, viewing, editing, and deleting notes.
* **User Stories:**
    * As a user, I want to create a new blank note so I can capture a quick thought.
    * As a user, I want to open an existing note to read or edit its content.
    * As a user, I want to edit the content of a note using Markdown syntax.
    * As a user, I want to save the changes I make to a note.
    * As a user, I want to delete a note I no longer need.
* **Acceptance Criteria:**
    * App provides a clear way to initiate new note creation.
    * Notes are displayed in a readable format, rendering basic Markdown.
    * A text editor is provided for modifying note content, supporting standard Markdown.
    * Changes are saved persistently to the chosen storage (local/.md file).
    * Deleted notes are removed from the user's view and storage. Confirmation prompt before deletion.

### 3.2 Feature: Content Capturing & Summarization
* **Description:** Importing external content and generating AI summaries using user-provided keys.
* **User Stories:**
    * As a user, I want to share a URL from my browser to Second Brain to save and summarize the article.
    * As a user, I want to share selected text to Second Brain to create a note with it.
    * As a user, I want to paste text into a new note and trigger a summarization action.
    * As a user, I want the summary generated by my chosen AI (configured in settings) to be added to the note.
    * As a user, I want the original source URL and capture date automatically saved as metadata with the summarized note.
* **Acceptance Criteria:**
    * App appears in the Android Share Sheet for URLs and Text.
    * Sharing a URL successfully creates a new note, fetches content (basic article extraction), triggers summarization via selected AI, and saves summary + metadata.
    * Sharing text creates a new note with that text, allowing manual summarization trigger.
    * Pasting text allows manual summarization trigger.
    * Summarization uses the API key and AI provider selected in settings.
    * Summary is appended/prepended to the note content or replaces selected text, as appropriate.
    * Metadata (source URL, timestamp) is saved (e.g., in YAML front matter).
    * Clear indication of summarization progress and success/failure. Errors from AI API (invalid key, quota) are reported to the user.

### 3.3 Feature: AI Integration (User API Keys)
* **Description:** Allowing users to configure and use their own AI API keys.
* **User Stories:**
    * As a user, I want to go to Settings and find a section for AI Integration.
    * As a user, I want to see a list of supported AI providers (ChatGPT, Gemini, etc.).
    * As a user, I want to tap on a provider and see an input field to securely enter my API key.
    * As a user, I want to tap a link next to the input field that takes me directly to the official webpage explaining how to get that provider's API key.
    * As a user, I want to select which configured AI provider should be used by default for summarization.
    * As a user, I want my entered API keys to be stored securely.
* **Acceptance Criteria:**
    * Dedicated "AI Integration" section in app settings.
    * List of supported providers is displayed.
    * Each provider has a dedicated input field for the API key. Key is masked during input.
    * Each provider has a functional, clickable link (opens external browser) pointing to the correct API key generation page. (See Appendix for example links).
    * Mechanism (e.g., dropdown, radio buttons) allows selecting the default summarizer AI from configured providers.
    * API keys are stored using Android Keystore or equivalent secure storage mechanism. They are not stored in plain text.
    * App successfully uses the selected provider and key for API calls.

### 3.4 Feature: Flexible Storage Options
* **Description:** Configuring storage location (Local, Google Drive, OneDrive) and sync behavior.
* **User Stories:**
    * As a user, I want to go to Settings and choose where my notes are stored: Local Only, Google Drive, or OneDrive.
    * As a user, when selecting Google Drive or OneDrive, I want to be guided through an authentication process to grant the app access.
    * As a user, I want to choose if only the `.md` summary files are synced to the cloud, or if the original source files (if kept) are also synced.
    * As a user, I want my notes to be automatically synced between my device and my chosen cloud storage when changes are made.
* **Acceptance Criteria:**
    * Dedicated "Storage Configuration" section in settings.
    * Options for Local, Google Drive, OneDrive are present.
    * Selecting a cloud provider initiates the standard OAuth 2.0 flow using official SDKs/APIs. Access permissions requested should be minimal necessary (e.g., access to app-specific folder).
    * Clear options exist to configure sync scope (`.md` only vs. All Data).
    * Background sync service reliably uploads/downloads changes. Basic conflict resolution is handled (e.g., last write wins with potential notification, or file duplication).
    * User is notified of major sync errors (e.g., authentication failure, insufficient cloud storage).

### 3.5 Feature: Markdown Notes & Storage Management
* **Description:** Using `.md` format and providing tools to manage storage space.
* **User Stories:**
    * As a user, I want all my notes and summaries saved as standard `.md` files so I can easily use them with other tools.
    * As a user, I want note metadata (like source URL, tags, creation date) stored within the `.md` file using YAML front matter.
    * As a user, I want an option in Settings to automatically delete the original downloaded file (e.g., PDF) after its summary is created, to save space.
    * As a user, I want tools in Settings under "Manage Storage" to bulk-delete original files older than a specific time (1 week, 1 month, etc.).
    * As a user, I want tools in Settings to bulk-delete original files of a specific type (e.g., all PDFs).
* **Acceptance Criteria:**
    * Notes are created and saved with the `.md` extension.
    * A defined YAML front matter structure is used at the beginning of each `.md` file for metadata (e.g., `title:`, `url:`, `date:`, `tags: []`).
    * Markdown rendering correctly interprets standard Markdown syntax.
    * Settings include a toggle for "Delete original file after summarization".
    * "Manage Storage" section provides options for timed deletion and type-based deletion of *original source files* (not the `.md` notes themselves).
    * Clear confirmation dialogs are shown before any bulk deletion action is performed.

### 3.6 Feature: Linked Thinking (Bi-Directional Links)
* **Description:** Creating and navigating connections between notes.
* **User Stories:**
    * As a user, while editing a note, I want to type `[[` and see suggestions for existing note titles to easily create a link.
    * As a user, I want to type `[[Existing Note Title]]` to create a link to that note.
    * As a user, when viewing a note, I want to tap on a `[[Link]]` to navigate directly to the linked note.
    * As a user, when viewing "Note A", I want to see a clearly marked section listing all other notes that link *to* "Note A" (backlinks/linked mentions).
    * As a user, I want to see a list of "Unlinked Mentions" where the title of the current note appears in other notes without being explicitly linked, so I can decide whether to link them.
* **Acceptance Criteria:**
    * Typing `[[` in the editor triggers an auto-complete dropdown populated with existing note titles. Selecting a title inserts the `[[Note Title]]` syntax.
    * `[[Note Title]]` syntax is parsed and rendered as a clickable link in the note view.
    * Tapping a link navigates the user to the target note. Handle broken links gracefully (e.g., indicate the target doesn't exist, offer to create it).
    * A "Linked Mentions" or "Backlinks" section is displayed in the note view, listing notes linking to the current one. Each item in the list is navigable.
    * An "Unlinked Mentions" section (optional for v1, but desirable) lists notes containing the current note's title as plain text. Provides an action to easily turn an unlinked mention into a link.
    * Link and backlink information is updated reliably when notes are created, deleted, or renamed.

### 3.7 Feature: Search & Discovery
* **Description:** Finding notes within the knowledge base.
* **User Stories:**
    * As a user, I want a search bar to quickly find notes by keywords in their title or content.
    * As a user, I want the search results to be displayed clearly, allowing me to tap to open the relevant note.
* **Acceptance Criteria:**
    * A persistent search input field is available in the main note list view.
    * Searching queries note titles and full-text content across all notes in the selected storage.
    * Search results are displayed dynamically as the user types (debounce recommended).
    * Tapping a search result navigates to that note.

### 3.8 Feature: Open Source & Free
* **Description:** Ensuring the app is free and its code is publicly available.
* **User Stories:**
    * As a user, I want to download and use the app without any cost or subscription.
    * As a developer or curious user, I want to access the app's source code on a public platform like GitHub.
    * As a user, I want to be assured that the app does not contain hidden tracking or monetization elements beyond using my own API keys/storage.
* **Acceptance Criteria:**
    * App is listed for free on the Google Play Store.
    * Source code is available in a public repository under a clear open-source license (e.g., MIT, Apache 2.0).
    * README file in the repository clearly states the project's goals, license, and contribution guidelines.
    * No in-app purchases, ads, or subscriptions are implemented.

---

## 4. Non-Functional Requirements

* **4.1 Performance:**
    * App launch time should be reasonably fast (< 3 seconds on mid-range devices).
    * UI scrolling (note lists, note content) should be smooth (60fps target).
    * Search results should appear quickly (< 1 second for typical queries on moderate-sized databases).
    * AI Summarization time is dependent on the external API, but the app should remain responsive during the API call (using background threads/coroutines) and provide progress indication.
    * Sync operations should run efficiently in the background without significantly impacting foreground performance or battery life.
* **4.2 Scalability:**
    * The app should handle a large number of notes (e.g., 10,000+) without significant degradation in search or linking performance. This implies efficient database indexing for notes and links.
    * Storage integration should handle potentially large files (if originals are kept) and numerous small `.md` files efficiently.
* **4.3 Security:**
    * User API keys for AI services must be stored securely using Android Keystore. They must not be exposed or logged inadvertently.
    * Cloud storage authentication (OAuth tokens) must be handled securely according to best practices and stored using appropriate secure mechanisms.
    * Network communication for API calls and cloud sync must use HTTPS.
* **4.4 Usability:**
    * The app must be intuitive and easy to navigate for users familiar with Android conventions.
    * Key workflows (adding notes, linking, configuring AI/storage) should be straightforward and require minimal steps.
    * Error messages (API errors, sync issues) should be user-friendly and provide actionable information where possible.
    * Onboarding/first-run experience should guide users through essential setups (storage, optionally AI keys).
* **4.5 Reliability:**
    * Data integrity is paramount. Note content and links should not be corrupted during saving or syncing.
    * Cloud sync should be robust, handling offline scenarios and resuming correctly when connectivity returns. Conflict resolution should prevent accidental data loss.
    * App should handle background process termination gracefully (e.g., during sync or AI processing).
* **4.6 Maintainability:**
    * Codebase should follow standard Kotlin conventions and Android architecture best practices (e.g., MVVM or MVI).
    * Code should be well-documented, especially complex parts like sync logic and API integrations.
    * Modular design should allow for easier addition of new features (AI providers, storage options) in the future.
    * Dependency versions should be kept reasonably up-to-date.
* **4.7 Compatibility:**
    * Target a reasonably broad range of Android versions (e.g., Android 8.0 Oreo / API 26 and above) to maximize reach while leveraging modern features. Define `minSdkVersion` and `targetSdkVersion`.
    * App should function correctly on various screen sizes and densities common in the Android ecosystem.

---

## 5. Design & UI/UX Requirements

* **5.1 Overall Aesthetics:** Clean, minimal, and modern design, following Material Design 3 guidelines where appropriate. Focus on readability and content. Offer light and dark themes.
* **5.2 Key Workflows:**
    * **Adding Note/Summary:** Streamlined flow via Share Sheet or in-app button. Clear indication of AI processing.
    * **Linking:** Intuitive `[[` auto-complete and clear display of links/backlinks. Easy navigation between linked notes.
    * **Settings:** Logically organized sections for AI, Storage, Storage Management. Clear instructions and visual feedback during setup (auth flows, key validation if possible).
* **5.3 Editor:** Functional plain-text Markdown editor with syntax highlighting. Provide basic formatting actions via a toolbar if feasible (bold, italic, lists, link insertion).
* **5.4 Navigation:** Standard Android navigation patterns (e.g., bottom navigation bar or navigation drawer) for accessing main sections (Notes List, Search, Settings).
* **5.5 Accessibility:** Adhere to accessibility best practices: sufficient color contrast, touch target sizes, content descriptions for UI elements (for screen readers like TalkBack).

---

## 6. Release Criteria (MVP - Version 1.0)

* All "In Scope" features listed in Section 2.1 implemented and tested.
* Core functionality (note CRUD, linking, backlinks, local storage, search) is stable.
* AI Integration framework functions correctly with at least 2-3 major providers (e.g., OpenAI, Gemini). Guidance links are present and correct. API keys are stored securely.
* Google Drive integration (auth, sync for `.md` files) is functional and reliable. OneDrive integration is desirable but could be fast-follow if complex.
* Basic Storage Management options (e.g., delete originals older than X) are implemented.
* No critical or blocker bugs identified in core features.
* App meets basic performance and reliability standards on target devices.
* Basic usability testing completed.
* Source code published to a public repository with README and LICENSE.

---

## 7. Open Issues & Future Considerations

* **7.1 Open Issues:**
    * Final decision on specific open-source license (MIT vs Apache 2.0 vs GPLv3).
    * Detailed conflict resolution strategy for cloud sync (beyond basic last-write-wins).
    * Exact structure/fields for YAML front matter.
    * Feasibility/priority of Unlinked Mentions for v1.0.
* **7.2 Future Enhancements:**
    * **Transcription:** Integrate AI providers for transcribing audio/video notes.
    * **Advanced AI:** Chat with notes, AI-powered tagging/organization suggestions.
    * **Spaced Repetition/Active Recall:** Implement SRS features inspired by Recall.
    * **More Integrations:** Support for other cloud storage providers (Dropbox, etc.), other AI models.
    * **Advanced Search:** Faceted search (by tag, date, type), saved searches.
    * **Tagging System:** Dedicated tagging UI and management beyond basic front matter.
    * **Rich Editor:** WYSIWYG or improved Markdown editor experience.
    * **Import/Export:** More robust options, potentially supporting formats from other PKM tools.
    * **Cross-Platform:** Explore web or potentially iOS versions (long-term).
    * **Templates:** Allow users to create templates for new notes.
    * **Graph View:** Visual representation of note links (like Recall's web version).

---

## 8. Appendix

### 8.1 API Key Guidance Links (Examples - Verify before implementation)
* **OpenAI (ChatGPT):** `https://platform.openai.com/account/api-keys`
* **Google (Gemini):** `https://aistudio.google.com/app/apikey`
* **Anthropic (Claude):** `https://console.anthropic.com/account/keys`
* **Perplexity:** `https://docs.perplexity.ai/docs/getting-started` (Check for specific API key page)
* **Deepseek:** `https://platform.deepseek.com/api_keys`
* **OpenRouter:** `https://openrouter.ai/keys`
* **Grok (xAI):** (Link to be added when/if publicly available and documented)

### 8.2 Glossary
* **PKM:** Personal Knowledge Management
* **API Key:** Authentication token used to access an AI service's API.
* **Markdown (.md):** Lightweight markup language with plain-text formatting syntax.
* **YAML Front Matter:** Block of key-value pairs at the beginning of a file used for metadata.
* **Bi-directional Links:** Links that connect two notes, where each note is aware of the connection.
* **Backlinks (Linked Mentions):** Links pointing *to* the current note from other notes.
* **Unlinked Mentions:** Occurrences of a note's title in other notes' text, without an explicit link.
* **CRUD:** Create, Read, Update, Delete (basic data operations).
* **OAuth 2.0:** Standard protocol for authorization.
* **SDK:** Software Development Kit.
* **MVP:** Minimum Viable Product.
* **SRS:** Spaced Repetition System.

---
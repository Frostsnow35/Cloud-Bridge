# Tasks

- [x] Task 1: Backend - Implement AI Chat Endpoint
  - [x] Create `AIController.java` to expose `POST /api/ai/chat`.
  - [x] Connect controller to `AIService.chatWithIntent`.

- [x] Task 2: Backend - Implement Resource Detail Endpoint
  - [x] Update `LibraryController.java` to add `GET /api/libraries/{category}/{id}`.
  - [x] Ensure `SearchService` has `getById` method (verified, just need to ensure it's public and working).

- [x] Task 3: Frontend - Homepage Enhancements
  - [x] Update `Home.vue` to add icons/images to "Policy News" list.
  - [x] Update `Home.vue` to add icons/images to "Latest Demands" list.

- [x] Task 4: Frontend - Fix AI Copilot
  - [x] Verify `AICopilot.vue` calls the correct endpoint (`/api/ai/chat`).
  - [x] Ensure the chat UI handles the response correctly (text display).

- [x] Task 5: Frontend - Resource Detail Pages
  - [x] Create `views/ResourceDetail.vue` to display details for any category.
  - [x] Update `router/index.ts` to add route `/libraries/:category/:id`.
  - [x] Update `views/LibraryView.vue` to make items clickable and navigate to the detail page.

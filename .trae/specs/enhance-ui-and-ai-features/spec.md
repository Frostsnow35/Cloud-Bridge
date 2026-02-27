# Enhance UI and AI Features Spec

## Why
To improve user engagement and providing a more complete platform experience, we need to enhance the visual appeal of the homepage, ensure the AI chat assistant is fully functional, and provide detailed views for all resource categories.

## What Changes
- **Homepage**: Add visual icons/images to "Policy News" and "Latest Demands" sections.
- **AI Assistant**: Fix the floating chatbot (`AICopilot.vue`) by ensuring the backend endpoint `/api/ai/chat` exists and functions correctly.
- **Resource Center**: Create a generic detail page (`ResourceDetail.vue`) for all resource categories (Policies, Experts, Funds, Equipments, Patents, Enterprises) and add backend support for fetching single items.

## Impact
- **Frontend**:
  - `Home.vue`: UI updates.
  - `components/AICopilot.vue`: Verify integration.
  - `views/ResourceDetail.vue`: New component.
  - `router/index.ts`: New routes.
  - `views/LibraryView.vue`: Add links to detail pages.
- **Backend**:
  - `AIController.java`: New controller for chat.
  - `LibraryController.java`: New endpoint for fetching item details.

## ADDED Requirements
### Requirement: Homepage Visuals
The system SHALL display relevant icons or placeholder images for items in the "Policy News" and "Latest Demands" sections on the homepage.

### Requirement: AI Chatbot
The system SHALL provide a floating chatbot that can answer user queries by calling the `/api/ai/chat` endpoint.
- **Scenario**: User asks "How to publish a demand?" -> AI responds with guidance.

### Requirement: Resource Details
The system SHALL allow users to click on any resource item (Policy, Expert, etc.) in the Library list and view a dedicated detail page.
- **URL Pattern**: `/libraries/:category/:id`
- **Backend**: `GET /api/libraries/:category/:id` returns the full JSON of the item.

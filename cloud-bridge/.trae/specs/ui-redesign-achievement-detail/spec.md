# UI Redesign Spec for Achievement Detail Page

## Why
The current UI of the Achievement Detail page, while functional, lacks aesthetic appeal and clarity. The user finds it "cluttered" and wants a "cleaner, more aesthetic" design. The goal is to improve readability, visual hierarchy, and overall user experience through a modern, polished interface.

## What Changes
1.  **Header Redesign**:
    *   Create a more prominent and structured header area.
    *   Separate key info (title, tags) from actions (price, buttons) more distinctly.
    *   Use subtle gradients or glassmorphism for the background.
    *   Refine tag styling (e.g., outlined or soft background colors).

2.  **Content Typography & Spacing**:
    *   Increase line-height for better readability.
    *   Add more whitespace (padding/margin) between sections.
    *   Use a consistent and elegant font stack (e.g., Inter, Roboto, or system sans-serif).
    *   Style section titles with decorative elements (e.g., colored bar or icon).

3.  **Card Enhancements**:
    *   **Team Info Card**: Make the avatar larger and more integrated. Add subtle hover effects.
    *   **Recommendations Card**: Clean up the list items, add hover background change.
    *   **Service Cards**: Add depth (shadow) and a lift effect on hover. use a glass-like background.

4.  **Interactive Elements**:
    *   **Buttons**: Refine button styles (gradients, rounded corners, shadows).
    *   **Links**: Make "Related Tech" link more visually distinct but not intrusive.
    *   **Carousel**: Ensure patent images fit well and navigation dots/arrows are styled.

## Impact
- **Files**: `frontend/src/views/AchievementDetail.vue` (template + style).
- **Scope**: Visual changes only; no functional logic changes.

## Requirements

### Requirement: Aesthetic Header
The header SHALL display the achievement title prominently, with tags below it. The price and action buttons SHALL be grouped on the right side, aligned vertically or horizontally depending on screen size. The background SHALL be distinct from the body but harmonious.

### Requirement: Clean Content Layout
The main content area SHALL have ample padding. Section titles SHALL be clearly distinguished. Text SHALL be easy to read with appropriate contrast and line spacing.

### Requirement: Polished Cards
All cards (Team, Recommendations, Services) SHALL have a consistent visual language (border radius, shadow, background color). Hover states SHALL provide feedback.

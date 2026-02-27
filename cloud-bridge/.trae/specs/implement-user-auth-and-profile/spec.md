# User Authentication & Profile Implementation Spec

## Why
Currently, the application manages user state via `localStorage` directly in components, leading to scattered logic and poor reactivity. The navigation bar does not update to show the user's logged-in state (avatar/dropdown) after login. Additionally, there is no User Profile page for users to view their information. We need to centralize state management using Pinia and implement a proper authenticated user experience.

## What Changes

1.  **State Management (Pinia)**:
    *   Initialize Pinia in `main.ts`.
    *   Create a `useUserStore` in `stores/user.ts` to manage `user` and `token` state, including login/logout actions and persistence (sync with localStorage).

2.  **Navigation Bar (`App.vue`)**:
    *   Replace the static "Login / Register" button with a dynamic component.
    *   **If Logged In**: Show User Avatar + Dropdown Menu (Profile, Logout).
    *   **If Logged Out**: Show "Login / Register" button.

3.  **User Profile Page**:
    *   Create a new view `src/views/UserProfile.vue`.
    *   Display user information (Avatar, Name, Role, Email).
    *   Add a route `/profile` in `router/index.ts`.

4.  **Auth Logic Refactor**:
    *   Update `Login.vue` to use `useUserStore` for login instead of direct axios calls/localStorage manipulation.

## Impact
- **Files**:
    - `frontend/src/main.ts` (Modified)
    - `frontend/src/App.vue` (Modified)
    - `frontend/src/views/Login.vue` (Modified)
    - `frontend/src/router/index.ts` (Modified)
    - `frontend/src/stores/user.ts` (New)
    - `frontend/src/views/UserProfile.vue` (New)

## Requirements

### Requirement: Centralized User State
The system SHALL use Pinia to manage user authentication state. The store SHALL persist state to `localStorage` to survive page reloads.

### Requirement: Dynamic Navigation Header
- **WHEN** a user is logged in, the header SHALL display the user's avatar and name.
- **WHEN** the user clicks the avatar, a dropdown menu SHALL appear with options: "Personal Center" (links to `/profile`) and "Logout".
- **WHEN** the user logs out, the header SHALL revert to showing the "Login / Register" button.

### Requirement: User Profile Page
The system SHALL provide a `/profile` page that displays the current user's details (Name, Role, Contact Info).

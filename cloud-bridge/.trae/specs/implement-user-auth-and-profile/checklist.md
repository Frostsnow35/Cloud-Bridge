# Verification Checklist

- [ ] **State Management**
  - [ ] Pinia is correctly initialized in `main.ts`.
  - [ ] `useUserStore` correctly persists user/token to `localStorage`.
  - [ ] `useUserStore` correctly rehydrates state on app launch.

- [ ] **Login Flow**
  - [ ] Login page successfully authenticates via Store action.
  - [ ] After login, user is redirected and header updates *without* page refresh.

- [ ] **Header UI**
  - [ ] **Logged Out**: Shows "Login / Register" button.
  - [ ] **Logged In**: Shows Avatar + Name.
  - [ ] Dropdown menu opens on hover/click.
  - [ ] "Logout" button works and redirects to Home/Login.

- [ ] **Profile Page**
  - [ ] `/profile` route is accessible only when logged in (optional guard, but at least page exists).
  - [ ] Page displays correct user data from the Store.

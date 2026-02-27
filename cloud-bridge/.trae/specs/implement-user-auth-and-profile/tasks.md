# Tasks for User Auth & Profile

- [ ] **Task 1: Setup Pinia & User Store**
  - [ ] Initialize Pinia in `main.ts`.
  - [ ] Create `src/stores/user.ts` using Pinia.
  - [ ] Implement `state` (user, token), `getters` (isLoggedIn), and `actions` (login, logout, initializeFromStorage).

- [ ] **Task 2: Refactor Login Logic**
  - [ ] Update `src/views/Login.vue` to use `useUserStore().login()`.
  - [ ] Ensure redirect to home/previous page after success.

- [ ] **Task 3: Implement Dynamic Header**
  - [ ] Modify `src/App.vue` to use `useUserStore`.
  - [ ] Implement `v-if/v-else` logic to toggle between "Login Button" and "User Dropdown".
  - [ ] Add "Logout" functionality in the dropdown.

- [ ] **Task 4: Create User Profile Page**
  - [ ] Create `src/views/UserProfile.vue` with a clean UI (Avatar, Info Card).
  - [ ] Add `/profile` route in `src/router/index.ts`.

- [ ] **Task 5: Verification**
  - [ ] Verify login flow updates the header immediately.
  - [ ] Verify page reload keeps user logged in.
  - [ ] Verify logout clears state and redirects.

# Tasks

- [x] **Task 1: Backend - User System & Auth**
  - [x] Create `User` entity and `UserRepository`.
  - [x] Implement `JwtUtil` for token generation and validation.
  - [x] Create `AuthService` and `AuthController` (Login, Register).
  - [x] Add `AuthInterceptor` to protect API routes.

- [x] **Task 2: Backend - Content Management APIs**
  - [x] Update `Demand` and `Achievement` entities (add `ownerId`, `status`).
  - [x] Update `DemandController` (Create, Update, Delete, Audit, My List).
  - [x] Update `AchievementController` (Create, Update, Delete, Audit, My List).

- [x] **Task 3: Frontend - Auth Pages & State**
  - [x] Update `stores/user.ts` to handle JWT and user roles.
  - [x] Create `src/views/Login.vue`.
  - [x] Create `src/views/Register.vue` (with role selection).
  - [x] Update `router/index.ts` with auth guards.

- [x] **Task 4: Frontend - Publishing Forms**
  - [x] Create `src/views/PublishNeed.vue` (Enterprise only).
  - [x] Create `src/views/PublishAchievement.vue` (Expert only).

- [x] **Task 5: Frontend - User Profile & Management**
  - [x] Create `src/views/UserProfile.vue`.
  - [x] Implement "My Demands" tab with Edit/Delete buttons.
  - [x] Implement "My Achievements" tab with Edit/Delete buttons.

- [x] **Task 6: Frontend - Admin Dashboard**
  - [x] Create `src/views/AdminDashboard.vue`.
  - [x] Implement "Pending Audit" list with Approve/Reject actions.

- [x] **Task 7: Frontend - Detail Pages**
  - [x] Create `src/views/DemandDetail.vue`.
  - [x] Create `src/views/AchievementDetail.vue`.

- [x] **Task 8: Integration & Verification**
  - [x] Verify full flow: Register -> Login -> Publish -> Admin Audit -> View Detail.

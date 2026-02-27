# Tasks Breakdown

## 1. Backend Tasks
- [ ] **Mock Service Data**: Implement a method in `MatchingService` or a new `ServiceController` to return a list of recommended service products.
- [ ] **Update API**: Ensure the `GET /api/achievements/{id}` or `POST /api/matching/match` returns the recommended services.

## 2. Frontend Tasks
- [ ] **Remove Graph**: Delete the ECharts graph component and related logic from `AchievementDetail.vue`.
- [ ] **Add Patent Section**:
  - [ ] Implement a carousel or grid to display placeholder patent images in `AchievementDetail.vue`.
- [ ] **Add Service Recommendations**:
  - [ ] Create a `ServiceCard` component (optional, or inline).
  - [ ] Fetch and display recommended services at the bottom of `AchievementDetail.vue`.
- [ ] **Implement Jump Logic**:
  - [ ] Update tags to link to `/achievements?tag={tagName}`.
  - [ ] Update "Related Technology" text (if present) to link to `/needs?keyword={techName}`.
- [ ] **Create Team Detail Page**:
  - [ ] Create `src/views/TeamDetail.vue`.
  - [ ] Implement layout: Header (Team Info), Body (Achievement List).
  - [ ] Add mock data for "Professor Zhang's Team".
  - [ ] Register route `/team/:id` in `router/index.ts`.
- [ ] **Link Team Page**:
  - [ ] Update the owner avatar/name in `AchievementDetail.vue` to link to `/team/{ownerId}`.

## 3. Verification
- [ ] Verify graph is gone.
- [ ] Verify patent images are displayed.
- [ ] Verify clicking team avatar goes to Team Detail page.
- [ ] Verify Team Detail page shows mock info.
- [ ] Verify clicking tags filters the Achievement Hall.
- [ ] Verify clicking related tech filters the Demand Hall.
- [ ] Verify service recommendations are displayed.

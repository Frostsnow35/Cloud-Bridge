# Tasks

- [x] Task 1: Backend - Update Achievement Data Import
  - [x] Update `DataLoader.java` or `RAGDataSeeder.java` to read `广州市白云区省、市级科技项目立项名单.csv`.
  - [x] Implement mapping logic: `承担单位` -> `institution`, `项目名称` -> `title`, `支持方向` + `项目批次` + `级别` -> `description`.
  - [x] Clear old achievement mock data (if any).
  - [x] Assign random relevant image URLs to the top achievements.

- [x] Task 2: Backend - Implement Public Data Open Platform Library
  - [x] Update `SearchService.java` to include a new mock index `public_platforms`.
  - [x] Update `RAGDataSeeder.java` (or create new `DataLoader` logic) to read `广州市白云区公共数据开放计划.csv` and populate the mock index.
  - [x] Implement `getById` support for `public_platforms`.

- [x] Task 3: Frontend - Add Public Data Open Platform to Resource Center
  - [x] Update `LibraryView.vue` to include the `public_platforms` category.
  - [x] Update `ResourceDetail.vue` to handle `public_platforms` fields (Provider, Format, Frequency, Domain).
  - [x] Update `Home.vue` or Navigation to link to this new category if needed.

- [x] Task 4: Frontend - Verify and Fix Home Images
  - [x] Verify "Policy News" icons/images are working.
  - [x] Ensure "Featured Achievements" carousel displays the newly imported real achievements with their random background images.

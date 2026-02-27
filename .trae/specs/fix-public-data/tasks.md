# Tasks

- [ ] Task 1: Backend - Fix CSV Loading in RAGDataSeeder
  - [ ] Update `seedPublicPlatformsFromCSV` to use `GBK` encoding (common for Chinese CSVs on Windows).
  - [ ] Add explicit logging for each successful/failed line parsing.
  - [ ] Verify if the path is correct (it seems correct, but maybe the file is locked or has permission issues).

- [ ] Task 2: Frontend - Enhance Public Platform Detail
  - [ ] Update `ResourceDetail.vue` `handleAction` to handle `public_platforms` specifically.
  - [ ] Show a dialog or specific message: "已为您跳转至广州市白云区政府数据开放平台进行申请" (Redirected to Baiyun District Government Data Open Platform for application).

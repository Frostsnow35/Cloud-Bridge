# Tasks

- [ ] Task 1: Update `docker-compose.yml` to mount datasets
  - [ ] Mount `./数据集` to `/app/datasets` in the `backend` service.
- [ ] Task 2: Update `RAGDataSeeder.java` for flexible path resolution
  - [ ] Implement a path resolution strategy that checks `/app/datasets`, `e:\...`, and relative paths.
  - [ ] Move `achievementRepository.deleteAll()` to ensure it runs before loading (but only if load path is valid to avoid nuking DB on misconfig? Or nuke it anyway to guarantee no fake data? -> Nuke it if we want "Only CSV").
  - [ ] Log which path was used.

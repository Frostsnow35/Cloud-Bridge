#!/usr/bin/env bash
set -euo pipefail

out_dir="${1:-./backups}"
timestamp="$(date +%Y%m%d_%H%M%S)"
backup_dir="${out_dir%/}/${timestamp}"

mkdir -p "$backup_dir"

if [ -f "./docker-compose.yml" ]; then
  cp "./docker-compose.yml" "${backup_dir}/docker-compose.yml"
fi

if [ -f "./.env" ]; then
  cp "./.env" "${backup_dir}/.env"
fi

if [ -f "./init.sql" ]; then
  cp "./init.sql" "${backup_dir}/init.sql"
fi

if [ -f "../frontend/nginx.conf" ]; then
  mkdir -p "${backup_dir}/frontend"
  cp "../frontend/nginx.conf" "${backup_dir}/frontend/nginx.conf"
fi

services="$(docker compose ps --services 2>/dev/null || true)"

if echo "$services" | grep -q "^mysql$"; then
  docker compose exec -T mysql sh -lc 'mysqldump -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' > "${backup_dir}/mysql_dump.sql" || true
fi

if echo "$services" | grep -q "^postgres$"; then
  docker compose exec -T postgres sh -lc 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > "${backup_dir}/postgres_dump.sql" || true
fi

for vol in mysql_data redis_data neo4j_data es_data uploads_data; do
  if docker volume inspect "$vol" >/dev/null 2>&1; then
    docker run --rm -v "${vol}:/volume:ro" -v "${backup_dir}:/backup" alpine sh -lc "cd /volume && tar czf /backup/${vol}.tgz ."
  fi
done

printf '%s\n' "$backup_dir"

#!/usr/bin/env bash
set -euo pipefail

database_url="${DATABASE_URL:-${DB_URL:-}}"

if [[ -z "$database_url" ]]; then
  echo "DATABASE_URL or DB_URL is required." >&2
  exit 1
fi

database_url="${database_url#jdbc:}"

psql "$database_url" \
  -v ON_ERROR_STOP=1 \
  -f src/main/resources/db/sql/tourist_spots_ddl.sql \
  -f src/main/resources/db/sql/tourist_spots_seed.sql

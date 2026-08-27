#!/bin/bash
#
# Sauvegarde de TOUTES les bases PostgreSQL : un fichier par base.
#
#   /var/backup_dropbox/main-prod/<date>/<db>.sql.gz   -> archives datees (retention 90j)
#   /var/backup/prod8/<db>/<db>.sql.gz                 -> dernier dump (ecrase a chaque fois)
#
set -uo pipefail

ARCHIVE_ROOT="/var/backup_dropbox/main-prod"
LATEST_ROOT="/var/backup/prod8"
DB_USER="postgres"
RETENTION_DAYS=90

# Bases a ignorer (en plus des templates)
EXCLUDE_DBS="postgres"

DATE=$(date +%Y%m%d)
ARCHIVE_DIR="$ARCHIVE_ROOT/$DATE"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }

mkdir -p "$ARCHIVE_DIR"

# Liste des bases accessibles (hors templates)
DATABASES=$(sudo -u postgres psql -U "$DB_USER" -Atqc \
  "SELECT datname FROM pg_database WHERE datistemplate = false AND datallowconn ORDER BY datname;")

if [ -z "$DATABASES" ]; then
    log "ERREUR: impossible de lister les bases de donnees."
    exit 1
fi

FAILED=0

for DB_NAME in $DATABASES; do

    # Filtre d'exclusion
    skip=0
    for excluded in $EXCLUDE_DBS; do
        [ "$DB_NAME" = "$excluded" ] && skip=1
    done
    if [ "$skip" -eq 1 ]; then
        log "SKIP  $DB_NAME"
        continue
    fi

    ARCHIVE_FILE="$ARCHIVE_DIR/backup_${DB_NAME}_${DATE}.sql.gz"
    LATEST_DIR="$LATEST_ROOT/$DB_NAME"
    LATEST_FILE="$LATEST_DIR/${DB_NAME}.sql.gz"
    TMP_FILE="${ARCHIVE_FILE}.part"

    log "DUMP  $DB_NAME"

    # On dump dans un fichier temporaire : en cas d'echec, on ne remplace
    # jamais une sauvegarde valide par un fichier tronque.
    if sudo -u postgres pg_dump -U "$DB_USER" -d "$DB_NAME" | gzip -c > "$TMP_FILE"; then
        mv "$TMP_FILE" "$ARCHIVE_FILE"
        mkdir -p "$LATEST_DIR"
        cp "$ARCHIVE_FILE" "$LATEST_FILE"
        log "OK    $DB_NAME ($(du -h "$ARCHIVE_FILE" | cut -f1))"
    else
        rm -f "$TMP_FILE"
        log "ECHEC $DB_NAME"
        FAILED=$((FAILED + 1))
    fi
done

# Globals du cluster (roles, tablespaces) - indispensable pour une restauration complete
GLOBALS_FILE="$ARCHIVE_DIR/globals_${DATE}.sql.gz"
if sudo -u postgres pg_dumpall -U "$DB_USER" --globals-only | gzip -c > "$GLOBALS_FILE.part"; then
    mv "$GLOBALS_FILE.part" "$GLOBALS_FILE"
    log "OK    globals"
else
    rm -f "$GLOBALS_FILE.part"
    log "ECHEC globals"
    FAILED=$((FAILED + 1))
fi

# Purge des sauvegardes de plus de RETENTION_DAYS jours
find "$ARCHIVE_ROOT" -type f -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete
find "$ARCHIVE_ROOT" -mindepth 1 -type d -empty -delete

if [ "$FAILED" -gt 0 ]; then
    log "TERMINE avec $FAILED echec(s)"
    exit 1
fi

log "TERMINE - sauvegardes dans $ARCHIVE_DIR"

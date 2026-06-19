#!/bin/bash
set -o pipefail
# ==============================================================================
# Script de Respaldo Automatizado de Base de Datos - UrbanWear
# ==============================================================================
# Este script realiza un volcado lógico de la base de datos de PostgreSQL,
# lo comprime, lo sube a un almacenamiento S3 de AWS y reporta el estado
# (éxito o fallo) al backend para su respectiva notificación por email.
#
# Recomendación: Ejecutar mediante Cron Job o una tarea programada diaria.
# ==============================================================================

# Intentar cargar variables desde el archivo .env si existe en el directorio principal
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
if [ -f "$SCRIPT_DIR/../.env" ]; then
    echo "Cargando configuración desde el archivo .env..."
    # Cargar las variables exportándolas al entorno del shell
    export $(grep -v '^#' "$SCRIPT_DIR/../.env" | grep -v '^[[:space:]]*$' | xargs)
fi

# 1. Configuración de Base de Datos y S3
DB_HOST="aws-1-us-east-2.pooler.supabase.com"
DB_PORT="5432" # Se recomienda conexión directa en lugar de pooler transaccional
DB_USER="postgres.rujrnngxhrouaqjeurkp"
DB_NAME="postgres"

# Si DB_PASSWORD se cargó del .env, lo mapeamos para pg_dump
if [ -n "$DB_PASSWORD" ]; then
    export PGPASSWORD="$DB_PASSWORD"
fi

# Construir ruta S3 basada en el .env, con fallback
BUCKET_NAME="${S3_BUCKET_NAME:-urbanwear-backups}"
S3_BUCKET="s3://$BUCKET_NAME/database"

# 2. Configuración de Reportes del Backend
API_REPORT_URL="http://localhost:8080/api/internal/backups/report"
BACKUP_SECRET_TOKEN="${INTERNAL_TOKEN:-urbanwear-secret-token-2026}"

# 3. Variables de Entorno Locales
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/tmp/db_backups"
BACKUP_FILE="urbanwear_db_$TIMESTAMP.sql.gz"

# Crear directorio temporal si no existe
mkdir -p "$BACKUP_DIR"


echo "Iniciando respaldo de base de datos..."

# Ejecutar pg_dump y comprimir
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" --clean --no-owner --no-acl | gzip > "$BACKUP_DIR/$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "Respaldo creado localmente con éxito: $BACKUP_FILE"
    
    # Subir a AWS S3 (requiere AWS CLI configurado)
    echo "Subiendo a S3..."
    
    EXTRA_ARGS=""
    if [ -n "$AWS_ENDPOINT_URL" ]; then
        EXTRA_ARGS="--endpoint-url $AWS_ENDPOINT_URL"
    fi
    
    aws s3 cp "$BACKUP_DIR/$BACKUP_FILE" "$S3_BUCKET/$BACKUP_FILE" $EXTRA_ARGS
    
    if [ $? -eq 0 ]; then
        echo "Copia subida a la nube correctamente."
        
        # Reportar ÉXITO al backend de Spring Boot
        curl -X POST -H "Authorization: Bearer $BACKUP_SECRET_TOKEN" \
             -H "Content-Type: application/json" \
             -d "{\"status\": \"SUCCESS\", \"filename\": \"$BACKUP_FILE\"}" \
             "$API_REPORT_URL"
        
        # Limpiar archivo temporal
        rm "$BACKUP_DIR/$BACKUP_FILE"
    else
        ERROR_MSG="Fallo al subir el archivo $BACKUP_FILE al bucket S3"
        echo "ERROR: $ERROR_MSG"
        
        # Reportar FALLO al backend de Spring Boot
        curl -X POST -H "Authorization: Bearer $BACKUP_SECRET_TOKEN" \
             -H "Content-Type: application/json" \
             -d "{\"status\": \"FAILED\", \"error\": \"$ERROR_MSG\"}" \
             "$API_REPORT_URL"
        exit 1
    fi
else
    ERROR_MSG="Fallo en la ejecución de pg_dump"
    echo "ERROR: $ERROR_MSG"
    
    # Reportar FALLO al backend de Spring Boot
    curl -X POST -H "Authorization: Bearer $BACKUP_SECRET_TOKEN" \
         -H "Content-Type: application/json" \
         -d "{\"status\": \"FAILED\", \"error\": \"$ERROR_MSG\"}" \
         "$API_REPORT_URL"
    exit 1
fi

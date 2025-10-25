#!/bin/bash
set -e

echo "Initializing databases..."

# Create sonar database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE sonar;
    GRANT ALL PRIVILEGES ON DATABASE sonar TO "$POSTGRES_USER";
EOSQL

echo "Databases initialized successfully!"

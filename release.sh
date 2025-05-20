#!/usr/bin/env bash

# This script builds and packages playreact application.
# - Builds the React API and generates production files (js, css, etc.)
# - Run sbt packaging command to package playreact along with the react files
# - Move the package file to the required destination
# - A higher level user, such as itest, will upload all release packages to s3 release bucket

set -e

usage() {
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  --destination DIR    Copy release to destination folder"
    echo "  --tag TAG           Release tag name"
    echo "  --unarchived        Untar release in target/universal or destination"
    echo "  --force             Force no user input"
    exit 1
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --destination) DEST="$2"; shift 2 ;;
        --tag) TAG="$2"; shift 2 ;;
        --unarchived) UNARCHIVED=1; shift ;;
        --force) FORCE=1; shift ;;
        *) usage ;;
    esac
done

# dirname /Users/username/Home/playreact/release.sh
//dirname /Users/username/Home/playreact
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SBT_OPTS="-batch -no-colors"

# Clean and build
echo "[INFO] Cleaning previous build..."
sbt clean ${SBT_OPTS} || echo "[WARN] SBT clean error"

echo "[INFO] Checking disk space..."
df -h

echo "[INFO] Starting SBT universal packaging..."
sbt ${SBT_OPTS} universal:packageZipTarball

# Find package
PACKAGE=$(ls ${SCRIPT_DIR}/target/universal/yugaware*.tgz 2>/dev/null)
if [ -z "$PACKAGE" ]; then
    echo "[ERROR] No package found matching pattern: yugaware*.tgz"
    exit 1
elif [ $(echo "$PACKAGE" | wc -l) -gt 1 ]; then
    echo "[ERROR] Multiple packages found: $PACKAGE"
    exit 1
fi

if [ "$UNARCHIVED" = "1" ]; then
    # Extract package
    if [ -n "$DEST" ]; then
        tar xf "$PACKAGE" -C "$DEST"
    else
        tar xf "$PACKAGE"
    fi
else
    # Rename with commit SHA
    echo "[INFO] Adding commit SHA to release file..."
    SHA=$(git rev-parse --short HEAD)
    RELEASE_FILE="${SCRIPT_DIR}/yugaware-${SHA}.tgz"
    cp "$PACKAGE" "$RELEASE_FILE"
    
    if [ -n "$DEST" ]; then
        if [ ! -d "$DEST" ]; then
            echo "[ERROR] Destination is not a directory: $DEST"
            exit 1
        fi
        cp "$RELEASE_FILE" "$DEST"
    fi
fi

echo "[INFO] Package created successfully"
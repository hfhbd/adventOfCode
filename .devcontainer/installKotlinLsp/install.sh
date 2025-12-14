#!/bin/bash

# Function to get the latest version from GitHub Releases
get_latest_version() {
  curl -s https://api.github.com/repos/Kotlin/kotlin-lsp/releases/latest \
| jq '.["tag_name"]' | sed 's|"kotlin-lsp/v||;s|"||g'
}

# Check if VERSION environment variable is set
if [ -z "$VERSION" ]; then
  echo "VERSION not set, fetching the latest version from GitHub Releases..."
  VERSION=$(get_latest_version)
  if [ -z "$VERSION" ]; then
    echo "Error: Unable to determine the latest version."
    exit 1
  fi
  echo "Using latest version: $VERSION"
fi

# Construct the download URL
VSIX_URL="https://download-cdn.jetbrains.com/kotlin-lsp/${VERSION}/kotlin-lsp-${VERSION}-linux-x64.vsix"
VSIX_FILE="/usr/local/share/kotlin.vsix"

# Download the .vsix file using curl
echo "Downloading $VSIX_URL ..."
curl -L -o "$VSIX_FILE" "$VSIX_URL"
if [ $? -ne 0 ]; then
  echo "Error: Failed to download $VSIX_URL"
  exit 1
fi

chmod 644 "$VSIX_FILE"

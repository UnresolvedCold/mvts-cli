#!/usr/bin/env bash
set -euo pipefail

REPO="unresolvedcold/mvts-cli"
BINARY_NAME="mvts-cli"
INSTALL_DIR="${HOME}/.local/bin"

# -------------------------
# helpers
# -------------------------
err() {
  echo "❌ $1" >&2
  exit 1
}

info() {
  echo "▶ $1"
}

# -------------------------
# platform detection
# -------------------------
OS="$(uname -s)"
ARCH="$(uname -m)"

case "$OS" in
  Linux) OS="linux" ;;
  Darwin) OS="macos" ;;
  *) err "Unsupported OS: $OS" ;;
esac

case "$ARCH" in
  x86_64|amd64) ARCH="x86_64" ;;
  arm64|aarch64) ARCH="arm64" ;;
  *) err "Unsupported architecture: $ARCH" ;;
esac

ASSET="${BINARY_NAME}-${OS}-${ARCH}"

info "Detected platform: $OS/$ARCH"
info "Binary: $ASSET"

# -------------------------
# latest release
# -------------------------
LATEST_URL="https://api.github.com/repos/${REPO}/releases/latest"

info "Fetching latest release info..."

DOWNLOAD_URL=$(curl -fsSL "$LATEST_URL" \
  | grep browser_download_url \
  | grep "$ASSET\"" \
  | cut -d '"' -f 4)

[ -z "$DOWNLOAD_URL" ] && err "Binary not found in latest release"

# -------------------------
# install
# -------------------------
mkdir -p "$INSTALL_DIR"
TMP="$(mktemp)"

info "Downloading $DOWNLOAD_URL"
curl -fsSL "$DOWNLOAD_URL" -o "$TMP"

chmod +x "$TMP"
mv "$TMP" "$INSTALL_DIR/$BINARY_NAME"

info "Installed to $INSTALL_DIR/$BINARY_NAME"

# -------------------------
# PATH check
# -------------------------
if ! echo "$PATH" | grep -q "$INSTALL_DIR"; then
  echo
  echo "⚠️  $INSTALL_DIR is not in your PATH"
  echo "Add this to your shell config:"
  echo
  echo "  export PATH=\"\$PATH:$INSTALL_DIR\""
fi

echo
info "Done 🎉"
echo "Run: mvts --help"

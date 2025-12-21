#!/usr/bin/env bash
set -euo pipefail

REPO="unresolvedcold/mvts-cli"
JAR_NAME="mvts-cli.jar"

BIN_DIR="$HOME/.local/bin"
APP_DIR="$HOME/.local/share/mvts-cli"

CLI_NAME="mvts-cli"

err() {
  echo "❌ $1" >&2
  exit 1
}

info() {
  echo "▶ $1"
}

info "Fetching latest release info..."

DOWNLOAD_URL=$(curl -fsSL \
  "https://api.github.com/repos/${REPO}/releases/latest" |
  grep browser_download_url |
  grep "$JAR_NAME\"" |
  cut -d '"' -f 4)

[ -z "$DOWNLOAD_URL" ] && err "JAR not found in latest release"

mkdir -p "$BIN_DIR" "$APP_DIR"

TMP="$(mktemp)"

info "Downloading $DOWNLOAD_URL"
curl -fsSL "$DOWNLOAD_URL" -o "$TMP"

mv "$TMP" "$APP_DIR/$JAR_NAME"

# launcher
cat > "$BIN_DIR/$CLI_NAME" <<'EOF'
#!/usr/bin/env bash
exec java -jar "$HOME/.local/share/mvts-cli/mvts-cli.jar" "$@"
EOF

chmod +x "$BIN_DIR/$CLI_NAME"

info "Installed mvts CLI"

if ! echo "$PATH" | grep -q "$BIN_DIR"; then
  echo
  echo "⚠️  $BIN_DIR is not in your PATH"
  echo "Add this to your shell config:"
  echo
  echo "  export PATH=\"\$PATH:$BIN_DIR\""
fi

echo
info "Done 🎉"
echo "Run: $CLI_NAME --help"

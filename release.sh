#!/bin/bash

BOLD="\033[1m"
DIM="\033[2m"
RESET="\033[0m"
GREEN="\033[32m"
YELLOW="\033[33m"
RED="\033[31m"
CYAN="\033[36m"
BLUE="\033[34m"

# Ensure Python user-installed CLI tools such as mike and mkdocs are available.
export PATH="$HOME/Library/Python/3.13/bin:$PATH"

TOTAL_STEPS=6
CURRENT_STEP=0

divider() {
  echo -e "${DIM}────────────────────────────────────────────────────────────────────────────────${RESET}"
}

step() {
  local msg="$1"
  CURRENT_STEP=$((CURRENT_STEP + 1))
  echo
  divider
  echo -e "  ${BOLD}${CYAN}[${CURRENT_STEP}/${TOTAL_STEPS}]${RESET}  ${BOLD}${msg}${RESET}"
  divider
  echo
}

info() {
  echo -e "  ${BLUE}→${RESET}  $1"
}

success() {
  echo -e "  ${GREEN}✔${RESET}  $1"
}

error() {
  echo -e "  ${RED}✖${RESET}  ${RED}$1${RESET}"
}

warn() {
  echo -e "  ${YELLOW}⚠${RESET}  ${YELLOW}$1${RESET}"
}

banner() {
  echo
  echo -e "${BOLD}${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${RESET}"
  echo -e "${BOLD}${CYAN}║                          🚀  Release Script                                  ║${RESET}"
  echo -e "${BOLD}${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${RESET}"
  echo
}

run_maven() {
  local description="$1"
  shift
  local args=("$@")
  local tmp_log
  tmp_log=$(mktemp)

  info "${description} ..."

  if ! mvn "${args[@]}" > "$tmp_log" 2>&1; then
    echo
    error "Maven command failed: mvn ${args[*]}"
    echo
    echo -e "${DIM}────────────────────── Maven Error Output ──────────────────────${RESET}"
    grep -E "\[ERROR\]|\[FATAL\]" "$tmp_log" | while IFS= read -r line; do
      echo -e "  ${RED}${line}${RESET}"
    done
    echo -e "${DIM}────────────────────────────────────────────────────────────────${RESET}"
    echo
    rm -f "$tmp_log"
    exit 1
  fi

  rm -f "$tmp_log"
}

run_silent() {
  local description="$1"
  shift
  local tmp_log
  tmp_log=$(mktemp)

  info "${description} ..."

  if ! "$@" > "$tmp_log" 2>&1; then
    echo
    error "Command failed: $*"
    echo
    echo -e "${DIM}────────────────────── Error Output ────────────────────────────${RESET}"
    while IFS= read -r line; do
      echo -e "  ${RED}${line}${RESET}"
    done < "$tmp_log"
    echo -e "${DIM}────────────────────────────────────────────────────────────────${RESET}"
    echo
    rm -f "$tmp_log"
    exit 1
  fi

  rm -f "$tmp_log"
}

bump_patch() {
  local ver="$1"
  IFS=. read -r major minor patch <<<"$ver"
  patch=$((patch + 1))
  echo "${major}.${minor}.${patch}"
}

is_prerelease_version() {
  local ver
  ver=$(echo "$1" | tr '[:upper:]' '[:lower:]')

  [[ "$ver" =~ beta || "$ver" =~ alpha || "$ver" =~ rc ]]
}

check_docs_toolchain() {
  info "Checking documentation toolchain ..."

  if ! command -v mike > /dev/null 2>&1; then
    error "mike command not found."
    echo
    echo "  Install it with:"
    echo "    python3 -m pip install mike"
    echo
    echo "  Or ensure Python's user bin directory is in PATH:"
    echo "    export PATH=\"\$HOME/Library/Python/3.13/bin:\$PATH\""
    exit 1
  fi

  if ! command -v mkdocs > /dev/null 2>&1; then
    error "mkdocs command not found."
    echo
    echo "  Install it with:"
    echo "    python3 -m pip install mkdocs"
    exit 1
  fi

  if ! python3 -c "import material" > /dev/null 2>&1; then
    error "mkdocs-material is not installed in the current Python environment."
    echo
    echo "  Install it with:"
    echo "    python3 -m pip install mkdocs-material"
    echo
    echo "  Current tools:"
    echo "    python : $(command -v python3)"
    echo "    mike   : $(command -v mike)"
    echo "    mkdocs : $(command -v mkdocs)"
    echo
    echo "  Current versions:"
    echo "    $(mike --version 2>/dev/null || true)"
    echo "    $(mkdocs --version 2>/dev/null || true)"
    exit 1
  fi

  if [[ ! -f mkdocs.yml && ! -f mkdocs.yaml ]]; then
    error "No mkdocs.yml or mkdocs.yaml found in the project root."
    exit 1
  fi

  echo
  info "Documentation tools:"
  echo "    python : $(command -v python3)"
  echo "    mike   : $(command -v mike)"
  echo "    mkdocs : $(command -v mkdocs)"
  echo "    $(mike --version)"
  echo "    $(mkdocs --version)"
  echo

  run_silent "Validating MkDocs configuration" mkdocs build --strict

  if ! git ls-remote --exit-code --heads origin gh-pages > /dev/null 2>&1; then
    warn "Remote branch origin/gh-pages was not found. mike may create it, but please verify your docs setup."
  fi

  success "Documentation toolchain is ready."
}

cleanup_backup_files() {
  rm -f pom.xml.versionsBackup
  rm -f mangooio-core/pom.xml.versionsBackup
  rm -f mangooio-integration-test/pom.xml.versionsBackup
  rm -f mangooio-maven-archetype/pom.xml.versionsBackup
  rm -f mangooio-maven-plugin/pom.xml.versionsBackup
  rm -f mangooio-test/pom.xml.versionsBackup
}

banner

step "Checking Git state"

git status --short
if [[ -n $(git status --porcelain) ]]; then
  error "Uncommitted changes detected. Please commit or stash them before releasing."
  exit 1
fi
success "Git working directory is clean."

step "Cleaning previous release data"

run_maven "Running mvn clean release:clean" clean release:clean
echo
success "Cleanup complete."

step "Running build and verification"

run_maven "Running mvn clean verify" clean verify
echo
success "Maven build succeeded."

step "Setting version and deploying"

CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
DEFAULT_RELEASE_VERSION="${CURRENT_VERSION%-SNAPSHOT}"

info "Current version  :  ${BOLD}${CURRENT_VERSION}${RESET}"
echo
read -rp "  ✏️   Enter new release version [${DEFAULT_RELEASE_VERSION}]: " NEW_VERSION
NEW_VERSION="${NEW_VERSION:-$DEFAULT_RELEASE_VERSION}"
echo

if ! is_prerelease_version "$NEW_VERSION"; then
  check_docs_toolchain
else
  warn "Skipping documentation preflight — version ${BOLD}${NEW_VERSION}${RESET} contains 'beta', 'alpha', or 'rc'."
fi

run_maven "Setting project version to ${NEW_VERSION}" versions:set -DnewVersion="$NEW_VERSION"

VERSION="$NEW_VERSION"
info "Deploying version  :  ${BOLD}${VERSION}${RESET}"
echo

run_maven "Running mvn deploy" deploy -Prelease -DskipTests
echo
success "Version ${BOLD}${VERSION}${RESET} deployed successfully."

step "Tagging Git and updating versions"

NEXT_DEV_BASE="$(bump_patch "$VERSION")"
NEXT_SNAPSHOT_DEFAULT="${NEXT_DEV_BASE}-SNAPSHOT"

read -rp "  ✏️   Enter next development version [${NEXT_SNAPSHOT_DEFAULT}]: " NEXT_SNAPSHOT_VERSION
NEXT_SNAPSHOT_VERSION="${NEXT_SNAPSHOT_VERSION:-$NEXT_SNAPSHOT_DEFAULT}"
echo

run_silent "Creating Git tag ${VERSION}" git tag "$VERSION"
run_maven "Setting next snapshot version ${NEXT_SNAPSHOT_VERSION}" versions:set -DnewVersion="${NEXT_SNAPSHOT_VERSION}"

cleanup_backup_files

run_silent "Committing release ${VERSION}" git commit -am "Release ${VERSION}, next dev version ${NEXT_SNAPSHOT_VERSION}"
run_silent "Pushing to origin main" git push origin main
run_silent "Pushing Git tag ${VERSION}" git push origin "$VERSION"

echo
success "Git tag ${BOLD}${VERSION}${RESET} pushed and next dev version set  :  ${BOLD}${NEXT_SNAPSHOT_VERSION}${RESET}"

step "Publishing documentation"

if ! is_prerelease_version "$VERSION"; then
  check_docs_toolchain

  run_silent "Deploying docs for ${BOLD}${VERSION}${RESET}" mike deploy --update-aliases "$VERSION" latest
  run_silent "Setting default docs version to ${BOLD}${VERSION}${RESET}" mike set-default "$VERSION"
  run_silent "Pushing gh-pages" git push origin gh-pages

  echo
  success "Documentation published for version ${BOLD}${VERSION}${RESET}."
else
  warn "Skipping documentation — version ${BOLD}${VERSION}${RESET} contains 'beta', 'alpha', or 'rc'."
fi

info "Cleaning up backup files ..."
cleanup_backup_files

echo
divider
echo -e "  ${BOLD}${GREEN}🏁  All done!${RESET}  Version ${BOLD}${GREEN}${VERSION}${RESET} is released and documented."
divider
echo
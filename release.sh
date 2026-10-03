#!/bin/bash

set -euo pipefail

BOLD="\033[1m"
DIM="\033[2m"
RESET="\033[0m"
GREEN="\033[32m"
YELLOW="\033[33m"
RED="\033[31m"
CYAN="\033[36m"
BLUE="\033[34m"

# Ensure Python user-installed CLI tools such as mike are available.
export PATH="$HOME/Library/Python/3.13/bin:$PATH"

TOTAL_STEPS=6
CURRENT_STEP=0
RELEASE_BRANCH="main"
DEPLOYED=false
VERSION=""

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
    done || true
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

run_zensical() {
  local description="$1"
  shift
  run_silent "$description" uvx zensical "$@"
}

is_prerelease_version() {
  local ver
  ver=$(echo "$1" | tr '[:upper:]' '[:lower:]')

  [[ "$ver" =~ beta || "$ver" =~ alpha || "$ver" =~ rc ]]
}

# A pre-release (e.g. 10.15.0-Beta1) keeps its base version as next snapshot,
# a final release bumps the patch version.
next_snapshot_default() {
  local ver="$1"
  local base="${ver%%-*}"
  local major minor patch

  if is_prerelease_version "$ver"; then
    echo "${base}-SNAPSHOT"
  else
    IFS=. read -r major minor patch <<<"$base"
    echo "${major}.${minor}.$((patch + 1))-SNAPSHOT"
  fi
}

is_valid_release_version() {
  [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9.]+)?$ && ! "$1" =~ SNAPSHOT ]]
}

is_valid_snapshot_version() {
  [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT$ ]]
}

on_exit() {
  local status=$?

  if [[ $status -ne 0 && "$DEPLOYED" == true ]]; then
    echo
    warn "Version ${BOLD}${VERSION}${RESET}${YELLOW} is already deployed to Maven Central — do NOT re-run this script."
    warn "Finish the remaining steps (commit, push, tag, docs) manually."
  fi
}

trap on_exit EXIT

docs_config_file() {
  if [[ -f mkdocs.yml ]]; then
    echo "mkdocs.yml"
  elif [[ -f mkdocs.yaml ]]; then
    echo "mkdocs.yaml"
  else
    echo ""
  fi
}

check_docs_toolchain() {
  info "Checking documentation toolchain ..."

  local config_file
  config_file=$(docs_config_file)

  if [[ -z "$config_file" ]]; then
    error "No mkdocs.yml or mkdocs.yaml found in the project root."
    exit 1
  fi

  if ! command -v uv > /dev/null 2>&1; then
    error "uv command not found."
    echo
    echo "  Install it with Homebrew:"
    echo "    brew install uv"
    exit 1
  fi

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

  if ! grep -Eq "^[[:space:]]*variant:[[:space:]]*classic[[:space:]]*$" "$config_file"; then
    warn "The documentation config does not appear to contain 'variant: classic'."
    warn "Zensical may use its modern theme unless this is configured intentionally."
  fi

  echo
  info "Documentation tools:"
  echo "    uv     : $(command -v uv)"
  echo "    mike   : $(command -v mike)"
  echo "    config : $config_file"
  echo "    $(uv --version)"
  echo "    $(mike --version)"
  echo

  run_zensical "Validating Zensical build" build --strict

  if ! git ls-remote --exit-code --heads origin gh-pages > /dev/null 2>&1; then
    warn "Remote branch origin/gh-pages was not found. mike may create it, but please verify your docs setup."
  fi

  success "Documentation toolchain is ready."
}

banner

step "Checking Git state"

git status --short
if [[ -n $(git status --porcelain) ]]; then
  error "Uncommitted changes detected. Please commit or stash them before releasing."
  exit 1
fi
success "Git working directory is clean."

CURRENT_BRANCH=$(git symbolic-ref --short -q HEAD || true)
if [[ "$CURRENT_BRANCH" != "$RELEASE_BRANCH" ]]; then
  error "Releases must be created from '${RELEASE_BRANCH}' (current: '${CURRENT_BRANCH:-detached HEAD}')."
  exit 1
fi

run_silent "Fetching origin/${RELEASE_BRANCH}" git fetch origin "$RELEASE_BRANCH"
if [[ $(git rev-parse HEAD) != $(git rev-parse "origin/${RELEASE_BRANCH}") ]]; then
  error "Local '${RELEASE_BRANCH}' is not in sync with origin/${RELEASE_BRANCH}. Please pull or push first."
  exit 1
fi
success "Branch '${RELEASE_BRANCH}' is in sync with origin."

step "Cleaning previous release data"

run_maven "Running mvn clean release:clean" clean release:clean
echo
success "Cleanup complete."

step "Running build and verification"

run_maven "Running mvn clean verify" clean verify
echo
success "Maven build succeeded."

step "Setting version and deploying"

CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout || true)
if ! is_valid_snapshot_version "$CURRENT_VERSION"; then
  error "Could not determine a valid SNAPSHOT project version (got: '${CURRENT_VERSION}')."
  exit 1
fi
DEFAULT_RELEASE_VERSION="${CURRENT_VERSION%-SNAPSHOT}"

info "Current version  :  ${BOLD}${CURRENT_VERSION}${RESET}"
echo
read -rp "  ✏️   Enter new release version [${DEFAULT_RELEASE_VERSION}]: " NEW_VERSION
NEW_VERSION="${NEW_VERSION:-$DEFAULT_RELEASE_VERSION}"

if ! is_valid_release_version "$NEW_VERSION"; then
  error "Invalid release version '${NEW_VERSION}' (expected e.g. 1.2.3 or 1.2.3-Beta1)."
  exit 1
fi

if git rev-parse -q --verify "refs/tags/${NEW_VERSION}" > /dev/null \
  || git ls-remote --exit-code --tags origin "refs/tags/${NEW_VERSION}" > /dev/null 2>&1; then
  error "Git tag ${NEW_VERSION} already exists locally or on origin."
  exit 1
fi

NEXT_SNAPSHOT_DEFAULT="$(next_snapshot_default "$NEW_VERSION")"
read -rp "  ✏️   Enter next development version [${NEXT_SNAPSHOT_DEFAULT}]: " NEXT_SNAPSHOT_VERSION
NEXT_SNAPSHOT_VERSION="${NEXT_SNAPSHOT_VERSION:-$NEXT_SNAPSHOT_DEFAULT}"

if ! is_valid_snapshot_version "$NEXT_SNAPSHOT_VERSION"; then
  error "Invalid development version '${NEXT_SNAPSHOT_VERSION}' (expected e.g. 1.2.4-SNAPSHOT)."
  exit 1
fi
echo

if ! is_prerelease_version "$NEW_VERSION"; then
  check_docs_toolchain
else
  warn "Skipping documentation preflight — version ${BOLD}${NEW_VERSION}${RESET} contains 'beta', 'alpha', or 'rc'."
fi

echo
info "Release version  :  ${BOLD}${NEW_VERSION}${RESET}"
info "Next dev version :  ${BOLD}${NEXT_SNAPSHOT_VERSION}${RESET}"
echo
read -rp "  ❓  Deploy ${NEW_VERSION} to Maven Central? This cannot be undone [y/N]: " CONFIRM
if [[ ! "$CONFIRM" =~ ^[yY]$ ]]; then
  warn "Release aborted."
  exit 1
fi
echo

run_maven "Setting project version to ${NEW_VERSION}" versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false

VERSION="$NEW_VERSION"
info "Deploying version  :  ${BOLD}${VERSION}${RESET}"
echo

run_maven "Running mvn deploy" deploy -Prelease -DskipTests
DEPLOYED=true
echo
success "Version ${BOLD}${VERSION}${RESET} deployed successfully."

step "Tagging Git and updating versions"

run_silent "Creating Git tag ${VERSION}" git tag "$VERSION"
run_maven "Setting next snapshot version ${NEXT_SNAPSHOT_VERSION}" versions:set -DnewVersion="${NEXT_SNAPSHOT_VERSION}" -DgenerateBackupPoms=false

if git diff --quiet; then
  warn "Project version is already ${BOLD}${NEXT_SNAPSHOT_VERSION}${RESET}${YELLOW} — nothing to commit."
else
  run_silent "Committing release ${VERSION}" git commit -am "Release ${VERSION}, next dev version ${NEXT_SNAPSHOT_VERSION}"
  run_silent "Pushing to origin ${RELEASE_BRANCH}" git push origin "$RELEASE_BRANCH"
fi
run_silent "Pushing Git tag ${VERSION}" git push origin "$VERSION"

echo
success "Git tag ${BOLD}${VERSION}${RESET} pushed and next dev version set  :  ${BOLD}${NEXT_SNAPSHOT_VERSION}${RESET}"

step "Publishing documentation"

if ! is_prerelease_version "$VERSION"; then
  check_docs_toolchain

  run_zensical "Building docs with Zensical for ${BOLD}${VERSION}${RESET}" build --strict

  run_silent "Updating local gh-pages from origin" git fetch origin gh-pages:gh-pages
  run_silent "Deploying docs for ${BOLD}${VERSION}${RESET}" mike deploy --update-aliases "$VERSION" latest
  run_silent "Setting default docs version to ${BOLD}${VERSION}${RESET}" mike set-default "$VERSION"
  run_silent "Pushing gh-pages" git push origin gh-pages

  echo
  success "Documentation published for version ${BOLD}${VERSION}${RESET}."
else
  warn "Skipping documentation — version ${BOLD}${VERSION}${RESET} contains 'beta', 'alpha', or 'rc'."
fi

echo
divider
echo -e "  ${BOLD}${GREEN}🏁  All done!${RESET}  Version ${BOLD}${GREEN}${VERSION}${RESET} is released and documented."
divider
echo

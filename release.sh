#!/bin/bash

BOLD="\033[1m"
DIM="\033[2m"
RESET="\033[0m"
GREEN="\033[32m"
YELLOW="\033[33m"
RED="\033[31m"
CYAN="\033[36m"
BLUE="\033[34m"

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

banner

step "Checking Git state"

git status --short
if [[ -n $(git status --porcelain) ]]; then
  error "Uncommitted changes detected. Please commit or stash them before releasing."
  exit 1
fi
success "Git working directory is clean."

step "Cleaning previous release data"

run_maven "Running mvn clean" clean
run_maven "Running mvn release:clean" release:clean
echo
success "Cleanup complete."

step "Running build and verification"

run_maven "Running mvn clean verify" clean verify
echo
success "Maven build succeeded."

step "Setting version and deploying"

run_maven "Setting project version" versions:set

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
info "Deploying version  :  ${BOLD}${VERSION}${RESET}"
echo

run_maven "Running mvn clean deploy" clean deploy -Prelease
echo
success "Version ${BOLD}${VERSION}${RESET} deployed successfully."

step "Tagging Git and updating versions"

run_silent "Creating Git tag ${VERSION}" git tag "$VERSION"
run_maven "Updating to next development version" release:update-versions
run_silent "Committing version update" git commit -am "Updated version after release"
run_silent "Pushing to origin main" git push origin main
echo
success "Git tag ${BOLD}${VERSION}${RESET} pushed and version updated."

step "Publishing documentation"

lower_version=$(echo "$VERSION" | tr '[:upper:]' '[:lower:]')
if ! [[ "$lower_version" =~ beta || "$lower_version" =~ alpha || "$lower_version" =~ rc ]]; then
  run_silent "Deploying docs for ${BOLD}${VERSION}${RESET}" mike deploy --update-aliases "$VERSION" latest
  run_silent "Setting default docs version to ${BOLD}${VERSION}${RESET}" mike set-default "$VERSION"
  run_silent "Pushing gh-pages" git push origin gh-pages
  echo
  success "Documentation published for version ${BOLD}${VERSION}${RESET}."
else
  warn "Skipping documentation — version ${BOLD}${VERSION}${RESET} contains 'beta', 'alpha', or 'rc'."
fi

info "Cleaning up backup files ..."
rm -f pom.xml.versionsBackup
rm -f mangooio-core/pom.xml.versionsBackup
rm -f mangooio-integration-test/pom.xml.versionsBackup
rm -f mangooio-maven-archetype/pom.xml.versionsBackup
rm -f mangooio-maven-plugin/pom.xml.versionsBackup
rm -f mangooio-test/pom.xml.versionsBackup

echo
divider
echo -e "  ${BOLD}${GREEN}🏁  All done!${RESET}  Version ${BOLD}${GREEN}${VERSION}${RESET} is released and documented."
divider
echo
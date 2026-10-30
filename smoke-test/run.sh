#!/usr/bin/env bash
# Starts a MongoDB replica set (3 nodes), two instances of the smoke app and nginx in front of them, then runs the
# scenarios of SmokeTest.java. Requires Java 25, Docker with the nginx:1.27-alpine image and a mongod binary
# (MONGOD, defaults to the one cached by the embedded MongoDB of the integration tests). The console shows the
# progress and a summary, the output of all processes ends up in target/run.
set -euo pipefail
cd "$(dirname "$0")"

WORK="$PWD/target/run"
SETUP_LOG="$WORK/setup.log"
SMOKE_LOG="$WORK/smoke-test.log"
MONGOD="${MONGOD:-$(find "$HOME/.embedmongo" -name mongod -type f 2>/dev/null | head -1)}"
NGINX_IMAGE="${NGINX_IMAGE:-nginx:1.27-alpine}"
# The app is built against the framework version of the repository, installed with mvn install
VERSION="$(sed -n 's:^\t<version>\(.*\)</version>:\1:p' ../pom.xml | head -1)"

TTY=false
COLOR=false
[ -t 1 ] && TTY=true
[ "$TTY" = true ] && [ -z "${NO_COLOR:-}" ] && COLOR=true
COLUMNS="$(tput cols 2>/dev/null || echo 120)"
if [ "$COLOR" = true ]; then
    GREEN=$'\033[32m'; RED=$'\033[31m'; YELLOW=$'\033[33m'; DIM=$'\033[2m'; BOLD=$'\033[1m'; RESET=$'\033[0m'
else
    GREEN=''; RED=''; YELLOW=''; DIM=''; BOLD=''; RESET=''
fi
OK="${GREEN}✓${RESET}"
FAIL="${RED}✗${RESET}"
SKIP="${YELLOW}–${RESET}"

TITLES=("Check prerequisites" "Build smoke app" "Start replica set (27117-27119)" "Start instances A (9101) and B (9102)" "Start nginx (9100)")
STATUS=()
DETAILS=()
SCENARIOS=not-run
STARTED=$SECONDS
SPINNER=''

# --- output ---

elapsed() {
    if [ "$1" -lt 60 ]; then echo "$1 s"; else echo "$(($1 / 60)) min $(($1 % 60)) s"; fi
}

result() { # result <mark> <title> <duration> [detail]
    printf '  %s %-40s %s%6s%s' "$1" "$2" "$DIM" "$3" "$RESET"
    [ -z "${4:-}" ] || printf '  %s%s%s' "$DIM" "$4" "$RESET"
    printf '\n'
}

indent() { # indent <prefix> <color>, for the lines on stdin
    while IFS= read -r line; do printf '%s%s%s%s\n' "$1" "$2" "$line" "$RESET"; done
}

spinner_start() {
    [ "$TTY" = true ] || return 0
    (
        frames=(⠋ ⠙ ⠹ ⠸ ⠼ ⠴ ⠦ ⠧ ⠇ ⠏)
        i=0
        start=$SECONDS
        while :; do
            printf '\r\033[K  %s %s %s%s%s' "${frames[i % 10]}" "$1" "$DIM" "$(elapsed $((SECONDS - start)))" "$RESET"
            i=$((i + 1))
            sleep 0.1
        done
    ) &
    SPINNER=$!
}

spinner_stop() {
    [ -n "$SPINNER" ] || return 0
    kill "$SPINNER" 2>/dev/null || true
    wait "$SPINNER" 2>/dev/null || true
    SPINNER=''
    printf '\r\033[K'
}

# step <index> <function>: runs the function with set -e and its output in setup.log, `detail` sets the text shown
# next to the result. A failed step shows the end of its output and ends the run.
step() {
    local index="$1" title="${TITLES[$1]}" start=$SECONDS from rc
    from=$(($(wc -l < "$SETUP_LOG") + 2))
    printf '=== %s\n' "$title" >> "$SETUP_LOG"
    : > "$WORK/detail"
    spinner_start "$title"
    set +e
    ( set -e; "$2" ) >> "$SETUP_LOG" 2>&1
    rc=$?
    set -e
    spinner_stop

    if [ "$rc" -eq 0 ]; then
        STATUS[index]=ok
        DETAILS[index]="$(cat "$WORK/detail")"
        result "$OK" "$title" "$(elapsed $((SECONDS - start)))" "${DETAILS[index]}"
    else
        STATUS[index]=fail
        DETAILS[index]="$(tail -n +"$from" "$SETUP_LOG" | grep -v '^[[:space:]]*$' | tail -1 || true)"
        result "$FAIL" "$title" "$(elapsed $((SECONDS - start)))"
        tail -n +"$from" "$SETUP_LOG" | tail -20 | indent '      ' "$DIM"
        exit 1
    fi
}

detail() {
    echo "$*" > "$WORK/detail"
}

summary() {
    local rc="$1" i passed=0 failed=0 state name millis message
    printf '\n%sSummary%s\n' "$BOLD" "$RESET"

    printf '  Setup\n'
    for i in "${!TITLES[@]}"; do
        case "${STATUS[i]:-}" in
            ok) printf '    %s %s\n' "$OK" "${TITLES[i]}" ;;
            fail) printf '    %s %s\n      %s%s%s\n' "$FAIL" "${TITLES[i]}" "$RED" "${DETAILS[i]}" "$RESET" ;;
            *) printf '    %s %s  %sskipped%s\n' "$SKIP" "${TITLES[i]}" "$DIM" "$RESET" ;;
        esac
    done

    printf '  Scenarios\n'
    if [ -s "$WORK/scenarios.tsv" ]; then
        while IFS=$'\t' read -r state name millis message; do
            if [ "$state" = PASS ]; then
                passed=$((passed + 1))
                printf '    %s %s\n' "$OK" "$name"
            else
                failed=$((failed + 1))
                printf '    %s %s\n      %s%s%s\n' "$FAIL" "$name" "$RED" "$message" "$RESET"
            fi
        done < "$WORK/scenarios.tsv"
    fi
    if [ "$SCENARIOS" = not-run ]; then
        printf '    %s %snot run%s\n' "$SKIP" "$DIM" "$RESET"
    elif [ "$SCENARIOS" = aborted ] || [ "$rc" -gt 1 ]; then
        printf '    %s %saborted, see %s%s\n' "$FAIL" "$RED" "${SMOKE_LOG#"$(dirname "$PWD")/"}" "$RESET"
    fi

    printf '\n'
    if [ "$rc" -eq 0 ]; then
        printf '  %sPASSED%s  %d of %d scenarios in %s\n' "$GREEN$BOLD" "$RESET" "$passed" "$passed" "$(elapsed $((SECONDS - STARTED)))"
    elif [ "$rc" -eq 130 ]; then
        printf '  %sABORTED%s  after %s\n' "$YELLOW$BOLD" "$RESET" "$(elapsed $((SECONDS - STARTED)))"
    elif [ "$SCENARIOS" = not-run ]; then
        printf '  %sFAILED%s  setup did not complete\n' "$RED$BOLD" "$RESET"
    elif [ "$failed" -gt 0 ]; then
        printf '  %sFAILED%s  %d of %d scenarios failed\n' "$RED$BOLD" "$RESET" "$failed" "$((passed + failed))"
    else
        printf '  %sFAILED%s  scenarios aborted\n' "$RED$BOLD" "$RESET"
    fi
    printf '  Logs in %s\n' "${WORK#"$(dirname "$PWD")/"}"
}

cleanup() {
    docker rm -f smoke-nginx >/dev/null 2>&1 || true
    for pidfile in "$WORK"/*.pid; do
        [ -f "$pidfile" ] && kill "$(cat "$pidfile")" 2>/dev/null || true
    done
}

finish() {
    local rc=$?
    set +e
    spinner_stop
    cleanup
    summary "$rc"
    exit "$rc"
}
trap finish EXIT
trap 'exit 130' INT TERM

# --- steps ---

classpath() {
    echo "$PWD/app/target/classes:$(cat "$WORK/cp.txt")"
}

smoke_java() {
    java -Dstdout.encoding=UTF-8 -Dlog4j2.configurationFile="$PWD/log4j2-smoke-test.xml" -Dsmoke.log="$SMOKE_LOG" \
        -Dsmoke.tty="$TTY" -Dsmoke.color="$COLOR" -Dsmoke.columns="$COLUMNS" -cp "$(classpath)" SmokeTest.java "$@"
}

await() { # await <url> <attempts>, half a second apart
    for _ in $(seq 1 "$2"); do
        curl -fs "$1" >/dev/null 2>&1 && return 0
        sleep 0.5
    done
    return 1
}

check_prerequisites() {
    [ -x "$MONGOD" ] || { echo "No mongod binary found, set MONGOD"; return 1; }
    command -v java >/dev/null || { echo "No java on the PATH"; return 1; }
    docker info >/dev/null 2>&1 || { echo "Docker is not running"; return 1; }
    detail "$("$MONGOD" --version | head -1 | sed 's/^db version //')"
}

build_app() {
    (cd app && mvn -o -q -Dmangooio.version="$VERSION" compile dependency:build-classpath -Dmdep.outputFile="$WORK/cp.txt")
    detail "mangoo I/O $VERSION"
}

start_replica_set() {
    local primary
    for port in 27117 27118 27119; do
        mkdir -p "$WORK/db-$port"
        "$MONGOD" --replSet rs0 --port "$port" --dbpath "$WORK/db-$port" --bind_ip localhost --logpath "$WORK/mongod-$port.log" &
        echo $! > "$WORK/mongod-$port.pid"
    done
    sleep 2
    primary="$(smoke_java init 2>> "$SMOKE_LOG")"
    detail "$primary"
}

start_instances() {
    for port in 9101 9102; do
        java -Dapplication.mode=prod -Dapplication.config="$PWD/config.yaml" -Dconnector.http.port="$port" \
            -cp "$(classpath)" io.mangoo.core.Application > "$WORK/app-$port.log" 2>&1 &
        echo $! > "$WORK/app-$port.pid"
    done
    for port in 9101 9102; do
        await "http://localhost:$port/@health/ready" 120 || { tail -20 "$WORK/app-$port.log"; echo "Instance $port did not become ready"; return 1; }
    done
}

start_nginx() {
    docker rm -f smoke-nginx >/dev/null 2>&1 || true
    docker run -d --name smoke-nginx -p 9100:9100 -v "$PWD/nginx.conf:/etc/nginx/conf.d/default.conf:ro" "$NGINX_IMAGE"
    await "http://localhost:9100/instance" 40 || { docker logs smoke-nginx 2>&1 | tail -20; echo "nginx does not reach the instances"; return 1; }
}

rm -rf "$WORK" && mkdir -p "$WORK"
: > "$SETUP_LOG"

printf '%smangoo I/O smoke test%s  %s%s, 2 instances behind nginx, replica set with 3 nodes%s\n' "$BOLD" "$RESET" "$DIM" "$VERSION" "$RESET"
printf '\n%sSetup%s\n' "$BOLD" "$RESET"
step 0 check_prerequisites
step 1 build_app
step 2 start_replica_set
step 3 start_instances
step 4 start_nginx

printf '\n%sScenarios%s\n' "$BOLD" "$RESET"
SCENARIOS=aborted
set +e
smoke_java run "$WORK" 2>> "$SMOKE_LOG"
rc=$?
set -e
SCENARIOS=done
exit "$rc"

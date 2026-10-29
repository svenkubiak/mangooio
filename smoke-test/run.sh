#!/usr/bin/env bash
# Starts a MongoDB replica set (3 nodes), two instances of the smoke app and nginx in front of them, then runs the
# scenarios of SmokeTest.java. Requires Java 25, Docker with the nginx:1.27-alpine image and a mongod binary
# (MONGOD, defaults to the one cached by the embedded MongoDB of the integration tests).
set -euo pipefail
cd "$(dirname "$0")"

WORK="$PWD/target/run"
MONGOD="${MONGOD:-$(find "$HOME/.embedmongo" -name mongod -type f 2>/dev/null | head -1)}"
NGINX_IMAGE="${NGINX_IMAGE:-nginx:1.27-alpine}"
[ -x "$MONGOD" ] || { echo "No mongod binary found, set MONGOD"; exit 1; }

cleanup() {
    docker rm -f smoke-nginx >/dev/null 2>&1 || true
    for pidfile in "$WORK"/*.pid; do
        [ -f "$pidfile" ] && kill "$(cat "$pidfile")" 2>/dev/null || true
    done
}
trap cleanup EXIT

rm -rf "$WORK" && mkdir -p "$WORK"

# The app is built against the framework version of the repository, installed with mvn install
VERSION="$(sed -n 's:^\t<version>\(.*\)</version>:\1:p' ../pom.xml | head -1)"
echo "Building the smoke app against mangoo I/O $VERSION"
(cd app && mvn -o -q -Dmangooio.version="$VERSION" compile dependency:build-classpath -Dmdep.outputFile="$WORK/cp.txt")
CP="$PWD/app/target/classes:$(cat "$WORK/cp.txt")"

echo "Starting the replica set"
for port in 27117 27118 27119; do
    mkdir -p "$WORK/db-$port"
    "$MONGOD" --replSet rs0 --port "$port" --dbpath "$WORK/db-$port" --bind_ip localhost --logpath "$WORK/mongod-$port.log" &
    echo $! > "$WORK/mongod-$port.pid"
done
sleep 2
java -cp "$CP" SmokeTest.java init

echo "Starting instances A (9101) and B (9102)"
for port in 9101 9102; do
    java -Dapplication.mode=prod -Dapplication.config="$PWD/config.yaml" -Dconnector.http.port="$port" \
        -cp "$CP" io.mangoo.core.Application > "$WORK/app-$port.log" 2>&1 &
    echo $! > "$WORK/app-$port.pid"
done
for port in 9101 9102; do
    for _ in $(seq 1 120); do
        curl -fs "http://localhost:$port/@health/ready" >/dev/null 2>&1 && break
        sleep 0.5
    done
    curl -fs "http://localhost:$port/@health/ready" >/dev/null || { echo "Instance $port did not become ready"; tail -40 "$WORK/app-$port.log"; exit 1; }
done

echo "Starting nginx (9100)"
docker run -d --name smoke-nginx -p 9100:9100 -v "$PWD/nginx.conf:/etc/nginx/conf.d/default.conf:ro" "$NGINX_IMAGE" >/dev/null
for _ in $(seq 1 40); do
    curl -fs "http://localhost:9100/instance" >/dev/null 2>&1 && break
    sleep 0.5
done

echo
java -cp "$CP" SmokeTest.java run "$WORK"

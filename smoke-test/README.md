# Smoke test: several instances

Runs two instances of a small mangoo I/O application against a MongoDB replica set with three nodes, with nginx
(round robin, no sticky sessions) in front of them, and checks the scenarios of `HORIZONTAL-SCALING.md`:

* a session from instance A is valid on B, nginx reaches both instances
* the failed login budget, logout revocation and used TOTP codes are shared
* the admin login with a second factor works when the two steps reach different instances
* a server sent event sent on A reaches a client connected to B
* a failover of the MongoDB primary
* a graceful shutdown of A: readiness 503, liveness 200, open request finishes, SSE connection closed, nginx serves B

```bash
mvn -o install -DskipTests    # in the repository root, so the app uses the current framework
./smoke-test/run.sh
```

The app in `app/` is built against the framework version of the repository and reads `config.yaml` of this directory,
which holds test secrets only. Requires Java 25, Docker with the image `nginx:1.27-alpine` and a `mongod` binary (`MONGOD`, defaults to the one the
embedded MongoDB of the integration tests downloaded to `~/.embedmongo`). Logs of all processes end up in
`smoke-test/target/run`.

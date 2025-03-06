# Operating

The built-in **maven-shade-plugin** packages the application as a **Fat-Jar**, making it deployable in multiple ways. Below are two common methods.

## Using Supervisord

[Supervisor](https://supervisord.org/) is a process control system that helps manage and monitor long-running applications on UNIX-like systems. It ensures processes automatically start, restart on failure, and provides control through a command-line or web interface. It is commonly used for running background services, offering logging, process grouping, and easy configuration through `.conf` files.

### Setting Up Supervisord

After copying your **JAR** file to the server, configure `supervisord` with the following settings:

```ini
[program:myapp]
command=/usr/bin/java -jar /opt/myapp/my-fat-jar.jar
directory=/opt/myapp
autostart=true
autorestart=true
stderr_logfile=/var/log/myapp.err.log
stdout_logfile=/var/log/myapp.out.log
user=myuser
environment=JAVA_OPTS="-Xms512m -Xmx1024m"
```

## Containerization

To run the **JAR** file in a Docker container, use the following **Dockerfile**:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/myapp.jar myapp.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/myapp.jar"]
```

This setup ensures the application runs efficiently in both supervised environments and containerized deployments.

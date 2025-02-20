The following script is an example of how to start, stop and restart a mangoo I/O application as a deamon on Debian.

```bash
#!/bin/sh
### BEGIN INIT INFO
# Provides:          mangoo I/O
# Required-Start:    $syslog
# Required-Stop:     $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start/Stop mangoo I/O Application
### END INIT INFO

### CONFIGURATION ###

NAME=MyApplication
APPLICATION_PATH=/path/to/application/app.jar

XMX=128m
XMS=64m

DAEMON=/usr/bin/java

chown www-data:www-data /path/to/application/app.jar
### CONFIGURATION ###

PIDFILE=/var/run/$NAME.pid
USER=www-data

case "$1" in
  start)
        echo -n "Starting "$NAME" ..."
        start-stop-daemon --start --quiet --make-pidfile --pidfile $PIDFILE --chuid ${USER} --background --exec $DAEMON -- $DAEMON_OPTS
        RETVAL=$?
        if [ $RETVAL -eq 0 ]; then
                echo " Success"
            else
                echo " Failed"
        fi
        ;;
  stop)
        echo -n "Stopping "$NAME" ..."
        start-stop-daemon --stop --quiet --oknodo --pidfile $PIDFILE
        RETVAL=$?
        if [ $RETVAL -eq 0 ]; then
                echo " Success"
            else
                echo " Failed"
        fi
        rm -f $PIDFILE
        ;;
  restart)
        echo -n "Stopping "$NAME" ..."
        start-stop-daemon --stop --quiet --oknodo --retry 30 --pidfile $PIDFILE
        RETVAL=$?
        if [ $RETVAL -eq 0 ]; then
                echo " Success"
            else
                echo " Failed"
        fi
        rm -f $PIDFILE
        echo -n "Starting "$NAME" ..."
        start-stop-daemon --start --quiet --make-pidfile --pidfile $PIDFILE --chuid ${USER} --background --exec $DAEMON -- $DAEMON_OPTS
        RETVAL=$?
        if [ $RETVAL -eq 0 ]; then
                echo " Success"
            else
                echo " Failed"
        fi
        ;;
   status)
        if [ -f $PIDFILE ]; then
                echo $NAME" is running"
        else
                echo $NAME" is NOT not running"
        fi
        ;;
*)
        echo "Usage: "$1" {start|stop|restart|status}"
        exit 1
esac

exit 0
```

Place this script in /etc/init.d and use it as follows

```bash
chmod +x /etc/init.d/MyScript
/etc/init.d/MyScript (start|stop|restart|status)
```

If you are using Debian, than [Supervisord](http://supervisord.org/) might be an alternative to the init.d Script.


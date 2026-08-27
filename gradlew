#!/usr/bin/env sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

exec "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"

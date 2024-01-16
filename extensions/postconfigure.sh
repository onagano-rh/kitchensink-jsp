#!/usr/bin/env bash
echo "=== Executing postconfigure.sh"
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/config-database.cli


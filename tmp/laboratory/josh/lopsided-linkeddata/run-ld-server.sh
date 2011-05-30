JAR
JAR=`dirname $0`/target/lopsided-linkeddata-*-full.jar

# Find Java
if [ "$JAVA_HOME" = "" ] ; then
	JAVA="java"
else
	JAVA="$JAVA_HOME/bin/java"
fi

# Set Java options
if [ "$JAVA_OPTIONS" = "" ] ; then
	JAVA_OPTIONS="-Xms32M -Xmx512M"
fi

if [ $# -ge 1 ] ; then
    JAVA_OPTIONS=$JAVA_OPTIONS" -Dorg.linkedprocess.configurationProperties="$1
fi

# Launch the application
$JAVA $JAVA_OPTIONS -cp $JAR org.linkedprocess.linkeddata.server.DemoServer

# Return the program's exit code
exit $?

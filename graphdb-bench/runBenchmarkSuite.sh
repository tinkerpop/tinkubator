export MAVEN_OPTS="-server -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -Xms15G -Xmx15G"
mvn -e exec:java -Dexec.mainClass="com.tinkerpop.bench.BenchmarkSuite" -Dexec.args=""

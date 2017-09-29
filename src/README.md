Session DB Analyzer
===================

Commands
-----

```bash
CLASSPATH=mvn dependency:build-classpath |grep ".m2"
mvn package

java -classpath target/session-db-evaluator-1.0-SNAPSHOT.jar:$CLASSPATH com.uniberg.sessionDbEvaluator.App
```

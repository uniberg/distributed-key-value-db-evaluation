# Session DB Analyzer

This folder contains all the data of the database test application. The application is written in Java and can be configured by the config.properties.

One application instance does simulate 15000 sessions. There is also a script available to configure 15 instances on one host. This results in 225000 simulated sessions for this host.

This application should be run on the load hosts.

## Compile

To compile the application please run the following commands while the current working directory is this folder.

### Build and run

To build the application and bundle it into a .jar file, execute the following command. You can also run one instance with the next commands.

```bash
# Build and Bundle .jar
mvn package

# Run
CLASSPATH=mvn dependency:build-classpath |grep ".m2"
java -classpath target/session-db-evaluator-1.0-SNAPSHOT.jar:$CLASSPATH com.uniberg.sessionDbEvaluator.App
```

### Setup 15 instances

To set up 15 instances with individual session-keys and generate a start-script for all the instances, you need to build and bundle/package the application. Afterwards you can run the script setupTests from this folder while being in this folder.

This will result in a folder ../tests with 15 individual configuration-files and a script ../tests/startAll.sh to start all the Instances.

```bash
# Build and Bundle .jar
mvn package

# Setup 15 instances
./setupTests.sh

# Run all 15 instances
../tests/startAll.sh
```
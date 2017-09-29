#!/bin/bash

DIR=`dirname $0`
old_DIR=`pwd`
cd $DIR
host=`hostname`

if [ ! -d ../tests ]; then
    mkdir ../tests
fi

cp target/session-db-evaluator-1.0-SNAPSHOT.jar ../tests/.
CLASSPATH=`mvn dependency:build-classpath |grep ".m2"`
echo "#!/bin/bash" > ../tests/startAll.sh
echo "cd \`dirname \$0\`" >> ../tests/startAll.sh
for ((i=1; i <= 15; i++)); do
    if [ ! -d ../tests/$i ]; then
        mkdir ../tests/$i
    fi
    grep -v app.sessionPrefix config.properties |grep -v prometheus.jobname > ../tests/$i/config.properties
    echo "app.sessionPrefix=${i}-${host}-" >> ../tests/$i/config.properties
    echo "prometheus.jobname=node${i}-${host}" >> ../tests/$i/config.properties
    cat >../tests/$i/run.sh <<EOF
#!/bin/bash

DIR=\`dirname \$0\`
cd \$DIR
java -classpath $CLASSPATH:../session-db-evaluator-1.0-SNAPSHOT.jar com.uniberg.sessionDbEvaluator.App | tee ./output.log
cd -
EOF
    chmod u+x ../tests/$i/run.sh
    echo "$i/run.sh &" >> ../tests/startAll.sh
done
chmod u+x ../tests/startAll.sh
cd $old_DIR


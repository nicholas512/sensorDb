SDB_VER=$1
mvn clean compile
mvn clean install
cd sensorDb-command/target/
tar zxvf "sensorDb-command-"$SDB_VER"-sensorDb.tar.gz"
chmod -R o+r,o+x "sensorDb-command-"$SDB_VER"-sensorDb.tar.gz"

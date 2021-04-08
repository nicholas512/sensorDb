if [ $# -eq 0 ]; then
	echo "No arguments Provided."
	echo "You must provide the SensorDb version (e.g. 0.0.2-SNAPSHOT)."
    exit 1
fi

SDB_VER=$1
mvn clean compile
mvn clean install
cd sensorDb-command/target/
tar zxvf "sensorDb-command-"$SDB_VER"-sensorDb.tar.gz"
chmod -R o+r,o+x "sensorDb-command-"$SDB_VER"-sensorDb.tar.gz"

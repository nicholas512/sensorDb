# sensorDb

SensorDB is a webservice that facilitates writing data to the permafrost database.

## Installation

### Install dependencies
SensorDb requires Maven, Java 8, and Nunaliit 2.2.9. 

```bash
apt-get install maven
apt-get install openjdk-8-jdk
```
If you have multiple versions of Java installed, make sure that you switch to Java 8: `update-java-alternatives --set <Directory where JAVA 8 has been extracted>/bin/java`

```
MAINDIR=/opt
cd $MAINDIR
git clone
cd nunaliit
git checkout nunaliit2-2.2.7
mvn clean compile
mvn clean install
```

The original version of sensorDb uses 2.2.9-SNAPSHOT as a dependency, but there the [resources are missing](https://mvnrepository.com/artifact/ca.carleton.gcrc/nunaliit2-json) for the 2.2.9 version, so the dependency has been reverted to 2.2.7.

If you want to use the Nunaliit 2.2.9 binary, you can get it from https://github.com/GCRC/nunaliit/releases. Then install it according to the  [Nunaliit installation instructions ](https://github.com/GCRC/nunaliit/wiki/Installation).


### Install SensorDb

```bash
MAINDIR=/opt/
SDB_VER=0.0.1-SNAPSHOT

# Download and build sensordb
cd $MAINDIR
git clone https://github.com/geocryology/sensorDb
cd sensorDb
mvn clean compile
mvn clean install

# add sensorDb to path
cd $MAINDIR"sensorDb/sensorDb-command/target/"
tar zxvf "sensorDb-command-"$SDB_VER"-sensorDb.tar.gz"
chmod -R o+r,o+x "sensorDb-command-"$SDB_VER"-sensorDb.tar.gz"
export PATH=$PATH:/usr/local/nunaliit_VERSION_DATE_BUILD/bin
export PATH=$PATH:$MAINDIR"sensorDb/sensorDb-command/target/sensorDb-command-"$SDB_VER"/bin"
```

### Configuration
To configure a new sensorDb server, run `sensorDb create` (ensure the sensorDb-command bin directory is on your `PATH`). You will be prompted for the following information:

* port: *any port is ok.*
* connection string: *in the format `//HOST/DATABASE` (e.g. `//206.12.92.139:5432/observations`).*
* user: sensordb *your database must have a user role by this name with write-access and an entry in pg_hba.conf* 
* password: *choose a secure password*

To launch sensorDb, type `sensorDb run`

### Run sensorDb as a background service
To have sensorDb run automatically when the system starts, create the file `sensordb.service` in the directory `/etc/systemd/system`: 

```bash
[Unit]
Description=Permafrost database interface.
After=network.target
StartLimitIntervalSec=0

[Service]
Type=simple
WorkingDirectory=/home/nbr512/sensorDb_Server
ExecStart=/opt/sensorDb/sensorDb-command/target/sensorDb-command-0.0.1-SNAPSHOT/bin/sensorDb run
Restart=always
RestartSec=1

[Install]
WantedBy=multi-user.target
```

Make sure your ExecStart points to the correct sensordb /bin directory and that the working directory has a sensordb configuration. To activate the service, type: `sudo systemctl enable sensordb`

### Set up proxy server
If you want sensorDb to be reachable from a web address, you will want to set up a reverse proxy.  Follow the instructions at
  https://github.com/GCRC/nunaliit/wiki/Proxying-Nunaliit-with-Apache2-and-SSL

## Troubleshooting
*  make sure user sensordb has an entry in pg_hba.conf
* If you aren't able to install the Nunaliit 2.2.9 binary, you can try installing Nunaliit 2.2.7 from source, and changing the dependency in `pom.xml`
* 

## Usage
```
sudo su -l postgres

export SDBUSER=<user>

createuser $SDBUSER 

createdb --owner=$SDBUSER observations

psql --file=./observations.sql observations

psql -d observations -c "COPY device_sensor_profiles (device_type,manufacturer,manufacturer_device_name,sensor_label,sensor_type_of_measurement,sensor_unit_of_measurement,sensor_accuracy,sensor_precision,sensor_height_in_metres) FROM STDIN CSV HEADER;" < device_sensor_profiles.csv

for tbl in `psql -qAt -c "select tablename from pg_tables where schemaname = 'public';" observations` ; do  psql -c "alter table $tbl owner to $SDBUSER" observations ; done

for tbl in `psql -qAt -c "select sequence_name from information_schema.sequences where sequence_schema = 'public';" observations` ; do  psql -c "alter table $tbl owner to $SDBUSER" observations ; done

for tbl in `psql -qAt -c "select table_name from information_schema.views where table_schema = 'public';" observations` ; do  psql -c "alter table $tbl owner to $SDBUSER" observations ; done

for tbl in `psql -qAt -c "select tablename from pg_tables where schemaname = 'topology';" observations` ; do  psql -c "alter table $tbl owner to $SDBUSER" observations ; done

for tbl in `psql -qAt -c "select sequence_name from information_schema.sequences where sequence_schema = 'topology';" observations` ; do  psql -c "alter table $tbl owner to $SDBUSER" observations ; done
```

# Queries

select name,responsible_party,ST_AsText(coordinates),elevation from locations;


# Dump / Restore

> pg_dump observations > 201XMMDD_observations_data.txt

> psql observations < 201XMMDD_observations_data.txt

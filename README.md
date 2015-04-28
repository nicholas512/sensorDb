# sensorDb
```
sudo -u postgres createdb --owner=<user> observations
sudo -u postgres psql --command="CREATE EXTENSION postgis; CREATE EXTENSION postgis_topology;" observations
sudo -u postgres psql --file=./observations.sql observations
```

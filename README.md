# sensorDb
```
sudo -u postgres createdb --owner=&lt;user&gt; observations
sudo -u postgres psql --command="CREATE EXTENSION postgis; CREATE EXTENSION postgis_topology;" observations
sudo -u postgres psql --file=./observations.sql observations
```

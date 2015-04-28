# sensorDb

> sudo -u postgres createdb --owner=&lt;user&gt; observations
> sudo -u postgres psql --command="CREATE EXTENSION postgis; CREATE EXTENSION postgis_topology;" observations
> sudo -u postgres psql --file=./observations.sql observations

> sudo su -l postgres
> for tbl in `psql -qAt -c "select tablename from pg_tables where schemaname = 'public';" observations` ; do  psql -c "alter table $tbl owner to &lt;user&gt;" observations ; done
> for tbl in `psql -qAt -c "select sequence_name from information_schema.sequences where sequence_schema = 'public';" observations` ; do  psql -c "alter table $tbl owner to &lt;user&gt;" observations ; done
> for tbl in `psql -qAt -c "select table_name from information_schema.views where table_schema = 'public';" observations` ; do  psql -c "alter table $tbl owner to &lt;user&gt;" observations ; done

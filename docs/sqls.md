# Create index

```
create index spatial_geom_idx on trajectory using GIST(geometry);

```

# Explain statement to test 




```
explain select count(*)_ from trajectory trajectory0_ where (trajectory0_.geometry && st_geomfromewkt('SRID=4326;POLYGON((0 0,0 100,100 100,100 0,0 0))'))=true;

```
!! The spatial index is NOT being used !! 

This is different in Postgis were it IS being used ( verified ).


```
explain select trajectory0_.id as id1_0_, trajectory0_.duration_in_minutes as duration2_0_, trajectory0_.geometry as geometry3_0_, trajectory0_.start as start4_0_, trajectory0_.trajectory as trajecto5_0_ from trajectory trajectory0_ where (trajectory0_.geometry && st_geomfromewkt('SRID=4326;POLYGON((0 0,0 100,100 100,100 0,0 0))'))=true;

```

#Test

Using Httpie python utility.

```
http GET "http://localhost:9000/trajectories/search/bbox?minlon=0&minlat=0&maxlon=100&maxlat=100"

```

@echo off

if not exist ".\input\" (
  echo create input directory...
  mkdir .\input\
)

if exist .\input\campus.osm (
  echo delete old osm file...
  del .\input\*.osm
)

echo download new files...

wget -t10 "overpass-api.de/api/map?bbox=14.2225,48.2361,14.5176,48.3714" -O .\input\linz.osm
if %errorlevel% neq 0 goto :error

wget -t10 "overpass-api.de/api/map?bbox=14.3104,48.3288,14.3292,48.3400" -O .\input\campus.osm
if %errorlevel% neq 0 goto :error

wget -t10 "overpass-api.de/api/map?bbox=14.2993,48.2998,14.3116,48.3058" -O .\input\medcampus.osm
if %errorlevel% neq 0 goto :error

wget -t10 "overpass-api.de/api/map?bbox=14.2977,48.3043,14.3009,48.3071" -O .\input\lifesciencecampus.osm
if %errorlevel% neq 0 goto :error

wget -t10 "overpass-api.de/api/map?bbox=14.5104,48.3656,14.5176,48.3714" -O .\input\softwareparkhagenberg.osm
if %errorlevel% neq 0 goto :error

wget -t10 "overpass-api.de/api/map?bbox=14.2724,48.3188,14.2780,48.3215" -O .\input\petrinum.osm
if %errorlevel% neq 0 goto :error

echo convert osm file...

call .\osmosis\bin\osmosis.bat --read-xml .\input\linz.osm --tf accept-ways highway=* --used-node --mapfile-writer file=.\output\linz.map 
call .\osmosis\bin\osmosis.bat --read-xml .\input\campus.osm --mapfile-writer file=.\output\campus.map 
call .\osmosis\bin\osmosis.bat --read-xml .\input\medcampus.osm --mapfile-writer file=.\output\medcampus.map 
call .\osmosis\bin\osmosis.bat --read-xml .\input\lifesciencecampus.osm --mapfile-writer file=.\output\lifesciencecampus.map 
call .\osmosis\bin\osmosis.bat --read-xml .\input\softwareparkhagenberg.osm --mapfile-writer file=.\output\softwareparkhagenberg.map 
call .\osmosis\bin\osmosis.bat --read-xml .\input\petrinum.osm --mapfile-writer file=.\output\petrinum.map 

rem bbox=48.2835,14.2323,48.3629,14.4139
rem overpass-api.de/api/map?bbox=14.2323,48.2835,14.4139,48.3629

pause

goto :eof	

:error

pause

goto :eof	


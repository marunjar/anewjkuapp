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
echo.

call :download_file linz "14.2225,48.2361,14.5176,48.3714" 0

call :download_file campus "14.3104,48.3288,14.3292,48.3400" 1

call :download_file medcampus "14.2993,48.2998,14.3116,48.3058" 1

call :download_file lifesciencecampus "14.2977,48.3043,14.3009,48.3071" 1

call :download_file softwareparkhagenberg "14.5104,48.3656,14.5176,48.3714" 1

call :download_file petrinum "14.2724,48.3188,14.2780,48.3215" 1

pause

goto :eof	

:error

pause

goto :eof

:download_file
echo downloading %1 with bbox=%2...
echo.

wget -t5 -w10 --random-wait "overpass-api.de/api/map?bbox=%2" -O .\input\%1.osm
if %errorlevel% neq 0 goto :error

IF %3 GTR 0 (
    echo converting %1.osm to %1.map...
	echo.
    call .\osmosis\bin\osmosis.bat --read-xml .\input\%1.osm --mapfile-writer file=.\output\%1.map 
) ELSE (
	echo converting %1.osm to %1.map without details...
	echo.
	call .\osmosis\bin\osmosis.bat --read-xml .\input\%1.osm --tf accept-ways highway=* --used-node --mapfile-writer file=.\output\%1.map 
)

timeout /t 120 /nobreak
exit /b

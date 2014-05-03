@echo off

call .\osmosis\bin\osmosis.bat --rx .\input\campus.osm --mw file=.\output\campus.map

pause

goto :eof	


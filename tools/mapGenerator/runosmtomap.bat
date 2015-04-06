@echo off

call .\osmosis\bin\osmosis.bat --read-xml .\input\campus.osm --mapfile-writer file=.\output\campus.map 
rem bbox=48.2974,14.2603,48.3494,14.3864

pause

goto :eof	


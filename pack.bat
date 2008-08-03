@echo off

echo Packing the complete GPSLogger source archive:

date /t >pack.log
time /t >>pack.log

set theDate=%date%
rem jar cvfM serge-works-GPSLogger-%theDate%.zip *
rem pkzipc -add -directories -level=9 serge-works-GPSLogger-%theDate%.zip *
rem tar cvf serge-works-GPSLogger-%theDate%.tar *
rem bzip2 -v9 serge-works-GPSLogger-%theDate%.tar
7z a -scsUTF-8 serge-works-GPSLogger-%theDate%.7z *

echo Done.

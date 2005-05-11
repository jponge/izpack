@echo off
rem %1 = file name of your own choice

IF !%1==! goto USAGE

jar cf %1 .\com\izforge\izpack\sample\*.class 
goto ok

:USAGE
echo "USAGE: %0 <Jar_File_Name.jar>"
goto exit

:ok
echo "Success: jar %1 has been created."

:exit
@echo off

echo Arguments: %*

cd %~dp0

set LD_LIBRARY_PATH=%SystemRoot%\system32;.
set PATH=%PATH%;.

set JAVA_HOME="%ProgramFiles%\Java\jdk1.5.0_17"

%JAVA_HOME%\bin\java -cp . gk.deploy.v20_00.setup.WinSetupAPIMain %*
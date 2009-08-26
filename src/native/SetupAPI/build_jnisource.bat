@echo off

set CC=C:\Borland\bcc55

set JDK=C:\Program Files\Java\jdk1.5.0_17

set PATH=%CC%\Bin

del WinSetupAPI.dll

bcc32 -DWIN32 -I"%CC%\Include" -I%JDK%\include -I%JDK%\include\win32 -WM -WD -c util.cpp WinSetupAPIBase.cpp
ilink32 -L"%CC%\Lib" -L"%CC%\Lib\PSDK" -Tpd -ap -Gn -c -x c0d32 util WinSetupAPIBase,WinSetupAPI,,import32 cw32 kernel32 setupapi,,

del *.obj
del *.tds

:: Windows launcher script for Mutant
@echo off

cd %CD%\target\

set TARGET=

for /f "tokens=*" %%a in ('dir /b /ad') do (
if exist "%%a\bin\mutant.bat" set TARGET=%%a
)

cd %TARGET%\bin\
call mutant.bat %*

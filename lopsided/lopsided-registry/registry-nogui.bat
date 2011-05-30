:: Windows launcher script for the Lopsided farm (no GUI)

if not "%JAVA_OPTIONS%" == "" goto gotOpts
set JAVA_OPTIONS=-Xms32M -Xmx512M
:gotOpts

if "%1" == "" goto noConfig
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dorg.linkedprocess.configurationProperties=%1
:noConfig

java %JAVA_OPTIONS% %JAVA_ARGS% -jar target\lopsided-registry-*-standalone.jar

:: TODO: return exit code

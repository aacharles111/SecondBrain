@echo off
echo Monitoring logs for TOKEN USAGE information...
echo Press Ctrl+C to stop

adb logcat -v time | findstr "TOKEN_USAGE"

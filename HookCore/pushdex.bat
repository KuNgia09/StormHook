adb push classes.dex /data/local/tmp/
adb shell mv /data/local/tmp/classes.dex /data/local/tmp/hook.dex
adb shell chmod 775 /data/local/tmp/hook.dex
pause

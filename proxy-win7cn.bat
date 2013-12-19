@echo off
for /f "tokens=2 skip=7 delims=:" %%i in (
	'netsh interface ip show ipaddresses "无线网络连接"'
) do (
	set state=%%i
	goto :next
)
:next
if %state% == 反对 (
	echo 无线网络未正常连接
	goto :end
)
for /f "tokens=2 skip=1 delims= " %%i in (
	'netsh interface ip show ipaddresses "无线网络连接"'
) do (
	set ip=%%i
	goto :next2
)
:next2
echo 当前IP %ip%  状态%state%
adb shell am broadcast -a proxyrobot.apply_proxy -f 32 --es host %ip% --es port 8888 > nul && echo 完成 || echo 失败
:end
pause

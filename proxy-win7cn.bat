@echo off
for /f "tokens=2 skip=7 delims=:" %%i in (
	'netsh interface ip show ipaddresses "������������"'
) do (
	set state=%%i
	goto :next
)
:next
if %state% == ���� (
	echo ��������δ��������
	goto :end
)
for /f "tokens=2 skip=1 delims= " %%i in (
	'netsh interface ip show ipaddresses "������������"'
) do (
	set ip=%%i
	goto :next2
)
:next2
echo ��ǰIP %ip%  ״̬%state%
adb shell am broadcast -a proxyrobot.apply_proxy -f 32 --es host %ip% --es port 8888 > nul && echo ��� || echo ʧ��
:end
pause

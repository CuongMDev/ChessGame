@echo off
setlocal

REM ==== Thông tin ứng dụng ====
set APP_NAME=Chess!
set APP_VERSION=1.0.0
set MODULE=org.example.chessgame/org.example.chessgame.Main
set RUNTIME_IMAGE=target\Chess!
set ICON_PATH=icon.ico
set RESOURCE_SRC=GameProcess
set RESOURCE_DEST=%RUNTIME_IMAGE%\GameProcess
set OUTPUT_DIR=target\dist

REM ==== Bước 1: Copy GameProcess vào runtime image ====
echo 🔄 Copying GameProcess to runtime image...
xcopy /E /I /Y "%RESOURCE_SRC%" "%RESOURCE_DEST%"

REM ==== Xóa output cũ nếu có ====
if exist "%OUTPUT_DIR%" (
    rmdir /s /q "%OUTPUT_DIR%"
)

REM ==== Đóng gói bằng jpackage sử dụng runtime từ jlink ====
jpackage ^
  --type msi ^
  --name "%APP_NAME%" ^
  --app-version "%APP_VERSION%" ^
  --vendor "org.example" ^
  --runtime-image "%RUNTIME_IMAGE%" ^
  --module "%MODULE%" ^
  --icon "%ICON_PATH%" ^
  --dest "%OUTPUT_DIR%" ^
  --win-shortcut ^
  --win-menu

IF %ERRORLEVEL% NEQ 0 (
    echo ❌ Đóng gói thất bại!
    pause
    exit /b 1
)

:CLEANUP
REM ==== Bước 5: Xóa GameProcess ra khỏi runtime sau khi đóng gói ====
echo 🧹 Cleaning up runtime image...
if exist "%RESOURCE_DEST%" (
    rmdir /s /q "%RESOURCE_DEST%"
)

echo ✅ Đóng gói thành công tại %OUTPUT_DIR%
pause

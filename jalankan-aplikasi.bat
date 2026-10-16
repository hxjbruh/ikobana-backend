@echo off
REM Klik dua kali file ini untuk menjalankan backend + frontend Ikobana.
REM Backend memakai database H2 (data hilang saat jendela backend ditutup).
REM Untuk berhenti: tutup kedua jendela hitam "Ikobana Backend" dan "Ikobana Frontend".

set BACKEND_DIR=%~dp0
set FRONTEND_DIR=%~dp0..\pbl-framework-frontend-main
set JAR=%BACKEND_DIR%target\ikobana-backend-0.0.1-SNAPSHOT.jar

cd /d "%BACKEND_DIR%"

if not exist "%JAR%" (
    echo Build backend dulu, tunggu sebentar...
    call mvnw.cmd -q clean package -DskipTests
    if errorlevel 1 (
        echo Build backend gagal.
        pause
        exit /b 1
    )
)

if not exist "%FRONTEND_DIR%\package.json" (
    echo Folder frontend tidak ditemukan di: %FRONTEND_DIR%
    pause
    exit /b 1
)

if not exist "%FRONTEND_DIR%\node_modules" (
    echo Install dependency frontend, tunggu sebentar...
    pushd "%FRONTEND_DIR%"
    call npm install --no-audit --no-fund
    popd
)

echo Menjalankan backend di http://localhost:8080 ...
start "Ikobana Backend" java -jar "%JAR%" --spring.profiles.active=h2

echo Menjalankan frontend di http://localhost:3000 ...
start "Ikobana Frontend" /d "%FRONTEND_DIR%" cmd /k npm run dev

echo Menunggu backend siap...
:wait_backend
timeout /t 2 /nobreak >nul
curl -s -o nul http://localhost:8080/api/products || goto wait_backend

echo Siap! Membuka browser...
start http://localhost:3000
echo.
echo Login admin: admin@ikobana.com / Admin@12345
echo Jendela ini boleh ditutup.
pause

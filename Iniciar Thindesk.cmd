@echo off
cd /d "%~dp0"
echo Iniciando Thindesk em http://localhost:8081/login
echo Mantenha esta janela aberta enquanto utiliza o sistema.
call mvnw.cmd "-Dmaven.repo.local=%USERPROFILE%\.m2\repository" spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
pause

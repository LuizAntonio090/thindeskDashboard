@echo off
cd /d "%~dp0"
echo Este comando remove apenas os registros marcados como demonstracao Thindesk.
echo Feche a aplicacao na porta 8081 antes de continuar.
choice /m "Remover a carga de demonstracao"
if errorlevel 2 exit /b
call mvnw.cmd "-Dmaven.repo.local=%USERPROFILE%\.m2\repository" spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 --spring.profiles.active=demo --thindesk.demo.limpar=true"
pause

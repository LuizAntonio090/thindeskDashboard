@echo off
cd /d "%~dp0"
echo Carregando dados ficticios e iniciando Thindesk em http://localhost:8081/login
echo A carga pode ser executada novamente: os registros existentes nao serao duplicados.
echo Mantenha esta janela aberta durante a gravacao.
call mvnw.cmd "-Dmaven.repo.local=%USERPROFILE%\.m2\repository" spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 --spring.profiles.active=demo"
pause

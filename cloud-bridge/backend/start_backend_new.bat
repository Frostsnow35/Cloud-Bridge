@echo off
set JAVA_HOME=E:\env\jdk-17
set PATH=%JAVA_HOME%\bin;E:\env\apache-maven-3.9.6\bin;%PATH%
cd /d "%~dp0"
mvn spring-boot:run

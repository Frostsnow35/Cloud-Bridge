$env:JAVA_HOME = "E:\env\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Path: $env:Path"
& "$env:JAVA_HOME\bin\java.exe" -version
Set-Location "cloud-bridge\backend"
& "E:\env\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run

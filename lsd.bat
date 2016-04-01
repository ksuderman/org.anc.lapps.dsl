@ECHO OFF
set "DIR=%~dp0"
set "JAR=%DIR%/lsd-2.2.1-SNAPSHOT.jar"
set "HEAP_SIZE=-Xmx4G"
set "CLASSPATH=%JAR%:./"
java %HEAP_SIZE% -cp "%CLASSPATH%\org\anc\lapps\dsl\LappsDSL" %*
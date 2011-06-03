@if "%DEBUG%" == "" @echo off

@rem 
@rem $Revision: 2770 $ $Date: 2005-08-29 10:49:42 +0000 (Mon, 29 Aug 2005) $
@rem 

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

:begin
@rem Determine what directory it is in.
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

set JAVA_OPTS=%JAVA_OPTS% -Xdebug -Xnoagent -Dgrails.full.stacktrace=true -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005

CALL "%DIRNAME%\startGrails.bat" "%DIRNAME%" org.codehaus.groovy.grails.cli.GrailsScriptRunner %*


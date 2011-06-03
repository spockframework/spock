@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem                                                                         ##
@rem  Groovy JVM Bootstrap for Windowz                                       ##
@rem                                                                         ##
@rem ##########################################################################

@rem 
@rem $Revision: 4170 $ $Date: 2006-10-26 12:11:12 +0000 (Thu, 26 Oct 2006) $
@rem 

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~1
shift

set CLASS=%~1
shift

if exist "%USERPROFILE%/.groovy/preinit.bat" call "%USERPROFILE%/.groovy/preinit.bat"

@rem Determine the command interpreter to execute the "CD" later
set COMMAND_COM="cmd.exe"
if exist "%SystemRoot%\system32\cmd.exe" set COMMAND_COM="%SystemRoot%\system32\cmd.exe"
if exist "%SystemRoot%\command.com" set COMMAND_COM="%SystemRoot%\command.com"

@rem Use explicit find.exe to prevent cygwin and others find.exe from being used
set FIND_EXE="find.exe"
if exist "%SystemRoot%\system32\find.exe" set FIND_EXE="%SystemRoot%\system32\find.exe"
if exist "%SystemRoot%\command\find.exe" set FIND_EXE="%SystemRoot%\command\find.exe"

:check_JAVA_HOME
@rem Make sure we have a valid JAVA_HOME
if not "%JAVA_HOME%" == "" goto have_JAVA_HOME

echo.
echo ERROR: Environment variable JAVA_HOME has not been set.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto end

:have_JAVA_HOME
@rem Validate JAVA_HOME
%COMMAND_COM% /C DIR "%JAVA_HOME%" 2>&1 | %FIND_EXE% /I /C "%JAVA_HOME%" >nul
if not errorlevel 1 goto check_GRAILS_HOME

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto end

:check_GRAILS_HOME
@rem Define GRAILS_HOME if not set
if "%GRAILS_HOME%" == "" set GRAILS_HOME=%DIRNAME%..
 
:init
@rem Get command-line arguments, handling Windowz variants
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem remove trailing slash from GRAILS_HOME
if "%GRAILS_HOME:~-1%"=="\" SET GRAILS_HOME=%GRAILS_HOME:~0,-1%

@rem Slurp the command line arguments.  
set CMD_LINE_ARGS=
set CP=
set GRAILS_OPTIONS=

:win9xME_args_slurp
if "x%~1" == "x" goto execute
set CURR_ARG=%~1
if "%CURR_ARG:~0,2%" == "-D" (
	set GRAILS_OPTIONS=%GRAILS_OPTIONS% %~1=%~2
	shift
	shift	
	goto win9xME_args_slurp
) else (
	if "x%~1" == "x-cp" (
		set CP=%~2
		shift	
		shift	
		goto win9xME_args_slurp
	) else (
		if "x%~1" == "x-classpath" (
			set CP=%~2
			shift	
			shift	
			goto win9xME_args_slurp
		) else (
			if "x%~1" == "x-agent" (
				set AGENT="-javaagent:%GRAILS_HOME%\lib\com.springsource.springloaded\springloaded-core\jars\springloaded-core-@spring.loaded.version@.jar -noverify -Dspringloaded=profile=grails"
				shift	
				shift	
				goto win9xME_args_slurp
			) else (
				if "x%~1" == "x-run-app" (
					set AGENT="-javaagent:%GRAILS_HOME%\lib\com.springsource.springloaded\springloaded-core\jars\springloaded-core-@spring.loaded.version@.jar -noverify -Dspringloaded=profile=grails"
					shift	
					shift	
					goto win9xME_args_slurp
				) else (
					set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
					shift
					goto win9xME_args_slurp
				)
			)
		)
	)
)

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line
set STARTER_CLASSPATH=%GRAILS_HOME%\lib\org.codehaus.groovy\groovy-all\jars\groovy-all-@groovy.version@.jar;%GRAILS_HOME%\dist\grails-bootstrap-@grails.version@.jar

if exist "%USERPROFILE%/.groovy/init.bat" call "%USERPROFILE%/.groovy/init.bat"

@rem Setting a classpath using the -cp or -classpath option means not to use
@rem the global classpath. Groovy behaves then the same as the java 
@rem interpreter

if "x" == "x%CLASSPATH%" goto after_classpath
set CP=%CP%;%CLASSPATH%
:after_classpath

set STARTER_MAIN_CLASS=org.codehaus.groovy.grails.cli.support.GrailsStarter
set STARTER_CONF=%GRAILS_HOME%\conf\groovy-starter.conf

set JAVA_EXE=%JAVA_HOME%\bin\java.exe
set TOOLS_JAR=%JAVA_HOME%\lib\tools.jar

if "%JAVA_OPTS%" == "" set JAVA_OPTS=-Xmx512m -XX:MaxPermSize=96m
set JAVA_OPTS=%JAVA_OPTS%  %AGENT%
set JAVA_OPTS=%JAVA_OPTS% -Dprogram.name="%PROGNAME%"
set JAVA_OPTS=%JAVA_OPTS% -Dgrails.home="%GRAILS_HOME%"
set JAVA_OPTS=%JAVA_OPTS% -Dgrails.version="@grails.version@"
set JAVA_OPTS=%JAVA_OPTS% -Dbase.dir="."
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding="UTF-8"
set JAVA_OPTS=%JAVA_OPTS% -Dtools.jar="%TOOLS_JAR%"
set JAVA_OPTS=%JAVA_OPTS% -Dgroovy.starter.conf="%STARTER_CONF%"

if exist "%USERPROFILE%/.groovy/postinit.bat" call "%USERPROFILE%/.groovy/postinit.bat"

@rem Execute Groovy
CALL "%JAVA_EXE%" %JAVA_OPTS% %GRAILS_OPTIONS% -classpath "%STARTER_CLASSPATH%" %STARTER_MAIN_CLASS% --main %CLASS% --conf "%STARTER_CONF%" --classpath "%CP%" "%CMD_LINE_ARGS%"
:end
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal

@rem Optional pause the batch file
if "%GROOVY_BATCH_PAUSE%" == "on" pause


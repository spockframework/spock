@echo off
setlocal ENABLEDELAYEDEXPANSION

set script_dir=%~dp0
set script_dir=%script_dir:~0,-1%
set parameters=%*

if x%1==x--no-pause (
    set parameters=%parameters:~11%
) else (
    set pause_on_error=true
)

for %%v in (2.5 3.0 4.0 5.0) do (
    echo(
    echo Variant: %%v
    call %script_dir%\gradlew.bat -p "%script_dir%" -Dvariant=%%v %parameters%
    if errorlevel 1 (
       if defined error (set error=!error!, %%v) else (set error=%%v)
       if defined pause_on_error pause
    )
)

if defined error (
   if not defined pause_on_error (
      echo( >&2
      echo *********************************************************** >&2
      echo * Error during execution of the following variants: %error% >&2
      echo *********************************************************** >&2
   )
   exit /b 1
)

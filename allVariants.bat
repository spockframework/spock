@echo off
for %%v in (2.5 3.0 4.0) do (
    gradlew.bat -Dvariant=%%v %*
)

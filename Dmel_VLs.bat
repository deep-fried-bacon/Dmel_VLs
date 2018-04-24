@echo off

echo starting utilities
javac -cp "%FijiPath%\jars\*;%FijiPath%\plugins\*;." .\vlms\utilities\*.java
if errorlevel 1 exit /b

echo starting vlms
javac -cp "%FijiPath%\jars\*;%FijiPath%\plugins\*;." .\vlms\*.java
if errorlevel 1 exit /b

echo starting Dmel_Vls
javac -cp "%FijiPath%\jars\*;%FijiPath%\plugins\*;." Dmel_Vls.java
if errorlevel 1 exit /b

if not "%~1" == "" (
	if not "%~1" == "-noCommit" (
		git commit -a -m %1
	)
	python make_GitVjava.py
	javac -cp "%FijiPath%\jars\*;%FijiPath%\plugins\*;" .\vlms\GitV.java
)

echo starting jar
jar cf Dmel_Vls.jar .
move /y .\Dmel_Vls.jar "%FijiPath%\plugins\Dmel_Vls.jar"

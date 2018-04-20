javac -cp "%FijiPath%\jars\*;%FijiPath%\plugins\*;." .\vlms\utilities\*.java
javac -cp "%FijiPath%\jars\*;%FijiPath%\plugins\*;." .\vlms\*.java
javac -cp "%FijiPath%\jars\*;%FijiPath%\plugins\*;." Dmel_Vls.java
jar cf Dmel_Vls.jar .
move /y .\Dmel_Vls.jar "%FijiPath%\plugins\Dmel_Vls.jar"

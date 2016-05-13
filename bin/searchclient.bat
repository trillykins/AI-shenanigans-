javac sampleclients/*.java
javac searchclient/*.java
javac analysis/*.java
javac atoms/*.java
:javac bdi/*.java
javac conflicts/*.java
:javac FIPA/*.java
:javac heuristics/*.java
:javac strategies/*.java
javac utils/*.java
java -jar server.jar -l levels/%1.lvl -c "java searchclient.Run" -g
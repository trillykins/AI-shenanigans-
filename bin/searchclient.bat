javac searchclient/*.java
javac analysis/*.java
javac atoms/*.java
javac bdi/*.java
javac conflicts/*.java
javac heuristics/*.java
javac strategies/*.java
javac utils/*.java
java -jar server.jar -l levels/sathereddot.lvl -c "java searchclient.Run" -g 150
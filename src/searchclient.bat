javac sampleclients/*.java
javac searchclient/*.java
java -jar server.jar -l levels/%1.lvl -c "java searchclient.Run" -g -p
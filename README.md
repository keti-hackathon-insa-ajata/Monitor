## Prerequisites 
  * Maven
  * Java 8

## Build
```
mvn clean compile assembly:single
```
This create a single jar file (in the /target folder) named Monitor.jar (for example).
NB : for development, if after running this command, you cannot run Monitor.main(), run ```mvn clean``` and retry

## Run
```
java -jar Monitor.jar
```
Optional argument : 
```
-d : to deploy all necessary AE, etc... in all the mn-cse
```
NB : we can configure the Monitor with the file /config/config.properties

## Config.properties
Example : 
```properties
#Properties

#OM2M
originator=admin:admin
cseProtocol=http
inCseIp=127.0.0.1
inCsePort=8080
targetCse=in-cse/in-name
aeMonitorName=Monitor
aeDangerReports=DangerReports
aeAjataName=AjataSensor
subName=AjataSub

#Monitor and database
monitorIp=192.168.43.129
monitorPort=1600
monitorContext=/Monitor
databaseUri=http://localhost:12345/dangerReports

#Dangerous criteria (speed : km/h, distance : cm)
minRelativeSpeed=30
maxDistance=100
```
The most important fields are : 
  * the monitor's ip and port and context (the IP must correspond to the server's accessible IP) which will be accessed by the Raspberry Pi via OneM2M
  * the database's uri (here, we use localhost because the REST API is on the server aside with the Monitor)
  * the dangerous criteria : 
    * the minimum relative speed (in km/h)
    * the maximum distance between the vehicle and the bike (in cm) 

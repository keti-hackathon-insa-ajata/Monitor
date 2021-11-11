## Build
```
mvn clean compile assembly:single
```
NB : if after running this command, you cannot run Monitor.main(), run ```mvn clean``` 
## Run
Argument : 
```
-d : to deploy AE, etc... in all mn-cse
```
NB : we can configure the Monitor with the file /config/config.properties
## Build
```
mvn clean compile assembly:single
```

## Run
Argument format: 
```
(-d) monitor-ip port database-uri
```
Example:
```
-d 192.168.43.129 1600 http://localhost:12345/dangerReports
```
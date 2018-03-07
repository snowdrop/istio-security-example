#Running application locally

Start name server in terminal 1
```
cd spring-boot-istio-tls-name
mvn spring-boot:run
```

Start greeting server in terminal 2
```
cd spring-boot-istio-tls-greeting
mvn spring-boot:run -Drun.arguments="--name.url=http://localhost:8080/api/name,--server.port=8081"
```

Invoke greeting service in terminal 3
```
curl http://localhost:8081/api/greeting
```
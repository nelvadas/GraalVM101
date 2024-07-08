# MnBondPricing Java 21 & Micronaut Support 

## Build Locally 

Build 
```
$ mvn clean package
```

Run the App
```
$ java -jar  target/MnBondPricing-0.1.jar
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
14:54:21.155 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 883ms. Server Running: http://localhost:8080

```
## Docker Container
```sh
docker build -t nelvadas/bondpricing:1.0.0-micronaut-oraclejdk22 -f ./docker/Dockerfile.jit.oraclejdk .
```


## Metrics 

- Check the metrics endpoint 
- Root metrics endpoint 
```sh 
$ curl localhost:8080/metrics
```

- Microservice uptime
```sh 
$ curl localhost:8080/metrics/process.uptime

{
  "name": "process.uptime",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 1053.217
    }
  ],
  "description": "The uptime of the Java virtual machine"
}

```

## 

``` 
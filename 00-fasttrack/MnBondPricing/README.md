# GraalVM 101 - Micronaut

GraalVM 101: <i>Practical Workshop to Get Started with GraalVM Enterprise Edition and Micronaut Framework.</i>

<b> Table of Contents</b>

 1. [Install Oracle GraalVM ](../README.md/#installing-graalvm-ee)
 2. [Creating a simple Micronautt Bond Princing API](#bond-pricing-spring-boot-api)
 3. [GraalVM JIT to boost Throughputs](#performance-boot-with-graalvm-jit-compiler)
 4. [GraalVM Native Image : Faster,Leaner](#performance-boot-with-graalvm-nativeimage)
 5. [Cloud Native Deployments with GraalVM Native Image](#cloud-native-devployment)


# Tooling and requirements

- [sdk](https://sdkman.io/install)
- [micronaut 4.6.3+](https://micronaut.io/download/)
- [Apache benchmark](https://httpd.apache.org/docs/2.4/programs/ab.html)
- [hey](https://github.com/rakyll/hey)
- [Maven](https://maven.apache.org/)
- [upx](https://github.com/upx/upx/releases)
- [Docker/Podman](https://docs.docker.com/engine/)
- [kubectl](https://kubernetes.io/docs/reference/kubectl/)

## Installing GraalVM EE

Use the following docs/links to install Oracle GraalVM  23+ 

- [GraalVM Installation instructions](https://docs.oracle.com/en/graalvm/enterprise/23/docs/getting-started/#install-graalvm-enterprise)


## Bond Pricing Micronaut API

A [Bond](<https://www.investopedia.com/terms/b/bond.asp>) is a financial instrument that represent a loan made by an investor to a borrower that pays investor a fixed rate of return over a specific timeframe(Maturity).
At the end of the maturity period the Principal amount is paid back to the investor.

In this section, you will have to create a  Bond Pricing SpringBoot API that compute the selling price of a bond using the [Present Value Model](https://en.wikipedia.org/wiki/Bond_valuation#Present_value_approach) with

![Bond Valuation](../01-springboot//images/PV%20Value.png)
Where:
- `PV` is the Selling price/Fair Value to compute
- `C` is a coupon, periodic interest received by the lender.
`C = Contractual  * Face Value of the bond`
- `r` is the market yield to maturity
- `T` represents the number of payment to received/ years( Maturity Term)
- [More about the formular](https://www.simtrade.fr/blog_simtrade/how-compute-present-value-asset/)

1. Use [Micronaut Launch](https://micronaut.io/launch/) to create the following project
![BondPricingProject](./images/mnlaunch.png)
Fill the form with the following details.
Application Type: <b>Micronaut Application</b><br>
Java Version: <b>17</b><br>
Name: <b>MnBondPricing</b>
Base Package: <b>com.oracle.graalvm.demo</b><br>
Include Features: <b>GraalVM, Micrometer-Prometheus</b><br>

2. Download the project, unzip it and open the directory in your favorite code editor.
Add a `com.oracle.graalvm.demo.PricerController` class as describeb below.

- The  price conroller is annotated `@Controller("/price")` has two endpoints
- `/` return a welcome text
- `/{name}/{principal}/{maturity}?yield=xx&interestRate=y` used to compute the bond fair price

```java
@Controller("/price")
public class PriceController {
  ...
@Get("/{name}/{principal}/{maturity}")
 String price (
   @PathVariable(name = "name") String name,
   @PathVariable(value ="principal" ) double principal,
   @PathVariable(value = "maturity") int maturity,
   @QueryValue(value = "yield") double yield,
   @QueryValue( value="interestRate" ) double interestRate
 )
 {
  Double bondMarketPrice=
    Stream.iterate(1, year -> year +1)
    .limit(maturity)
    .parallel()
    .mapToDouble(t -> interestRate*principal/Math.pow(1+yield,t)) // compute coupon (interest) stream
    .sum() ;

  bondMarketPrice += principal/Math.pow(1+yield,maturity);

  return String.format("%.3f\n",bondMarketPrice);
 }
 ...
```
### Java version 
```
$ java -version
java version "23" 2024-09-17
Java(TM) SE Runtime Environment Oracle GraalVM 23+37.1 (build 23+37-jvmci-b01)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 23+37.1 (build 23+37-jvmci-b01, mixed mode, sharing)
```


A full version is available in the repository.

### Open JDK JIT Build 
0. Install Open jdk sdk install java 21.0.2-open
```sh
sdk install java 21.0.2-open
```

1. Clone the project Micronaut BondPricing project - Select the graal23 branch
```sh
$git  clone https://github.com/nelvadas/GraalVM101 -b graal23
```
2. Change the working direcotry to `GraalVM101/00-fast_track/MnBondPricing`

```sh
$cd GraalVM101/02-micronaut/MnBondPricing 
```

```sh 
sdk use  java 21.0.2-open
Using java version 21.0.2-open in this shell.
```

3. Use Maven to package the application.
```sh
$ mvn clean package
[INFO] Replacing original artifact with shaded artifact.
[INFO] Replacing ../GraalVM101/02-micronaut/MnBondPricing/target/MnBondPricing-0.1.jar with /.../GraalVM101/02-micronaut/MnBondPricing/target/MnBondPricing-0.1-shaded.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.053 s
[INFO] Finished at: 2024-10-22T18:20:51+02:00
[INFO] --------------------------------------
```


4. Run the JIT Binary with Open JDK

```sh
java -jar target/MnBondPricing-0.1.jar
 __  __ _                                  _
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
18:22:55.270 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 264ms. Server Running: http://localhost:8080
```
The Application start in 246 ms


5. Use jps command to find the process ID of your micronaut application 
```sh
jps
67152 org.eclipse.equinox.launcher_1.6.900.v20240613-2009.jar
76948 Main
60504 Jps
58282 MnBondPricing-0.1.jar
43404 org.eclipse.equinox.launcher_1.6.900.v20240613-2009.jar
82575 JMC
```
Here we have several java processes and the Micronaut Bond pricer is running with PID=58282 


6. Check the RSS Memory used by your application 
```sh 
ps -o rss= -p 58282 |  numfmt --to=iec
128K
```
Replace 58282 with your own application <PID>

6. Run 10000  Pricing Request with 100  concurent shots
Restrict your application on a SingleCore before benchmarking 

```sh
java  -Xlog:os+cpu -XX:ActiveProcessorCount=1  -jar target/MnBondPricing-0.1.jar
````


```sh
ab -n 10000 -c 100  'http://localhost:8080/price/graalvm/100/20?yield=0.01&interestRate=0.05'
This is ApacheBench, Version 2.3 <$Revision: 1913912 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 1000 requests
Completed 2000 requests
Completed 3000 requests
Completed 4000 requests
Completed 5000 requests
Completed 6000 requests
Completed 7000 requests
Completed 8000 requests
Completed 9000 requests
Completed 10000 requests
Finished 10000 requests


Server Software:
Server Hostname:        localhost
Server Port:            8080

Document Path:          /price/graalvm/100/20?yield=0.01&interestRate=0.05
Document Length:        8 bytes

Concurrency Level:      100
Time taken for tests:   2.633 seconds
Complete requests:      10000
Failed requests:        0
Total transferred:      1150000 bytes
HTML transferred:       80000 bytes
Requests per second:    3797.91 [#/sec] (mean)
Time per request:       26.330 [ms] (mean)
Time per request:       0.263 [ms] (mean, across all concurrent requests)
Transfer rate:          426.52 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        1   11   4.5     11      62
Processing:     2   14   5.5     13     139
Waiting:        1   12   4.6     11     109
Total:         14   25   6.5     23     141

Percentage of the requests served within a certain time (ms)
  50%     23
  66%     25
  75%     26
  80%     26
  90%     30
  95%     34
  98%     41
  99%     68
 100%    141 (longest request)
```


7. Run the application with Graal JIT 
```sh 
sdk use java 23-graal

Using java version 23-graal in this shell.
```

```sh
java   -Xlog:os+cpu=info  -XX:ActiveProcessorCount=1  -jar target/MnBondPricing-0.1.jar 
 _                                  _
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
19:19:12.371 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 304ms. Server Running: http://localhost:8080
```
Notice the startuo time - ! JIT does not optimize Startup times.


8. Check the memory used by your application 
Process identification
```sh jps
84529 MnBondPricing-0.1.jar
67152 org.eclipse.equinox.launcher_1.6.900.v20240613-2009.jar
84962 Jps
76948 Main
43404 org.eclipse.equinox.launcher_1.6.900.v20240613-2009.jar
82575 JMC
```
Memory footprint
```sh 
ps -o rss= -p 84529 |  numfmt --to=iec
81K
```


9. Benchmark with ab

10. Building a native image 

```sh
./mvnw package -Dpackaging=native-image

```
Prior running this command, make sure you initialize Logback at build time 

Add the following line in the compilerArgs tag of the Maven compiler plugin
```xml
            <arg>--initialize-at-build-time=ch.qos.logback.classic.Logger</arg>  
```

A native image binary is available in the target directory 
```sh
ls -rtlh target
total 138760
drwxr-xr-x@ 3 enono  staff    96B 22 oct 18:20 maven-status
drwxr-xr-x@ 3 enono  staff    96B 22 oct 18:20 generated-sources
drwxr-xr-x@ 6 enono  staff   192B 22 oct 18:20 classes
drwxr-xr-x@ 3 enono  staff    96B 22 oct 18:20 generated-test-sources
drwxr-xr-x@ 4 enono  staff   128B 22 oct 18:20 test-classes
drwxr-xr-x@ 3 enono  staff    96B 22 oct 18:20 maven-archiver
-rw-r--r--@ 1 enono  staff   9,8K 22 oct 18:20 original-MnBondPricing-0.1.jar
drwxr-xr-x@ 3 enono  staff    96B 22 oct 19:17 native
drwxr-xr-x@ 3 enono  staff    96B 22 oct 19:17 graalvm-reachability-metadata
drwxr-xr-x@ 4 enono  staff   128B 22 oct 22:13 surefire-reports
-rw-r--r--@ 1 enono  staff   9,9K 22 oct 22:13 MnBondPricing-0.1.jar
-rwxr-xr-x@ 1 enono  staff    68M 22 oct 22:14 MnBondPricing
```



11. Check the Startup time 
```sh
 graal23 ±  ./target/MnBondPricing
 __  __ _                                  _
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
22:05:58.322 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 14ms. Server Running: http://localhost:8080
```

12. Check the memory size 


13. Benchmark Again 

```sh
ab -n 10000 -c 100  'http://localhost:8080/price/graalvm/100/20?yield=0.01&interestRate=0.05'

This is ApacheBench, Version 2.3 <$Revision: 1913912 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 1000 requests
Completed 2000 requests
Completed 3000 requests
Completed 4000 requests
Completed 5000 requests
Completed 6000 requests
Completed 7000 requests
Completed 8000 requests
Completed 9000 requests
Completed 10000 requests
Finished 10000 requests


Server Software:
Server Hostname:        localhost
Server Port:            8080

Document Path:          /price/graalvm/100/20?yield=0.01&interestRate=0.05
Document Length:        8 bytes

Concurrency Level:      100
Time taken for tests:   2.429 seconds
Complete requests:      10000
Failed requests:        0
Total transferred:      1150000 bytes
HTML transferred:       80000 bytes
Requests per second:    4116.74 [#/sec] (mean)
Time per request:       24.291 [ms] (mean)
Time per request:       0.243 [ms] (mean, across all concurrent requests)
Transfer rate:          462.33 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        1   12   7.2     12      86
Processing:     2   11   7.2     11      86
Waiting:        0   11   7.0     10      86
Total:          6   24   9.9     22     103

Percentage of the requests served within a certain time (ms)
  50%     22
  66%     23
  75%     23
  80%     23
  90%     24
  95%     25
  98%     59
  99%     87
 100%    103 (longest request)
 ```

 How would you restrict this process to 1 CPU only ?

14. Rebuilt the native image and optimze its size
```xml 
 <compilerArgs>
           ...
            <arg>--initialize-at-build-time=ch.qos.logback.classic.Logger</arg> 
            <arg>-J-Xlog:os+cpu=info</arg>
            <arg>-J-XX:ActiveProcessorCount=1</arg>
            <arg>-Os</arg>
      </compilerArgs>
```

15. Shrink the size of your native image with UPX 
```sh 
upx --force-macos target/MnBondPricing
                       Ultimate Packer for eXecutables
                          Copyright (C) 1996 - 2024
UPX 4.2.4       Markus Oberhumer, Laszlo Molnar & John Reiser    May 9th 2024

        File size         Ratio      Format      Name
   --------------------   ------   -----------   -----------
  71019232 ->  26853392   37.81%   macho/arm64   MnBondPricing

Packed 1 file.
```


15. Check the GC used by your native image 

```sh
-Xlog:gc=info
```











## Working with Containers 

9. Throuputs with Graal JIT 

```sh
ab -n 10000 -c 100  'http://localhost:8080/price/graalvm/100/20?yield=0.01&interestRate=0.05'
T

4. Build a container image with OpenJDK17 as base image 
```sh
$ docker build -t nelvadas/bondpricing:1.0.0-micronaut-openjdk17 -f ./docker/Dockerfile.openjdk .
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
STEP 1/5: FROM openjdk:17-oraclelinux8
STEP 2/5: ARG APP_FILE=MnBondPricing-0.1.jar
--> Using cache 5970960c925f7b58c86cb4f8c920946972d4b5d42fa118a61edfc427eec935ce
--> 5970960c925
STEP 3/5: EXPOSE 8080
--> Using cache 692bce678d0af7fc3b4fbf792b5f7d883dbb4da0200063854841f24783ca8451
--> 692bce678d0
STEP 4/5: COPY ./target/${APP_FILE} app.jar
--> 92da598c1e8
STEP 5/5: CMD ["java","-jar","app.jar"]
COMMIT nelvadas/bondpricing:1.0.0-micronaut-openjdk17
--> 90f5b44f481
Successfully tagged localhost/nelvadas/bondpricing:1.0.0-micronaut-openjdk17
90f5b44f481d48600589fcd36cd0efe3f0f4d208b1ebad3a3710387e5ddeae86 
```

5. Start a local container 
```sh
$ docker run -d -p 8080:8080 --rm --name MnBondPricing nelvadas/bondpricing:1.0.0-micronaut-openjdk17
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
565683c94ad0923be9a39a0833110f56026696cbaa15ea6f47d3651fda186640
```

6. Check the containers logs and startup times.

```sh 
$ docker logs 565683c94ad0
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
 __  __ _                                  _
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
  Micronaut (v3.7.4)

13:42:50.677 [main] INFO  i.m.context.env.DefaultEnvironment - Established active environments: [oraclecloud, cloud]
13:42:51.589 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 1562ms. Server Running: http://565683c94ad0:8080
```
This containers is ready to fullfill user resquests after `1.6 seconds` 

7. Send requests to the container.
```sh
$ curl 'localhost:8080/price/graalvm/100/10/?yield=0.03&interestRate=0.02'
91.470
```
The application is responding perfectly. We can now deploy it in any container platform of our choice.
For this demo, we have a running Verrazzano cluster running on top of kubernetes 1.24

8. Deploy your application on Verrazzano or Any Kubernetes Plateform with Prometheus and Graphana to collect metrics.

```sh
$ kubectl apply -f ../03-verrazzano/bondpricer-jit.yaml`
```
9. Check application pods 
```sh 
$ kubectl get pods -n ms-dev
NAME                                       READY   STATUS    RESTARTS   AGE
bondpricer-jit-workload-6bc7b6fcc5-7wb47   2/2     Running   0          4d23h
```


10. Check Virtual services 

```sh 
$ kubectl get virtualservices
NAME                               GATEWAYS                               HOSTS                                                    AGE
bondpricer-jit-ingress-rule-0-vs   ["ms-dev-bondpricer-jit-appconf-gw"]   ["bondpricer-jit-appconf.ms-dev.141.148.54.96.nip.io"]   61m
```


11. Call the application 
```sh
$ curl -k  'https://bondpricer-jit-appconf.ms-dev.141.148.54.96.nip.io/price/graalvm/100/10/?yield=0.03&interestRate=0.02'
91.470
```



12. Launch stress tests 
```sh 
hey -z 20m 'https://bondpricer-jit-appconf.ms-dev.141.148.54.96.nip.io/price/graalvm/100/10/?yield=0.03&interestRate=0.02'
```

13. Check prometheus metrics from your web browser
```sh
https://bondpricer-jit-appconf.ms-dev.141.148.54.96.nip.io/prometheus
```

In the next section we are building the same application container image using AOT and GraalVM Native Image Enterprise.

## Native image build with GraalVM Enterprise 
0. check the multistage Dockerfile 
```dockerfile 
# Install tar and gzip to extract the Maven binaries
RUN microdnf update \
 && microdnf install --nodocs \
    tar \
    gzip \
    maven \
 && microdnf clean all \
 && rm -rf /var/cache/yum


ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# Set the working directory to /home/app
WORKDIR /build

# Copy the source code into the image for building
COPY . /build

# Build
RUN mvn clean package  -Dpackaging=native-image


# The deployment Image
FROM docker.io/oraclelinux:8-slim
EXPOSE 8080
# Copy the native executable into the containers
COPY --from=builder /build/target/MnBondPricing .
ENTRYPOINT ["/MnBondPricing"]
```
available for native image build.
[Micronaut Maven plugin] (<https://micronaut-projects.github.io/micronaut-maven-plugin/latest/examples/package.html>)

1. Build native image 
```sh
$ docker build  -t nelvadas/bondpricing:1.0.0-micronaut-graalee-native-u1 -f ./docker/Dockerfile.native-graalvm-ee .
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
[1/2] STEP 1/7: FROM container-registry.oracle.com/graalvm/native-image-ee:ol8-java17 AS builder
[1/2] STEP 2/7: RUN microdnf update  && microdnf install --nodocs     tar     gzip     maven  && microdnf clean all  && rm -rf /var/cache/yum
Downloading metadata...
...
------------------------------------------------------------------------------------------------------------------------
                       16.6s (3.2% of total time) in 104 GCs | Peak RSS: 3.90GB | CPU load: 1.82
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /build/target/MnBondPricing (executable)
 /build/target/MnBondPricing.build_artifacts.txt (txt)
========================================================================================================================
Finished generating 'MnBondPricing' in 8m 38s.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 09:11 min
[INFO] Finished at: 2022-11-24T14:56:34Z
[INFO] ------------------------------------------------------------------------
[WARNING] The requested profile "native" could not be activated because it does not exist.
--> 32abad31a8b
[2/2] STEP 1/4: FROM gcr.io/distroless/base-debian10
[2/2] STEP 2/4: EXPOSE 8080
--> 700ab57d45f

```

2. Push image to Docker registry repository 
```sh
 docker push nelvadas/bondpricing:1.0.0-micronaut-graalee-native-u1

[opc@enono-workstation-01 MnBondPricing]$ docker images |grep micronaut
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
localhost/nelvadas/bondpricing                         1.0.0-micronaut-graalee-native-u1  cd15b918c4e0  About an hour ago  86.8 MB
docker.io/nelvadas/bondpricing                         1.0.0-micronaut-graalce-native-u1  f66c72050e18  3 hours ago        165 MB
localhost/nelvadas/bondpricing                         1.0.0-micronaut-graalce-native-u1  f66c72050e18  3 hours ago        165 MB
localhost/nelvadas/bondpricing                         1.0.0-micronaut-openjdk17-u2       1e43d1de2965  26 hours ago       491 MB
localhost/nelvadas/bondpricing                         1.0.0-micronaut-openjdk17-u1       acea9d6cb391  2 days ago         491 MB
localhost/nelvadas/bondpricing                         1.0.0-micronaut-openjdk17          155536d84d4d  2 days ago         491 MB
[opc@enono-workstation-01 MnBondPricing]$
```


3. Deploy your application on Verrazzano or Any Kubernetes Plateform with Prometheus and Graphana to collect metrics.

```sh
kubectl apply -f ../03-verrazzano/bondpricer-native-ee.yaml`
```

4. Check application pods

```sh
$ kubectl get pods -n ms-dev-ee
NAME                                             READY   STATUS    RESTARTS   AGE
bondpricer-native-ee-workload-6b64fd985f-jmwtf   2/2     Running   0          3d21h
```

5. Check Virtual services

```sh
$$ kubectl get virtualservices -n ms-dev-ee
NAME                                     GATEWAYS                                        HOSTS                                                             AGE
bondpricer-native-ee-ingress-rule-0-vs   ["ms-dev-ee-bondpricer-native-ee-appconf-gw"]   ["bondpricer-native-ee-appconf.ms-dev-ee.141.148.54.96.nip.io"]   3d21h
```


### Graphana Live Metrics dashboard

Send a huge workload by running the scrip

```sh
$ ./generateLoad.sh
```
Check the metrics on the grafana dashboad.

![Grafana Dashboard](./images/grafana.png)

GraalVM EE application starts faster than the traditionnal app, and use less CPU & Memory.

-






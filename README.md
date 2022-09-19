# GraalVM 101
GraalVM 101: <i>Practical Workshop to Get Started with GraalVM Enterprise Edition.</i>


<b> Table of Contents</b>

 1. [Install GraalVM EE](#installing-graalvm-ee)
 2. [Creating a simple Spring Boot Bond Princing API](#bond-pricing-spring-boot-api)
 3. [GraalVM JIT to boost Throughputs](#performance-boot-with-graalvm-jit-compiler)
 4. [GraalVM Native Image : Faster,Leaner ](#performance-boot-with-graalvm-nativeimage)
 4. [Cloud Native Deployments with GraalVM Native Image](#cloud-native-devployment)

# Tooling and requirements

- Java Runtimes: GraalVM + OpenJDK
- [Apache benchmark](https://httpd.apache.org/docs/2.4/programs/ab.html)
- [hey](https://github.com/rakyll/hey)
- [Maven](https://maven.apache.org/)
- [upx](https://github.com/upx/upx/releases)
- [Docker/Podman](https://docs.docker.com/engine/)

## Installing GraalVM EE

Use the following docs/links to install GraalVM Enterprise 22.2+

* [GraalVM Installation instructions](https://docs.oracle.com/en/graalvm/enterprise/22/docs/getting-started/#install-graalvm-enterprise)
* [Oracle Linux /OCI](https://docs.oracle.com/en/graalvm/enterprise/22/docs/getting-started/#install-graalvm-enterprise)


## Bond Pricing Spring Boot API

A [Bond](<https://www.investopedia.com/terms/b/bond.asp>) is a financial instrument that represent a loan made by an investor to a borrower that pays investor a fixed rate of return over a specific timeframe(Maturity).
At the end of the maturity period the Principal amount is paid back to the investor. 


In this section, you will have to create a  Bond Pricing SpringBoot API that compute the selling price of a bond using the [Present Value Model](https://en.wikipedia.org/wiki/Bond_valuation#Present_value_approach) with

![Bond Valuation](./images/PV%20Value.png)
Where: 
* `PV` is the Selling price/Fair Value to compute 
* `C` is a coupon, periodic interest received by the lender.
`C = Contractual  * Face Value of the bond `
* `r` is the market yield to maturity
* `T` represents the number of payment to received/ years( Maturity Term)
* [More about the formular](https://www.simtrade.fr/blog_simtrade/how-compute-present-value-asset/)

1. Use spring initialzr to create the following project
![BondPricingProject](./images/BondPricingSpringInitialzr.png)
Fill the form with the following details.
Group: <b>com.oracle.graalvm.demo</b><br>
Artifact: <b>BondPricing</b><br>
Description : <b>Bond Pricing Demo Project for GraalVM & Spring Boot</b><br>
Package Name: <b>com.oracle.graalvm.demo</b><br>
Packaging: <b>Jar</b><br>
Java:  <b>11</b><br>



2. Download the project, unzip it and open the directory in your favorite code editor.
Add a `com.oracle.graalvm.demo.PricerController` class as describeb below.

- The conroller has two endpoints
- `/` return a welcome text
- `/price/{name}/{principal}/{maturity}?yield=xx&interestRate=y` used to compute the bond fair price

```java
package com.oracle.graalvm.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@RestController
public class PriceController {

 @GetMapping("/")
 public String index() {
  return "Welcome to GraalVM Spring Boot HOL!\n";
 }

 /**
  * @param name : Bond name
  * @param interestRate  : Contractual Interest Rate
  * @param principal : the bond PAR/FACE Value or principal ( amount returned to the bond acquirer at the maturity)
  * @param maturity : Number of years/coupon for the bond to mature: For simplicity we stick on annual payments
  * @param yield : Market Interest rate / appropriate yield to maturity
  * @see  <a href="https://en.wikipedia.org/wiki/Bond_valuation#Present_value_approach"></a>
  * @return The Bond Selling price
  */
 @GetMapping("/price/{name}/{principal}/{maturity}")
 ResponseEntity price (
   @PathVariable(name = "name") String name,
   @PathVariable(value ="principal" , required = true ) double principal,
   @PathVariable(value = "maturity", required = true) int maturity,
   @RequestParam(value = "yield", required = true ) double yield,
   @RequestParam( name="interestRate",required = true ) double interestRate
 )

 {

  Double bondMarketPrice=
    Stream.iterate(1, year -> year +1)
    .limit(maturity)
    .parallel()
    .mapToDouble(t -> interestRate*principal/Math.pow(1+yield,t)) // compute coupon (interest) stream
    .sum() ;

  bondMarketPrice += principal/Math.pow(1+yield,maturity);

  return ResponseEntity.ok().body(String.format("%.3f\n",bondMarketPrice));
 }

}

```

Upate the Test Case

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PriceControllerTest {

    @Autowired
    private TestRestTemplate template;
    ...

    @Test
    public void getPrice() throws Exception {
        ResponseEntity<Double> response = template.getForEntity("/price/GRAALVM_PREMIUM_BOND/100/1?yield=0.01&interestRate=0.05", Double.class);
        assertThat(response.getBody()).isEqualTo(103.960);
    }

```



3. Use GraalVM Enterprise 22.2.0 as your Java Runtime 
various options available:
- export JAVA_HOME=..
- sdk use java x.y.z

```sh 
$ $ java -version
java version "11.0.16" 2022-07-19 LTS
Java(TM) SE Runtime Environment GraalVM EE 22.2.0 (build 11.0.16+11-LTS-jvmci-22.2-b05)
Java HotSpot(TM) 64-Bit Server VM GraalVM EE 22.2.0 (build 11.0.16+11-LTS-jvmci-22.2-b05, mixed mode, sharing)
```

4- Build the Application and get a couple of fair prices using Maven/Gradle
```sh
$ mvn clean package 
...
[INFO] --- maven-jar-plugin:3.2.2:jar (default-jar) @ BondPricing ---
[INFO] Building jar: /Users/nono/Projects/Workshops/EMEA-HOL-GraalVM22/GraalVM101/BondPricing/target/BondPricing-0.0.1-SNAPSHOT.jar
[INFO]
[INFO] --- spring-boot-maven-plugin:2.7.3:repackage (repackage) @ BondPricing ---
[INFO] Replacing main artifact with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  35.096 s
[INFO] Finished at: 2022-09-19T13:59:12+02:00
[INFO] ------------------------------------------------------------------------
```
the `BondPricing-0.0.1-SNAPSHOT.jar` is  generates and can be started using 

```sh
$ java -jar target/BondPricing-0.0.1-SNAPSHOT.jar
2022-09-19 14:02:56.375  INFO 11751 --- [           main] o.s.nativex.NativeListener               : AOT mode disabled

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.3)

2022-09-19 14:02:56.459  INFO 11751 --- [           main] c.o.graalvm.demo.BondPricingApplication  : Starting BondPricingApplication v0.0.1-SNAPSHOT using Java 11.0.16 on nono-mac with PID 11751 (/Users/nono/Projects/Workshops/EMEA-HOL-GraalVM22/GraalVM101/BondPricing/target/BondPricing-0.0.1-SNAPSHOT.jar 
BondPricingApplication  : Started BondPricingApplication in 2.087 seconds (JVM running for 2.638)
```
the application starts in <b>2.087 seconds.</b>

5- Compute a couple of bond Fair Values 
```sh
$ curl  'http://localhost:8080/price/graalvm-premium-bond/100/1?yield=0.01&interestRate=0.05'
103,960

$ curl  'http://localhost:8080/price/graalvm-discount_bond/100/1?yield=0.06&interestRate=0.05'
99,057
```


## Performance Boost with GraalVM JIT Compiler
### GraalVM JIT
In the following section we start the BondPricer with GraalVM JIT, then send 10000 pricing request with a factor 100 for concurrency level.
Load is sent using  Apache Benchmark 

```sh 
$ ab -n 10000 -c 100  'http://localhost:8080/price/graalvm/100/20?yield=0.01&interestRate=0.05'
This is ApacheBench, Version 2.3 <$Revision: 1879490 $>
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
Time taken for tests:   4.562 seconds
Complete requests:      10000
Failed requests:        0
Total transferred:      1400000 bytes
HTML transferred:       80000 bytes
Requests per second:    2192.14 [#/sec] (mean)
Time per request:       45.618 [ms] (mean)
Time per request:       0.456 [ms] (mean, across all concurrent requests)
Transfer rate:          299.71 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        1   23  10.6     22     140
Processing:     2   22  11.9     20     141
Waiting:        1   21  11.4     19     139
Total:         24   45  16.7     41     189

Percentage of the requests served within a certain time (ms)
  50%     41
  66%     44
  75%     46
  80%     48
  90%     56
  95%     70
  98%     94
  99%     97
 100%    189 (longest request)
 ```



### Non GraalVM Java JIT Compiler

In the following section we restart the BondPricer with a default JIT compiler, and  send the same load( 10000 pricing  requests)  with Apache Benchmark and capture the performance Metrics.
use the `-XX:-UseJVMCICompiler` JVM option to fall back of the default JIT compiler or restart your application with an openJDK 11 for eg.

```sh
$ java`-XX:-UseJVMCICompiler -jar target/BondPricing-0.0.1-SNAPSHOT.jar
```
Benchmark - C2 compiler
```sh
$ ab -n 10000 -c 100  'http://localhost:8080/price/graalvm/100/20?yield=0.01&interestRate=0.05'
This is ApacheBench, Version 2.3 <$Revision: 1879490 $>
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
Time taken for tests:   5.910 seconds
Complete requests:      10000
Failed requests:        0
Total transferred:      1400000 bytes
HTML transferred:       80000 bytes
Requests per second:    1692.16 [#/sec] (mean)
Time per request:       59.096 [ms] (mean)
Time per request:       0.591 [ms] (mean, across all concurrent requests)
Transfer rate:          231.35 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        1   27   9.4     24      85
Processing:     7   30  12.5     27     163
Waiting:        7   28  11.0     25     116
Total:         32   57  16.6     51     165

Percentage of the requests served within a certain time (ms)
  50%     51
  66%     56
  75%     61
  80%     66
  90%     85
  95%     96
  98%    104
  99%    112
 100%    165 (longest request)
````
Benchmark with Amazon Correto

```sh
$ java -version
openjdk version "11.0.16" 2022-07-19 LTS
OpenJDK Runtime Environment Corretto-11.0.16.8.1 (build 11.0.16+8-LTS)
OpenJDK 64-Bit Server VM Corretto-11.0.16.8.1 (build 11.0.16+8-LTS, mixed mode)
```
Outputs
```sh 
Server Software:
Server Hostname:        localhost
Server Port:            8080

Document Path:          /price/graalvm/100/20?yield=0.01&interestRate=0.05
Document Length:        8 bytes

Concurrency Level:      100
Time taken for tests:   5.282 seconds
Complete requests:      10000
Failed requests:        0
Total transferred:      1400000 bytes
HTML transferred:       80000 bytes
Requests per second:    1893.09 [#/sec] (mean)
Time per request:       52.824 [ms] (mean)
Time per request:       0.528 [ms] (mean, across all concurrent requests)
Transfer rate:          258.82 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        1   25   8.8     23      82
Processing:     7   27  11.6     23     156
Waiting:        7   25  10.0     23     119
Total:         31   51  14.6     46     158

Percentage of the requests served within a certain time (ms)
  50%     46
  66%     50
  75%     53
  80%     56
  90%     71
  95%     86
  98%    100
  99%    102
 100%    158 (longest request)
nono-mac
```

GraalVM outperformes both openjdk and Correto JIT for this use case.

|  JIT Performance |<i>Req/sec   |  Time per Req(mean)ms | Overall time (s)  |   |
|---|---|---|---|---|
|  GralVM 22.2.0-ee11.0.16  | <b><span style="color:green">2192</span></b>  | <b><span style="color:green">0.456</span>  | <b><span style="color:green">4.562</span> </b> |
|  Coretto 11.0.16.8.1 |1893.09   | 0.528  | 5.282  |
|  Open JDK 11.0.16 |  1692.16 |  0.591 |   5.910|

Graal JIT compiler accelerates latency, throughput and performs ~22% better than both Corretto 11  JI and OpenJDK JIT. 

## Performance Boost with GraalVM Native Image

### Native Image Build
To build the native image associated to the application use the command
<i> - We are skipping tests for this build.</i>


```sh 
$ mvn package -DskipTests -Pnative

...
GraalVM Native Image: Generating 'BondPricing' (executable)...
========================================================================================================================
Warning: Could not register org.springframework.boot.actuate.health.ReactiveHealthEndpointWebExtension: allPublicMethods for reflection. Reason: java.lang.NoClassDefFoundError: reactor/core/publisher/Mono.
Warning: Could not register org.springframework.boot.autoconfigure.jdbc.HikariDriverConfigurationFailureAnalyzer: allDeclaredConstructors for reflection. Reason: java.lang.NoClassDefFoundError: org/springframework/jdbc/CannotGetJdbcConnectionException.
Warning: Could not register org.springframework.boot.diagnostics.analyzer.ValidationExceptionFailureAnalyzer: allDeclaredConstructors for reflection. Reason: java.lang.NoClassDefFoundError: javax/validation/ValidationException.
Warning: Could not register org.springframework.boot.liquibase.LiquibaseChangelogMissingFailureAnalyzer: allDeclaredConstructors for reflection. Reason: java.lang.NoClassDefFoundError: liquibase/exception/ChangeLogParseException.
[1/7] Initializing...                                                                                    (9,7s @ 0,19GB)
 Version info: 'GraalVM 22.2.0 Java 11 EE'
 Java version info: '11.0.16+11-LTS-jvmci-22.2-b05'
 C compiler: cc (apple, x86_64, 14.0.0)
 Garbage collector: Serial GC
Warning: Could not register complete reflection metadata for org.springframework.validation.beanvalidation.SpringValidatorAdapter$ViolationFieldError. Reason(s): java.lang.NoClassDefFoundError: javax/validation/Validator
[2/7] Performing analysis...  [*********]                                                               (39,0s @ 3,77GB)
  14 904 (90,67%) of 16 437 classes reachable
  23 701 (69,72%) of 33 994 fields reachable
  77 921 (65,64%) of 118 716 methods reachable
     856 classes,   153 fields, and 4 012 methods registered for reflection
      69 classes,    89 fields, and    56 methods registered for JNI access
       6 native libraries: -framework CoreServices, -framework Foundation, dl, pthread, stdc++, z
[3/7] Building universe...                                                                               (5,8s @ 2,34GB)
[4/7] Parsing methods...      [***]                                                                      (5,8s @ 3,36GB)
[5/7] Inlining methods...     [***]                                                                      (2,6s @ 1,37GB)
[6/7] Compiling methods...    [*********]                                                               (77,7s @ 4,99GB)
[7/7] Creating image...                                                                                  (8,3s @ 3,87GB)
  36,79MB (57,35%) for code area:    44 529 compilation units
  27,02MB (42,13%) for image heap:  351 763 objects and 358 resources
 345,59KB ( 0,53%) for other data
  64,15MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 packages in code area:                               Top 10 object types in image heap:
   3,07MB com.oracle.svm.core.code                             7,69MB byte[] for code metadata
   1,56MB sun.security.ssl                                     3,28MB byte[] for java.lang.String
   1,29MB java.util                                            3,00MB byte[] for embedded resources
 864,96KB com.sun.crypto.provider                              2,60MB java.lang.Class
 685,05KB java.util.concurrent                                 2,48MB java.lang.String
 670,54KB org.apache.tomcat.util.net                           2,25MB byte[] for general heap data
 644,53KB java.lang                                          896,43KB byte[] for reflection metadata
 642,16KB org.apache.coyote.http2                            698,63KB com.oracle.svm.core.hub.DynamicHubCompanion
 625,53KB org.apache.catalina.core                           453,72KB java.util.HashMap$Node
 536,46KB sun.security.x509                                  423,06KB c.o.svm.core.hub.DynamicHub$ReflectionMetadata
  25,98MB for 624 more packages                                3,23MB for 2986 more object types
------------------------------------------------------------------------------------------------------------------------
                        5,8s (3,7% of total time) in 56 GCs | Peak RSS: 6,39GB | CPU load: 8,87
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /Users/nono/Projects/Workshops/EMEA-HOL-GraalVM22/GraalVM101/BondPricing/target/BondPricing (executable)
 /Users/nono/Projects/Workshops/EMEA-HOL-GraalVM22/GraalVM101/BondPricing/target/BondPricing.build_artifacts.txt (txt)
========================================================================================================================
Finished generating 'BondPricing' in 2m 37s.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  02:52 min
[INFO] Finished at: 2022-09-19T15:23:16+02:00
[INFO] ------------------------------------------------------------------------
nono-mac:BondPricing nono$
```

A native `BondPricing` image file is generated
```sh
$ ls -rtlh target/
total 324272
drwxr-xr-x  3 nono  staff    96B 19 sep 15:07 generated-sources
drwxr-xr-x  3 nono  staff    96B 19 sep 15:07 maven-status
drwxr-xr-x  3 nono  staff    96B 19 sep 15:07 generated-test-sources
drwxr-xr-x  3 nono  staff    96B 19 sep 15:07 generated-runtime-test-sources
drwxr-xr-x  4 nono  staff   128B 19 sep 15:07 surefire-reports
drwxr-xr-x  6 nono  staff   192B 19 sep 15:07 classes
drwxr-xr-x  3 nono  staff    96B 19 sep 15:07 maven-archiver
-rw-r--r--  1 nono  staff   104K 19 sep 15:07 BondPricing-0.0.1-SNAPSHOT.jar.original
drwxr-xr-x  3 nono  staff    96B 19 sep 15:08 test-ids
-rwxr-xr-x  1 nono  staff    75M 19 sep 15:11 native-tests
-rw-r--r--  1 nono  staff    27B 19 sep 15:11 native-tests.build_artifacts.txt
drwxr-xr-x  3 nono  staff    96B 19 sep 15:11 native-test-reports
drwxr-xr-x  5 nono  staff   160B 19 sep 15:20 test-classes
drwxr-xr-x  3 nono  staff    96B 19 sep 15:20 generated-runtime-sources
-rw-r--r--  1 nono  staff   104K 19 sep 15:20 BondPricing-0.0.1-SNAPSHOT.jar
-rw-r--r--  1 nono  staff    19M 19 sep 15:20 BondPricing-0.0.1-SNAPSHOT-exec.jar
-rwxr-xr-x  1 nono  staff    64M 19 sep 15:23 BondPricing
-rw-r--r--  1 nono  staff    26B 19 sep 15:23 BondPricing.build_artifacts.txt
```
### Fast startup 

```sh 
$ ./target/BondPricing
2022-09-19 15:42:35.686  INFO 15914 --- [           main] o.s.nativex.NativeListener               : AOT mode enabled

  .   ____          ___ __
 /\\ / ___'___ __(_)_ ____ _\ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
\\/___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.3)

2022-09-19 15:42:35.688  INFO 15914 --- [           main] c.o.graalvm.demo.BondPricingApplication  : Starting BondPricingApplication v0.0.1-SNAPSHOT using Java 11.0.16 on nono-mac with PID 15914 (/Users/nono/Projects/Workshops/EMEA-HOL-GraalVM22/GraalVM101/BondPricing/target/BondPricing started by nono in /Users/nono/Projects/Workshops/EMEA-HOL-GraalVM22/GraalVM101/BondPricing)
2022-09-19 15:42:35.688  INFO 15914 --- [           main] c.o.graalvm.demo.BondPricingApplication  : No active profile set, falling back to 1 default profile: "default"
2022-09-19 15:42:35.724  INFO 15914 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2022-09-19 15:42:35.725  INFO 15914 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2022-09-19 15:42:35.725  INFO 15914 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.65]
2022-09-19 15:42:35.731  INFO 15914 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2022-09-19 15:42:35.731  INFO 15914 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 43 ms
2022-09-19 15:42:35.733  WARN 15914 --- [           main] i.m.c.i.binder.jvm.JvmGcMetrics          : GC notifications will not be available because MemoryPoolMXBeans are not provided by the JVM
2022-09-19 15:42:35.760  INFO 15914 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint(s) beneath base path '/actuator'
2022-09-19 15:42:35.762  INFO 15914 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2022-09-19 15:42:35.762  INFO 15914 --- [           main] c.o.graalvm.demo.BondPricingApplication  : Started BondPricingApplication in 0.134 seconds (JVM running for 0.136)

```
The native image build starts very fast <b><span style="color:green">0.134s</span></b> compared to <span style="color:orange">2 seconds </span>2 in tthe [JIT mode](#graalvm-jit)


## Cloud Native Devployment
 

### The Old way
```sh 
$ cd BondPricing
$ docker build -f docker/Dockerfile.jit -t nelvadas/bondpricing:1.0-jit-temurin-11.0.16 .
```
Image size 

```sh 
$ docker images | grep bondpricing
nelvadas/bondpricing               1.0-jit-temurin-11.0.16         a203dd86e670   5 minutes ago    972MB
```
Application up and Running 

```sh 
$ docker run -p 7070:8080 -d docker.io/nelvadas/bondpricing:1.0-jit-temurin-11.0.16
5f0d75a17a17bcc4c70e68c6c3cbe6261ba909d5e8822e9a12020549a745ccb8
nono-mac:BondPricing nono$ docker ps
CONTAINER ID   IMAGE                                          COMMAND                  CREATED         STATUS         PORTS                    NAMES
5f0d75a17a17   nelvadas/bondpricing:1.0-jit-temurin-11.0.16   "/root/.sdkman/candi…"   3 seconds ago   Up 2 seconds   0.0.0.0:7070->8080/tcp   youthful_mestorf
```
### Natime image patterns

#### Build a native image 
From Oracle linux station, build a new native image. 
```sh
$ mvn clean package -Pnative
...
GraalVM Native Image: Generating 'BondPricing' (executable)...
========================================================================================================================

[1/7] Initializing...                                                                                   (14.1s @ 0.20GB)
 Version info: 'GraalVM 22.2.0 Java 11 EE'
 Java version info: '11.0.16+11-LTS-jvmci-22.2-b05'
 C compiler: gcc (redhat, x86_64, 8.5.0)
 Garbage collector: Serial GC
Warning: Could not register complete reflection metadata for org.springframework.validation.beanvalidation.SpringValidatorAdapter$ViolationFieldError. Reason(s): java.lang.NoClassDefFoundError: javax/validation/Validator
[2/7] Performing analysis...  [*********]                                                              (122.5s @ 2.24GB)
  14,930 (90.53%) of 16,491 classes reachable
  23,717 (69.67%) of 34,041 fields reachable
  78,092 (65.56%) of 119,121 methods reachable
     856 classes,   153 fields, and 4,014 methods registered for reflection
      69 classes,    88 fields, and    56 methods registered for JNI access
       5 native libraries: dl, pthread, rt, stdc++, z
[3/7] Building universe...                                                                              (15.6s @ 1.55GB)
[4/7] Parsing methods...      [****]                                                                    (19.4s @ 1.39GB)
[5/7] Inlining methods...     [***]                                                                      (6.9s @ 1.87GB)
[6/7] Compiling methods...    [[6/7] Compiling methods...    [*****************]                                                      (299.2s @ 1.29GB)
[7/7] Creating image...                                                                                  (9.9s @ 1.86GB)
  36.82MB (57.18%) for code area:    44,615 compilation units
  27.03MB (41.97%) for image heap:  351,708 objects and 358 resources
 559.02KB ( 0.85%) for other data
  64.39MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 packages in code area:                               Top 10 object types in image heap:
   3.07MB com.oracle.svm.core.code                             7.69MB byte[] for code metadata
   1.57MB sun.security.ssl                                     3.29MB byte[] for java.lang.String
   1.29MB java.util                                            2.99MB byte[] for embedded resources
 865.01KB com.sun.crypto.provider                              2.60MB java.lang.Class
 685.25KB java.util.concurrent                                 2.48MB java.lang.String
 673.54KB org.apache.tomcat.util.net                           2.25MB byte[] for general heap data
 638.37KB org.apache.coyote.http2                            897.66KB byte[] for reflection metadata
 633.08KB java.lang                                          699.84KB com.oracle.svm.core.hub.DynamicHubCompanion
 622.60KB org.apache.catalina.core                           453.91KB java.util.HashMap$Node
 536.46KB sun.security.x509                                  423.72KB c.o.svm.core.hub.DynamicHub$ReflectionMetadata
  26.02MB for 626 more packages                                3.22MB for 2985 more object types
------------------------------------------------------------------------------------------------------------------------
                       19.0s (3.8% of total time) in 106 GCs | Peak RSS: 3.88GB | CPU load: 1.93
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /home/opc/GraalVM101/BondPricing/target/BondPricing (executable)
 /home/opc/GraalVM101/BondPricing/target/BondPricing.build_artifacts.txt (txt)
========================================================================================================================
Finished generating 'BondPricing' in 8m 17s.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 18:48 min
[INFO] Finished at: 2022-09-19T19:11:17Z
[INFO] ------------------------------------------------------------------------
[opc@enono-workstation-01 BondPricing]$ ls -rtlh target/
total 158M
drwxrwxr-x. 3 opc opc   25 Sep 19 18:52 generated-sources
drwxrwxr-x. 3 opc opc   35 Sep 19 18:52 maven-status
drwxrwxr-x. 3 opc opc   30 Sep 19 18:52 generated-test-sources
drwxrwxr-x. 3 opc opc   24 Sep 19 18:52 generated-runtime-test-sources
drwxrwxr-x. 5 opc opc   69 Sep 19 18:53 test-classes
drwxrwxr-x. 2 opc opc  121 Sep 19 18:53 surefire-reports
drwxrwxr-x. 2 opc opc   63 Sep 19 18:53 test-ids
-rwxrwxr-x. 1 opc opc  75M Sep 19 19:02 native-tests
-rw-rw-r--. 1 opc opc   27 Sep 19 19:02 native-tests.build_artifacts.txt
drwxrwxr-x. 2 opc opc   36 Sep 19 19:02 native-test-reports
drwxrwxr-x. 3 opc opc   24 Sep 19 19:02 generated-runtime-sources
drwxrwxr-x. 5 opc opc   74 Sep 19 19:02 classes
drwxrwxr-x. 2 opc opc   28 Sep 19 19:02 maven-archiver
-rw-rw-r--. 1 opc opc 105K Sep 19 19:02 BondPricing-0.0.1-SNAPSHOT.jar
-rw-rw-r--. 1 opc opc  20M Sep 19 19:02 BondPricing-0.0.1-SNAPSHOT-exec.jar
-rwxrwxr-x. 1 opc opc  65M Sep 19 19:11 BondPricing
-rw-rw-r--. 1 opc opc   26 Sep 19 19:11 BondPricing.build_artifacts.txt
[opc@enono-workstation-01 BondPricing]$
```

#### Optimizing package size
Package size can be optimized using upx 

```sh
$curl --location --output upx-3.96-amd64_linux.tar.xz "https://github.com/upx/upx/releases/download/v3.96/upx-3.96-amd64_linux.tar.xz"
$tar -xJf upx-3.96-amd64_linux.tar.xz
$sudo mv upx-3.96-amd64_linux/upx /bin/
```


Reduce the package size with upx util

```sh 
$ [opc@enono-workstation-01 target]$ upx -o BondPricing.upx BondPricing
                       Ultimate Packer for eXecutables
                          Copyright (C) 1996 - 2020
UPX 3.96        Markus Oberhumer, Laszlo Molnar & John Reiser   Jan 23rd 2020

        File size         Ratio      Format      Name
   --------------------   ------   -----------   -----------
  67348096 ->  22634004   33.61%   linux/amd64   BondPricing.upx

Packed 1 file.
```
Native image reduce from <b>65M</b> to <b>22M</b>


### Embark the compressed native image in the container
Build a new docker image

```sh
$ docker build -f docker/Dockerfile.native.basic -t nelvadas/bondpricing:1.0-native-basic .
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
STEP 1/4: FROM docker.io/oraclelinux:8-slim
STEP 2/4: EXPOSE 8080
--> Using cache 8981b8fbcafd16e8c6094ffd0a1f965e95ab741b4309d95a12e88d95c20dd961
--> 8981b8fbcaf
STEP 3/4: COPY ../target/BondPricing.upx ./app
--> Using cache c7b5c603c83ce1d401f00656eebaee6f735ad03d3f7e99ad8b5e99aac75114f3
--> c7b5c603c83
STEP 4/4: ENTRYPOINT ["/app"]
--> Using cache b16f2016d9be5477ec7d665bb0ed7ba65815de6323d3f5d6bfc83cd4776f7be6
COMMIT nelvadas/bondpricing:1.0-native-basic
--> b16f2016d9b
Successfully tagged localhost/nelvadas/bondpricing:1.0-native-basic
```

Run a container built on top of Native Image executable 

```sh 
[opc@enono-workstation-01 BondPricing]$ docker run -d -p 7070:8080 nelvadas/bondpricing:1.0-native-basic
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
c91348f823e5de9b44b93f7e8539585fa62891d90cfa949e6193481c5a672afa
[opc@enono-workstation-01 BondPricing]$ docker ps
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
CONTAINER ID  IMAGE                                            COMMAND     CREATED        STATUS            PORTS                   NAMES
c91348f823e5  localhost/nelvadas/bondpricing:1.0-native-basic              2 seconds ago  Up 3 seconds ago  0.0.0.0:7070->8080/tcp  epic_leavitt
[opc@enono-workstation-01 BondPricing]$ docker logs c9
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
2022-09-19 19:25:29.243  INFO 1 --- [           main] o.s.nativex.NativeListener               : AOT mode enabled

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.3)

2022-09-19 19:25:29.244  INFO 1 --- [           main] c.o.graalvm.demo.BondPricingApplication  : Starting BondPricingApplication v0.0.1-SNAPSHOT using Java 11.0.16 on c91348f823e5 with PID 1 (/app started by root in /)
2022-09-19 19:25:29.244  INFO 1 --- [           main] c.o.graalvm.demo.BondPricingApplication  : No active profile set, falling back to 1 default profile: "default"
2022-09-19 19:25:29.254  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2022-09-19 19:25:29.254  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2022-09-19 19:25:29.254  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.65]
2022-09-19 19:25:29.257  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2022-09-19 19:25:29.257  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 13 ms
2022-09-19 19:25:29.259  WARN 1 --- [           main] i.m.c.i.binder.jvm.JvmGcMetrics          : GC notifications will not be available because MemoryPoolMXBeans are not provided by the JVM
2022-09-19 19:25:29.282  INFO 1 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint(s) beneath base path '/actuator'
2022-09-19 19:25:29.284  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2022-09-19 19:25:29.284  INFO 1 --- [           main] c.o.graalvm.demo.BondPricingApplication  : Started BondPricingApplication in 0.048 seconds (JVM running for 0.049)
```
Application start very fast. <b>~ (0,048seco~d)</b>

The package size produced with the native image build is leaner

```sh 
[opc@enono-workstation-01 BondPricing]$ docker images | grep bond
localhost/nelvadas/bondpricing                1.0-jit-temurin-11.0.16  6adc009e52ad  46 seconds ago  960 MB
localhost/nelvadas/bondpricing                1.0-native-basic         b16f2016d9be  4 minutes ago   130 MB
[opc@enono-workstation-01 BondPricing]$
```

The docker images are available as packages on the current repository 
```sh
docker pull ghcr.io/nelvadas/bondpricing:1.0-native-basic
docker pull ghcr.io/nelvadas/bondpricing:1.0-jit-temurin-11.0.16
```

### Graphana Live Metrics dashboard
:todo:





# More Readings and Workshops

- [GraalVM HelloWorld](https://github.com/nelvadas/graalvm-helloworld-nativeimage)
- [GraalVM Native Image](https://github.com/nelvadas/Native-Image-Workshop)
- [GraalVM & Spring Boot Native Image Workshop](https://github.com/nelvadas/GraalVM-SpringBoot-Labs)
- [GraalVM Polyglot Workshop](https://github.com/nelvadas/GraalVM-Polyglot-Labs)
- [Accelerating Apache Spark with GraalVM](https://github.com/nelvadas/spark-with-graalvm)
- [GraalVM and Serverless](https://github.com/nelvadas/graalvm-serverless)
- [Accelerating Weblogic with GraalVM](https://github.com/nelvadas/graalvm-weblogic-jaxrs-demo)

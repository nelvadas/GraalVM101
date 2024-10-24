# GraalVM Workshop

This document provides general guidance and describe the labs to be completed individually by participants during the GraalVM Workshop.
A couple of questions were left intentionally without solution for you to challenge yourself!



## Agenda
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Part1: Getting Started With GraalVM](#part1-getting-started-with-graalvm)
     <ol>
    <li><a href="#lab-01-graalvm-installation">Lab01: Installation</a></li>
    <li><a href="#lab02---getting-started-with-graalvm-jit">Lab02: Getting Started with GraalJIT</a></li>
    <li><a href="#lab03---getting-started-with-graalvm-aot">Lab03: GraalVM AOT</a></li>
    </ol>
3. [Part2: Advanced Microservices with GraalVM](#part-2--advanceed-microservices-with-graalvm-technology)
     <ol>
    <li><a href="#lab05---microservices-with-graalvm-jit--aot-benchmarks">Lab05 - Microservices with GraalVM JIT & AOT Benchmarks</a></li>
    <li><a href="#lab06---native-imade-deep-dive---from-dynamic-to-fully-static-images"> Lab06 - Native Imade Deep Dive - From Dynamic to Fully Static Images</a></li>
    <li><a href="#lab07---monitoring-and-performance-with-jfrjmc">Lab07 - Monitoring and Performance with JFR/JMC</a></li>

    <li><a href="#lab08---multicloud-app-with-gdk"> Lab08 - Multicloud App with GDK</a></li>
    </ol>

4. [Quiz](#final-quiz)

## Introduction 


## Prerequisites
<i>Duration</i>: 30 min

<i>Objective</i>: Prepare your workstation to launch GraalVM rockets! :rocket


Please make sure you completed the following requirements.
* Access to a workstation : Windows/Linux/Mac OS  - Linux  x86/ARM Recommended
* A Terminal/Command line
* Basic Java Development Experience
* Tooling:
    <ul>
     <li>Maven 3.6+</li>
     <li>SDKMan</li>
     <li>Apache Benchmark</li>
     <li>Your Favorite code Editor - or  Visual Studio Code :smile </li>
     <li>Podman/Docker for runing containers </li>
</ul>



## Part 1: Getting Started With GraalVM

### Lab 01:  GraalVM Installation 
<i>Objective</i>: Setup Oracle GraalVM <br>
<i>Duration</i>: 15 min

1. Download SDK from [sdkman.io](sdkman.io])
2. Download and install Oracle GraalVM on your workstation using SDKman: 
    `sdk install java 23-graal`

3. Set `23-graal` as the default Java version : 
`sdk use java 23-graal`
4. Explore the installation directory of GraalVM:
`ls -rtl $JAVA_HOME`
5. What can you see ? compare GraalVM installation directory's content to a regular Oracle JDK / Open JDK distribution ?  ` use sdk install java 23-oracle` to setup Oracle JDK.
6. There is a community Edition of GraalVM available with tag `<version>-graalce` . Download the community Edition of GraalVM. What are the main differences between Oracle GraalVM ( formerly GraalEE/ Graal Enterprise) and GraalCE?


### Lab02 - Getting Started With GraalVM JIT
<i>Objective</i>: Getting Started with GraalJIT compiler <br>
<i>Duration</i>: 15 min

A JIT compiler purpose is to translate bytecode to executable machine Code.

1. Clone the [hello-graal](https://github.com/nelvadas/hello-graal.git) from [https://github.com/nelvadas/hello-graal.git](https://github.com/nelvadas/hello-graal.git) .

2. Explore the source code, Build the `/helloworld-1.0-SNAPSHOT.jar` using : `mvn clean package ` command

3. Check the bytecode generated for `com.oracle.graalvm.App.class`  

```sh
$javap  -c target/classes/com/oracle/graalvm/App.class
Compiled from "App.java"
public class com.oracle.graalvm.App {
  public com.oracle.graalvm.App();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #13                 // String Hello World!
       5: invokevirtual #15                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
}
```
To execute the hello-graal application, The JVM needs to convert this bytecode into executable machine code. 
This task is accomplished by the JIT Compiler.

4. Execute the hello-graal App class with the traditional C2 JIT Compiler from Open JDK 21
```sh
$sdk use java 21.0.2-open

$java -showversion  -cp target/helloworld-1.0-SNAPSHOT.jar com.oracle.graalvm.App
openjdk version "21.0.2" 2024-01-16
OpenJDK Runtime Environment (build 21.0.2+13-58)
OpenJDK 64-Bit Server VM (build 21.0.2+13-58, mixed mode, sharing)
Hello World!
```

GraalVM introduces a new JIT Compiler called GraalJIT
written in Java .

5. Run the helloworld application with GraalJIT compiler 
```sh
$sdk use java 23-graal
java -showversion  -cp target/helloworld-1.0-SNAPSHOT.jar com.oracle.graalvm.App
java version "23" 2024-09-17
Java(TM) SE Runtime Environment Oracle GraalVM 23+37.1 (build 23+37-jvmci-b01)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 23+37.1 (build 23+37-jvmci-b01, mixed mode, sharing)
Hello World!
```

GraalJIT comes alongs with Hotspot C2 Compiler. You can disable the GraalJIT compiler with option `-XX:-UseJVMCICompiler`
```bash
$java  -XX:-UseJVMCICompiler -showversion  -cp target/helloworld-1.0-SNAPSHOT.jar com.oracle.graalvm.App
java version "23" 2024-09-17
Java(TM) SE Runtime Environment Oracle GraalVM 23+37.1 (build 23+37-jvmci-b01)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 23+37.1 (build 23+37-jvmci-b01, mixed mode, sharing)
Hello World!
``` 

6. How would you print inthe logs the ref/details of the JIT compiler executing your application?



### Lab03 - Getting Started With GraalVM AOT
<i>Objective</i><ul>
<li>Familiarize yourself with Native Image Technology </li>
<li>Build an AOT basic java application</li>
<li>Play with native-image command</li>
</ul>
<i>Duration</i>: 30 min

GraalVM introduces the AOT(Ahead of Time ) compilation for java applications to produce binary that run on target environment whitout the requirement of a JVM.
GraalVM includes a builder `native-image` that package java application( class, jar, ...)
In early GraalVM releases, native image was provided a part from the graalvm core binary along with an utility call `gu` to  install it .


1. Ensure your java 23-graal contains a native image command.
```bash
$ which native-image
/Users/enono/.sdkman/candidates/java/23-graal/bin/native-image
```

2. Grap the native image version.
```bash
$ native-image -version
native-image 23 2024-09-17
GraalVM Runtime Environment Oracle GraalVM 23+37.1 (build 23+37-jvmci-b01)
Substrate VM Oracle GraalVM 23+37.1 (build 23+37, serial gc, compressed references)
```

3. Build the hello-graal to a native application 
```sh
native-image -cp target/*.jar com.oracle.graalvm.App hello
========================================================================================================================
GraalVM Native Image: Generating 'hello' (executable)...
========================================================================================================================
[1/8] Initializing...                                                                                    (8,7s @ 0,17GB)
 Java version: 23+37, vendor version: Oracle GraalVM 23+37.1
 Graal compiler: optimization level: 2, target machine: armv8-a, PGO: ML-inferred
 C compiler: cc (apple, arm64, 15.0.0)
 Garbage collector: Serial GC (max heap size: 80% of RAM)
 1 user-specific feature(s):
 - com.oracle.svm.thirdparty.gson.GsonFeature
------------------------------------------------------------------------------------------------------------------------
Build resources:
 - 26,49GB of memory (73,6% of 36,00GB system memory, determined at start)
 - 11 thread(s) (100,0% of 11 available processor(s), determined at start)
[2/8] Performing analysis...  [*****]                                                                    (3,0s @ 0,25GB)
    1 985 reachable types   (57,5% of    3 453 total)
    1 769 reachable fields  (38,2% of    4 631 total)
    8 548 reachable methods (35,2% of   24 316 total)
      730 types,     7 fields, and    90 methods registered for reflection
       49 types,    33 fields, and    48 methods registered for JNI access
        4 native libraries: -framework Foundation, dl, pthread, z
[3/8] Building universe...                                                                               (0,5s @ 0,31GB)
[4/8] Parsing methods...      [*]                                                                        (0,9s @ 0,22GB)
[5/8] Inlining methods...     [***]                                                                      (0,4s @ 0,28GB)
[6/8] Compiling methods...    [***]                                                                      (7,9s @ 0,31GB)
[7/8] Laying out methods...   [*]                                                                        (0,7s @ 0,34GB)
[8/8] Creating image...       [*]                                                                        (0,7s @ 0,39GB)
   2,83MB (44,14%) for code area:     3 861 compilation units
   3,38MB (52,73%) for image heap:   52 750 objects and 86 resources
 204,88kB ( 3,13%) for other data
   6,40MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 origins of code area:                                Top 10 object types in image heap:
   1,31MB svm.jar (Native Image)                             671,80kB byte[] for java.lang.String
   1,25MB java.base                                          660,97kB byte[] for code metadata
  81,66kB com.oracle.svm.svm_enterprise                      428,11kB heap alignment
  36,83kB jdk.proxy2                                         357,02kB java.lang.String
  33,46kB org.graalvm.nativeimage.base                       320,09kB java.lang.Class
  28,33kB jdk.graal.compiler                                 147,31kB java.util.HashMap$Node
  27,14kB jdk.proxy1                                         114,52kB char[]
  23,05kB org.graalvm.collections                             93,05kB com.oracle.svm.core.hub.DynamicHubCompanion
  15,07kB jdk.internal.vm.ci                                  84,88kB java.lang.Object[]
   8,77kB jdk.proxy3                                          80,83kB byte[] for reflection metadata
   1,10kB for 4 more packages                                497,41kB for 513 more object types
                            Use '--emit build-report' to create a report with more details.
------------------------------------------------------------------------------------------------------------------------
Security report:
 - Binary includes Java deserialization.
 - Use '--enable-sbom' to assemble a Software Bill of Materials (SBOM).
------------------------------------------------------------------------------------------------------------------------
Recommendations:
 PGO:  Use Profile-Guided Optimizations ('--pgo') for improved throughput.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
 QBM:  Use the quick build mode ('-Ob') to speed up builds during development.
------------------------------------------------------------------------------------------------------------------------
                        0,7s (2,5% of total time) in 240 GCs | Peak RSS: 0,89GB | CPU load: 3,60
------------------------------------------------------------------------------------------------------------------------
Build artifacts:
 /Users/enono/Workspace/Java/.../hello-graal/hello (executable)
========================================================================================================================
Finished generating 'hello' in 26,7s.
```

4. Run the native application produced.

```sh 
ls -rtlh
total 13144
-rw-r--r--@  1 enono  staff   943B 26 sep 13:28 graalvm-helloworld-nativeimage.iml
drwxr-xr-x@  4 enono  staff   128B 26 sep 13:40 src
-rw-r--r--@  1 enono  staff   948B 27 sep 13:08 pom.xml
drwxr-xr-x@  3 enono  staff    96B  1 oct 14:08 images
-rw-r--r--@  1 enono  staff   5,5K  1 oct 14:13 README.md
drwxr-xr-x@ 10 enono  staff   320B 23 oct 13:43 target
-rwxr-xr-x@  1 enono  staff   6,4M 23 oct 15:10 hello
```

The `./hello` does not require a JVM on your workstation to start.
it contains everything ( dependencies, jdk class, libs, class ...)
```bash
./hello
Hello World!
```
5. How would you accelerate the build time of your native images
```bash 
native-image -Ob -cp target/*.jar com.oracle.graalvm.App hello1
```
Option `-Ob` => Finished generating 'hello1' in 16,2s.


6. How would you give priority on the image size optimisation during  the build Process
```bash 
native-image -Os -cp target/*.jar com.oracle.graalvm.App hello2
```
Option `-Os` => Finished generating 'hello1' in 17,2s.

Check the different file size
```bash
ls -rtlh
total 33984
-rw-r--r--@  1 enono  staff   943B 26 sep 13:28 graalvm-helloworld-nativeimage.iml
drwxr-xr-x@  4 enono  staff   128B 26 sep 13:40 src
-rw-r--r--@  1 enono  staff   948B 27 sep 13:08 pom.xml
drwxr-xr-x@  3 enono  staff    96B  1 oct 14:08 images
-rw-r--r--@  1 enono  staff   5,5K  1 oct 14:13 README.md
drwxr-xr-x@ 10 enono  staff   320B 23 oct 13:43 target
-rwxr-xr-x@  1 enono  staff   6,4M 23 oct 15:18 hello
-rwxr-xr-x@  1 enono  staff   5,4M 23 oct 15:18 hello1
-rwxr-xr-x@  1 enono  staff   4,8M 23 oct 15:20 hello2
```

7. Generate a build report
```sh
$ native-image --emit build-report  -cp target/*.jar com.oracle.graalvm.App hello3
```
Open the [hello3-build-report.html](./hello3-build-report.html) generated file.

8. What is the default GC used in native images? 

9. How would you instruct native image to use the EpsilonGC while generating native image for your benchmarks?

```bash
native-image --gc=epsilon --emit build-report  -cp target/*.jar com.oracle.graalvm.App hello4
```
10. How much code Area/ Image Heap  is  occupied by the SerialGC components?






### Lab04- GraalVM PolyGlot
<i>Objective</i><ul>
<li>Getting started with GraalVM Polyglot Features</li>
<li>Build a Polyglot Java/Python application</li>
<li>Leverage GraalVM Context and Polyglots Bindings</li>
</ul>
<i>Duration</i>: 45 min


## Part 2 : Advanceed Microservices with GraalVM Technology




### Lab05 - Microservices with GraalVM JIT & AOT Benchmarks
<i>Objective</i><ul>
<li>Build Microservices with GraalVM Technology</li>
<li>Benchmark and Track Micro Service Peformance</li>
<li>Improve Microservices performance with GraalVM AOT</li>
</ul>
<i>Duration</i>: 55 min
Follow instructions in [MnBondPricing/README.md](./MnBondPricing/README.md)


### Lab06 - Native Imade Deep Dive - From Dynamic to Fully Static Images

<i>Objective</i><ul>
<li>Play with GraalVM Container Images</li>
<li>Build Efficient Container Images with GraalVM Technology</li>
<li>Optimize Containers images size Packages</li>
</ul>
<i>Duration</i>: 55 min

1. Build a Fully Dynamic Image of the MnBondPricing Application 
2. Build a Mostly Static Image of the MnBondPricing Application 
3. Build a Fully Static Image of the MnBondPricing Application  
4. Which is the best one for your container deployments?


### Lab07 - Monitoring and Performance with JFR/JMC

Live Labs

### Lab08 - Multicloud App with GDK

[https://graal.cloud/gdk/hands-on-labs/](https://graal.cloud/gdk/hands-on-labs/)

## Final Quiz

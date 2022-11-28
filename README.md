# GraalVM 101 - Spring Boot + Micronaut
GraalVM 101: <i>Practical Workshop to Get Started with GraalVM Enterprise Edition.</i>


<b> Table of Contents</b>

 1. [Install GraalVM EE](#installing-graalvm-ee)
 2. [Creating a simple Spring Boot/Micronaut Bond Princing API](#bond-pricing-spring-boot-api)
 3. [GraalVM JIT to boost Throughputs](#performance-boot-with-graalvm-jit-compiler)
 4. [GraalVM Native Image : Faster,Leaner ](#performance-boot-with-graalvm-nativeimage)
 5. [Cloud Native Deployments with GraalVM Native Image](#cloud-native-devployment)
 6. [Micronaut with GraalVM Native Image](./02-micronaut/)

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

![Bond Valuation](./01-springboot//images/PV%20Value.png)
Where: 
* `PV` is the Selling price/Fair Value to compute 
* `C` is a coupon, periodic interest received by the lender.
`C = Contractual  * Face Value of the bond `
* `r` is the market yield to maturity
* `T` represents the number of payment to received/ years( Maturity Term)
* [More about the formular](https://www.simtrade.fr/blog_simtrade/how-compute-present-value-asset/)


You can either build this API with SpringBoot or Micronaut by following the links bellow.
1. [BondPricing API with SpringBoot and GraalVM](./01-springboot/)

2. [BondPricing API with Micronaut  and GraalVM](./02-micronaut/)





# More Readings and Workshops

- [GraalVM HelloWorld](https://github.com/nelvadas/graalvm-helloworld-nativeimage)
- [GraalVM Native Image](https://github.com/nelvadas/Native-Image-Workshop)
- [GraalVM & Spring Boot Native Image Workshop](https://github.com/nelvadas/GraalVM-SpringBoot-Labs)
- [GraalVM Polyglot Workshop](https://github.com/nelvadas/GraalVM-Polyglot-Labs)
- [Accelerating Apache Spark with GraalVM](https://github.com/nelvadas/spark-with-graalvm)
- [GraalVM and Serverless](https://github.com/nelvadas/graalvm-serverless)
- [Accelerating Weblogic with GraalVM](https://github.com/nelvadas/graalvm-weblogic-jaxrs-demo)

package com.oracle.graalvm.demo;

import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micronaut.configuration.metrics.micrometer.prometheus.PrometheusMeterRegistryFactory;
import io.micronaut.runtime.Micronaut;


public class Application {


    public static void main(String[] args) {
;        Micronaut.run(Application.class, args);
    }
}

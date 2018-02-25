# fR24feed Processor

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Build Status](https://api.travis-ci.org/codebrewer/fr24feed-processor.svg?branch=develop)](http://travis-ci.org/codebrewer/fr24feed-processor)

## About

**fr24feed Processor** is a Java application to "process" data generated by *fr24feed*, the application provided by
[Flightradar24](https://www.flightradar24.com) to [feed ADS-B data](https://www.flightradar24.com/build-your-own) into
their network. What "process" might mean isn't yet clear...

## Motivation

An ADS-B receiver has the potential (depending on location and equipment used) to receive many hundreds of messages per
second broadcast from nearby aircraft - an interesting domain and an ideal datasource for playing with/learning about
the processing of streaming data. There's a vague idea of deploying the application in a Docker container on a Raspberry
Pi.

## Requirements

* JDK 1.8
* (Optional) A PostgreSQL database server with various PostGIS extensions if persistence of ADS-B messages is enabled

## Building and Testing

Dependency management and building are handled by Gradle, and the Gradle wrapper is provided.

```bash
./gradlew build
```

```bat
gradlew.bat build
```

The build generates a 'fat' jar.

There are currently no tests on the `master` branch :blush:

## Usage

The application's main class is `org.codebrewer.fr24feedprocessor.Fr24feedProcessorApplication`.

At present the application can parse messages in the "SBS-1 BaseStation" format and (optionally) persist them to a
PostgreSQL database (chosen to have a play with the location/mapping objects provided by the PostGIS extension).

## Application Monitoring and Runtime Control

Various managed attributes and operations are exposed to JMX and can therefore be viewed and changed using a client such
as `jconsole`. The Spring Boot Actuator is/can be enabled and provides a large number of informational REST endpoints.

## Acknowledgments

The **fr24feed Processor** project uses [Spring Boot](https://projects.spring.io/spring-boot/) and is built by
[Gradle](https://gradle.org). It optionally persists data to a [PostgreSQL](https://www.postgresql.org) database that
has the [PostGIS](https://postgis.net) extensions installed.

**fr24feed Processor** is not associated with Flightradar24 in any way.

## Licensing

This software is licensed under the Apache License Version 2.0. Please see [LICENSE](LICENSE) for details.

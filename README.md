# weather-java
[![Build Status](https://travis-ci.org/ideaqe/weather-java.svg?branch=develop)](https://travis-ci.org/ideaqe/weather-java)

Regarding API see the [ideaqe/weather](https://github.com/ideaqe/weather) repository.

## Build

Build and run the project via Gradle:
```bash
# build project
$ gradle clean build

# task for creating fat Jar file
$ gradle createJar

# executing fat jar (runs service on port 8080)
$ java -jar build/libs/weather-all-0.1.0-SNAPSHOT.jar
```


          ____        _           ___         ____
         |  _ \ _   _| |___  ___ ( _ )       / ___|___  _ __ ___
         | |_) | | | | / __|/ _ \/ _ \ _____| |   / _ \| '__/ _ \
         |  __/| |_| | \__ \  __/ (_) |_____| |__| (_) | | |  __/
         |_|    \__,_|_|___/\___|\___/       \____\___/|_|  \___|

# Pulse8 Core Access Control

## Scripts

### Build

`./mvnw clean install -DskipTests`

### Build and test

`./mvnw clean install`

### Run without debug

`./mvnw clean spring-boot:run`

### Run with debug

`./mvnw clean spring-boot:run -Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010`

### Build and build Dockerfile with Google Jib

`./compile.sh`

### Build and build Dockerfile with Google Jib and deploy docker image

`./deploy.sh`

### Shutdown docker image and remove created volumes

`./shutdown.sh`

## Available endpoints

- /
 


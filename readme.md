# REST version of Spring eprescribing Sample Application (spring-framework-eprescribing extend ) 

[![Java Build Status](https://github.com/spring-eprescribing/spring-eprescribing-rest/actions/workflows/maven-build.yml/badge.svg)](https://github.com/spring-eprescribing/spring-eprescribing-rest/actions/workflows/maven-build.yml)
[![Docker Build Status](https://github.com/spring-eprescribing/spring-eprescribing-rest/actions/workflows/docker-build.yml/badge.svg)](https://github.com/spring-eprescribing/spring-eprescribing-rest/actions/workflows/docker-build.yml)

This backend version of the Spring eprescribing application only provides a REST API. **There is no UI**.
The [spring-eprescribing-angular project](https://github.com/spring-eprescribing/spring-eprescribing-angular) is a Angular front-end application which consumes the REST API.

## Understanding the Spring eprescribing application with a few diagrams

[See the presentation of the Spring eprescribing Framework version](http://fr.slideshare.net/AntoineRey/spring-framework-eprescribing-sample-application)

### eprescribing ER Model

![alt eprescribing-ermodel](eprescribing-ermodel.png)

## Running eprescribing locally

### With maven command line
```
git clone https://github.com/spring-eprescribing/spring-eprescribing-rest.git
cd spring-eprescribing-rest
./mvnw spring-boot:run
```

### With Docker
```
docker run -p 9966:9966 springcommunity/spring-eprescribing-rest
```

You can then access eprescribing here: [http://localhost:9966/eprescribing/](http://localhost:9966/eprescribing/)

There are actuator health check and info routes as well: 
* [http://localhost:9966/eprescribing/actuator/health](http://localhost:9966/eprescribing/actuator/health)
* [http://localhost:9966/eprescribing/actuator/info](http://localhost:9966/eprescribing/actuator/info)

## OpenAPI REST API documentation presented here (after application start):

You can reach the swagger UI with this URL
[http://localhost:9966/eprescribing/](http://localhost:9966/eprescribing/swagger-ui.html).

You then can get the Open API description reaching this URL [localhost:9966/eprescribing/v3/api-docs](localhost:9966/eprescribing/v3/api-docs).

## Screenshot of the Angular client

<img width="1427" alt="spring-eprescribing-angular2" src="https://cloud.githubusercontent.com/assets/838318/23263243/f4509c4a-f9dd-11e6-951b-69d0ef72d8bd.png">

## In case you find a bug/suggested improvement for Spring eprescribing
Our issue tracker is available here: https://github.com/spring-eprescribing/spring-eprescribing-rest/issues


## Database configuration

In its default configuration, eprescribing uses an in-memory database (HSQLDB) which
gets populated at startup with data.
A similar setups is provided for MySql and PostgreSQL in case a persistent database configuration is needed.
To run eprescribing locally using persistent database, it is needed to change profile defined in application.properties file.

For MySQL database, it is needed to change param "hsqldb" to "mysql" in string
```
spring.profiles.active=hsqldb,spring-data-jpa
```
 defined in application.properties file.

Before do this, would be good to check properties defined in application-mysql.properties file.

```
spring.datasource.url = jdbc:mysql://localhost:3306/eprescribing?useUnicode=true
spring.datasource.username=pc
spring.datasource.password=eprescribing 
spring.datasource.driver-class-name=com.mysql.jdbc.Driver 
spring.jpa.database=MYSQL
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=none
```      

You may also start a MySql database with docker:

```
docker run --name mysql-eprescribing -e MYSQL_ROOT_PASSWORD=eprescribing -e MYSQL_DATABASE=eprescribing -p 3306:3306 mysql:5.7.8
```

For PostgeSQL database, it is needed to change param "hsqldb" to "postgresql" in string
```
spring.profiles.active=hsqldb,spring-data-jpa
```
 defined in application.properties file.

Before do this, would be good to check properties defined in application-postgresql.properties file.

```
spring.datasource.url=jdbc:postgresql://localhost:5432/eprescribing
spring.datasource.username=postgres
spring.datasource.password=eprescribing
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database=POSTGRESQL
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=none
```
You may also start a Postgres database with docker:

```
docker run --name postgres-eprescribing -e POSTGRES_PASSWORD=eprescribing -e POSTGRES_DB=eprescribing -p 5432:5432 -d postgres:9.6.0
```
## API First Approach

This API is built following some [API First approach principles](https://swagger.io/resources/articles/adopting-an-api-first-approach/).

It is specified through the [OpenAPI](https://oai.github.io/Documentation/).
It is specified in this [file](./src/main/resources/openapi.yml).

Some of the required classes are generated during the build time. 
Here are the generated file types:
* DTOs
* API template interfaces specifying methods to override in the controllers

To see how to get them generated you can read the next chapter. 

## Generated code

Some of the required classes are generated during the build time using maven or any IDE (e.g., IntelliJ Idea or Eclipse).

All of these classes are generated into the ``target/generated-sources`` folder.

Here is a list of the generated packages and the corresponding tooling:

| Package name                                   | Tool             |
|------------------------------------------------|------------------|
| org.springframework.labs.eprescribing.mapper   | [MapStruct](https://mapstruct.org/)        |
| org.springframework.labs.eprescribing.rest.dto | [OpenAPI Generator maven plugin](https://github.com/OpenAPITools/openapi-generator/) |


To get both, you have to run the following command:

```jshelllanguage
mvn clean install
```

## Security configuration
In its default configuration, eprescribing doesn't have authentication and authorization enabled.

### Basic Authentication
In order to use the basic authentication functionality, turn in on from the application.properties file
```
eprescribing.security.enable=true
```
This will secure all APIs and in order to access them, basic authentication is required.
Apart from authentication, APIs also require authorization. This is done via roles that a user can have.
The existing roles are listed below with the corresponding permissions 
* OWNER_ADMIN -> OwnerController, PetController, PetTypeController (getAllPetTypes and getPetType), VisitController
* VET_ADMIN   -> PetTypeController, SpecialityController, VetController
* ADMIN       -> UserController

There is an existing user with the username `admin` and password `admin` that has access to all APIs.
 In order to add a new user, please use the following API:
```
POST /api/users
{
    "username": "secondAdmin",
    "password": "password",
    "enabled": true,
    "roles": [
    	{ "name" : "OWNER_ADMIN" }
	]
}
```

## Working with eprescribing in Eclipse/STS

### prerequisites
The following items should be installed in your system:
* Maven 3 (https://maven.apache.org/install.html)
* git command line tool (https://help.github.com/articles/set-up-git)
* Eclipse with the m2e plugin (m2e is installed by default when using the STS (http://www.springsource.org/sts) distribution of Eclipse)

Note: when m2e is available, there is an m2 icon in Help -> About dialog.
If m2e is not there, just follow the install process here: http://eclipse.org/m2e/download/
* Eclipse with the [mapstruct plugin](https://mapstruct.org/documentation/ide-support/) installed.

### Steps:

1) In the command line
```
git clone https://github.com/spring-eprescribing/spring-eprescribing-rest.git
```
2) Inside Eclipse
```
File -> Import -> Maven -> Existing Maven project
```


## Looking for something in particular?

| Layer | Source |
|--|--|
| REST API controllers | [REST folder](src/main/java/org/springframework/labs/eprescribing/rest) |
| Service | [ClinicServiceImpl.java](src/main/java/org/springframework/labs/eprescribing/service/ClinicServiceImpl.java) |
| JDBC | [jdbc folder](src/main/java/org/springframework/labs/eprescribing/repository/jdbc) |
| JPA | [jpa folder](src/main/java/org/springframework/labs/eprescribing/repository/jpa) |
| Spring Data JPA | [springdatajpa folder](src/main/java/org/springframework/labs/eprescribing/repository/springdatajpa) |
| Tests | [AbstractClinicServiceTests.java](src/test/java/org/springframework/labs/eprescribing/service/clinicService/AbstractClinicServiceTests.java) |


## Publishing a Docker image

This application uses [Google Jib]([https://github.com/GoogleContainerTools/jib) to build an optimized Docker image
into the [Docker Hub](https://cloud.docker.com/u/springcommunity/repository/docker/springcommunity/spring-eprescribing-rest/)
repository.
The [pom.xml](pom.xml) has been configured to publish the image with a the `springcommunity/spring-eprescribing-rest`image name.

Command line to run:
```
mvn compile jib:build -X -DjibSerialize=true -Djib.to.auth.username=xxx -Djib.to.auth.password=xxxxx
```

## Interesting Spring eprescribing forks

The Spring eprescribing master branch in the main [spring-projects](https://github.com/spring-projects/spring-eprescribing)
GitHub org is the "canonical" implementation, currently based on Spring Boot and Thymeleaf.

This [spring-eprescribing-rest](https://github.com/spring-eprescribing/spring-eprescribing-rest/) project is one of the [several forks](https://spring-eprescribing.github.io/docs/forks.html) 
hosted in a special GitHub org: [spring-eprescribing](https://github.com/spring-eprescribing).
If you have a special interest in a different technology stack
that could be used to implement the Pet Clinic then please join the community there.


# Contributing

The [issue tracker](https://github.com/spring-eprescribing/spring-eprescribing-rest/issues) is the preferred channel for bug reports, features requests and submitting pull requests.

For pull requests, editor preferences are available in the [editor config](https://github.com/spring-eprescribing/spring-eprescribing-rest/blob/master/.editorconfig) for easy use in common text editors. Read more and download plugins at <http://editorconfig.org>.





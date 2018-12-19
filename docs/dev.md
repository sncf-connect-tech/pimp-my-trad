# Installation and development

## Building

The backend uses [Maven](https://maven.apache.org/). Just tweak some settings before building (the [sample file](/back/pimp-my-trad-api/src/main/resources/application.tmpl.yml) is mostly empty); currently, the app requires adding Github/Gitlab/etc credentials with write access to the repositories in the configuration file. 

```
$ cp back/pimp-my-trad-api/src/main/resources/application.tmpl.yml back/pimp-my-trad-api/src/main/resources/application.yml
$ mvn package
```

Alternatively, with Docker:

```
docker run --rm -v "$PWD/back":/usr/src/app -v "$HOME/.m2":/root/.m2 -v "$PWD/back/pimp-my-trad-api/target":/usr/src/app/target -w /usr/src/app maven mvn package
```

To build the frontend, just install the [NPM](https://www.npmjs.com/) dependencies and `run build`.

```
$ npm install
$ npm run build
```

Again, with Docker:

```
docker run --rm -v "$PWD/front":/usr/src/app -v "$PWD/front/build":/usr/src/app/build -w /usr/src/app node /bin/bash -c 'npm install && npm run build'
```

## Running

You may run the project using Docker and docker-compose for ease. A sample [docker-compose.yml](/docker-compose.yml) is included. Don't forget to include a proper configuration file before building, see [application-docker.tmpl.yml](/back/pimp-my-trad-api/src/main/resources/application-docker.tmpl.yml).

```
$ docker-compose up -d
```

## Development

During development, just `java -jar` the fat jar once it has been built. 

The frontend can be run using `npm start`.

You will also need a Mongo instance running.

## Testing

Maven should run some unit tests automatically.

"Behaviour" tests are available in the `bdd` package. A sample [docker-compose-test.yml](/docker-compose-test.yml) file is included to demonstrate how to run these tests.

```
docker-compose -f docker-compose-test.yml run --use-aliases bdd
```

The `bdd` module does the following:

* runs an embedded Jetty server that mocks a Git repository, with basic auth (expecting any username, **and "password" as the password**)
* connects to the backend (API) without authentication -- the user is **hardcoded** to be "test_user" when the API runs with the `test` profile
* runs tests by calling the API

Data will be inserted into your Mongo database but should be removed by the end of the tests.
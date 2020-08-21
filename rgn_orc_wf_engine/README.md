# Ragna ORC - DDD reactive  task orchestrator


```
                               ██▀███   ▄▄▄        ▄████  ███▄    █  ▄▄▄      
                              ▓██ ▒ ██▒▒████▄     ██▒ ▀█▒ ██ ▀█   █ ▒████▄    
                              ▓██ ░▄█ ▒▒██  ▀█▄  ▒██░▄▄▄░▓██  ▀█ ██▒▒██  ▀█▄  
                              ▒██▀▀█▄  ░██▄▄▄▄██ ░▓█  ██▓▓██▒  ▐▌██▒░██▄▄▄▄██ 
                              ░██▓ ▒██▒ ▓█   ▓██▒░▒▓███▀▒▒██░   ▓██░ ▓█   ▓██▒
                              ░ ▒▓ ░▒▓░ ▒▒   ▓▒█░ ░▒   ▒ ░ ▒░   ▒ ▒  ▒▒   ▓▒█░
                                ░▒ ░ ▒░  ▒   ▒▒ ░  ░   ░ ░ ░░   ░ ▒░  ▒   ▒▒ ░
                                ░░   ░   ░   ▒   ░ ░   ░    ░   ░ ░   ░   ▒   
                                 ░           ░  ░      ░          ░       ░  ░
                                                                                          
                                              /~     | |   ;~\                        ,;;;/;;'
                                             /|      | |   ;~\\                     ,;;;;/;;;'
                                            |/|      \_/   ;;;|\                    ,;;;;/;;;;'
                                            |/ \          ;;;/  )                 ,;;;;/;;;;;'
                                        ___ | ______     ;_____ |___....__      ,;;;;/;;;;;'
                                  ___.-~ \\(| \  \.\ \__/ /./ /:|)~   ~   \   ,;;;;/;;;;;'
                              /~~~    ~\    |  ~-.     |   .-~: |//  _.-~~--,;;;;/;;;;;'
                             (.-~___     \.'|    | /-.__.-\|::::| //~     ,;;;;/;;;;;'
                             /      ~~--._ \|   /          `\:: |/      ,;;;;/;;;;;'
                          .-|             ~~|   |  /V""""V\ |:  |     ,;;;;/;;;;;' \
                         /                   \  |  ~`^~~^'~ |  /    ,;;;;/;;;;;'    ;
                        (        \             \|`\._____./'|/    ,;;;;/;;;;;'      '\
                       / \        \                             ,;;;;/;;;;;'     /    |
                      |            |                          ,;;;;/;;;;;'      |     |


                                          ▒█████   ██▀███   ▄████▄
                                         ▒██▒  ██▒▓██ ▒ ██▒▒██▀ ▀█
                                         ▒██░  ██▒▓██ ░▄█ ▒▒▓█    ▄
                                         ▒██   ██░▒██▀▀█▄  ▒▓▓▄ ▄██▒
                                         ░ ████▓▒░░██▓ ▒██▒▒ ▓███▀ ░
                                         ░ ▒░▒░▒░ ░ ▒▓ ░▒▓░░ ░▒ ▒  ░
                                           ░ ▒ ▒░   ░▒ ░ ▒░  ░  ▒
                                         ░ ░ ░ ▒    ░░   ░ ░
                                             ░ ░     ░     ░ ░
                                                           ░





```



### Test Environment

### Transactions

See references.
Include profile ``spring_profiles_active=embedMongoWithTx``

### Swagger UI
http://localhost:8080/swagger-ui/index.html


### docker compose

```
$ docker-compose up
$ docker-compose restart orc_mongo_express
 
```


#### mongodb init
Mongo init script is not running in docker desktop. It should be run in a bash terminal.

```
$ docker-compose up --build orc_mongodb
$ winpty docker exec -it orc_mongodb bash

# cd docker-entrypoint-initdb.d/
# ./mongo-init.sh

```
#### Mongo Express

http://localhost:8888/


### Tips

**Forcing Reactor's Agent disabling**

In application startup params
```
-Dspring.reactor.debug-agent.enabled=false
```


## References

* MongoDB / Docker
    * [Gist - mongo replica set in docker compose](https://gist.github.com/harveyconnor/518e088bad23a273cae6ba7fc4643549)
    * [Mongo Express container with Docker Compose](https://zgadzaj.com/development/docker/docker-compose/containers/mongo-express)
    * [How to run MongoDB and Mongo-express with docker-compose?](https://stackoverflow.com/questions/47901561/how-to-run-mongodb-and-mongo-express-with-docker-compose)
    * [MongoDB container with Docker Compose](https://zgadzaj.com/development/docker/docker-compose/containers/mongodb#docker-mongodb-mongod-conf)
    * [Multi-project test dependencies with gradle](https://stackoverflow.com/questions/5644011/multi-project-test-dependencies-with-gradle/60138176#60138176)
    * [MongoDB Admin Commands - setFeatureCompatibilityVersion](https://docs.mongodb.com/manual/reference/command/setFeatureCompatibilityVersion/)
* Spring
    * [Spring Boot Auto-configuration for Embedded MongoDB with Support for Transactions](https://apisimulator.io/spring-boot-auto-configuration-embedded-mongodb-transactions/)
* Spring Webflux / Reactor
    * [Project Reactor expand method](https://www.javacodegeeks.com/2020/02/project-reactor-expand-method.html)
    * [How to use Processor in Java's Reactor](https://ducmanhphan.github.io/2019-08-25-How-to-use-Processor-in-Reactor-Java/#topicprocessor)
* Gradle
    * [easy reuse of test artifacts](https://stackoverflow.com/questions/5644011/multi-project-test-dependencies-with-gradle/60138176#60138176)
* Microservices / DDD
    * [Microservices Patterns: With examples in Java](https://www.amazon.com/Microservices-Patterns-examples-Chris-Richardson/dp/1617294543/ref=sr_1_1?crid=3M34XT81XSEAB&dchild=1&keywords=microservices+patterns&qid=1597881668&s=books&sprefix=microservices+p%2Caps%2C233&sr=1-1)
    * [Implementing Domain-Driven Design](https://www.amazon.com/Implementing-Domain-Driven-Design-Vaughn-Vernon-ebook/dp/B00BCLEBN8/ref=sr_1_2?crid=2HLI0BW7SN70O&dchild=1&keywords=domain+driven+design&qid=1597881752&s=books&sprefix=domain+%2Cstripbooks-intl-ship%2C250&sr=1-2)
* CDC
    * https://hub.docker.com/r/debezium/example-mongodb
    * https://github.com/debezium/docker-images/blob/master/examples/mongodb/0.10/init-inventory.sh
    * https://start-scs.cfapps.io/
    * https://docs.spring.io/spring-cloud-stream-app-starters/docs/Einstein.SR6/reference/htmlsingle/#spring-cloud-stream-modules-cdc-debezium-source
    * https://debezium.io/documentation/reference/1.2/development/engine.html
    * https://rmoff.net/2018/03/27/streaming-data-from-mongodb-into-kafka-with-kafka-connect-and-debezium/
* General
    * [Announcing Snowflake (twitter sequence generator)](https://blog.twitter.com/engineering/en_us/a/2010/announcing-snowflake.html)
    

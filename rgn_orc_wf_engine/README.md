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

### Swagger UI
http://localhost:8080/swagger-ui/index.html


### docker compose

```
$ docker-compose up 
```

#### mongodb init
```
$ winpty docker exec -it orc_mongodb mongo

# rs.initiate({_id: 'repDB', member: [{_id:0, host:'orc_mongodb', arbiterOnly:true}]})
# rs.slaveOk()
# rs.initiate()

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

* https://gist.github.com/harveyconnor/518e088bad23a273cae6ba7fc4643549
* [Mongo Express container with Docker Compose](https://zgadzaj.com/development/docker/docker-compose/containers/mongo-express)
* [How to run MongoDB and Mongo-express with docker-compose?](https://stackoverflow.com/questions/47901561/how-to-run-mongodb-and-mongo-express-with-docker-compose)
* [MongoDB container with Docker Compose](https://zgadzaj.com/development/docker/docker-compose/containers/mongodb#docker-mongodb-mongod-conf)

Get docker image
```docker pull postgres```
start image
```docker run --rm   --name pg-docker -e POSTGRES_PASSWORD=docker -d -p 5440:5432 -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data  postgres```
Note the password and port mapping (I already have postgress on its standard port)
Connect locally to the container and login with the password
```psql -h localhost -U postgres -d postgres```
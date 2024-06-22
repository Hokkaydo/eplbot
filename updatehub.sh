./gradlew shadowJar
docker build -t eplbot ./
docker image tag eplbot:latest hokkaydo/eplbot:latest
./build_code_docker.sh
docker image push hokkaydo/eplbot:latest
./gradlew shadowJar
docker build -t eplbot ./
docker build -t java-runner -f src/main/java/com/github/hokkaydo/eplbot/module/code/java/Dockerfile .
docker compose -f docker-compose.yml up
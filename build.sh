./gradlew shadowJar
docker build -t eplbot ./
docker compose -f docker-compose.yml up
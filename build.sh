./gradlew shadowJar
docker build -t eplbot --target local-build ./
#docker build -t eplbot --target profiler ./
docker compose -f docker-compose-local.yml up eplbot --remove-orphans --force-recreate
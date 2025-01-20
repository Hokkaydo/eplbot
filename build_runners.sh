LIST=$(ls -d src/main/java/com/github/hokkaydo/eplbot/module/code/*/)
for dir in $LIST
do
    DIRNAME=$(basename $dir)
    docker build -t $DIRNAME-runner -f $dir/Dockerfile .
    docker image tag $DIRNAME-runner:latest hokkaydo/$DIRNAME-runner:latest
done
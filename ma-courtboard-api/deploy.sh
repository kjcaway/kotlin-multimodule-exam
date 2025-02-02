# for deploy to server

echo "deploying to server start"

echo "gradle build..."
cd ..
gradlew :ma-courtboard-api:build
cd ma-courtboard-api

echo "docker build..."
docker build --platform linux/amd64 -t courtboard-api:0.9 .

echo "docker image to file..."
docker save -o ./courtboard-api.tar courtboard-api:0.9

echo "docker file to server with scp"
scp ./courtboard-api.tar 유저명@원격지:/home/유저명

echo "end to ready deploy"
echo "please load image in server (ex. docker load -i courtboard-api.tar)"
echo "and run container (ex. docker run --name courtboard-api -d -p 8080:8080 courtboard-api:0.9)"
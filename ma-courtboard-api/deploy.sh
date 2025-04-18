#!/bin/bash
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'

version="$1"

help()
{
    echo -e "${GREEN}Usage : $0 [version]"
    echo -e "${CYAN}Example: $0 1.1"
}

echo -e "${GREEN}deploying to server start"

echo -e "${GREEN}gradle build..."
cd ..
./gradlew :ma-courtboard-api:build
cd ma-courtboard-api

echo -e "${CYAN}docker build..."
docker build --platform linux/amd64 -t courtboard-api:${version} .

echo -e "${CYAN}docker image to file..."
docker save -o ./courtboard-api-${version}.tar courtboard-api:${version}
cp ./courtboard-api-${version}.tar /Users/jckang/courtboard-api-${version}.tar

echo -e "${CYAN}docker file to server with scp"
cd /Users/jckang
scp -i ssh-key???.pem ./courtboard-api-${version}.tar ??@???:/home/??

echo -e "${YELLOW}end to ready deploy"
echo -e "please load image in server (ex. docker load -i courtboard-api.tar)"
echo -e "and run container (ex. docker run --name courtboard-api -d -p 8080:8080 courtboard-api:0.9)"
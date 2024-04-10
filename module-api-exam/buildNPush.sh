# this is for building docker image and uploading to minikube cluster
docker build -t api-exam:0.0.2 .
docker image save -o image.tar api-exam:0.0.2
minikube image load image.tar
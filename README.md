# ecommerce-user-service
I will develop an e-commerce website backend (project is suggested by ChatGPT) to develop my azure, microservices and kubernetes experience
docker build -t user-service:0.0.1-SNAPSHOT -f docker/Dockerfile .
docker tag user-service:0.0.1-SNAPSHOT bmcnpnr/ecommerce-user-service:latest
docker push bmcnpnr/ecommerce-user-service:latest

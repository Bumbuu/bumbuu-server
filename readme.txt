$ echo "gcmkey" > src/main/resources/gcm.key
$ docker build -t bumbuu/server .
$ docker run -d -p 4567:4567 bumbuu/server

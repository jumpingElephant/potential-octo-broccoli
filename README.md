# potential-octo-broccoli

>Project, that uses [Spark](http://sparkjava.com/) as server to deploy the webapp from repository [automatic-octo-carnival](https://github.com/jumpingElephant/automatic-octo-carnival). Uses MongoDB to persist the data.

To run the app start [MongoDB](https://www.mongodb.com) MongoDB first with
```
./run-mongodb.sh /path/to/mongodb/data
```

then on first invocation of the app run parts of install.sh, where *#port* needs to be replaced adequate:
```
mvn clean package -q \
&& sudo docker build -t consumption . \
&& sudo docker rm consumption-app \
&& sudo ./run.sh #port \
&& wget --retry-connrefused -q -S -T 0.1 localhost:#port
```

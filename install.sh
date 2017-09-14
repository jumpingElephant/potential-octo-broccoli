if [ -z $1 ];
then
	echo Command: ./install.sh port
	exit 1
fi

sudo docker kill consumption-app \
&& mvn clean package -q -Pfat-jar \
&& sudo docker build -t consumption . \
&& sudo docker rm consumption-app \
&& sudo ./run.sh $1 \
&& wget --retry-connrefused -q -S -T 0.1 localhost:$1

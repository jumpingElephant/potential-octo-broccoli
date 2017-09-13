if [ -z $1 ];
then
	echo Command: run-mongodb.sh /path/to/mongodb/data
	exit 1
fi

docker run --name mongo-consumption \
	--volume $1:/data/db \
	--network consumption_nw \
	--restart always \
	--detach \
	mongo


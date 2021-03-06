# Also executed by install.sh
if [ -z $1 ];
then
	echo Command: run.sh port
	exit 1
fi

docker run --name consumption-app \
	--env mongoHost='mongo' \
	--network consumption_bridge \
	--link mongo-consumption:mongo \
	--publish $1:4567 \
    --volume /var/log/docker:/var/log \
	--restart always \
	--detach \
	consumption

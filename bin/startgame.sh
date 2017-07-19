#!/bin/sh
dir="/root/cardgame"
jar_prefix="cardgame"
cd $dir
rm -rf log
nohup java -server -Xmx4g -Xms4g \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-Xloggc:"$dir""/log/gc.log" \
-jar "$jar_prefix"*.jar >log &
tail -f log

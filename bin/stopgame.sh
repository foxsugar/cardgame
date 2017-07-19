#!/bin/sh
dir="/root/game/"
kill -9 $(ps ax|grep java |grep $dir |awk '{print$1'})
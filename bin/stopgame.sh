#!/bin/sh
dir="/root/cardgame/"
kill -9 $(ps ax|grep java |grep $dir |awk '{print$1'})
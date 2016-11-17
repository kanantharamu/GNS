#!/usr/bin/env bash

ant
for ((i=1; i<=5000; i++)); do
	echo "Test number $i"
	ant test
done

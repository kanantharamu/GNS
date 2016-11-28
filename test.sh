#!/usr/bin/env bash

timestamp() {
  date +"%s"
}


ant
for ((i=1; i<=5000; i++)); do
	TIMESTAMP=`timestamp`
	echo "Test number $i, $TIMESTAMP"
	ant test > ${TRAVIS_BUILD_NUMBER}_${TIMESTAMP}_log.log
	zip -r ${TRAVIS_BUILD_NUMBER}_${TIMESTAMP}_${TRAVIS_BRANCH}_logs.zip ./${TRAVIS_BUILD_NUMBER}_${TIMESTAMP}_log.log ./logs/*
	scp -i id_rsa ${TRAVIS_BUILD_NUMBER}_${TIMESTAMP}_${TRAVIS_BRANCH}_logs.zip $AWS_USER@$AWS_HOST:$AWS_PATH
	rm ${TRAVIS_BUILD_NUMBER}_${TIMESTAMP}_log.log ${TRAVIS_BUILD_NUMBER}_${TIMESTAMP}_${TRAVIS_BRANCH}_logs.zip
done

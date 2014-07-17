#!/bin/bash
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
    -Dfile=../native-libs/LeapJava.jar \
    -DgroupId=com.leapmotion.leap -DartifactId=leapmotion \
    -Dversion=1.2.0 -Dpackaging=jar

#!/bin/bash
echo "$1" > Main.java
javac Main.java >stdout.log 2>&1
java Main >stdout.log 2>&1

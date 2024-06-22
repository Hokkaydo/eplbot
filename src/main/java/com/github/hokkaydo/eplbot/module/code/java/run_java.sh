#!/bin/bash
echo -e "$1" > Main.java
javac Main.java >stdout.log 2>&1
java Main >>stdout.log 2>&1
cat stdout.log
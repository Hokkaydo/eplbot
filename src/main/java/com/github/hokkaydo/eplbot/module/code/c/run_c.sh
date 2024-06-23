#!/bin/bash
echo -e "$1" > main.c
# gnu99, the version to be used for lepl1503
gcc -std=gnu99 -o main main.c -Wall -Werror
./main
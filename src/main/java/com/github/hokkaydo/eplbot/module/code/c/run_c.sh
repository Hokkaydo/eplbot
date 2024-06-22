#!/bin/bash
echo -e "$1" > main.c
gcc -std=gnu99 -o main main.c -Wall -Werror
./main
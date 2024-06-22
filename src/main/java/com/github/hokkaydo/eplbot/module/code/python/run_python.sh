#!/bin/bash
echo -e "$1" > main.py
python main.py >>stdout.log 2>&1
cat stdout.log
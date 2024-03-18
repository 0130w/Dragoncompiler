#!/bin/bash

if [ $# -ne 2 ]; then
    echo "Usage: $0 <Input file path> <Expected file path>"
    exit 1
fi

input_file=$1
expected_file=$2

output=$(make run FILEPATH="$input_file" > /dev/null 2>&1 && java -classpath ./classes:/usr/local/lib/antlr-4.9.1-complete.jar Main "$input_file" 2>&1)

expected_output=$(cat "$expected_file")

if [ "$output" == "$expected_output" ]; then
    echo "output match expected output"
else
    echo "output does not match expected output"
    echo "Expected: $expected_output"
    echo "Got: $output"
fi
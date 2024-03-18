#!/bin/bash

if [ $# -ne 2 ]; then
    echo "Usage: $0 <Input file path> <Expected file path>"
    exit 1
fi

input_file=$1
expected_file=$2

output=$(make run FILEPATH="$input_file")

expected_output=$(cat "$expected_file")

if [ "$output" == "$expected_output" ]; then
    echo "output match expected output"
else
    echo "output does not match expected output"
fi
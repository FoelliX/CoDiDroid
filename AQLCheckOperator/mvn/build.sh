#!/bin/bash

cd target/build

zip -u $1 tool.properties
rm tool.properties
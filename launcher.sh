#!/bin/bash -x

# make temp directory
mkdir exec

# copy over map
cp -r imgs exec/imgs

# Compile all .java files from pre-made list of all .java files into exec directoy
javac -g @argfile -d ./exec

# Run the program
cd exec
java controller/Main

# clean
cd ..
rm -rf exec

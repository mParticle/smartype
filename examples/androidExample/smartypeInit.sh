#!/bin/bash
SM_FILE=smartype.jar
if [ -f "$SM_FILE" ]; then
    echo "$SM_FILE exists, skipping download."
else 
    curl https://repo1.maven.org/maven2/com/mparticle/smartype-generator/1.0.2/smartype-generator-1.0.2.jar > smartype.jar
fi
java -jar smartype.jar init

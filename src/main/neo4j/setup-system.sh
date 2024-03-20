#!/bin/bash

# Create Service Home
printf "Creating Service Home in /srv/glowmoss/\n"
mkdir -p /srv/glowmoss/
# Create Neo4j Path
mkdir -p /srv/glowmoss/neo4j/data
mkdir -p /srv/glowmoss/neo4j/conf
mkdir -p /srv/glowmoss/neo4j/logs

# preconfigure with defaults
cp $(pwd)/neo4j.conf /srv/glowmoss/neo4j/conf/neo4j.conf

# add scripts
cp $(pwd)/start.sh /srv/glowmoss/neo4j/start.sh
cp $(pwd)/stop.sh /srv/glowmoss/neo4j/stop.sh

chmod +x /srv/glowmoss/neo4j/start.sh

printf "Switch to /srv/glowmoss/neo4j/ and run start.sh to start the database\n"


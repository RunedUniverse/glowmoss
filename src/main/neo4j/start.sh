#!/bin/bash

podman run \
--rm \
--publish=8474:7474 --publish=8687:7687 \
--volume=/srv/glowmoss/neo4j/conf:/var/lib/neo4j/conf:Z \
--volume=/srv/glowmoss/neo4j/data:/data:Z \
--volume=/srv/glowmoss/neo4j/logs:/logs:Z \
--name=glowmoss-db-neo4j \
--env NEO4J_AUTH=neo4j/glowmoss \
docker.io/library/neo4j:latest


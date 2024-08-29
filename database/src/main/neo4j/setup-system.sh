#!/bin/bash
#
# Copyright © 2024 VenaNocta (venanocta@gmail.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


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

chmod +x /srv/glowmoss/neo4j/start.sh

printf "Switch to /srv/glowmoss/neo4j/ and run start.sh to start the database\n"


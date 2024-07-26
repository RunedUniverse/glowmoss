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


podman run \
--rm \
--publish=7474:7474 --publish=7687:7687 \
--volume=/srv/glowmoss/neo4j/conf:/var/lib/neo4j/conf:Z \
--volume=/srv/glowmoss/neo4j/data:/data:Z \
--volume=/srv/glowmoss/neo4j/logs:/logs:Z \
--name=glowmoss-db-neo4j \
--env NEO4J_AUTH=neo4j/glowmoss \
docker.io/library/neo4j:latest


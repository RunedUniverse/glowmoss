# glowmoss
GlowMoss is a Linux system tool for analyzing NFTables rules and network routes

## Testing

```bash
./test.sh --db-addr 127.0.01 --db-port 7687 --nft-ruleset $(pwd)/src/main/resources/ruleset.txt --log
```

In the `src/main/neo4j` path contains an advanced database setup script.

Watch out, the scripts expect you to run them as root and that Podman is installed!

Alternatively you could just run:
```bash
sudo mkdir -p /srv/glowmoss/neo4j/{conf,data,logs}

sudo podman run \
--rm \
--publish=7474:7474 --publish=7687:7687 \
--volume=/srv/glowmoss/neo4j/conf:/var/lib/neo4j/conf:Z \
--volume=/srv/glowmoss/neo4j/data:/data:Z \
--volume=/srv/glowmoss/neo4j/logs:/logs:Z \
--name=glowmoss-db-neo4j \
--env NEO4J_AUTH=neo4j/glowmoss \
docker.io/library/neo4j:4.4
```


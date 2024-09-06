# glowmoss
GlowMoss is a Linux system tool for analyzing NFTables rules and network routes

## Usage

Install the `glowmoss-app` and the `glowmoss-database` packages (see [Releases](https://github.com/RunedUniverse/glowmoss/releases)).

Next start the database service:

```bash
sudo systemctl start glowmoss-db.service
```

The tool is executed via the launch script (`/usr/bin/glowmoss`) as shown in the example:

```bash
glowmoss import --db-addr 127.0.0.1 --db-port 7687 --nft-ruleset $(pwd)/src/main/resources/ruleset.txt --log
```


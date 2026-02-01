#/bin/bash --version
cd ~/dev/Projet_Pokedex/pokedex-back
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
mvn embedded-glassfish:run

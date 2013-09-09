call mvn -f pom-core.xml clean install
cd tools
cd local-module
call mvn clean install
call mvn -f pom-jnlp.xml clean install
cd ..
cd ..
call mvn compile war:exploded -P include-webstart
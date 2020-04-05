call mvn clean
call mvn -P javlo.install compile war:war install -Dmaven.test.skip=true
call mvn-core -P javlo.install compile war:war install -Dmaven.test.skip=true
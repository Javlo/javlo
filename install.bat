call mvn clean
call mvn -P javlo compile war:war install -Dmaven.test.skip=true
call mvn-core -P javlo compile war:war install -Dmaven.test.skip=true
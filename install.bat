call mvn clean
call mvn -P javlo compile war:war install
call mvn-core -P javlo compile war:war install
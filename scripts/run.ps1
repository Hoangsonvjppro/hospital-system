$MVN = "C:\maven\apache-maven-3.9.6\bin\mvn.cmd"
& $MVN compile exec:java -f "$PSScriptRoot\..\pom.xml"

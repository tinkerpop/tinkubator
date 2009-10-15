This README provides the instructions for creating a deployment of the LoPSideD packages. These instructions were provided by Joshua Shinavier (josh@fortytwo.net).

For each of the Maven modules with assembly configurations for distributions.

1) cd into the module directory
2) mvn site   (this will fail near the end, which is fine)
3) mvn install -Dmaven.test.skip=true
4) now go into the pom.xml of the module and comment out this line:
   <descriptor>src/assembly/standalone.xml</descriptor>
5) uncomment this line:
   <!--<descriptor>src/assembly/distribution.xml</descriptor>-->
6) mvn install -Dmaven.test.skip=true
7) restore pom.xml to the original commenting

You will find the distribution .zip file in the target/ directory. This can now be uploaded to the Google Code downloads page.



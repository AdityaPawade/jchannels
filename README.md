# Jetty Channels  [![License: MIT](https://img.shields.io/badge/License-MIT-brightgreen.svg)](https://opensource.org/licenses/MIT) ![Maven Central](https://img.shields.io/maven-central/v/com.adtsw/jchannels?color=blue&label=Version) ![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.adtsw/jchannels?label=Snapshot&server=https%3A%2F%2Foss.sonatype.org%2F)


***Wrapper over Jetty library for simple inter and intra process communication***

---

### Features

* [x] Easily build HTTP / WebSocket servers
* [x] Integrate with existing servers using HTTP / WebSocket client
* [x] Integration with JWT and GAuth Token Managers
* [x] Easy sync / async inter / intra process communication using queue and actor abstractions
* [x] In Memory and HTTP based implementation of messaging queue

### Maven configuration

JChannels is available on [Maven Central](http://search.maven.org/#search). You just have to add the following dependency in your `pom.xml` file.

```xml
<dependency>
  <groupId>com.adtsw</groupId>
  <artifactId>jchannels</artifactId>
  <version>1.0.28</version>
</dependency>
```

For ***snapshots***, add the following repository to your `pom.xml` file.
```xml
<repository>
    <id>sonatype snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```
The ***snapshot version*** has not been released yet.
```xml
<dependency>
  <groupId>com.adtsw</groupId>
  <artifactId>jchannels</artifactId>
  <version>TBD</version>
</dependency>
```

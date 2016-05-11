Aroma Java Client
==============================================

[<img src="https://raw.githubusercontent.com/RedRoma/Aroma/develop/Graphics/Logo.png" width="300">](http://aroma.redroma.tech/)

[![Build Status](http://jenkins.redroma.tech/view/Aroma/job/Aroma%20Java%20Client/badge/icon)](http://jenkins.redroma.tech/view/Aroma/job/Aroma%20Java%20Client/)

The Java Client to the Famed Aroma Service!

COMMAND your Software, with Aroma.

# Download

To use, simply add the following maven dependency.


## Release
```xml
<dependency>
	<groupId>tech.aroma</groupId>
	<artifactId>aroma-java-client</artifactId>
	<version>1.2</version>
</dependency>
```

## Snapshot

>First add the Snapshot Repository
```xml
<repository>
	<id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```

```xml
<dependency>
	<groupId>tech.aroma</groupId>
	<artifactId>aroma-java-client</artifactId>
	<version>1.3-SNAPSHOT</version>
</dependency>
```


# Connecting with Aroma

By following a few simple steps, you can connect your Java application with Aroma and start viewing messages on your iPhone.


## Maven

Add the following dependency to your pom to start using Aroma.

```xml
<dependency>
	<groupId>tech.aroma</groupId>
	<artifactId>aroma-java-client</artifactId>
	<version>1.2</version>
</dependency>
```

## Gradle

Add the following line to your build.gradle file to start using Aroma.
```groovy
compile group: 'tech.aroma', name: 'aroma-java-client', version: '1.2'
```

## Add Import Statement
```java
import tech.aroma.client.Aroma;
```

## Create the Client
```java
Aroma aroma = Aroma.create(APP_TOKEN);
```

## Send a Message
```java
aroma.begin()
     .titled("New User")
     .text("Email: {}",
     .send();
```

That's really all there is to it.

## Best Practices

### Send Important Messages
>Try to only Send messages that are actually interesting. You don't want to bombard Aroma with too many diagnostic messages that are better suited for Logging.

### Set the Urgency
>Set an Urgency to each message. Think of Urgency like you would a Log Severity Level. Using them allows you and your team to know just how important a message is.


# [Javadocs](http://www.javadoc.io/doc/tech.aroma/aroma-java-client/)

Aroma Java Client
==============================================

[<img src="https://raw.githubusercontent.com/RedRoma/Aroma/develop/Graphics/Logo.png" width="300">](http://aroma.redroma.tech/)

[![Build Status](http://jenkins.redroma.tech/view/Aroma/job/Aroma%20Java%20Client/badge/icon)](http://jenkins.redroma.tech/view/Aroma/job/Aroma%20Java%20Client/)
![Maven Central Version](http://img.shields.io/maven-central/v/tech.aroma/aroma-java-client.svg)

The Java Client to the Famed Aroma Service!

COMMAND your Software, with Aroma.

# Download

To use, simply add the following maven dependency.


## Release
```xml
<dependency>
	<groupId>tech.aroma</groupId>
	<artifactId>aroma-java-client</artifactId>
	<version>2.1</version>
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
	<version>2.2-SNAPSHOT</version>
</dependency>
```


# Connecting with Aroma

By following a few simple steps, you can connect your Java application with Aroma and start viewing messages on your iPhone.

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
     .text("Email: {}", user.email)
     .send();
```

### Shorthand
You can also send messages using a one-shot shorthand way.

```java
aroma.sendLowPriorityMessage("New User", "Email: {}", user.email);
```

That's really all there is to it.

## Best Practices

### Send Important Messages
>Try to only Send messages that are actually interesting. You don't want to bombard Aroma with too many diagnostic messages that are better suited for Logging.

### Set the Urgency
>Set an Urgency to each message. Think of Urgency like you would a Log Severity Level. Using them allows you and your team to know just how important a message is.


# [Javadocs](http://www.javadoc.io/doc/tech.aroma/aroma-java-client/)

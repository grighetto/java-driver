## Logging

### Setup

The driver uses [SLF4J](http://www.slf4j.org) to emit log messages; but being a library, 
it does not attempt to configure any specific logging framework itself. 

It is up to client applications using the driver to correctly set up their classpath and 
runtime configuration to be able to correctly capture log messages emitted by the driver.

Concretely, client applications need to provide, at runtime, a _binding_ to any logging framework 
of their choice that is [compatible with SLF4J](http://www.slf4j.org/manual.html#swapping).

If your application is built with Maven, this usually involves adding one dependency to your POM file.
For example, if you intend to use [Logback](http://logback.qos.ch), add the following dependency:

```xml
<dependency>
	<groupId>ch.qos.logback</groupId>
	<artifactId>logback-classic</artifactId>
	<version>1.1.3</version>
</dependency>
```

If your are using Log4J 1.2 instead, add the following dependency:

```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-log4j12</artifactId>
  <version>1.7.12</version>
</dependency>
```

Check [SLF4J's documentation](http://www.slf4j.org/manual.html#projectDep) for examples for 
other logging frameworks, and for troubleshooting dependency resolution problems. 

### Configuration

The driver log level can be adjusted according

The

Example for logback production
Adjust log levels

<table>
<thead>
<tr><th>Logger Name</th><th>ERROR</th><th>WARN</th><th>INFO</th><th>DEBUG</th><th>TRACE</th></tr>
</thead>
<tbody>
<tr>
    <th><code>com.datastax.driver.core.Cluster</code></th>
    <td>
        Authentication errors
        Unexpected errors when handling events, reconnecting, refreshing schemas
        Unexpected events
    </td>
    <td>
        Cluster name mismatches, 
        Unsupported protocol versions, 
        Schema disagreement, 
        Ignored notifications due to contention
        Unreachable contact points
    </td>
    <td>
        Host Added / Removed
    </td>
    <td>
        Cluster lifecycle (start, shutdown), 
        Event delivery notifications (schema changes, topology changes, , 
        Hosts Up / Down / Added / Removed events, 
        hosts being ignored because not enough info, 
        Protocol version negotiation, 
        Reconnection attempts
        Schema metadata refreshes
    </td>
    <td>TRACE
        Renewing pools
    </td>
</tr>
<tr>
    <th><code>com.datastax.driver.core.Session</code></th>
    <td>ERROR
        Pool creation/refresh error
        Error preparing query
    </td>
    <td>
        Replacing non closed pool
    </td>
    <td>INFO</td>
    <td>DEBUG
        Connection pool added renewed
    </td>
    <td>TRACE
    </td>
</tr>

<tr>
    <th><code>com.datastax.driver.core.RequestHandler</code></th>
    <td>ERROR
        Pool creation/refresh error
        Error preparing query
        host is bootstrapping
        unknown prepared query
        error querying host
    </td>
    <td>
        Replacing non closed pool
        host overloaded, server error
    </td>
    <td>INFO
        Query {} is not prepared on {}, preparing before retrying executing
    </td>
    <td>DEBUG
        Error querying host trying next
        query state race conditions (in progress, complete, timeout)
        retries attempts
    </td>
    <td>TRACE
        host being queried
    </td>
</tr>

<tr>
    <th><code>com.datastax.driver.core.Connection</code></th>
    <td>ERROR
    </td>
    <td>
    problem setting keyspace
    error closing channel
    </td>
    <td>INFO
    </td>
    <td>DEBUG
        open close defunct
        error connecting, writing request
        already terminated, not terminating
        unsupported protocol version
        heartbeats
    </td>
    <td>TRACE
    authentication
    keyspace
    writing request
    request sent
    response received
    </td>
</tr>
</tbody>
</table>

### Logging query latencies


```java
Cluster cluster = Cluster.builder()
    .addContactPoint("1.2.3.4")
    .withAddressTranslater(new EC2MultiRegionAddressTranslater())
    .build();
```

### Using JMX


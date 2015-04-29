## Manual Paging

Requests on Cassandra that generate a large amount of data are transferred in 
applicative frames called pages. These pages are identified by Cassandra and are 
communicated through the Native protocol with their identifier.

Since each page is identified by a unique page identifier (called Paging State), 
it is possible to send a request to a Cassandra node, using a valid 
Paging State, to start fetching data starting after the last row of 
the specified Paging State.

For instance, we can easily see the utility of this feature when 
considering the use of the Java Driver in a Stateless Web Service. 
When displaying a list of data results on a web page with other 
results page indexes, we can imagine that the Web Service which 
will display the next page of results do not want to fetch data 
it has already fetched. It just wants to execute the exact same 
query and gather the next results. 
To do so, the first step is to save the Paging State of the last page 
when iterating through the results of the first request. The service 
can then do other things, and when it needs to gather the next page of
the same query, it just has to provide the previously saved Paging State
to the statement it is going to execute.

The Paging State is an entity we can collect after the successful execution
of requests generating a result on multiple pages. It can be safely serialized
and de-serialized. We bring particular attention on the fact that 
it is made do only be collected, stored, and re-used. Therefore, its content
must never be changed manually and the driver will recognize it if it has been
changed and refuse executing a request where a Paging State is modified.

Here is an example of usage : 

```java
    Statement st = new SimpleStatement("your query");
    // ... *execute the statement and start iterating through the result* ...
    String savedPagingState = resultSet.getExecutionInfo().getPagingState().toString();
    // ... *do things* ...
    st = new SimpleStatement("your query");
    st.setPagingState(PagingState.fromString(savedPagingState));
```

Here, when we will start iterating through the result of the second request,
the data will start from the first data of the page following the page when 
the Paging State was saved.
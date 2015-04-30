## Manual Paging

When sending a request, you can set a number of rows to fetch when receiving
 a result from a Cassandra cluster. This number defines then the size of rows pages
 the cluster will return. Setting this fetch size can be done in 2 ways : 
 
 - Setting the fetch size when building the cluster and then defining it globally 
    for all the statements, with `QueryOptions#setFetchSize(int)`.
 - Setting the fetch size for a particular statement. The default or previously
    globally configured fetch size will be used for all statements except when 
    explicitly changing it using `Statement#setFetchSize(int)`.
    
Please notice that setting a fetch size doesn't mean that Cassandra will always return
the exact number of rows, it is possible that it returns slightly more or less 
results.

When a query generates more rows than the fetch size defined, Cassandra will
send results in multiple pages of rows where each page has an identifier.

Since each page is identified by a unique page identifier (called Paging State), 
it is possible to send a request to a Cassandra node, using a valid 
Paging State previously saved, to start fetching data starting after the 
last row of the specified Paging State.

For instance, we can easily see the utility of this feature when 
considering the use of the Java Driver in a Stateless Web Service. 
When displaying a list of data results on a web page with for example a link
to the next page of results, we can imagine that the Web Service which 
will display the next page of results does not want to fetch data
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
it is made to only be collected, stored, and re-used. Therefore, its content
must never be changed manually and the driver will recognize it if it has been
changed and refuse executing a request where the Paging State has been 
modified.

Here is an example of usage : 

```java
    Statement st = new SimpleStatement("your query");
    ResultSet resultSet = session.execute(st);
    // ... iterate into the result set 
    String savedPagingState = resultSet.getExecutionInfo().getPagingState().toString();
```
The service could then do other things and only has to keep the String
object representing the Paging State. And later when it has to re-execute
the same query but wants the following results, it just needs to provide
the previously saved Paging State.

```java
    st = new SimpleStatement("your query");
    st.setPagingState(PagingState.fromString(savedPagingState));
    session.execute(st);
```

Here, when we will start iterating through the result of the second request,
the data will start from the first data of the page following the page when 
the Paging State was saved.
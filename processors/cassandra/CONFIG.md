# Configuration Options

This processors persists an audit event to a Cassandra cluster.

It is generally recommended to only manage one Cassandra session
object per keyspace / per application
(http://www.datastax.com/dev/blog/4-simple-rules-when-using-the-datastax-drivers-for-cassandra).
Hence, the ideal method to obtain the required Cassandra session
depends on the concrete usage of this library. The following
choices are generally available:

- **Cassandra native:** manages the Cassandra connection within
  the audit library. Use this implementation if this library is
  the only Cassandra client used in your application.
  (`org.beiter.michael.eaudit4j.processors.cassandra.TODO`)
- **Cassandra session** obtains a session that is managed by the
   integrating applicaiton. Use this implementation if this
   library is **not** the only Cassandra client in your appplication
   (`org.beiter.michael.eaudit4j.processors.cassandra.CassandraProcessor`)

The event is stored in a database table of the following structure:

```
CREATE TABLE events (
  eventId ASCII PRIMARY KEY,
  auditStream ASCII,
  eventJson VARCHAR);
```

- The `eventId` field holds an identifier that uniquely identifies
  audit events. See below for more information on this field, and
  how to configure the length of the field. The collation of this
  field is `ASCII` when using the default `eventId` generator that
  comes with this library, but the correct collation depends on the
  method of how the event ID is being generated.
- The `auditStream` field holds the name of the audit stream a
  specific event occurred in. The collation of this field is `ASCII`
  when `ASCII`-only audit stream names are being used, but the correct
  collation depends on the method of how the audit streams are named
  in a specific deployment.
- The `eventJson` holds a JSON representation of the JSON-serialized
  audit event. The recommended collation of this field is `UTF-8`.

It is commonly **not** recommended to create search indexes on high-
cardinality columns that have many distinct values, thus resulting
in queries over large datasets that return very few results. For this
reason, it may be better to create a search index just on the
`auditStreamName` column, but not on the `eventId` column. However,
the decision of whether to create such indexes or not should be made
on the concrete deployment scenario, considering the available options
for record retention and long term archiving. When search is required,
then it is strongly recommended to consider the option of using a
dedicated search and indexing infrastructure over the built-in
functionality that comes with Cassandra.

## Cassandra Processor

### audit.processor.cassandra.insertEventCqlStmt

Sets the CQL statement to be used when inserting events into Cassandra.
The statement must be parametrized and use named parameters, as configured
with the following settings:

1. `audit.processor.cassandra.eventIdCqlParam`
2. `audit.processor.cassandra.auditStreamNameCqlParam`
3. `audit.processor.cassandra.eventJsonCqlParam`

The parameterized statement **should** also explicitly specify the
keyspace as `keyspace.tablename`, unless the application manages the
session and provides a session to the library that has been initialized
to use the correct key space.

An example statement that meets the requirements is:

`INSERT INTO events (eventId, auditStream, eventJson) VALUES (:eid, :stream, :json)`

Default: `"TODO - CONFIGURE ME!"`

### audit.processor.cassandra.eventIdCqlParam

Set the name of the "event ID" parameter in the event CQL "INSERT"
parameterized statement when creating new event records in the event table.

The value of this configuration setting must match a named parameter in the
CQL `INSERT` parameterized statement. For the example above, the value of
this configuration setting would be `eid` (without the `:`).

Note that this field is **not** necessarily equal to the column name.

### audit.processor.cassandra.auditStreamNameCqlParam

Set the name of the "audit stream name" parameter in the event CQL "INSERT"
parameterized statement when creating new event records in the event table.

The value of this configuration setting must match a named parameter in the
CQL `INSERT` parameterized statement. For the example above, the value of
this configuration setting would be `stream` (without the `:`).

Note that this field is **not** necessarily equal to the column name.

### audit.processor.cassandra.eventJsonCqlParam

Set the name of the "event JSON" parameter in the event CQL "INSERT"
parameterized statement when creating new event records in the event table.

The value of this configuration setting must match a named parameter in the
CQL `INSERT` parameterized statement. For the example above, the value of
this configuration setting would be `json` (without the `:`).

Note that this field is **not** necessarily equal to the column name.

### audit.processor.cassandra.stringEncoding

The encoding to use when when converting bytes to a String.

Default: `UTF-8`

### audit.processor.cassandra.eventIdFieldName

The name of a field in the event that holds a unique event ID (i.e.
an ID that is unqiue for each event). The event ID must be available
from the event as a String. It is being used in this processor to
link events (in the events table) to indexed fields (in the search table).

The entropy of this random String is approximately 3/4 of the overall
String length. It is recommended to configure event IDs with at least
96 bits of entropy (about 16 chars).

This event ID can be created for instance with the
`org.beiter.michael.eaudit4j.processors.eventid.EventIdProcessor` processor.

Default: `TODO - CONFIGURE ME!`

### audit.processor.cassandra.sessionName

This processor connects to Cassandra via a session that is provided by the
integrating application through the `ProcessingObjects` in the
`process(Event, String, ProcessingObjects)` method of `CassandraProcessor`.
The processor will not close the session, and expects that the session's
lifecycle is fully managed by the integrating application.

The object referenced by this name must be of type
`com.datastax.driver.core.Session`.

Default: `"TODO - CONFIGURE ME!"`

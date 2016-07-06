# Configuration Options

This processors persists an audit event to a Cassandra database connection.
The event is stored in a database table of the following structure:

```
CREATE TABLE events (
  eventId VARCHAR PRIMARY KEY,
  auditStream VARCHAR,
  eventJson VARCHAR);
```
- The `eventId` field holds an identifier that uniquely identifies
  audit events. See below for more information on this field, and
  how to configure the length of the field. Note that the field
  length in the database must match the length configuration for
  creation of that id string in this library. The collation of this
  field is `ASCII` when using the default `eventId` generator that
  comes with this library, but the correct collation depends on the
  method of how the event ID is being generated.
- The `auditStream` field holds the name of the audit stream a
  specific event occurred in. Note that field length in the database
  must be greater or equal to the longest possible audit stream name
  used in this library. The collation of this field is `ASCII` when
  `ASCII`-only audit stream names are being used, but the correct
  collation depends on the method of how the audit streams are named
  in a specific deployment.
- The `eventJson` holds a JSON representation of the JSON-serialized
  audit event. A good choice for the type of this field is a large
  text object field, such as `CLOB`, `LONGTEXT`, or similar. The
  recommended collation of this field is `UTF-8`.

## Generic Settings for the Cassandra Processors

### audit.processor.cassandra.insertEventSqlStmt

Sets the SQL statement to be used when inserting events in the database
table storing events (i.e. the `events` table above). The statement must
be parametrized, and the order of the parameters must be:

1. `eventId`
2. `auditStream`
3. `eventJson`

An example statement that meets the requirements is:

`INSERT INTO events (eventId, auditStream, eventJson) VALUES (?, ?, ?)`

The syntax of the statement depends on the underlying RDBMS.

Default: `"TODO - CONFIGURE ME!"`

### audit.processor.cassandra.stringEncoding

The encoding to use when when converting bytes to a String.

Default: `UTF-8`

## Cassandra Connection

To connect with Cassandra, the application that integrates with the audit lib must set an open Session instance to the lib.
This Session instance must pe set as a `processingObject` as follow:

```
ProcessingObjects processingObjects = new ProcessingObjects();
processingObjects.add(MapBasedCassandraPropsBuilder.KEY_CASSANDRA_CONNECTION_SESSION,
        sessionInstance);
```

**KEY_CASSANDRA_CONNECTION_SESSION=**`audit.processor.cassandra.session`

To audit an event passing this processingObject:

```
audit.audit(event, AUDIT_STREAM_NAME, processingObjects);
```




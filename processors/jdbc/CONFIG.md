# Configuration Options

This processors persists an audit event to a JDBC database connection.
The method to obtain the required database connection depends on the
concrete implementation. The following choices are available:

- **JDBC Pool:** uses a configurable connection pool to connect to
  the database
  (`org.beiter.michael.eaudit4j.processors.jdbc.JdbcPoolProcessor`)
- **JDBC JNDI** uses a connection managed by JNDI to connect to
  the database
  (`org.beiter.michael.eaudit4j.processors.jdbc.JdbcJndiProcessor`)
- **JDBC Data Source** obtains a connection from a data source that
   is managed by the integrating to connect to the database
   (`org.beiter.michael.eaudit4j.processors.jdbc.JdbcDsProcessor`)

The event is stored in a database table of the following structure:

```
CREATE TABLE events (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  eventId VARCHAR(16) NOT NULL UNIQUE,
  auditStreamName VARCHAR(32) NOT NULL,
  eventJson CLOB NOT NULL);
```

- The `id` field is not used in this processor, but usually improves
  performance of `CUD` operations on common RDBMS, such as MySQL.
- The `eventId` field holds an identifier that uniquely identifies
  audit events. See below for more information on this field, and
  how to configure the length of the field. Note that the field
  length in the database must match the length configuration for
  creation of that id string in this library. The collation of this
  field is `ASCII` when using the default `eventId` generator that
  comes with this library, but the correct collation depends on the
  method of how the event ID is being generated.
- The `auditStreamName` field holds the name of the audit stream a
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

It is recommended to created search indexes on the `eventId` and
`auditStreamName` columns.


This processor allows to optionally index a set of event fields and
make them searchable in the database. If an event contains a field
from the indexed field list, it is extracted from the event, and then
stored in a dedicated database table that holds the indexed fields.

Each field is inserted as a dedicated row in the indexed fields table.
This has significant impact on the size of this table, and may have
impact on the overall performance of the audit library due to excessive
table growth. For this reason, it is recommended to limit the number
of indexed fields to a reasonable size.

The indexed fields are stored in a table of the following structure:

```
CREATE TABLE fields (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  eventId VARCHAR(16) NOT NULL,
  auditStreamName VARCHAR(32) NOT NULL,
  fieldName VARCHAR(128) NOT NULL,
  fieldValue VARCHAR(255) NOT NULL);
```

- The `id` field is not used in this processor, but usually improves
  performance of `CUD` operations on common RDBMS, such as MySQL.
- The `eventId` is used to link event fields to entries in the `events`
  table. While this is a foreign key reference, and may be modeled
  as such, it depends on the specific RDBMS on whether such a reference
  should be modeled explicitly: There is a trade-off between performance
  and strong relational consistency, and it depends on the
  underlying RDBMS whether a foreign key reference would significantly
  impact performance in a negative way. The specs of this field should
  match the specs of its counterpart in the `events` table.
- The `auditStreamName` field holds the name of the audit stream a
  specific event occurred in. The specs of this field should match
  match the specs of its counterpart in the `events` table.
- The `fieldName` field holds the name of the indexed field. This is
  by default the field name as it occurs in the audit event, but it
  is possible to define field name aliases in persistency. See below
  for a description on how to select fields for indexing and how to
  define alias names. The size of this field must be greater or equal
  to the longest field name to be indexed (as configured in this
  processor). The recommended collation of this field is `ASCII`, but
  but the correct collation depends on the method of how the fields are
  named in a specific deployment.
- The `fieldValue` field holds the value of an indexed field. The length
  of this field should be greater or equal to the value configured for
  the maximum indexed field value length (see below). The recommended
  collation of this field is `UTF-8`. Note that the value of this field
  is normalized using NFC form (Canonical decomposition, followed by
  canonical composition) before it is stored to allow deterministic
  search.

It is required to create a unique index on the `{eventId, fieldName}`
columns.

It is recommended to created search indexes on the `eventId`,
`auditStreamName`, and `fieldName` columns.

An additional index on the `fieldValue` column is optional, and should
only be created after a careful consideration of the implications on
creating a search index on a comparatively large text field with a
`UTF-8` collation. If it is required to search on this field for audit
analytics, then note that it is generally recommended to perform such
analytics on a dedicated offline system, rather than directly on the
audit database.

## Generic Settings for the JDBC Processors

### audit.processor.jdbc.insertEventSqlStmt

Sets the SQL statement to be used when inserting events in the database
table storing events (i.e. the `events` table above). The statement must
be parametrized, and the order of the parameters must be:

1. `eventId`
2. `auditStreamName`
3. `eventJson`

An example statement that meets the requirements is:

`INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)`

The syntax of the statement depends on the underlying RDBMS.

Default: `"TODO - CONFIGURE ME!"`

### audit.processor.jdbc.stringEncoding

The encoding to use when when converting bytes to a String.

Default: `UTF-8`

### audit.processor.jdbc.eventIdFieldName

The name of a field in the event that holds a unique event ID (i.e.
an ID that is unqiue for each event). The event ID must be available
from the event as a String. It is being used in this processor to
link events (in the events table) to indexed fields (in the search table).

The entropy of this random String is approximately 3/4 of the overall
String length. It is recommended to configure event IDs with at least
96 bits of entropy (about 16 chars).

This event ID can be created for instance with the
`org.beiter.michael.eaudit4j.processors.eventid.EventIdProcessor` processor.

Note that the field (referenced in the INSERT SQL statements both for
the events table and the search table) that holds this value must be
long enough to accept a value of the length configured here.

Default: `TODO - CONFIGURE ME!`

### audit.processor.jdbc.indexedFields

Set the fields to be added to the search / index table.

Provide a list of fields that is separated with the separation character
specified in `audit.processor.jdbc.indexedFieldSeparator`. You may define
a mapping of Event field names to field names in the database as shown in
the example below, using the character specified in the
`audit.processor.jdbc.indexedFieldNameSeparator` property.

Note that the mapping of an event field name to database field name is
optional. If no field names separator is used for a specific field, then
the event field name is used to store the field in the database.

Set this to `null` or empty if no event field names should be added to
the indexed (`fields`) table.

This example uses `,` as the fields separator, and `:` as the field
names separator.

Example: `eventActor:myActor,eventSubject,eventObject:myObject`

Default: `null`

### audit.processor.jdbc.indexedFieldsMaxLength

Set the maximum length of the values to be stored in indexed fields.

If the value of a specific indexed field in an event is longer than
this setting, then the value of the field is truncated to this length
before it is inserted in the indexed fields table.

Default: `255`

### audit.processor.jdbc.indexedFieldsToLower

Indicate whether to convert all characters in indexed fields to lowercase
before they are stored in the database.

Set this to `true` when using a database that only supports case-sensitive
search, or if the case-insensitive search on the database is not as
performant as the case-sensitive search.

Default: `false`

### audit.processor.jdbc.insertIndexedFieldSqlStmt

Sets the SQL statement to be used when inserting events in the database
table storing indexed fields (i.e. the `fields` table above). The
statement must be parametrized, and the order of the parameters must be:

1. `eventId`
2. `auditStreamName`
3. `fieldName`
4. `fieldValue`

An example statement that meets the requirements is:

`INSERT INTO fields (eventId, auditStreamName, fieldName, fieldValue) VALUES (?, ?, ?, ?)`

The syntax of the statement depends on the underlying RDBMS.

Default: `"TODO - CONFIGURE ME!"`

### audit.processor.jdbc.indexedFieldSeparator

The field separator character used in the `audit.processor.jdbc.indexedFields`
property.

This must be a single character, i.e. the maximum length of this string
is `1`.

Default: `,`

### audit.processor.jdbc.indexedFieldNameSeparator

Set the field name separator character used in the
`audit.processor.jdbc.indexedFields` property to separate field names
in the database from the event field name. This allows using a different
field name in the MDC than in the event.

This must be a single character, i.e. the maximum length of this string
is `1`.

Default: `:`

## Processor: JDBC Pool

This processor connects to the database via a configurable database
connection pool that it manages internally.

### JDBC Connection Pool Configuration

This library uses the configuration settings as documented here:

https://github.com/mbeiter/util/blob/master/db/src/main/java/org/beiter/michael/db/ConnectionProperties.java

## Processor: JDBC JNDI

This processor connects to the database via a JNDI connection name.
The connection is managed through JNDI.

### audit.processor.jdbc.jndi.connectionName

Set the name of the JNDI connection. This is required when connecting
through JNDI, that is, when using the `JdbcJndiProcessor`.

Default: `"TODO - CONFIGURE ME!"`

## Processor: JDBC Data Source

This processor connects to the database via a JDBC connection obtained from
a `DataSource` provided through the `ProcessingObjects` in the
`process(Event, String, ProcessingObjects)` method of `AbstractJdbcProcessor`.
The data source (e.g. a connection pool) is managed by the integrating
application.

### audit.processor.jdbc.dataSource.name

Set the name of JDBC data source provided in the `ProcessingObjects`.
This is required when connecting through a data source, that is, when using
the `JdbcDsProcessor`.

The object referenced by this name must be of type `javax.sql.DataSource`.

Default: `"TODO - CONFIGURE ME!"`

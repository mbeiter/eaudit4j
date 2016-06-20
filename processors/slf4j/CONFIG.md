# Configuration Options

## Processor: slf4j

This processor logs audit events using slf4j, and passes the event on to
subsequent processors without modifying it.

The lines that are submitted to slf4j are prefixed with a configurable
"audit marker" and the name of the audit stream as provided, or the default
audit stream if none was provided.

If the underlying logger supports MDC, the following fields are stored in the
MDC:
  * A JSON-serialized representation of the event, using a configured field name
    as the field identifier
  * The audit stream name, using a configured field name as the field identifier

If so configured, then this processor will additionally include specific fields
in the MDC map, making them directly available for further processing by the
underlying logger.

This processor also supports storing specific Event fields in the MDC, either
under their Event field name, or through an "alias", which allows storing Event
fields in the MDC using an alternative name.

Class name: `org.beiter.michael.eaudit4j.processors.slf4j.Slf4jProcessor`

### audit.processor.slf4j.marker

The marker to identify serialized audit events in slf4j. This marker is prefixed
to all log messages.

The marker is used as-is, and you will need to include a suitable separator to
properly separate it from the audit log message.

Default: `[AUDIT] `

### audit.processor.slf4j.stringEncoding

The encoding to use when when converting bytes to a String.

Default: `UTF-8`

### audit.processor.slf4j.auditStreamFieldName

The name to use for the audit stream field in the MDC, if the underlying slf4j
logger implementation supports this feature.

The name of the audit stream will be available from the MDC under this field
name. Note that, even though this implementation does not limit the possible
values of this field, the possible values of this field name may be restricted
by the underlying logger implementation that is used by slf4j.

Default: `auditStreamName`

### audit.processor.slf4j.serializedEventFieldName

Set the name to use for the serialized event field in the MDC, if the
underlying slf4j logger implementation supports this feature.

A JSON serialized representation of the Event will be available from the MDC
under this field name. Note that, even though this implementation does not
limit the possible values of this field, the possible values of this field
name may be restricted by the underlying logger implementation that is used by
slf4j.

Default: `serializedEvent`

### audit.processor.slf4j.mdcFields

Set the fields to be included in the MDC (beyond the audit stream name and the
serialized Event), if the underlying logger supports MDC.

Provide a list of fields that is separated with the separation character
specified in `audit.processor.slf4j.mdcFieldSeparator`. You may define a
mapping of Event field names to MDC field names (i.e. names under which the
Event field will be made known to the MDC) as shown in the example below, using
the character specified in the `audit.processor.slf4j.mdcFieldNameSeparator`
property.

Note that the mapping of an event field name to an MDC field name is optional.
If no MDC field names separator is used for a specific field, then the event
field name is used to store the field in the MDC.

Set this to `null` or empty if none of the event fields (beyond the audit
stream name and the serialized Event) should be included in the MDC.

This example uses `,` as the MDC fields separator, and `:` as the MDC field
names separator.

Example: `eventActor:mdcActor,eventSubject,eventObject:mdcObject`

Default: `null`

### audit.processor.slf4j.mdcFieldSeparator

The MDC field separator character used in the `audit.processor.slf4j.mdcFields`
property.

This must be a single character, i.e. the maximum length of this string is `1`.

Default: `,`

### audit.processor.slf4j.mdcFieldNameSeparator

Set the field name separator character used in the
`audit.processor.slf4j.mdcFields` property to separate field names in the MDC
from the event field name. This allows using a different field name in the MDC
than in the event.

This must be a single character, i.e. the maximum length of this string is `1`.

Default: `:`

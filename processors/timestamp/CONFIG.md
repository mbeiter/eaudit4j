# Configuration Options

## Processor: Timestamp

This processors retrieves the system time, and adds it as a field to
the audit event. The system time is formatted as a String timestamp,
with a configurable format string (and timezone).

Class name: `org.beiter.michael.eaudit4j.processors.timestamp.TimestampProcessor`

### audit.processor.timestamp.timezone

Sets the timezone for the String representation of trhe timestamps.

The timezone of the timestamp mey be configured either as:
- an abbreviation (e.g. "PST") or
- a full name (e.g. "America/Los_Angeles") or
- a custom ID such as "GMT-8:00"

The support of abbreviations is deprecated and only and full names should
be used. See `java.util.TimeZone`` for a list of available options.

If the provided timezone configuration is unknown, then `GMT` is being used
as a fallback.

Note that in most distributed deployments, it is commonly a good approach
to use a common timezone (e.g. `UTC`) as the timezone, instead of the local
machine's timezone.

Default: `America/Denver`

### audit.processor.timestamp.format

Set the format for the timestamps.

Note that the timezone is nor added as a separate field to the timestamp.
Hence, in most deployments, it is a good idea to include the timezone in
the timestamp format string.

See `java.text.SimpleDateFormat` for a list of available options.

Default: `yyyy-MM-dd'T'HH:mm:ss.SSSZ`


### audit.processor.timestamp.eventFieldName

The name of the field that this processor will use to store the timestamp
in audit events.

Default: `org.beiter.michael.eaudit4j.processors.timestamp`

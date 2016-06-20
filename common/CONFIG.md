# Configuration Options

## Core

### audit.auditClassName

The audit class to instantiate. The class must implement the
`org.beiter.michael.eaudit4j.common.Audit` interface.

Default: `org.beiter.michael.eaudit4j.common.impl.SyncAudit`

### audit.defaultAuditStreamName

The default audit stream, which is used when no audit stream is provided in the
`org.beiter.michael.eaudit4j.common.Audit.audit(Event)` calls.

Default: "Default audit stream - CONFIGURE ME!"

### audit.encoding

The character encoding to use when converting `char` to `byte` and vice versa.

Default: `UTF-8`

### audit.dateFormat

The date format string to use when rendering a timestamp into human readable form.

Default: `yyyy-MM-dd'T'HH:mm:ss.SSSZ`

### audit.processors

The chain of audit processor to instantiate. This is a comma separated list of
fully qualified class names. The classes must implement the
`org.beiter.michael.eaudit4j.common.Processor` interface.

The order of the class names is relevant, as the  audit chain will be built and
processed from left to right (i.e. the class on the most left will be
instantiated and executed first, the class on the very right will be instantiated
and executed last).

Default: `null`

### audit.failOnMissingProcessors

Indicates whether to fail auditing if no processors are configured in the audit
processing chain.

Allowed values:

| Value             | Setting                                        |
|-------------------|------------------------------------------------|
| `true`            | Fail the audit operation if `audit.processors` |
|                   | is `null` or empty                             |
| `false` (default) | Do not fail the audit operation, but log a     |
|                   | warning instead.                               |

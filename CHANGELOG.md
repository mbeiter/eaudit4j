# eAudit4j Library Change Log

## 1.1

#### New Processors

- Cassandra (with application managed Session) (fixes #6)

#### Enhancements

- Simplified the overly complex `ProcessorFactory.reset()` method (fixes #2)

#### Defects

- Added `Event ID`` Processor reference to /CONFIG.md (fixes #1)
- Cassandra depends on Guava, and does not pull in the dependency transitively (fixes #7)

## 1.0

Initial release with the following processors:

- Machine ID
- Timestamp
- Event ID
- slf4j
- JDBC

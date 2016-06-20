# Configuration Options

## Processor: Machine ID

This processors allows to either configure a machine identifier through
a configuration property (`audit.processor.mid.machineId` option), or
to determine it as follows:

First, attempt to retrieve the machine ID from an environment variable
(see `audit.processor.mid.fromEnv` and `audit.processor.mid.envVarName`
options).

If this failed (e.g. the processor is not configured to retrieve the machine
ID from the environment, or that retrieval operation failed), then the
processor will attempt to craft a machine ID from a combination of the
canonical host name and a timestamp (see `audit.processor.mid.fromHostname`
option).

If this failed (e.g. the processor is not configured to use the canonical
hostname, or the canonical hostname could not be resolved), then the processor
will create a pseudo-random machine ID and log that machine ID in the log.
Look for the string `audit.processor.mid.machineId` in the log files to track
the machine ID that was eventually chosen.

Note that each instance of the Machine ID Processor will create an individual
machine ID. This allows applications maximum control, but also requires users
of this library to preserve audit chains between audit calls to have consistent
machine IDs. Obviously, it is a good idea from a performance perspective to not
recreate the audit chain for every audit request in the first place (see the
`org.beiter.michael.eaudit4j.common.AuditFactory` class on how to retrieve a
singleton audit chain, or individual instances).

Class name: `org.beiter.michael.eaudit4j.processors.machineid.MachineIdProcessor`

### audit.processor.mid.machineId

The machine ID of the machine executing the audit library. This may be `null`
or blank if no machine ID is to be configured, in which case the processor
will try alternative means to determine a unique ID for the machine.

Default: `null`

### audit.processor.mid.eventFieldName

The name of the field that this processor will use to store the machine ID in
audit events.

Default: `org.beiter.michael.eaudit4j.processors.machineid`

### audit.processor.mid.fromEnv

Enables or disables reading the machine ID from an environment variable.

Allowed values:

| Value             | Setting                                                  |
|-------------------|----------------------------------------------------------|
| `true`            | Try reading the machine ID from the environment variable |
|                   | specified in `audit.processor.mid.envVarName`            |
| `false` (default) | Do not try to read the machine ID from the environment   |


### audit.processor.mid.envVarName

The name of the environment variable from which the machine ID of this machine
should be read. See `audit.processor.mid.machineId`: If no machine ID can be
obtained, this processor will try alternative means to determine a unique ID for
the machine.

Default: `null`

### audit.processor.mid.fromHostname

Enables or disables creating a machine ID based on the canonical hostname.

Allowed values:

| Value             | Setting                                               |
|-------------------|-------------------------------------------------------|
| `true`            | Try resolving the canonical hostname, and create a    |
|                   | machine ID from the hostname                          |
| `false` (default) | Do not try to create the machine ID from the hostname |

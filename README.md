# eAudit4j Library

This library provides a simple and pluggable solution for auditing in Java.

This library uses the concept of "audit streams", with a stream being comprised
of one or more "audit processors". An application can configure an unlimited
number of audit streams, each with a different configuration. When an application
wants to audit an event, it will create and populate an "Event" instance, and
then submit the event to the audit stream for processing and persitance.

Depending on the audit stream configuration and the processors being used in the
audit stream configuration, processing my be synchronous or asynchronous, and
utilize different security features.

Extension points in the library include:
- Custom processors
- Custom events
- Custom event attributes
- Custom audit strategies (e.g. synchronous, asynchronous)

The library includes a few Event implementations, as well as multiple processors.
See the [configuration instructions](CONFIG.md) for an overview on available
components, features, and configuration options.

## Documentation

* 1.1
  * [Maven Project Docs](http://mbeiter.github.io/eaudit4j/docs/1.1/)
  * Java Docs
    * [Common](http://mbeiter.github.io/eaudit4j/docs/1.1/common/apidocs/index.html)
    * [Machine ID Processor](http://mbeiter.github.io/eaudit4j/docs/1.1/machineId/apidocs/index.html)
    * [Timestamp Processor](http://mbeiter.github.io/eaudit4j/docs/1.1/timestamp/apidocs/index.html)
    * [Event ID Processor](http://mbeiter.github.io/eaudit4j/docs/1.1/eventId/apidocs/index.html)
    * [slf4j Processor](http://mbeiter.github.io/eaudit4j/docs/1.1/slf4j/apidocs/index.html)
    * [JDBC Processor](http://mbeiter.github.io/eaudit4j/docs/1.1/jdbc/apidocs/index.html)
    * [Cassandra Processor](http://mbeiter.github.io/eaudit4j/docs/1.1/cassandra/apidocs/index.html)
* 1.0
  * [Maven Project Docs](http://mbeiter.github.io/eaudit4j/docs/1.0/)
  * Java Docs
    * [Common](http://mbeiter.github.io/eaudit4j/docs/1.0/common/apidocs/index.html)
    * [Machine ID Processor](http://mbeiter.github.io/eaudit4j/docs/1.0/machineId/apidocs/index.html)
    * [Timestamp Processor](http://mbeiter.github.io/eaudit4j/docs/1.0/timestamp/apidocs/index.html)
    * [Event ID Processor](http://mbeiter.github.io/eaudit4j/docs/1.0/eventId/apidocs/index.html)
    * [slf4j Processor](http://mbeiter.github.io/eaudit4j/docs/1.0/slf4j/apidocs/index.html)
    * [JDBC Processor](http://mbeiter.github.io/eaudit4j/docs/1.0/jdbc/apidocs/index.html)

## Useful Links

- [Mike's Blog](http://www.michael.beiter.org)
- [Project home](http://mbeiter.github.io/eaudit4j/)
- [Build instructions](BUILD.md)
- [GitHub Issue Tracker](https://github.com/mbeiter/eaudit4j/issues)
- [Contribute](CONTRIBUTING.md) - Some pointers for contributing
- [Configuration instructions](CONFIG.md)

## License

Copyright (c) 2016, Michael Beiter (<michael@beiter.org>)

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list of conditions and the following 
  disclaimer.
- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following 
  disclaimer in the documentation and/or other materials provided with the distribution.
- Neither the name of the copyright holder nor the names of the contributors may be used to endorse or promote products 
  derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Performance Test

## Performance core
This module is implemented by class org.beiter.michael.eaudit4j.Performance. It generates a JMeter JTL
file with performance results.

In order to use, create an instance of Performance class:
```Java
Performance(int numThreads, int numLoops, long rampUpTimeInMilliseconds,
            File outputFile, int maxTestTime, TimeUnit maxTestTimeUnit)
```
then execute a test:
```Java
public <P> void runTests(String label, Supplier<P> parametersSupplier, Consumer<P> testParametersConsumer)
```
When the test is running, the _parametersSupplier_ will create an object that will be used in _testParametersConsumer_.

Only _testParametersConsumer_ will be logged in JTL file.

Example of use:
```Java
        Performance p = new Performance(10, 5, 50, new File("~/someFile.jtl"), 2, TimeUnit.HOURS);
        final Random random = new Random();
        p.runTests("Mocked test", () -> random.nextLong(1000), param -> {
            System.out.print(".");
            try {
                Thread.sleep(100 + param);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
```

## PerconaTest
This test always creates the needed database tables (dropping before if exists) in order to perform the audit.
Before test execute, it create a warm up execution of tests for the time defined.

The PerconaTest use some properties configured in Java System Properties:
 * TEST PARAMETERS
     * numThreads (**default: `65`**)
     * numLoops (**default: `Integer.MAX_VALUE`**)
     * rumpUpInMilliseconds (**default: `1`**)
     * jtlFile (**default: `<user_home>/test.jtl`**)
     * storeFields (**default: `false`**) # if true it stores indexed field values in a separated tale on DB, default false
     * maxTestTime (**default: `10`**)
     * maxTestTimeUnit (**default: `SECONDS`**)
     * maxWarmUpTime (**default: `1`**)
     * maxWarmUpTimeUnit (**default: `SECONDS`**)
 * DB PARAMETERS
     * DriverClass (**default: `com.mysql.jdbc.Driver`**)
     * jdbcUrl (**default: `jdbc:mysql://192.168.99.100:3306/test`**) 
     * user (**default: `root`**)
     * password (**current password**)
     * minPoolSize (**default: `5`**)
     * acquireIncrement (**default: `10`**)
     * maxPoolSize (**default: `140`**)
     * maxStatements (**default: `500`**)

### Execution using fat jar
Use the above command as template:
```
java -DnumThreads=10 -DrumpUpInMilliseconds=1000 -DjtlFile=./perconaTest.jtl -DmaxTestTime=30 \
 -DmaxTestTimeUnit=MINUTES -DmaxWarmUpTime=10 -DmaxWarmUpTimeUnit=SECONDS \
 -DjdbcUrl=jdbc:mysql://192.168.99.100:3306/test -Duser=root -Dpassword=rootPsw \
 -DmaxPoolSize=140 -DmaxStatements=400 -cp auditPerformanceTest-jar-with-dependencies.jar \
 org.beiter.michael.eaudit4j.performance.mysql.MysqlTest
```



## CassandraTest
The Audit performance test against Cassandra will destroy and recreate the audit keyspace and execute the tests.

The test parameters will be retrieved by Java System Properties. The allowed properties are:
 * TEST PARAMETERS
     * numThreads (**default: `40`**)
     * numLoops (**default: `Integer.MAX_VALUE`**)
     * rumpUpInMilliseconds (**default: `1`**)
     * jtlFile (**default: `<user_home>/test.jtl`**)
     * maxTestTime (**default: `10`**)
     * maxTestTimeUnit (**default: `MINUTES`**)
     * maxWarmUpTime (**default: `1`**)
     * maxWarmUpTimeUnit (**default: `MINUTES`**)
 * DB PARAMETERS
     * contactPoint (**default: `192.168.99.100`**)
     * port (**default: `9142`**)
     * username (**default: `cassandra`**)
     * password (**default: `cassandra`**)


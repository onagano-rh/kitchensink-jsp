# How to install a JDBC driver as a module

1. Download a driver you want from the Maven repository

    # IBM DB2 for iSeriese (AS400): ls ~/.m2/repository/net/sf/jt400/jt400/20.0.6/jt400-20.0.6.jar
    mvn dependency:get -Dartifact=net.sf.jt400:jt400:20.0.6

2. Install the driver as a module

Here is example of IBM DB2 for AS400.
You can find other JBoss CLI `module add` command examples in the Configu Guide.

    cd $JBOSS_HOME
    ./bin/jboss-cli.sh
    [disconnected /] module add --name=com.ibm.as400 --resources=~/.m2/repository/net/sf/jt400/jt400/20.0.6/jt400-20.0.6.jar --dependencies=javaee.api,sun.jdk,ibm.jdk,javax.api,javax.transaction.api
    [disconnected /] exit

3. Configure the driver and a datasource using it in standalone.xml

Here is example of IBM DB2 for AS400 (non-XA).
You can find other JBoss CLI `module add` command examples in the Configu Guide.

    # Start server first so that you can connect to it by JBoss CLI
    $JBOSS_HOME/bin/jboss-cli.sh -c
    [standalone@localhost:9990 /] /subsystem=datasources/jdbc-driver=ibmdb2as400:add(driver-name=ibmdb2as400,driver-module-name=com.ibm.as400,driver-xa-datasource-class-name=com.ibm.as400.access.AS400JDBCXADataSource)
    [standalone@localhost:9990 /] data-source add --name=DB2DS --jndi-name=java:jboss/DB2DS --driver-name=ibmdb2as400 --connection-url=jdbc:as400://localhost;libraries=ibmdb2db; --user-name=admin --password=admin --validate-on-match=true --background-validation=false --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter --min-pool-size=0 --max-pool-size=50

Change the datasource configuration to what you want.

# Links

- [Red Hat JBoss Enterprise Application Platform (EAP) 7 Supported Configurations](https://access.redhat.com/articles/2026253)
- [EAP 7.4 Config Guide, 12.15. Example Datasource Configurations](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html/configuration_guide/datasource_management#example_datasource_configurations)


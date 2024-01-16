# How to install a JDBC driver as a module

1. Download a driver you want from the Maven repository

    # PostgreSQL: ~/.m2/repository/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar
    mvn dependency:get -Dartifact=org.postgresql:postgresql:42.7.1
    # MySQL: ~/.m2/repository/com/mysql/mysql-connector-j/8.2.0/mysql-connector-j-8.2.0.jar
    mvn dependency:get -Dartifact=com.mysql:mysql-connector-j:8.2.0
    # Oracle: ~/.m2/repository/com/oracle/database/jdbc/ojdbc11/23.2.0.0/ojdbc11-23.2.0.0.jar
    mvn dependency:get -Dartifact=com.oracle.database.jdbc:ojdbc11:23.2.0.0
    # IBM DB2: ~/.m2/repository/com/ibm/db2/jcc/11.5.8.0/jcc-11.5.8.0.jar
    mvn dependency:get -Dartifact=com.ibm.db2:jcc:11.5.8.0

2. Install the driver as a module

Here is an example of PostgreSQL.
You can find other JBoss CLI `module add` command examples in the Configu Guide.

    cd $JBOSS_HOME
    ./bin/jboss-cli.sh
    [disconnected /] module add --name=org.postgresql --resources=~/.m2/repository/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar --dependencies=javaee.api,sun.jdk,ibm.jdk,javax.api,javax.transaction.api
    [disconnected /] exit

3. Configure the driver and a datasource using it in standalone.xml

Here is an example of PostgreSQL.
You can find other JBoss CLI `module add` command examples in the Configu Guide.

    # Start server first so that you can connect to it by JBoss CLI
    $JBOSS_HOME/bin/jboss-cli.sh -c
    [standalone@localhost:9990 /] /subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)
    [standalone@localhost:9990 /] xa-data-source add --name=PostgresXADS --jndi-name=java:jboss/PostgresXADS --driver-name=postgresql --user-name=admin --password=admin --validate-on-match=true --background-validation=false --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter --xa-datasource-properties={"ServerName"=>"localhost","PortNumber"=>"5432","DatabaseName"=>"postgresdb"}

Change the datasource configuration to what you want.

# Links

- [Red Hat JBoss Enterprise Application Platform (EAP) 7 Supported Configurations](https://access.redhat.com/articles/2026253)
- [EAP 7.4 Config Guide, 12.15. Example Datasource Configurations](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html/configuration_guide/datasource_management#example_datasource_configurations)


# JDBC module

This module provides JDBC support to execute database queries.

For example, it can be used to set initial test data in a database. SQL queries are sent from file.

## Setup

1. Add a dependency of this module in your project.
2. Add JDBC driver dependency based on the database you are using.
3. Set `test-automation.integration.jdbc.*` properties in `application-override.yml`.
   * Define JDBC driver class name `driverClassName`.
   * Specify database `url`, `username` and `password` based on your database settings.
4. Create class that implements `JdbcUrlProvider`.

To execute database query, call `executeFromClasspath` method of `SqlScriptExecutor` class.
Pass SQL query file path to the `executeFromClasspath` method as a parameter.
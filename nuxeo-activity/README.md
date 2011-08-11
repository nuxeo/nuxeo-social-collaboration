# Nuxeo Activity

This addon provides survey creation and management in Nuxeo DM, with participation and visualization (of results) through an OpenSocial gadget. 

## Building and deploying

To see the list of all commands available for building and deploying, use the following:

    $ ant usage


### How to build

You can build Nuxeo Activity with:

    $ ant build

If you want to build and launch the tests, do it with:

    $ ant build-with-tests


### How to deploy

#### Deploy the module

Configure the build.properties files (starting from the `build.properties.sample` file to be found in the current folder), to point your Tomcat instance:

    $ cp build.properties.sample build.properties
    $ vi build.properties

You can then deploy Nuxeo Activity to your Tomcat instance with:

    $ ant deploy-tomcat

#### Configure the Datasource

Nuxeo Activity relies on a Datasource `nxactivities` which is not defined in a default distribution.
The easiest way to add id is to override the `nuxeo.xml` file in a custom template. Let's say we use the default template,for the H2 database.

In your Nuxeo instance, create the folder `templates/mytemplate/conf/Catalina/localhost`:

    $ mkdir -p templates/mytemplate/conf/Catalina/localhost

Then copy the `nuxeo.xml` file from the configuration you want to use. Here we will use the one from the `default` template:

    $ cp templates/default/conf/Catalina/localhost/nuxeo.xml templates/mytemplate/conf/Catalina/localhost/

Edit the `nuxeo.xml` file and add the following datasource after the one for `jdbc/nxuidsequencer`:

    <Resource name="jdbc/nxactivities" auth="Container" type="javax.sql.DataSource"
      maxActive="100" maxIdle="30" maxWait="10000" driverClassName="org.apache.derby.jdbc.EmbeddedDriver"
      url="jdbc:derby:${nuxeo.data.dir}/derby/${nuxeo.db.name};create=true" />

Edit the `bin/nuxeo.conf` file to deploy your custom template after the default configuration:

    nuxeo.templates=default,mytemplate

Restart the Nuxeo instance.

## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management] [1] and packaged applications for [document management] [2], [digital asset management] [3] and [case management] [4]. Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

[1]: http://www.nuxeo.com/en/products/ep
[2]: http://www.nuxeo.com/en/products/document-management
[3]: http://www.nuxeo.com/en/products/dam
[4]: http://www.nuxeo.com/en/products/case-management

More information on: <http://www.nuxeo.com/>



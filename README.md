# Nuxeo Social Collaboration

This addon provides a set of tools to build your social network on top of Nuxeo DM.

Here is the list of Social modules you can find inside this addon:

* Nuxeo Social Workspace: this module adds social features to existing Workspaces, like a dashboard, members management and invitations.

## How to build

Using maven 2.2.1 or later, from root of the `nuxeo-social-collaboration` folder:

    $ mvn clean install

This will build all the modules inside the `nuxeo-social-collaboration` addon.
You can also just build
the Nuxeo Social Workspace module by doing the following:

    $ cd nuxeo-social-workspace
    $ mvn clean install

Then copy the built jars `**/target/nuxeo-*-SNAPSHOT.jar` into the
`nxserver/bundles` folder of your Nuxeo DM (assuming the default
tomcat package).
  
  
## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep) and packaged applications for [document management](http://www.nuxeo.com/en/products/document-management), [digital asset management](http://www.nuxeo.com/en/products/dam) and [case management](http://www.nuxeo.com/en/products/case-management). Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information on: <http://www.nuxeo.com/>

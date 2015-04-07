# Nuxeo Social Collaboration

This addon provides a set of tools to build your social network on top of Nuxeo DM.

Here is the list of Social modules you can find inside this addon:

* Nuxeo Social Workspace: provides a new SocialWorkspace document with social features, like a dashboard, members management and invitations.
* Nuxeo User Relationship: provides a Service handling relations between two objects, a UI to manage relations between users.
* Nuxeo Mini Message: provides a service, JSF view and OpenSocial gadget to add / remove / display mini messages created by users.
* Nuxeo User Activity Stream: provides a JSF view and OpenSocial gadget to display what are doing the users you added in your relations.
* Nuxeo Wall: provides an OpenSocial gadget to display a wall for a Document, Workspace and Social Workspace for now.

# Building

You can build Nuxeo Social Collaboration with:

  $ ant build

If you want to build and launch the tests, do it with:

  $ ant build-with-tests

## Deploying

Install [the Nuxeo Social Collaboration](https://connect.nuxeo.com/nuxeo/site/marketplace/package/nuxeo-social-collaboration).
Or manually copy the built artifacts into `$NUXEO_HOME/templates/custom/bundles/` and activate the "custom" template.

## QA results

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=addons_nuxeo-social-collaboration-master)](https://qa.nuxeo.org/jenkins/job/addons_nuxeo-social-collaboration-master/)

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at www.nuxeo.com.

# Migrating from Social Collaboration

As Social Collaboration is deprecated since Nuxeo Platform 6.0, we have written a SQL script that will allow you to continue using all your data created within Social Collaboration in a Nuxeo Platform without Social Collaboration installed.

You can find the script [here](social-collaboration-migration.sql).

This script was tested on PostgreSQL.

# What Does the Script Do?

**Document Types Migration**

Migrate all Social Collaboration specific types to default ones:

- `SocialDomain` => `Domain`
- `SocialFolder` => `Folder`
- `SocialWorkspace` => `Workspace`
- `NewsItemsRoot` => `Folder`
- `NewsItem` => `Note`
- `Article` => `Note`

All icons are also migrated to have the right ones.

**Keeping Article / NewsItem Uploaded Image**

If an image was uploaded to an `Article` or `NewsItem`, it's migrated to be an attachment (stored on the `files` schema).

**Deleting Unused Documents**

Deletes the following documents (and all their children):

- All documents of type `SocialSection` and `DashboardSpacesContainer`
- Document with name `collaborationDashboardSpace` (the one storing the Collaboration dashboard)

**Dropping Unused Schema Tables**

Drops the following tables:

- `social_document`
- `social_workspace`
- `content_picture`
- `socialprofile`

**Deleting Everything Related to Social Collaboration Registration Requests**

Deletes documents of type `SocialRegistration`.

Drops the table `social_user_registration`

**Removing Social Workspace Validation Tasks**

**Removing All ACLs Defined by Social Collaboration**

- `socialWorkspaceAcl`
- `newsItemsRootAcl`
- `publicSocialWorkspaceAcl`

**Removing Social Collaboration Gadgets from All Dashboards** 

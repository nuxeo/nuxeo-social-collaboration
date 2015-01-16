# Migrating from Social Collaboration

As Social Collaboration is deprecated since Nuxeo Platform 6.0, we have written a SQL script that will allow you to continue using all your data created within Social Collaboration in a Nuxeo Platform without Social Collaboration installed.

You can find the script [here](social-collaboration-migration.sql).

This script was tested on PostgreSQL.

# What does the script do?

**Document types migration**

Migrate all Social Collaboration specific types to default ones:

- `SocialDomain` => `Domain`
- `SocialFolder` => `Folder`
- `SocialWorkspace` => `Workspace`
- `NewsItemsRoot` => `Folder`
- `NewsItem` => `Note`
- `Article` => `Note`

All icons are also migrated to have the right ones.

**Keeping Article / NewsItem uploaded image**

If an image was uploaded to a `Article` or `NewsItem`, it's migrated to be an attachment (stored on the `files` schema).

**Delete unused documents**

Delete the following documents (and all their children):

- All documents of type `SocialSection` and `DashboardSpacesContainer`
- Document with name `collaborationDashboardSpace` (the one storing the Collaboration dashboard)

**Drop unused schema tables**

Drop the following tables:

- `social_document`
- `social_workspace`
- `content_picture`
- `socialprofile`

**Delete everything related to Social Collaboration registration requests**

Delete documents of type `SocialRegistration`.

Drop table `social_user_registration`

**Remove Social Workspace validation tasks**

**Remove all ACLs defined by Social Collaboration**

- `socialWorkspaceAcl`
- `newsItemsRootAcl`
- `publicSocialWorkspaceAcl`

**Remove Social Collaboration gadgets from all dashboards** 
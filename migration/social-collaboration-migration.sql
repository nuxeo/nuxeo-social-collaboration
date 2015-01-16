-- migrate default ypes
UPDATE common SET icon = '/icons/domain.gif' FROM hierarchy WHERE common.id = hierarchy.id AND hierarchy.primarytype = 'SocialDomain';
UPDATE hierarchy SET primarytype = 'Domain' WHERE primarytype = 'SocialDomain';
UPDATE common SET icon = '/icons/folder.gif' FROM hierarchy WHERE common.id = hierarchy.id AND hierarchy.primarytype = 'SocialFolder';
UPDATE hierarchy SET primarytype = 'Folder' WHERE primarytype = 'SocialFolder';
UPDATE common SET icon = '/icons/workspace.gif' FROM hierarchy WHERE common.id = hierarchy.id AND hierarchy.primarytype = 'SocialWorkspace';
UPDATE hierarchy SET primarytype = 'Workspace' WHERE primarytype = 'SocialWorkspace';
UPDATE common SET icon = '/icons/folder.gif' FROM hierarchy WHERE common.id = hierarchy.id AND hierarchy.primarytype = 'NewsItemsRoot';
UPDATE hierarchy SET primarytype = 'Folder' WHERE primarytype = 'NewsItemsRoot';
UPDATE common SET icon = '/icons/note.gif' FROM hierarchy WHERE common.id = hierarchy.id AND hierarchy.primarytype = 'NewsItem';
UPDATE hierarchy SET primarytype = 'Note' WHERE primarytype = 'NewsItem';
UPDATE common SET icon = '/icons/note.gif' FROM hierarchy WHERE common.id = hierarchy.id AND hierarchy.primarytype = 'Article';
UPDATE hierarchy SET primarytype = 'Note' WHERE primarytype = 'Article';


-- migrate the content_picture schema to files:files
CREATE EXTENSION "uuid-ossp";
-- add new rows in hierarchy
INSERT INTO hierarchy (id, parentid, pos, name, isproperty, primarytype)
SELECT uuid_generate_v4(), cp.id, 0, 'files', true, 'file'
FROM content_picture cp;
-- update new ids
UPDATE content_picture cp SET id = h.id
FROM hierarchy h WHERE name = 'files' AND parentid = cp.id;
-- copy from content_picture to file
INSERT INTO file (id, filename)
SELECT cp.id, cp.picturename
FROM content_picture cp;
-- update contentpict:picture rows
UPDATE hierarchy h1 SET parentid = h2.id
FROM hierarchy h2 WHERE h2.name = 'files' AND h2.parentid = h1.parentid AND h1.name = 'contentpict:picture';
UPDATE hierarchy SET name = 'file' WHERE name = 'contentpict:picture';


-- delete no more needed documents
DELETE FROM hierarchy WHERE primarytype = 'SocialSection';
DELETE FROM hierarchy WHERE primarytype = 'DashboardSpacesContainer';
DELETE FROM hierarchy where name = 'collaborationDashboardSpace';


-- drop tables related to useless schemas
DROP TABLE social_document;
DROP TABLE social_workspace;
DROP TABLE content_picture;
DROP TABLE socialprofile;


-- cleanup registration request
DELETE FROM hierarchy WHERE primarytype = 'SocialRegistration';
DROP TABLE social_user_registration;


-- cleanup tasks
DELETE FROM hierarchy WHERE id IN (SELECT id FROM task WHERE name = 'validateSocialWorkspace');


-- cleanup acls
DELETE FROM acls WHERE name = 'socialWorkspaceAcl';
DELETE FROM acls WHERE name = 'newsItemsRootAcl';
DELETE FROM acls WHERE name = 'publicSocialWorkspaceAcl';

-- cleanup no more existing gadgets
DELETE FROM hierarchy WHERE id IN (SELECT h.parentid FROM wcopensocial wco, hierarchy h WHERE h.id = wco.id AND gadgetname IN ('articles', 'publicarticles', 'news', 'join', 'members', 'usersocialworkspaces', 'publicdocuments', 'publicsocialworkspaces', 'socialworkspaceactivitystream', 'socialworkspaceminimessages', 'library'));

-- rebuild acls
select nx_rebuild_read_acls();

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title></title>
<link href="create_document.css" rel="stylesheet" type="text/css">
</head>
<body>
<form action="${This.path}/createFolder" method="post" enctype="application/x-www-form-urlencoded" target="_parent">
<h3>Create folder in: '"${currentDoc.title}"' </h3>
<table>
<tr>
<td>Title</td>
<td><input type="text" name="dc:title" /></td>
</tr>
<tr>
<td>Description</td>
<td><textarea name="dc:description" rows="5"></textarea></td>
</tr>
</table>
<input type="submit" name="createFolder" value="Create" onclick="parent.submitForm(this)"/>
<input type="hidden" name="doctype" value="Folder" />
<input type="hidden" name="docRef" value="${currentDoc.id}" />

</form>
</body>
</html>
<div class="customSearchBox">
<form action="${This.path}/documentList" method="post" enctype="application/x-www-form-urlencoded" name="searchForm">
<div class="wideDefinition">
<input type="text" name="queryText" value="${queryText}" class="searchBox">
<input type="hidden" name="docRef" value="${socialWorkspace.id}">
<input type="submit" value="" name="search" class="button">
</div>
</form>
</div>

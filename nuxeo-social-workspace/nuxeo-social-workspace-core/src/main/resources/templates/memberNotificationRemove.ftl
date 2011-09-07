<html>
Dear member of <a href="${docUrl}">${Document.title}</a>,

I inform you our leaving members:
<ul>
<#list Context.principalsList as principal>
    <li>${principal}</li>
</#list>
</ul>

Regards,
</html>
<html>
Dear member of <a href="${docUrl}">${Document.title}</a>,

Let me introduce you our new incoming members:
<ul>
<#list Context.principalsList as principal>
    <li>${principal}</li>
</#list>
</ul>

Regards,
</html>
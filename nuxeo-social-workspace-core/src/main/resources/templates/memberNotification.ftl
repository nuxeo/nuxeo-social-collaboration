<html>
Dear member of <a href="${docUrl}">${Document.title}</a>,

<#if Context.addedMembers?has_content  >
Let me introduce you our new incoming members:
<ul>
<#list Context.addedMembers as addedMembers>
    <li>${addedMembers}</li>
</#list>
</ul>
</#if>
<#if Context.removedMembers?has_content >
I inform you that the following members leave us :
<ul>
<#list Context.removedMembers as removedMembers>
    <li>${removedMembers}</li>
</#list>
</ul>
</#if>


Regards,
</html>
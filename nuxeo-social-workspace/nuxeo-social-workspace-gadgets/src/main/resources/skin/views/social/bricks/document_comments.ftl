<tr><td colspan="4" style="border:0; padding-left:15px">
<table style="width: 100%;">
<tr>
  <td style="border-style:none;width:30%">
    ${comment.comment.author}
  </td>
  <td style="border-style:none;width:30%">
    ${comment.comment.text}
  </td>
  <td style="border-style:none;width:40%">
    <a href="#" id="${comment.id}" class="comment_button">Answer</a>
  </td>
</tr>
<tr class="${comment.id}">
  <td style="border-style:none;"/>
  <td style="border-style:none;"/>
  <td style="border-style:none;">
    <div id="box_${comment.id}" style="display:none">
        <textarea id="commentContent_${comment.id}" ></textarea><br/>
        <input type="button" value="Comment" onclick="Library.addComment('${doc.id}','#commentContent_${comment.id}','${comment.id}')"/>
    </div>
  </td>
</tr>
<#assign commentChildren = This.getCommentChildren(doc,comment)>
<#if commentChildren??>
  <#list commentChildren as comment>
    <#include "@bricks/document_comments">
  </#list>
</#if>
</table>
</td>
</tr>
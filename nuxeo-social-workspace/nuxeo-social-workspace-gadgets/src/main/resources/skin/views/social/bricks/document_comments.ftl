<tr>
  <td>
    ${comment.comment.author}
  </td>
  <td>
    ${comment.comment.text}
  </td>
  <td>
    <a href="#" id="${comment.id}" class="comment_button">Answer</a>
  </td>
</tr>
<tr class="${comment.id}">
  <td/>
  <td/>
  <td>
    <div class='panel' id="box_${comment.id}" style="display:none">
        <textarea id="commentContent_${comment.id}" style="width:390px;height:23px; border:1px solid #999999;"></textarea><br/>
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

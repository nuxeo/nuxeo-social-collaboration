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
<tr>
  <td/>
  <td/>
  <td>
    <div class='panel' id="slide${comment.id}" style="display:none">
      <form action="" method="post">
        <textarea style="width:390px;height:23px; border:1px solid #999999;"></textarea><br />
          <input type="submit" value=" Comment " />
      </form>
    </div>
  </td>
</tr>
<#assign commentChildren = This.getCommentChildren(doc,comment)>
<#if commentChildren??>
  <#list commentChildren as comment>
    <#include "@bricks/document_comments">
  </#list>
</#if>

<tr>
  <td colspan="4">
    <#assign comments = This.getComments(doc)>
    <a href="#" id="${doc.id}" class="comment_display_button">Show ${comments?size} threads</a>
    <a href="#" id="${doc.id}" class="root_comment_button">Comment</a>
	<a href="#" id="like_${doc.id}" onclick="Library.docLike('${doc.id}');" class="like_link">${This.getLikeStatus(doc)}</a>
    <div class='panel' id="box_comment_${doc.id}" style="display:none">
        <textarea id="rootCommentContent_${doc.id}" style="width:390px;height:23px; border:1px solid #999999;"></textarea><br/>
        <input type="button" value="Comment" onclick="Library.addComment('${doc.id}','#rootCommentContent_${doc.id}')"/>
    </div>
    <div id="${doc.id}_display" style="display:none">
      <table id="comments_list_${doc.id}" class="dataList">
        <#list comments as comment>
          <#include "@bricks/document_comments">
        </#list>
      </table>
    </div>
  </td>
</tr>
<script type="text/javascript">
    $(document).ready(function()
      {
        // Activate comment box toggles
        // All comments display handler
        $(".comment_display_button").click(function(){
          var element = $(this);
          var id = element.attr("id");
          $("#"+id+"_display").slideToggle(300);
          $("#"+id+"_display").style.display = true;
          return false;
        });
        // 'Comment' display handler
        $(".root_comment_button").click(function(){
          var element = $(this);
          var id = element.attr("id");
          $("#box_comment_"+id).slideToggle(300);
          $("#box_comment_"+id).style.display = true;
          return false;
        });
        // 'Answer' display handler
        $(".comment_button").click(function(){
          var element = $(this);
          var id = element.attr("id");
          $("#box_"+id).slideToggle(300);
          $("#box_"+id).style.display = true;
          return false;
        });});
</script>
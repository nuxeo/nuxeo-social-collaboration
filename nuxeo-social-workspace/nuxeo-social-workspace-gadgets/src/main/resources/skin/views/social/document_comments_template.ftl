<tr>
  <td colspan="4" style="border-style:none;">
    <#assign comments = This.getComments(doc)>
    <a href="#" id="${doc.id}" class="comment_display_button">Show ${comments?size} threads</a>
    <a href="#" id="${doc.id}" class="root_comment_button">Comment</a>
	<a href="#" id="like_${doc.id}" onclick="Library.docLike('${doc.id}');" class="like_link">${This.getLikeStatus(doc)}</a>
    <div class='panel' id="box_comment_${doc.id}" style="display:none">
        <textarea id="rootCommentContent_${doc.id}" style="width:390px;height:23px; border:1px solid #999999;"></textarea><br/>
        <input type="button" value="Comment" onclick="Library.addComment('${doc.id}','#rootCommentContent_${doc.id}')"/>
    </div>
    <div id="display_${doc.id}" style="display:none">
      <table id="comments_list_${doc.id}" style="border-bottom: 0px;width: 100%">
        <#list comments as comment>
          <#include "@bricks/document_comments">
        </#list>
      </table>
    </div>
  </td>
</tr>
<script type="text/javascript">
$(document).ready(function () {
    // Init comment display link
    if(${comments?size}==0){
        $(".comment_display_button").hide();
    }
    // Activate comment box toggles
    // All comments display handler
    $(".comment_display_button").click(function () {
        Library.commentsDisplayHandler($(this), ${comments?size}, '${doc.id}');
        Library.handleLinkClick($(this), "#display_");
    });
    // 'Comment' display handler
    $(".root_comment_button").click(function () {
        Library.commentsDisplayHandler($(".comment_display_button"), ${comments?size}, '${doc.id}');
        Library.handleLinkClick($(this), "#box_comment_");
    });
    // 'Answer' display handler
    $(".comment_button").click(function () {
        Library.handleLinkClick($(this), "#box_");
    });
});
</script>
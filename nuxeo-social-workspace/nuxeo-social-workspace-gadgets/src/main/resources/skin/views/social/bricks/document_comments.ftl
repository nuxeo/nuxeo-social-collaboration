<div>
  <script type="text/javascript" >
    $(document).ready(function()
      {
        $(".comment_button").click(function(){
        var element = $(this);
        var id = element.attr("id");
        $("#slide"+id).slideToggle(300);
        $("#slide"+id).style.display = true;
        return false;
      });});
  </script>
  <#list This.getComments(doc) as comment>
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
      <td>
        <div class='panel' id="slide${comment.id}" style="display:none">
          <form action="" method="post">
            <textarea style="width:390px;height:23px; border:1px solid #999999;"></textarea><br />
              <input type="submit" value=" Comment " />
          </form>
        </div>
      <td>
    </tr>
   </#list>
</div>

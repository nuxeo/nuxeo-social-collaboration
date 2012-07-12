<#if This.hasUserLiked(doc)>
  <img src="${contextPath}/icons/unlike.png" />
  ${This.getLikesCount(doc)}
<#else>
  <img src="${contextPath}/icons/like.png" />
  ${This.getLikesCount(doc)}
</#if>

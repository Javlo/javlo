<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<form method="post" action="${info.currentURL}" class="standard-form">
        <input type="hidden" name="previewEdit" value="true" />
        <input type="hidden" name="module" value="macro" />
        <input type="hidden" name="macro" value="import-image-from-url" />
        <input type="hidden" name="mode" value="3" />
        <input type="hidden" name="macro-import-image-from-url" value="macro-import-image-from-url" />
        <input type="hidden" name="webaction" value="macro.executeInteractiveMacro" />
        <div class="line">
            <label for="url">page url (extract images from this page)</label>
            <input id="url" name="url" type="text"  />
            <button>load url</button>
        </div>

</form>

<c:if test="${not empty images}">
<form method="post" action="${info.currentURL}" class="standard-form">

    <div class="images" style="display: flex; align-content: space-around; flex-wrap: wrap;">

        <input type="hidden" name="webaction" value="import-image-from-url.upload" />

        <c:forEach var="entry" items="${images}">
            <c:set var="image" value="${entry.value}" />
            <div class="line" style="margin: 1rem; padding: 1rem; border: 1px #fff solid; border-radius: 3px; width: calc(20% - 2rem);">
                <label style="width: 100%">
                    <div style="text-align: center; margin-bottom: 1rem;">
                        <input type="checkbox" name="img-${image.id}" />
                    </div>
                    <figure>
                    <img style="width: 100%" src="${image.url}" alt="${image.alt}" />
                        <figcaption style="margin-top: 1rem; font-size: 0.8rem;">${image.alt}</figcaption>
                    </figure>
                </label>
            </div>
        </c:forEach>

    </div>

    <div class="action">
        <input type="submit" value="import" />
    </div>

</form>
</c:if>




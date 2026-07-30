<%@ taglib uri="jakarta.tags.core" prefix="c" %><div class="maincontent display-error" style="padding: 1rem; background-color: #fff;">
    <div class="content">
    <h2><c:out value="${param.title}" /></h2>
    <code><c:out value="${param.body}" /></code>
    </div>
</div>

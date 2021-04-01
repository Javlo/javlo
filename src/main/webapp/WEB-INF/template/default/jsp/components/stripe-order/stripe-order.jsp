<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%>
<c:if test="${basket.step==3 || !contentContext.asViewMode}">
  <button id="stripe-checkout-button-${compid}"
  class="btn btn-primary btn-stripe btn-pay mt-3 mb-3"
  ${basket.step!=3?'disabled':''}>${vi18n['ecom.pay']}</button>
  </c:if>
 
  <script>
  document.addEventListener("DOMContentLoaded", function(event) { 
    var stripe = Stripe('${publicKey}');
    var checkoutButton = document.getElementById("stripe-checkout-button-${compid}");
    checkoutButton.addEventListener("click", function () {
  
    fetch("${createSessionUrl}", {
      method: "POST",
    })
      .then(function (response) {
        return response.json();
      })
      .then(function (session) {
        return stripe.redirectToCheckout({ sessionId: session.id });
      })
      .then(function (result) {
        if (result.error) {
          alert(result.error.message);
        }
      })
      .catch(function (error) {
        console.error("Error:", error);
      });
     });
  })
  </script>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${basket.step==4 || !contentContext.asViewMode}">
	<div class="btn-pay-group">
		<button id="stripe-checkout-button-${compid}" class="btn btn-outline-primary btn-stripe btn-pay btn-cc mt-3 mb-3 d-flex justify-content-between align-items-center" ${basket.step!=4?'disabled':''}>
			<jsp:include page="cc.svg" />
			${vi18n['ecom.pay.cc']}
		</button>

		<button id="stripe-checkout-button-bancontact-${compid}" class="btn btn-outline-primary btn-stripe btn-pay btn-bancontact mt-3 mb-3 d-flex justify-content-between align-items-center" ${basket.step!=4?'disabled':''}>
			<jsp:include page="bancontact.svg" />
			&nbsp;${vi18n['ecom.pay.bancontact']}
		</button>
	</div>

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
    
    <c:if test="${bancontact}">
    /* bancontact */
    var checkoutButton = document.getElementById("stripe-checkout-button-bancontact-${compid}");
    checkoutButton.addEventListener("click", function () {
  	  //Redirects to Bancontact website or app
  	  stripe.confirmBancontactPayment(
  	    '${PAYMENT_INTENT_CLIENT_SECRET}',
  	    {
  	      payment_method: {
  	        billing_details: {
  	          name: "${name}"
  	        }
  	      },
  	      return_url: '${bancontactSuccessURL}',
  	    }
  	  ).then(function(result) {
  	    if (result.error) {
  	      console.log("error : ",result.error);
  	    }
  	  });
    });    
    </c:if>
    
  })
  </script>
</c:if>
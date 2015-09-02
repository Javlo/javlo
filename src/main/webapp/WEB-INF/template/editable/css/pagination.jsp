@page {   
	margin: 1cm;
   /*@top-left {
   	content: "${info.globalTitle}";
   	font-family: sans-serif; 
   	color: #333333;
   	font-size: 9px;    
   }   
   @top-right {
   	content: "${info.pageTitle}";
   	font-family: sans-serif;
   	color: #333333;
   	font-size: 9px;
   }*/
   @bottom-center {
 	 content: counter(page) "/" counter(pages);
 	 font-family: sans-serif;
 	 color: #333333;
 	 font-size: 10px;
   }
}

img {
   	page-break-before: avoid;
}
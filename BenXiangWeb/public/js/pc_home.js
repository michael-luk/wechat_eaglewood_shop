$(function(){

	var li3 = "";	
	for (var i = 1; i<=6 ; i++) {
		li3 +='<div class="themeimg">'
		li3 +='<a href="/p/product?catalogsName='+i+'">'
		li3 +='<img src="/showimg/upload/'+i+'.jpg"/>'
		li3 +='</a>'
		li3 +='</div>'	
	}	
		$(".forimg").html(li3);	

	
	$.ajax({
	    type: "get",
	    url: "/products" ,
	    dataType: "json",
	    success:function(callback){
	    	console.log(callback);	
	    		var li1 = "";	
	    		for (var i = 0; i< callback['data'].length& i<8 ; i++) {
	    			str=callback['data'][i].images
	    			var strs= new Array(); //定义一数组 
	    			strs=str.split(","); //字符分割 
	    			li1 +='<li>'
					li1 +='<div class="pro_img">'
					li1 += '<a href="/p/productmessage?id='+ callback['data'][i].id+'"><img src="/showimg/upload/'+strs[0] +'"/></a>'
					li1 +='</div>'
					li1 +='<p class="prop noWrapEllipsis">'+callback['data'][i].name +'</p>'
					li1 +='<div class="protext clear">'
					/*li1 +='<span class="mey">￥<span class="mey_sp1">'+callback['data'][i].price+'</span></span>'*/
					li1 +='<div class="probnt" onclick="show()">微信购买</div>'
					li1 +='<a href="/p/productmessage?id='+ callback['data'][i].id+'"><div class="probnt1">查看详情</div></a>'
					li1 +='</div>'
					li1 +='</li>'		
	    		}	
		    		$(".pro_ul").html(li1);	
	    },      
	});	
	
	$.ajax({
	    type: "get",
	    url: "/infos" ,
	    dataType: "json",
	    success:function(callback){
	    	console.log(callback);	
	    		var li2 = "";	
	    		for (var i = 0; i< callback['data'].length& i<11 ; i++) {
					li2 +='<a href="/p/news2?newid=' +callback['data'][i].id+ '"style="color: #113533;">'
	    			li2 += '<li>'+callback['data'][i].name +'</li>'
					li2 +='</a>'
	    		}	
		    		$(".newsmain_ul").html(li2);	
	    },      
	});	
	
});
	     
   	     
	     
	     
	     
	     
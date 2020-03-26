var app = angular.module('allProductApp', []);

app.filter('safehtmls', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});

app.filter('getFirstImageFromSplitStr', function() {
	return function(splitStr, position) {
		return GetListFromStrInSplit(splitStr)[position];
	}
});

app.filter('getImageFromSplitStrtext', function() {
	return function(splitStr1, position1) {
		return GetListFromStrInSplitText(splitStr1)[position1];
	}
});

app.filter('cartCalculate', function () {
    return function (cartObj) {
        var cartItemsCount = 0
        for (x in cartObj.items) {
            cartItemsCount += cartObj.items[x].num
        }
        return cartItemsCount;
    }
});

app.controller('allProductController', [
		'$scope',
		'$http',

		//绑定id页	
		
		function($scope, $http) {
			 $scope.homes = [];
			 $scope.classw =[]
			 $scope.alls =[]
			 $scope.images1= [];
			 $http.get('/catalogs/' + GetQueryString('homeid')).success(
					 function(data, status, headers, config) {
						 if (data.flag) {
							 $scope.classw = data.data
						 } else {
							 alert(data.message);
						 }
					 });

		/*	 $http.get('/catalogs/'+GetQueryString('homeid')+'/products').success(

						function(data, status, headers, config) {
							if (data.flag) {
								$scope.alls = data.data
							} else {
								alert(data.message);
							}
						});*/
			 $http.get('/products?orderBy=isHotSale&sort=desc&size=50').success(

						function(data, status, headers, config) {
							if (data.flag) {
								$scope.alls = data.data
							} else {
								alert(data.message);
							}
						});

			$http.get('/catalogs?').success(

				function(data, status, headers, config) {
					if (data.flag) {

						$scope.homes = data.data
						setTimeout(funcw,"10");
						function funcw(){
							var li = document.getElementById("ul_id").getElementsByTagName("li").length;
							var li1 =li*1*25+8;
							if( li1<=4){
								document.getElementById("ul_id").style.width="100%"
								document.getElementById("ul_id").style.opacity="1"
								$(".liname").css("width","25%");
							}else{
								document.getElementById("ul_id").style.width= li1 +"%"
								document.getElementById("ul_id").style.opacity="1"
							}
							var url=window.location.search;
							//alert(url)
							for(i =1; i<=li; i++){
								//alert(i)
							    if(url=="?homeid="+i){
							        document.getElementById("c"+i).className="ulpac"
							    }
							}
						}
					} else {
						alert(data.message);
					}
				});


			$http.get('/cart/get').success(function (data, status, headers, config) {
				$scope.cart = JSON.parse(data.data);
			});

			//加入购物车
			$scope.addCart = function (product, num) {
					var hasItem = false
					for(x in $scope.cart.items){
						if(product.id == $scope.cart.items[x].pid){
							$scope.cart.items[x].num += num
							hasItem = true
							break
						}
					}
					if(!hasItem){
						$scope.cart.items.push({"pid":product.id,"num":num})
					}

					$http({
						method: 'PUT',
						url: '/cart/set',
						data: $scope.cart
					})
						.success(
							function (data, status, headers,
									  config) {
								if (data.flag) {
									$http.get('/cart/get').success(function (data, status, headers, config) {
										$scope.cart = JSON.parse(data.data);
									});
								} else {
									alert(data.message)
								}
							});

			}


		} ]);



function GetQueryString(name) {
	var url = decodeURI(window.location.search);
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = url.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}






function show() {
	document.getElementById("pxc3").style.opacity = "1"
	setTimeout("self.close()", 1000);
}

function close() {
	document.getElementById("pxc3").style.opacity = "0"
}








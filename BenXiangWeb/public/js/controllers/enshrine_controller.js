var app = angular.module('enshrineApp', []);

app.filter('getProductFirstImage', function () {
	return function (imageStr) {
		if(imageStr){
			return imageStr.split(",")[0]
		}
		else{
			return ""
		}
	}
});

app.controller('enshrineController', [
		'$scope',
		'$http',
		function($scope, $http) {
			$scope.favors = []
			
			
			//获取用户id
			$http.get('/users/current/login').success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.user = data.data;
							refreshData()
						} else {
						alert(data.message);
					}
				});
			
			//刷新用户收藏的产品
		function refreshData() {
			$http.get('/users/'+ $scope.user.id + '/favoriteProducts').success(
				function(data, status, headers, config) {
					if (data.flag) {
						$scope.favors = data.data;
		
					} else {
						$scope.favors = []
						 document.getElementById("dib").style.position="fixed";
						 document.getElementById("dib").innerHTML ="你还没收藏过产品哦!";
					}
				});
			}
			
			//取消收藏
			$scope.cancelFavorite = function(pid) {
				$http({
					method: 'PUT',
					url: '/users/'+ $scope.user.id + '/favoriteProduct/' +pid+ '/off',
					data: $scope.product
				}).success(

				function(data, status, headers, config) {
					if (data.flag) {
					
					} else { /* alert(data.message) */
						refreshData()
					}
					refreshData()
				});
				$scope.favoriteProduct = false
				
			};
			
					
			
			
		} ]);



function GetQueryString(name) {
	var url = decodeURI(window.location.search);
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = url.substr(1).match(reg);
	if (r != null) return unescape(r[2]);
	return null;
}

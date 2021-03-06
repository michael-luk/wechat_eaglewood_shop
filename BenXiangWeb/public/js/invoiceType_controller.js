var app = angular.module('InvoiceTypeApp', []);

app.filter('safehtml', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});

app.controller('InvoiceTypeController', [
		'$log',
		'$scope',
		'$upload',
		'$http',
		function($log, $scope, $upload, $http) {
		
			$scope.invoiceType = {}
			$scope.userId = GetQueryString('userId') 
			/*$log.log($scope.userId)*/
		    $scope.user = {}
		
		/*	$http.get('/users/' + $scope.userId).success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.user = data.data;
						} else {
							alert(data.message);
						}
					});
						*/						
				//我的发票信息
				$scope.baocun = function() {					
					$http({
						method : 'PUT',
						url : '/users/' + $scope.userId,
						data : $scope.user
					}).success(function(data, status, headers, config) {
						if (data.flag) {
							// $scope.productList.splice(indexNo, 1)
						} else {
							alert(data.message);
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
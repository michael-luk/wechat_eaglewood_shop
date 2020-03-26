var app = angular.module('LocationApp', []);

app.controller('LocationController', [
		'$scope',
		'$http',
		'$log',
		function($scope, $http, $log) {

			$scope.lid = GetQueryString('lid')
			$scope.pid = GetQueryString('pid')
			$scope.num = GetQueryString('num')
			$scope.price = GetQueryString('price')
			$scope.wishWord = GetQueryString('wishWord')
			$scope.theme = GetQueryString('theme')
			$scope.themeId = GetQueryString('themeId')
			$scope.shipPrice = GetQueryString('shipPrice')
			$scope.image = GetQueryString('image')
			$scope.userId = GetQueryString('userId')
			$scope.wishImage = GetQueryString('wishImage')
			$scope.wineBody = GetQueryString('wineBody')
			$scope.wineWeight = GetQueryString('wineWeight')
			$scope.decoration = GetQueryString('decoration')
			$scope.amount = GetQueryString('amount')


			$scope.user = {}


			if ($scope.lid) {
				$scope.editPage = true
			} else
				$scope.editPage = false

			$scope.Shipinfo = {
				'name' : '',
				'phone' : '',
				'provice' : '',
				'location' : '',
				'postCode' : '',
				'isDefault' : false,
			}
			
			$scope.shipPrices = []

			$http.get('/shipareaprices').success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.shipPrices = data.data;
						} else {
							alert(data.message);
						}
					});

			$http.get('/users/current/login').success(
				function(data, status, headers, config) {
					if (data.flag) {
						$scope.user = data.data;
					} else {
						alert(data.message);
					}
					if ($scope.editPage) {
						$http.get('/users/' +  $scope.user.id + '/shipinfos/' + $scope.lid).success(
							function(data, status, headers, config) {
								if (data.flag) {
									$scope.Shipinfo = data.data;
								} else {
									alert(data.message);
								}
							});
					}
				});
			



		   /* $scope.formSave = function(formOk){
		    	if(!formOk){
		            bootbox.alert('验证有误, 请重试');
		            return
		    	}
		        $scope.updateLocation();
		        $scope.$modalClose();
		    };*/
		    
			// 保存 or 更新
			$scope.updateLocation = function() {
				if ($scope.editPage) {
					$http({
						method : 'PUT',
						url : '/users/' +  $scope.user.id + '/shipinfos/' + $scope.lid,
						data : $scope.Shipinfo
					}).success(function(data, status, headers, config) {
						if (data.flag) {
							window.location.href = window.location.protocol + '//' + window.location.host + '/w/myLocation?userId=' + $scope.userId
							// $scope.productList.splice(indexNo, 1)
						} else {
							alert(data.message);
						}
					});
				} else {
					$http({
						method : 'POST',
						url : '/users/' +  $scope.user.id + '/shipinfos',
						data : $scope.Shipinfo
					}).success(function(data, status, headers, config) {
						if (data.flag) {
							if(window.location.search.length > 20){//下订单时候加地址
								window.location.href = window.location.protocol + '//' + window.location.host + '/w/myLocation' + window.location.search
							}
							else{//个人中心加地址
								window.location.href = window.location.protocol + '//' + window.location.host + '/w/myLocation?userId=' + $scope.userId
							}
						} else {
							alert(data.message)
						}
					});
				}
			};

		} ]);

function GetQueryString(name) {
	var url = decodeURI(window.location.search);
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = url.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}
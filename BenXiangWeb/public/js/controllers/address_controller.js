var app = angular.module('addressApp', []);


app.controller('addressController', [
		'$scope',
		'$http',
		function($scope, $http) {
			$scope.adds = []
			
			$scope.Shipinfo = {
				'name' : '',
				'phone' : '',
				'provice' : '',
				'location' : '',
				'postCode' : '',
				'isDefault' : false,
			}
			$scope.productAmount = GetQueryString('productAmount')*1
			$scope.productAmount = GetQueryString('productAmount')*1
			$scope.pid = GetQueryString('pid')*1
			$scope.quantity = GetQueryString('num')*1
			$scope.price = GetQueryString('price')*1

			$scope.user = {}
				//获取用户id
			$http.get('/users/current/login').success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.user = data.data;
							getAllLocation()
						} else {
						alert(data.message);
					}
				});
			
			//刷新数据
			function getAllLocation(){
				$http.get('/users/'+$scope.user.id+'/shipinfos').success(
				function(data, status, headers, config) {
					if (data.flag) {
						$scope.adds = data.data;
					} else {
						//alert(data.message);
					}
									
				});
			}
			
			
			//点击打开编辑地址
			$scope.xiugaidizhi = function (pid) {
				$http.get('/users/'+$scope.user.id+'/shipinfos/' + pid).success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.SeleckShipinfo = data.data;
						} else {
							alert(data.message);
						}
					});
			}
			
			//点击删除地址
			$scope.deleteLocation = function (del) {
				$http.delete('/users/'+$scope.user.id+'/shipinfos/'+ $scope.adds[del].id).success(
						function(data, status, headers, config) {
							if (data.flag) {
								$scope.adds.splice(del,1)
							} else {
								alert(data.message);
							}
						});
			}
			
			
			
			//更改默认地址状态
			$scope.on = function () {
				$scope.SeleckShipinfo.isDefault = true
			}
			$scope.off = function () {
				$scope.SeleckShipinfo.isDefault = false
			}
			//更改默认地址状态
			$scope.ond = function () {
				$scope.Shipinfo.isDefault = true
			}
			$scope.offd = function () {
				$scope.Shipinfo.isDefault = false
			}

			//选择地址
			$scope.selectLection = function (LocationIndex) {
				if($scope.quantity == 0){
					window.location.href = window.location.protocol + '//' + window.location.host + '/w/order?LocationIndex=' + LocationIndex + '&productAmount=' + $scope.productAmount
				}else{

					window.location.href = window.location.protocol + '//' + window.location.host + '/w/order' + window.location.search + '&LocationIndex=' + LocationIndex + '&productAmount=' + $scope.productAmount /*+ '&pid=' + $scope.pid + '&num=' + $scope.quantity + '&price=' + $scope.price*/
				}
			}
			// 添加地址
			$scope.addLocation = function() {
					$http({
						method : 'POST',
						url :'/users/'+$scope.user.id+'/shipinfos',
						data : $scope.Shipinfo
					}).success(function(data, status, headers, config) {
						if (data.flag) {
							getAllLocation()
						/*	if(window.location.search.length > 10){//下订单时候加地址
								window.location.href = window.location.protocol + '//' + window.location.host + '/w/order' + window.location.search + '&LocationId=' + data.data.id
							}
							else{//个人中心加地址
								//window.location.href = window.location.protocol + '//' + window.location.host + '/w/myLocation?userId=' + $scope.userId
							}*/
						} else {
							alert(data.message)
						}
					});			
			};
			
			// 编辑地址
			$scope.updateLocation = function(pid) {
			
			$http({
				method : 'PUT',
				url : '/users/'+$scope.user.id+'/shipinfos/' + pid,
				data : $scope.SeleckShipinfo
			}).success(function(data, status, headers, config) {
				if (data.flag) {
					getAllLocation()
					//window.location.href = window.location.protocol + '//' + window.location.host + '/w/myLocation?userId=' + $scope.userId
					// $scope.productList.splice(indexNo, 1)
				} else {
					alert(data.message);
				}
			});
	
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



function show() {
	document.getElementById("show").style.opacity = "1"

}
function showcl() {
	document.getElementById("show").style.opacity = "0"

}


function show1() {
	document.getElementById("“bianji").style.opacity = "1"
	document.getElementById("“bianji").style.zIndex="999"

}

function show1cl() {
	document.getElementById("“bianji").style.opacity = "0"
	document.getElementById("“bianji").style.zIndex="-999"

}

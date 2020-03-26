
var app = angular.module('HomeApp', ['ui.bootstrap']);


app.filter('safehtmls', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});


app.filter('getImageFromSplitStr', function() {
	return function(splitStr, position) {
		return GetListFromStrInSplit(splitStr)[position];
	}
});

app.filter('getImageFromSplitStrtext', function() {
	return function(splitStr1, position1) {
		return GetListFromStrInSplitText(splitStr1)[position1];
	}
});

app.filter('UploadImageFromSplitStr', function () {
	return function (splitStr, position) {
		return '/showimg/upload/' + GetListFromStrInSplit(splitStr)[position];
	}
});


app.controller('HomeController', ['$scope', '$http',

function($scope, $http) {

	$scope.homes = [];

	$scope.adList = []
	$scope.myInterval = 2000;//轮播时间间隔, 毫秒

	// 拿广告
	$http.get('/infos?classify=首页广告').success(
		function (data, status, headers, config) {
			if (data.flag) {
				$scope.adList = data.data
			} else {
//						alert(data.message);
			}
		});


	$http.get('/catalogs?').success(
	function(data, status, headers, config) {
		if (data.flag) {
			$scope.homes = data.data
		} else {
			alert(data.message);
		}
	});

}]);


































var app = angular.module('contactApp', []);


app.filter('safehtmls', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});

app.filter('getImageFromSplitStr', function () {
	return function (splitStr, position) {
		return GetListFromStrInSplit(splitStr)[position];
	}
});

app.filter('getImageFromSplitStrtext', function () {
	return function (splitStr1, position1) {
		return GetListFromStrInSplitText(splitStr1)[position1];
	}
});


app.controller('contactController', [
	'$scope',
	'$http',

	function($scope, $http) {
		$scope.contacts = [];

		$http.get('/infos?classify=集团联系我们').success(
			function(data, status, headers, config) {
				if (data.flag) {
					$scope.contacts = data.data
				} else {
					//alert(data.message);
				}
			});
	} ]);

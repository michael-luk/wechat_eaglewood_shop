

var app = angular.module('honorApp', []);


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


app.controller('honorController', [
	'$scope',
	'$http',

	function($scope, $http) {
		$scope.honors = [];

		$http.get('/infos?classify=集团资质荣誉').success(
			function(data, status, headers, config) {
				if (data.flag) {
					$scope.honors = data.data
				} else {
					//alert(data.message);
				}
			});
	} ]);

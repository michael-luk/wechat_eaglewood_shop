

var app = angular.module('profileApp', []);


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


app.controller('profileController', [
	'$scope',
	'$http',

	function($scope, $http) {
		$scope.profiles = [];

		$http.get('/infos?classify=集团公司概况').success(
			function(data, status, headers, config) {
				if (data.flag) {
					$scope.profiles = data.data
				} else {
					//alert(data.message);
				}
			});
	} ]);

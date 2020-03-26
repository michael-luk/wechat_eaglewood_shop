

var app = angular.module('addApp', []);


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


app.controller('addController', [
	'$scope',
	'$http',

	function($scope, $http) {
		$scope.adds = [];

		$http.get('/infos?classify=集团加入本香').success(
			function(data, status, headers, config) {
				if (data.flag) {
					$scope.adds = data.data
				} else {
					//alert(data.message);
				}
			});
	} ]);

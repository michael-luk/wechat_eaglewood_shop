

var app = angular.module('joinApp', []);


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


app.controller('joinController', [
	'$scope',
	'$http',

	function($scope, $http) {
		$scope.joins = [];

		$http.get('/infos?classify=集团招商加盟').success(
			function(data, status, headers, config) {
				if (data.flag) {
					$scope.joins = data.data
				} else {
					//alert(data.message);
				}
			});
	} ]);

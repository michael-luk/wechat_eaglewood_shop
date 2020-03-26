

var app = angular.module('historyApp', []);


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


app.controller('historyController', [
	'$scope',
	'$http',

	function($scope, $http) {
		$scope.historys = [];

		$http.get('/infos?classify=集团发展历程').success(
			function(data, status, headers, config) {
				if (data.flag) {
					$scope.historys = data.data
				} else {
					//alert(data.message);
				}
			});
	} ]);

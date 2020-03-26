
var app = angular.module('PcFrameApp', []);


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


app.controller('PcFrameController', ['$scope', '$http',

function($scope, $http) {

	$scope.homes = [];
	
	$http.get('/catalogs').success(

	function(data, status, headers, config) {
		if (data.flag) {
			$scope.homes = data.data
		} else {
			alert(data.message);
		}
	});

}]);
































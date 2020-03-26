
var app = angular.module('indentApp', []);


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

app.filter('getFirstImage', function () {
	return function (imageStr) {
		if(imageStr){
			return imageStr.split(",")[0]
		}
		else{
			return ""
		}
	}
});


app.controller('indentController', ['$scope', '$http',
function($scope, $http) {
	$scope.indents = {};
	$http.get('/orders/'+GetQueryString('orderid')).success(
	function(data, status, headers, config) {
		if (data.flag) {
			$scope.indents = data.data
		} else {
			alert(data.message);
		}
	});	
}]);







function GetQueryString(name) {
	var url = decodeURI(window.location.search);
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = url.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}


















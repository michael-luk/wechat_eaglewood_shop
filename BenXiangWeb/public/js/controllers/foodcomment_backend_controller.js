/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('FoodCommentBackendApp', ['ngGrid', 'angularFileUpload', 'fundoo.services']);
var cellEditableTemplate = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('FoodCommentBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
    $scope.list = []
    $scope.currentObj = {}
    $scope.page = 1;
    $scope.pageInfo = {}
    $scope.products=[]
    $scope.selectproductId = 0//0即选择"全部"

    $scope.$watch('page', function(){
        refreshDate();
    }, false);

    $scope.$watch('selectproductId', function(){
    	if($scope.products.length > 0){
    		refreshDate();
    	}
    }, false);
    
    $scope.goHomePage = function() {
    	$scope.page = 1;
    }
    
    $scope.goPrevPage = function() {
    	$scope.page = $scope.pageInfo.current -1;
    }

    $scope.goNextPage = function() {
        $scope.page = $scope.pageInfo.current +1;
    }
    
    $scope.goLastPage = function() {
    	$scope.page = $scope.pageInfo.total;
    }

    $scope.goJumpPage = function() {
        if($scope.jumpPage > $scope.pageInfo.total){
            $scope.jumpPage = $scope.pageInfo.current
            bootbox.alert('总页数最多为' +$scope.pageInfo.total+ '页');
        }else{
            $scope.page = $scope.jumpPage;
        }
    }

    function refreshDate(){
    	var url = '/foodcomments?page=' + $scope.page
    	if($scope.selectproductId != null) url += '&productId=' + $scope.selectproductId
    	$log.log('get foodcomments from api: ' + url)
    	
        $http.get(url).success(function (data, status, headers, config) {
        	$log.log(data)
            if (data.flag) {
            	for(x in data.data){
            		if(data.data[x].images){
            			data.data[x].imageList = data.data[x].images.split(',')
            		}
        			else{
        				data.data[x].imageList = ''
        			}
            	}
                $scope.list = data.data;
                $scope.pageInfo = data.page;
            }
            else {
            	bootbox.alert(data.message);
            }
        });
    }
    
    $http.get('/products').success(function (data, status, headers, config) {
    	$log.log('get products from api')
    	if (data.flag) {
    		$log.log(data)
    		$scope.products = data.data;
    	}
    	else {
    		bootbox.alert(data.message)
    	}
    });
    
    $scope.gridOptions = { data: 'list',
    		 rowHeight: 100,
    		 // showSelectionCheckbox:true,
    		 // enableCellSelection: false,
             enableRowSelection: true,
             selectedItems: [],
             multiSelect:false,
             // enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        columnDefs: [
            {field: 'id', displayName: 'Id', width: '5%'},
            {field: 'productName', displayName: '所属产品', width: '15%', enableCellEdit: false, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'userName', displayName: '用户名', width: '10%', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'createdAtStr', displayName: '评论时间', width: '15%', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'description', displayName: '美食点评', width: '20%', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'imageList',displayName: '点评图片',width: '25%',cellTemplate: '<div ng-repeat="imageName in row.entity.imageList"><a target="_blank" ng-href="/showImage/{{imageName}}"><img ng-src="/showImage/{{imageName}}" style="width:80px;height:80px;float:left"></a></div>' },
            {field: 'comment', displayName: '备注', width: '8%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
        ] };


    // 当前行更新字段
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function() {
        var content = $scope.currentObj;
        var isNew = !content.id
        var url = '/foodcomments'
        if(isNew){
        	var http_method = "POST";
        }else{
        	var http_method = "PUT";
        	url += '/' + content.id
            var pos = $scope.list.indexOf(content);
        }
        $http({method: http_method, url: url, data:content}).success(function(data, status, headers, config) {
                if(data.flag){
                    if(isNew){
                        $scope.list.push(data.data);
                        bootbox.alert('新建[' + data.data.area + ']成功');
                    }else{
                        $scope.list[pos] = data.data;
                    }
                }else{
                    bootbox.alert(data.message);
                }
            });
    };

    $scope.deleteContent = function(){
        var items = $scope.gridOptions.selectedItems;
        if(items.length == 0){
            bootbox.alert("请至少选择一个对象.");
        }else{
            var content = items[0];
            if(content.id){
                bootbox.confirm("您确定要删除[" + content.description + "]的评论吗?", function(result) {
                    if(result) {
                        $http.delete('/foodcomments/' + content.id).success(function(data, status, headers, config) {
                            if (data.flag) {
                            	var index = $scope.list.indexOf(content);
                                $scope.gridOptions.selectItem(index, false);
                                $scope.list.splice(index, 1);
                                bootbox.alert("删除成功");
                            }
                            else {
                                bootbox.alert(data.message);
                            }
                        });
                    }
                });
            }
        }
    };
    
//    $scope.addContent = function(){
//        $scope.currentObj = {};
//        createDialogService("/assets/js/controllers/shipprices_editortemplate.html",{
//            id: 'editor',
//            title: '新增送货区域',
//            success:{
//                label: '确定',
//                fn: $scope.saveContent
//            },
//            scope: $scope
//        });
//    };

}]);

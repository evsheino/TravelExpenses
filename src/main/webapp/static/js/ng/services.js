var expensesServices = angular.module('expensesServices', ['ngResource']);

expensesServices.factory('Expense', ['$resource',
  function($resource){
    return $resource('expenses/list', {}, {
      query: {method:'GET', isArray:true}
    });
  }]);

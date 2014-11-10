var adminPageControllers = angular.module('adminPageControllers', []);

adminPageControllers.controller('ownExpenceCtrl', ['$scope', 'Expense', function($scope, Expense) {
  $scope.expenses = Expense.query();
}]);

var app = angular.module('hdb-web',['ui.router', "ngResource"]);
angular.module('hdb-web').config(function ($urlRouterProvider, $stateProvider, $locationProvider) {
  $locationProvider.html5Mode(true);

  $stateProvider
    .state('main', {
      url: '/',
      templateUrl: 'map/index.html',
      controller : "DisplayMapController",
      controllerAs : "vm"
    });

  $urlRouterProvider.otherwise("/");
})
.constant("ENDPOINT", "http://localhost:3003/")
.config(['$provide', function ($provide) {
    $provide.decorator('$q', ['$delegate', function ($delegate) {
        var $q = $delegate;

        // Extention for q
        $q.allSettled = $q.allSettled || function (promises) {
            var deferred = $q.defer();
            if (angular.isArray(promises)) {
                var states = [];
                var results = [];
                var didAPromiseFail = false;

                // First create an array for all promises with their state
                angular.forEach(promises, function (promise, key) {
                    states[key] = false;
                });

                // Helper to check if all states are finished
                var checkStates = function (states, results, deferred, failed) {
                    var allFinished = true;
                    angular.forEach(states, function (state, key) {
                        if (!state) {
                            allFinished = false;
                        }
                    });
                    if (allFinished) {
                        if (failed) {
                            deferred.reject(results);
                        } else {
                            deferred.resolve(results);
                        }
                    }
                }

                // Loop through the promises
                // a second loop to be sure that checkStates is called when all states are set to false first
                angular.forEach(promises, function (promise, key) {
                    $q.when(promise).then(function (result) {
                        states[key] = true;
                        results[key] = result;
                        checkStates(states, results, deferred, didAPromiseFail);
                    }, function (reason) {
                        states[key] = true;
                        results[key] = reason;
                        didAPromiseFail = true;
                        checkStates(states, results, deferred, didAPromiseFail);
                    });
                });
            } else {
                throw 'allSettled can only handle an array of promises (for now)';
            }

            return deferred.promise;
        };

        return $q;
    }]);
}])
.factory("Beacons", function(ENDPOINT, $http, $q){
    return {
      getAllBeacons : function(){
        return $http({method : "GET", url : ENDPOINT+"api/beacons"});
      },
      getAllPresence : function(){
        return $http({method : "GET", url : ENDPOINT+"api/presence"});
      },
      getAllPresenceTrilateration : function(){
        return $http({method : "GET", url : ENDPOINT+"api/presenceTri"});
      },
      getAllPresenceCombined : function(){
        defer = $q.defer();
        $q.allSettled([this.getAllPresenceTrilateration(), this.getAllPresence()]).then(function(resultTwoPlaces){
            var trilaterationResult = resultTwoPlaces[0]["data"];
            var estimateResult = resultTwoPlaces[1]["data"];
            var uniqueDevices = [];
            var requiredDevices = [];
            trilaterationResult.forEach(function(triDevice){
              triDevice = triDevice['value']
              console.log(triDevice);
              if(triDevice['position']['x'] == undefined || triDevice['position']['x'] == 0 || triDevice['position']['y'] == undefined || triDevice['position']['y'] == 0){
                requiredDevices.push(triDevice['deviceId']);
              }else{
                console.log(triDevice['position']['x'], triDevice['position']['y'], "via trianguation");
                uniqueDevices.push({
                  x : triDevice['position']['x'],
                  y : triDevice['position']['y'],
                  nearestBeacon : {
                    id : triDevice['nearestBeacon']
                  }
                });

              }
            });
            requiredDevices.forEach(function(missingOne){
              for(var i = 0; i < estimateResult.length;i++){
                if(estimateResult[i]['deviceId'] == missingOne){
                  uniqueDevices.push(estimateResult[i]);
                }
              }
            });
            console.log(uniqueDevices);
            defer.resolve(uniqueDevices);
        });
        return defer.promise;
      }
    };
})
.controller("DisplayMapController", function($scope, $interval, Beacons,$q){
  var canvas = document.getElementById("canvas");
  var height = ($(window).height());
  var width = ($(window).width());
  var currentCanvas = "";

  canvas.width = width;
  canvas.height = height;
  var draw = function(){
    setTimeout(function(){
      if(canvas.getContext){
        var ctxt = canvas.getContext('2d');
        ctxt.clearRect(0,0,canvas.width, canvas.height);

        //ctxt.translate(0, canvas.height);
        //ctxt.scale(1, -1);

        var rectWidth = canvas.width * 0.80;
        var rectHeight = canvas.height * 0.70;
        var rectOrigins = {
          x : (canvas.width - rectWidth)/2,
          y : (canvas.height - rectHeight)/2
        }

        var scale = (rectHeight*0.5) / 3;//Every x pixels, is 1 metre,

        ctxt.strokeStyle= "limegreen";
        ctxt.strokeRect(rectOrigins.x, rectOrigins.y, rectWidth, rectHeight);
        var coordinates = [];
        var arrayOfBeacons = [];
        Beacons.getAllBeacons().then(function(beaconResult){
          beaconResult = beaconResult['data'];
          var drawRelativeToRectangle = function(offsetX, offsetY, width, height){
            ctxt.fillStyle='rgba(102,201,200,1)';
            //ctxt.fillStyle = 'rgb(' + (Math.floor(Math.random() * 256)) + ',' + (Math.floor(Math.random() * 256)) + ',' + (Math.floor(Math.random() * 256)) + ')';
            ctxt.fillRect(rectOrigins.x + offsetX , rectOrigins.y +offsetY , width, height);
            return {x : rectOrigins.x + offsetX + width ,  y : rectOrigins.y +offsetY + height, offsetX : offsetX, offsetY : offsetY, width : width, height : height}
          };
          beaconResult = beaconResult.sort(function(a,b){
            return a["id"] > b["id"];
          });
          arrayOfBeacons = beaconResult;

          var coord = drawRelativeToRectangle(0, 0 , rectWidth * 0.333, rectHeight * 0.5);
          ctxt.font = "30px Arial";
          ctxt.fillStyle= 'rgb(255,255,255)';
          ctxt.fillText("Exhibit 1", ((coord.x - coord.offsetX)/2 + coord.offsetX), (coord.y + coord.offsetY - rectOrigins.y)/2+ (rectHeight * 0.5/2));
          coordinates.push({
            x:  (coord.width)/2 + rectOrigins.x + coord.offsetX,
            y : (coord.height)/2 + rectOrigins.y + coord.offsetY
          });

          coord = drawRelativeToRectangle(rectWidth *  1/3 , 0, rectWidth * 0.333334, rectHeight * 0.5);
          ctxt.font = "30px Arial";
          ctxt.fillStyle= 'rgb(255,255,255)';
          ctxt.fillText("Exhibit 2", ((coord.x - coord.offsetX)/2 + coord.offsetX), (coord.y + coord.offsetY - rectOrigins.y)/2+ (rectHeight * 0.5/2));
          coordinates.push({
            x:  (coord.width)/2 + rectOrigins.x + coord.offsetX,
            y : (coord.height)/2 + rectOrigins.y + coord.offsetY
          });

          coord = drawRelativeToRectangle(rectWidth *  2/3 , 0, rectWidth * 0.333334, rectHeight * 0.5);
          ctxt.font = "30px Arial";
          ctxt.fillStyle= 'rgb(255,255,255)';
          ctxt.fillText("Exhibit 3", ((coord.x - coord.offsetX)/2 + coord.offsetX), (coord.y + coord.offsetY - rectOrigins.y)/2+ (rectHeight * 0.5/2));
          coordinates.push({
            x:  (coord.width)/2 + rectOrigins.x + coord.offsetX,
            y : (coord.height)/2 + rectOrigins.y + coord.offsetY
          });

          coord = drawRelativeToRectangle(0, rectHeight *0.5 , rectWidth * 0.333, rectHeight * 0.5);
          ctxt.font = "30px Arial";
          ctxt.fillStyle= 'rgb(255,255,255)';
          ctxt.fillText("Exhibit 4", ((coord.x - coord.offsetX)/2 + coord.offsetX), (coord.y + coord.offsetY - rectOrigins.y)/2+ (rectHeight * 0.5/2));
          coordinates.push({
            x:  (coord.width)/2 + rectOrigins.x + coord.offsetX,
            y : (coord.height)/2 + rectOrigins.y + coord.offsetY
          });


          coord = drawRelativeToRectangle(rectWidth *  1/3 , rectHeight *0.5, rectWidth * 0.333334, rectHeight * 0.5);
          ctxt.font = "30px Arial";
          ctxt.fillStyle= 'rgb(255,255,255)';
          ctxt.fillText("Exhibit 5", ((coord.x - coord.offsetX)/2 + coord.offsetX), (coord.y + coord.offsetY - rectOrigins.y)/2+ (rectHeight * 0.5/2));
          coordinates.push({
            x:  (coord.width)/2 + rectOrigins.x + coord.offsetX,
            y : (coord.height)/2 + rectOrigins.y + coord.offsetY
          });

          coord = drawRelativeToRectangle(rectWidth *  2/3 , rectHeight *0.5, rectWidth * 0.333334, rectHeight * 0.5);
          ctxt.font = "30px Arial";
          ctxt.fillStyle= 'rgb(255,255,255)';
          ctxt.fillText("Exhibit 6", ((coord.x - coord.offsetX)/2 + coord.offsetX), (coord.y + coord.offsetY - rectOrigins.y)/2+ (rectHeight * 0.5/2));
          coordinates.push({
            x:  (coord.width)/2 + rectOrigins.x + coord.offsetX,
            y : (coord.height)/2 + rectOrigins.y + coord.offsetY
          });
        });
        var arcCoordinates = [];
        function getPoint(c1,c2,radius,angle){
            return [c1+Math.cos(angle)*radius,c2+Math.sin(angle)*radius];
        }
        var refresh = function(){

        //  if(arcCoordinates.length > 0)
          //  ctxt.clearRect(arcCoordinates[0][0], arcCoordinates[0][1], 2.5, 2.5);
          Beacons.getAllPresenceCombined().then(function(result){
            console.log(result);
            var devices = result;
            devices.forEach(function(device){//loop through each device
              var beaconIndex = -1;
              arrayOfBeacons.forEach(function(beacon, i){
                if(beacon['id'] == device['nearestBeacon']['id']){
                  beaconIndex = i;
                  return;
                }
              });
              var coordinate = coordinates[beaconIndex];
              if(device['randomAngle']){
                console.log("Got random angle");
                ctxt.strokeStyle = "rgb(200,0,0)";
              }else{
                ctxt.strokeStyle = "rgb(0,0,0)";
              }
              ctxt.beginPath();
              ctxt.arc(coordinate.x - (scale * device['x']),coordinate.y + (scale * device['y']),5,0,2*Math.PI);
              ctxt.closePath();
              ctxt.stroke();
              arcCoordinates.push(getPoint(coordinate.x - (scale * device['x']),coordinate.y + (scale * device['y']), 5, 2*Math.PI));
            });
            console.log(arcCoordinates);
          });
        }
        refresh();
        setInterval(refresh,10 * 1000);
      }
    }, 100);
  }
    //draw();
  $(document).ready(draw);
  $(document).resize(draw);
});

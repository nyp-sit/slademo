var app = angular.module('hdb-web',['ui.router', "ngResource", "ngMap"]);
angular.module('hdb-web').config(function ($urlRouterProvider, $stateProvider, $locationProvider) {
  $locationProvider.html5Mode(true);
  $stateProvider
    .state('map', {//top map, bottom room
      url : '/map/:option',
      //views:{
        //"map" : {
          templateUrl : "main/map.html",
          controller : "MapCtrl",
        //},

      //},
      resolve :{
        option : ["$stateParams", function($stateParams){
          console.log($stateParams);
          return $stateParams.option;
        }]
      }
    });

  $urlRouterProvider.otherwise("/map/current");
})
.constant("ENDPOINT", "http://localhost:3002/")
.constant("INTERVAL", 1000 * 10 )
.factory("Beacons", function(ENDPOINT, $http){
    return {
      getLocations : function(start, end, state){
        console.log(ENDPOINT+"api/location?start="+start+"&end="+end+"&state="+state);
        if(state != undefined)
          return $http({method : "GET", url : ENDPOINT+"api/location?start="+start+"&end="+end+"&state="+state});
      },
      getDevices : function(){
        return $http({method : "GET", url : ENDPOINT+"api/devices"});
      },
      getDetection : function(){
        return $http({method : "GET", url : ENDPOINT+"api/detection"});
      },
      getAnalytics : function(deviceId, view){
        console.log(view);
        return $http({method : "GET", url : ENDPOINT+"api/analytics/time/"+deviceId+"?view="+view});
      }
    };
})
.factory("DetectStore", function(){
  var store = [];
  return {
    getData : function(){
      return store;
    },
    setData : function(data){
      store = data;
    }
  };
})//This data is used to store data in between MainCtrl and indoorcontrol
.controller("RoomCtrl", function($scope,$interval,Beacons,INTERVAL, DetectStore){
  //$scope.isTopView = isTopView;
  var draw = function(){
    var image = document.getElementById('floorplan');
    var top = document.getElementById('top-words');
    $("#floorplan").css({'height' : window.innerHeight - top.offsetHeight});

    var picture = "blank_floorplan";
    var keys = Object.keys($scope.status);
    keys.forEach(function(key){
      if($scope.status[key] == true){
        picture = key;
        return;
      }
    });
    $("#floorplan").attr({'src' : "img/"+picture+".png"})
  }
  $scope.status = {
    living : false,
    kitchen : false,
    main : false
  };
  $scope.refresh = function(){
    var keys = Object.keys($scope.status);
    keys.forEach(function(key){
      $scope.status[key] = false;
    });
    var detectData = DetectStore.getData();
    var newKeys = Object.keys(detectData);
    if(newKeys.length > 0){
      newKeys.forEach(function(newKey){
        $scope.status[newKey.toLowerCase()] = true;
      });
    }
    draw();
  }
  //setTimeout(function(){
  $scope.$on("detect-loaded", $scope.refresh);

  $interval($scope.refresh, 5 * 1000);

  angular.element(document).ready(draw);
})
.controller("ListCtrl", function($scope, Beacons){
  Beacons.getDevices().then(function(devices){
    $scope.devices = devices['data'];
  });
})
.controller("MapCtrl", function($scope, $rootScope,$interval,$q,$stateParams ,INTERVAL, Beacons, NgMap){
  $scope.start = new Date().getTime() -  1000 * 60 * 60 * 24;//when select past day, past week, past month , move this value
  $scope.end = new Date().getTime();
  $scope.markers = [];
  $scope.locations = [];
  $scope.option = $stateParams.option;
  setTimeout(function(){
    $scope.refresh();

  },100);
  var resize = function(){
    var tophalf = document.getElementById("bottom-top-half");
    if(tophalf != null){
      $("#map").css({
        'height' : window.innerHeight - tophalf.offsetHeight*1.2
      });
    }
  }
  $scope.refresh = function(){
    $scope.markers.forEach(function(marker){
      marker.setMap(null);
    });
    if($scope.option == "current"){
      $scope.option = "current";
      console.log("set current");
    }
    Beacons.getLocations($scope.start, $scope.end, $scope.option).then(function(locations){

      $scope.locations = (locations['data']);
      $scope.locations.sort(function(a,b){
        return parseInt(a['timeStamp']) > parseInt(b['timeStamp']);
      });
      console.log($scope.locations.length + "loc length");
        NgMap.getMap({id : 'map'}).then(function(map) {
          map.setOptions({
            scrollwheel : false
          });
          console.log("Current loation");
          if($scope.option == "current" || $scope.option == undefined || $scope.option == ""){
            $scope.locations = [$scope.locations[0]];
          }
          $scope.locations.forEach(function(location){
            if(location == undefined || location == null)
              return;
              var marker = new google.maps.Marker({
                position : {
                  lat : parseFloat(location["lat"]),
                  lng : parseFloat(location["lng"])
                },
                title : new Date(parseInt(location["timeStamp"]))+"",
                map : map
              });
              if($scope.option=='current'){
                map.panTo({
                  lat : parseFloat(location["lat"]),
                  lng : parseFloat(location["lng"])
                });
                map.setZoom(17);
                console.log("current");
              }
              $scope.markers.push(marker);
          });
      });
    });
  };

  $scope.dayFactor = (1000 * 60 * 60 * 24);
  if($scope.state == "day"){
    $scope.start = $scope.end - 7 * $scope.dayFactor;
  }else if($scope.state == "week"){
    $scope.start = $scope.end - 30 * $scope.dayFactor;
  }else if($scope.state == "month"){
    $scope.start = $scope.end - $scope.end;
  }
  resize();
})
.controller("AnalyticsCtrl", function($scope, Beacons, $interval, INTERVAL){
  $scope.view = 'day';
  $scope.changeView = function(text){
    $scope.view = text;
    $scope.getAnalyzedData();
  }
    var piechartData = [];
  var pieChart = null;
  var ctxt = document.getElementById('piechart').getContext('2d');

  $scope.getAnalyzedData = function(){
    var data = Beacons.getAnalytics("cdf927c8", $scope.view);
    data.then(function(result){
      piechartData = [];
      if(pieChart != null)
        pieChart.removeData();
      var dataItems = result['data'];
      var sum = dataItems["indoor"] + dataItems["outdoor"];
      piechartData.push({
        value : parseInt(dataItems["indoor"])/1000/60,
        color : "firebrick",
        label : "Indoor",
        labelColor : "white",
        labelFontSize : '16'
      });
      piechartData.push({
        value : parseInt(dataItems["outdoor"])/1000/60,
        color : "lightskyblue",
        label : "Outdoor",
        labelFontSize : '16',
        labelColor : 'white'
      });
      pieChart = new Chart(ctxt).Doughnut(piechartData,{
          legendTemplate : '<ul class="chartlegend"><% for (var i=0; i<segments.length; i++){ %><li><span style="background-color:<%= segments[i].fillColor %>">&nbsp;&nbsp</span><%if(segments[i].label){%><%=segments[i].label%> <% if(segments[i].value > 60) {%> <%= Math.round(segments[i].value/60 * 1)/1 %> Hours <% } else { %> <%= Math.round(segments[i].value*1)/1 %> Minutes <% } %> <%}%></li><%}%></ul>',
           tooltipTemplate: "<% if(value > 60) {%> <%= Math.round((value/60) * 1)/1 %> Hours <% } else { %> <%= Math.round(value * 1)/1 %> Minutes <% } %>"
      });
      document.getElementById('legend').innerHTML = pieChart.generateLegend();
    });
  }

  $scope.getAnalyzedData();
  //$interval($scope.getAnalyzedData, INTERVAL * 1.5);
  Beacons.getDevices().then(function(devices){
    $scope.devices = devices['data'];
    console.log($scope.devices);
  });

})
.controller("MainCtrl", function($scope, $rootScope, $interval,$state, Beacons,DetectStore,INTERVAL, $stateParams){
  $scope.section = "";
  $scope.scrollTo = function(id){
    $("html , body").animate({
      scrollTop : $("#"+id).offset().top
    },500);
  }
  $(document).scroll(function(){
    if($(this).scrollTop()>=$('#location').position().top){
       $scope.section = "location";
       $scope.$apply();
   } else if($(this).scrollTop()>=$('#indoor').position().top){
        $scope.section = "indoor";
        $scope.$apply();
    }
  });
  setTimeout(function(){
    if($state.current.name == "topRoom"){
      $scope.hasRoom = "top";
    }else{
      $scope.hasRoom = "bottom";
    }
  },0);

  var refresh = function(){
    console.log("refresh room main");
    Beacons.getDetection().then(function(detected){
      var keys = Object.keys(detected['data']);
      DetectStore.setData(detected['data']);
      $rootScope.$broadcast("detect-loaded");
      if(keys.length > 0){
        if($state.current.name != "topRoom"){
        //  $state.go("topRoom");
        }
      }else{
        //$state.go("topMap");
      }
    },function(err){
      console.log(err);
    });
  }
  refresh()
  $interval(refresh, INTERVAL);
});

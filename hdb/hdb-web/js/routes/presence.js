// Generated by CoffeeScript 1.10.0
(function() {
  var Presence, q,
    bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

  q = require("q");

  Presence = (function() {
    function Presence(App) {
      this.App = App;
      this.calculateAngles = bind(this.calculateAngles, this);
      this.getBeaconsByIdForPresence = bind(this.getBeaconsByIdForPresence, this);
      this.getAllBeacons = bind(this.getAllBeacons, this);
      this.beaconMap = {};
      this.App.router.get("/api/presence", this.getAllBeacons);
    }

    Presence.prototype.getAllBeacons = function(req, res) {
      return this.App.dynamoDB.scan({
        TableName: "hdb_presence",
        FilterExpression: "time_inserted > :time_five_minutes_ago",
        ExpressionAttributeValues: {
          ":time_five_minutes_ago": {
            "N": (Date.now() - (1000 * 60 * 60 * 24)) + ""
          }
        }
      }, (function(_this) {
        return function(err, result) {
          var results;
          console.log(err);
          results = _this.App.convertFromArray(result["Items"]);
          return _this.getBeaconsByIdForPresence(results).then(function(presenceWithInfo) {
            var i, len, moved, presence;
            moved = [];
            for (i = 0, len = presenceWithInfo.length; i < len; i++) {
              presence = presenceWithInfo[i];
              moved.push(presence["value"]);
            }
            _this.calculateAngles(moved);
            return _this.App.sendContent(req, res, moved);
          });
        };
      })(this));
    };

    Presence.prototype.getBeaconsByIdForPresence = function(presenceArray) {
      var getBeacon, i, len, originalDefer, presence, promises;
      originalDefer = q.defer();
      promises = [];
      for (i = 0, len = presenceArray.length; i < len; i++) {
        presence = presenceArray[i];
        getBeacon = (function(_this) {
          return function(presence) {
            var defer;
            defer = q.defer();
            if (_this.beaconMap[presence["nearestBeaconId"]]) {
              console.log("cache");
              presence["nearestBeacon"] = _this.beaconMap[presence["nearestBeaconId"]];
              defer.resolve(presence);
            } else {
              _this.App.dynamoDB.getItem({
                Key: {
                  id: {
                    S: presence["nearestBeaconId"]
                  }
                },
                TableName: "hdb_beacons"
              }, function(err, data) {
                data = _this.App.convertToObject(data["Item"]);
                presence["nearestBeacon"] = data;
                _this.beaconMap[presence["nearestBeaconId"]] = data;
                delete presence["nearestBeaconId"];
                return defer.resolve(presence);
              });
            }
            return defer.promise;
          };
        })(this);
        promises.push(getBeacon(presence));
      }
      q.allSettled(promises).then((function(_this) {
        return function(dataArray) {
          return originalDefer.resolve(dataArray);
        };
      })(this));
      return originalDefer.promise;
    };

    Presence.prototype.calculateAngles = function(items) {
      var cosResult, directionFactorX, directionFactorY, i, item, len, max, min, quandrantNo, refAngle, results1, sinResult;
      results1 = [];
      for (i = 0, len = items.length; i < len; i++) {
        item = items[i];
        max = item["nearestBeacon"]["angleEnd"];
        min = item["nearestBeacon"]["angleStart"];
        item["randomAngle"] = Math.floor(Math.random() * (max - min + 1)) + parseInt(min);
        quandrantNo = Math.floor(item["randomAngle"] / 90);
        refAngle = -1;
        directionFactorY = 1;
        directionFactorX = 1;
        console.log("factor init " + quandrantNo);
        if (quandrantNo === 0) {
          refAngle = item["randomAngle"] - 0;
          directionFactorY = -1;
        } else if (quandrantNo === 1) {
          refAngle = 180 - item["randomAngle"];
          directionFactorY = -1;
          directionFactorX = -1;
        } else if (quandrantNo === 2) {
          refAngle = item["randomAngle"] - 180;
          directionFactorX = -1;
        } else if (quandrantNo === 3) {
          refAngle = 360 - item["randomAngle"];
        }
        console.log("here");
        sinResult = Math.sin(refAngle * Math.PI / 180);
        cosResult = Math.cos(refAngle * Math.PI / 180);
        sinResult *= 20;
        cosResult *= 20;
        item["y"] = parseInt(item["nearestBeacon"]["y"]) + (directionFactorY * (sinResult * item["distance"]));
        results1.push(item["x"] = parseInt(item["nearestBeacon"]["x"]) + (directionFactorX * (cosResult * item["distance"])));
      }
      return results1;
    };

    return Presence;

  })();

  module.exports = Presence;

}).call(this);

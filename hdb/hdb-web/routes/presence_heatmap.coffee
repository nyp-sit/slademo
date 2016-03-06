q = require "q"
class Presence
  constructor : (@App)->
    @beaconMap = {}
    @App.router.get "/api/presence", @getAllBeacons
  getAllBeacons : (req, res)=>
    @App.dynamoDB.scan {
      TableName : "hdb_presence",
      #KeyConditionExpression : "time_inserted > :time_five_minutes_ago",
      FilterExpression  : "#timestamp > :time_five_minutes_ago",
      ExpressionAttributeValues : {
        ":time_five_minutes_ago" : {
          "N" : (Date.now() - (1000 * 60 * 60 * 2))+""
        }
      },
      ExpressionAttributeNames : {
        "#timestamp" : "timestamp"
      }
      #Select : "ALL_ATTRIBUTES",
    }, (err, result)=>

      results =  @App.convertFromArray result["Items"]
      console.log "Get ALL : #{results.length}"
      @getBeaconsByIdForPresence(results , req).then (presenceWithInfo)=>
        moved = []
        for presence in presenceWithInfo

          moved.push presence["value"]
        @calculateAngles moved
        @App.sendContent req, res,moved
  getBeaconsByIdForPresence : (presenceArray, req)=>
    originalDefer = q.defer();
    promises = []
    for presence in presenceArray
      getBeacon = (presence)=>

        defer = q.defer()
        if @beaconMap[presence["nearestBeaconId"]]
          console.log "cache"
          presence["nearestBeacon"] = @beaconMap[presence["nearestBeaconId"]]
          defer.resolve presence
        else
          @App.dynamoDB.getItem {
            Key :
              id :
                S : presence["nearestBeaconId"]
            TableName : "hdb_beacons"
          },(err, data)=>
            data = @App.convertToObject data["Item"]
            data["y"] = (parseInt(req.query.height)/100)* parseInt(data["y"])
            data["x"] = (parseInt(req.query.width)/100)* parseInt(data["x"])
            presence["nearestBeacon"] = data

            @beaconMap[presence["nearestBeaconId"]] = data;
            delete presence["nearestBeaconId"]

            defer.resolve(presence)
        return defer.promise;
      promises.push getBeacon(presence)
    q.allSettled(promises).then (dataArray)=>
      originalDefer.resolve(dataArray)
    return originalDefer.promise;
  calculateAngles : (items)=>
    for item in items
      max = item["nearestBeacon"]["angleEnd"]
      min = item["nearestBeacon"]["angleStart"]

      item["randomAngle"] = Math.floor(Math.random() * (max - min + 1) )+ parseInt(min);
      quandrantNo = Math.floor(item["randomAngle"]/90)
      refAngle = -1

      directionFactorY = 1
      directionFactorX = 1
    #  console.log "factor init #{quandrantNo}"
      if quandrantNo is 0
        refAngle = item["randomAngle"] - 0
        directionFactorY = -1
      else if quandrantNo is 1
        refAngle = 180 - item["randomAngle"]
        directionFactorY = -1
        directionFactorX = -1
      else if quandrantNo is 2
        refAngle = item["randomAngle"] - 180
        directionFactorX = -1
      else if quandrantNo == 3
        refAngle = 360 - item["randomAngle"]

      #console.log "here"
      sinResult = (Math.sin(refAngle  * Math.PI / 180));#This is y since, sin angle = y
      cosResult = (Math.cos(refAngle  * Math.PI / 180));#This is x since cos angle = x

      sinResult *= 25;
      cosResult *= 25;

      item["y"] = parseInt(item["nearestBeacon"]["y"]) + (directionFactorY * (sinResult * item["distance"]));
      item["x"] = parseInt(item["nearestBeacon"]["x"]) + (directionFactorX * (cosResult * item["distance"]));
      item["y"] = parseInt(item["y"])
      item["x"] = parseInt(item["x"])

module.exports = Presence

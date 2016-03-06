q = require "q"
geolib = require "geolib"
moment = require "moment"
class Presence
  constructor : (@App)->
    @beaconMap = {}
    @App.router.get "/api/devices", @getDevices
    @App.router.get "/api/detection", @getDetect
    @App.router.get "/api/location" , @getLocation
    @App.router.get "/api/analytics/time/:deviceId" , @getAnalyticsTimeSpend
  getDevices : (req, res)=>
    @App.dynamoDB.scan {
      TableName : "Devices"
    }, (err,result)=>
      if !err
        return @App.sendContent req, res, @App.convertFromArray result["Items"]
      else
        return @App.sendError res, 500, err['message']
  getLocation : (req, res)=>

    if !req.query.start
      req.query.start = (Date.now() - 1000 * 60 * 60 * 24)+""
    if !req.query.end
      req.query.end = Date.now() + ""
    query = {
      TableName : "TrackData",
      FilterExpression  : "#timestamp > :start AND #timestamp < :end",
      ExpressionAttributeValues : {
        ":end" : {
          "N" : req.query.end+""
        },
        ":start" : {
          "N" : req.query.start+""
        }
      },
      ExpressionAttributeNames : {
        "#timestamp" : "timeStamp"
      },
      ConsistentRead : true,
    }
    @App.dynamoDB.scan query, (err, result)=>
      if !err
        previousCoordinate = null
        coordinates = @App.convertFromArray result["Items"];#[100..1000]
        coordinates = coordinates.sort (a,b)=>
          return parseInt(a["timeStamp"]) > parseInt(b["timeStamp"])

        if req.query.state is "current"
          coordinates = [coordinates[coordinates.length - 1]]
        return @App.sendContent req, res, coordinates
      else
        console.log err
        return @App.sendError res, 500, err['message']
  getDetect : (req, res)=>
    getDataForDevice = (deviceId)=>
      defer = q.defer();
      #start of loop for getting for specfific deviceId
      @App.dynamoDB.query {
        TableName : "BeaconDetectionTrial",
        IndexName : "deviceId-timestamp-index"
        KeyConditionExpression : "#id = :deviceId AND #timestamp > :ten_minutes_ago",
        ExpressionAttributeValues : {
          ":ten_minutes_ago" : {
            "N" :  (Date.now() - 1000 * 20)+""
          },
          ":deviceId":{
            "S" : deviceId+""
          }
        },
        ExpressionAttributeNames : {
          "#timestamp" : "timestamp",
          "#id" : "deviceId"
        }
      }, (err, result)=>
        if !err
          results =  @App.convertFromArray result["Items"]
          console.log "Results from AWS : "+results.length
          latestPoints = {}
          for result in results
            if latestPoints[result["deviceId"]] #or (parseInt(result["timestamp"]) > parseInt(latestPoints[result['id']]["timestamp"]))
              if result["timestamp"] and (parseInt(result["timestamp"]) > latestPoints[result['deviceId']]['timestamp'])
                latestPoints[result['deviceId']] = result
            else
              latestPoints[result['deviceId']] = result
          defer.resolve latestPoints
      return defer.promise;
        #End of loop for device id
    getDevices = ()=>
      defer = q.defer();
      @App.dynamoDB.scan {
        TableName : "Devices"
      }, (err, devices)=>
        devices = @App.convertFromArray devices["Items"]
        console.log devices
        defer.resolve devices
      return defer.promise
    getDevices().then (devices)=>
      promises = []
      for device in devices
        promises.push getDataForDevice(device['imei_id'])
      q.allSettled(promises).then (results)=>
        uniquePoints = {}
        for result in results
          latestPoints = result['value']
          console.log result
          if result['state'] is "fulfilled"
            Object.keys(latestPoints).forEach (key)=>
              @App.config.beacons.forEach (beacon)=>
                if latestPoints[key] isnt null and latestPoints[key]['Id1'] != undefined and latestPoints[key]['Id2'] != undefined
                  latestPoints[key]['Id1'] = latestPoints[key]['Id1'].replace " ", ""
                  latestPoints[key]['Id2'] = latestPoints[key]['Id2'].replace " ", ""

                if beacon["id_1"] is latestPoints[key]['Id1'] and  beacon["id_2"] is latestPoints[key]['Id2']
                  latestPoints[key]["room"] = beacon["name"]

                  delete latestPoints[key]["Id1"]
                  delete latestPoints[key]["Id2"]
                  if uniquePoints[beacon["name"]]
                    uniquePoints[beacon["name"]].push latestPoints[key]
                  else
                    uniquePoints[beacon["name"]]  = [latestPoints[key]]
          else
            return @App.sendContent req, res, []
        return @App.sendContent req, res,uniquePoints

  extractTimes :  (results)=>
    startActive = -1
    endActive = -1
    activeTimes = []
    results.sort (a , b)=>
      return parseInt(a["timestamp"]) - parseInt(b["timestamp"])
    previousEntry = null

    for result, i in results
      currentTime = parseInt(result["timestamp"])

      if previousEntry isnt null
        previousTime = parseInt(previousEntry["timestamp"])
        if (currentTime - previousTime) < @App.config.threshold#Check if gap is too small

          if startActive is -1 #Start new one
            startActive = previousTime
        else#Too big
          #console.log "too big gap , #{currentTime} #{previousTime}"
          if startActive is -1 #Start new one
            startActive = previousTime
          activeTimes.push {
            start : startActive,
            end : previousTime,
            diff : previousTime - startActive,
            diffString : "Seconds : "+(previousTime - startActive)/1000
          }
          startActive = currentTime
          if i == results.length - 1
            activeTimes.push {
              start : currentTime,
              end : currentTime,
              diff : 1000,
              diffString : "Seconds : 1 "
            }
      previousEntry = result
    return activeTimes
  getAnalyticsTimeSpend : (req, res)=>
      #check if cache has data
      hasCache = false
      isMonthView = false

      if req.query['view'] and req.query['view'] is "month"
        isMonthView = true

      if @App.cache and @App.cache.time != -1
          hasCache = @App.cache["value"][req.params.deviceId+""]
          getIndoorBeacons = (hasCache)=>
            defer = q.defer();
            query = {
              TableName : "BeaconDetectionTrial",
              IndexName : "deviceId-timestamp-index",
              KeyConditionExpression : "#id = :deviceId",
              ExpressionAttributeValues : {
                ":deviceId": {
                  "S" : req.params.deviceId+""
                }
              },
              ExpressionAttributeNames : {
                "#id" : "deviceId"
              }
            }
            if !hasCache
              query["KeyConditionExpression"] = "#id = :deviceId AND #timestamp > :timestamp"
              query["ExpressionAttributeValues"][":timestamp"] = {
                "N" : (Date.now() - 1000 * 60 * 60 * 24)+""
              }
              query["ExpressionAttributeNames"]["#timestamp"] = "timestamp"
            if isMonthView
              query["ExpressionAttributeValues"][":timestamp"] = {
                "N" : (Date.now() - 1000 * 60 * 60 * 24 * 30)+""
              }
            @App.dynamoDB.query query,(err, results)=>
              if !err
                results = @App.convertFromArray results["Items"]
                defer.resolve @extractTimes results

            return defer.promise
          #Start group timing together
          #extractedTimes = @extractTimes results
          getLocationData = (hasCache)=>
            defer = q.defer()
            query = {
              TableName : "TrackData"
            }
            if !hasCache
              query["FilterExpression"] = "#timestamp > :time_ago"
              query["ExpressionAttributeNames"] = {
                "#timestamp" : "timeStamp"
              }
              query["ExpressionAttributeValues"] = {
                ":time_ago" : {
                  "N" : (Date.now() - 1000 * 60 * 60 * 24)+""
                }
              }
            if hasCache and isMonthView
              query["ExpressionAttributeValues"][":time_ago"] = {
                "N" : (Date.now() - 1000 * 60 * 60 * 24 * 30)+""
              }
            console.log query
            @App.dynamoDB.scan query, (err, result)=>
              console.log "error "+err
              results = @App.convertFromArray result["Items"]
              console.log results
              #console.log results
              for result in results
                result['timestamp'] = result['timeStamp']
                delete result['timeStamp']
              defer.resolve @extractTimes results
            return defer.promise

          promises = [getIndoorBeacons(hasCache), getLocationData(hasCache)]
          q.allSettled(promises).then (resolveds)=>
            stopAtPrevious = null
            if resolveds[0]["state"] is "fulfilled"
              indoorBeaconsDetect = resolveds[0]["value"]
              locationDetect = resolveds[1]["value"]
              console.log "indoor entries : #{indoorBeaconsDetect.length} location entries : #{locationDetect.length}"
              result = {
                indoor : 0,
                outdoor : 0
              }
              for beaconDetect in indoorBeaconsDetect
                if beaconDetect["diff"] is 0
                  beaconDetect["diff"] = 1000 * 60 * 2
                console.log beaconDetect
                console.log beaconDetect["diff"]
                result['indoor'] += beaconDetect["diff"]
              console.log "indoor"+result['indoor']
              for outdoor in locationDetect
                result['outdoor'] += outdoor["diff"]

              result['outdoorString'] = "Minutes : "+result['outdoor']/(1000 * 60)
              result['indoorString'] = "Minutes : "+result['indoor']/(1000 * 60)
              #var now = moment()
              #@App.cache["value"][req.params.deviceId][now.date()] = result['outdoorString']
              #@App.cache["time"] = Date.now()

              return @App.sendContent req, res, result
            else
              console.log "Indoor beacon retrieve isnt successful"
        else
          console.log err

module.exports = Presence

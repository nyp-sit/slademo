q = require "q"
tri = require "trilateration"
request = require "request"
class Presence
  constructor : (@App)->
    @beaconMap = {}
    @App.router.get "/api/presence", @getAllPresence
    @App.router.get "/api/presenceTri", @getAllPresenceTri

  getAllPresenceTri : (req, res)=>
    #Get all the devices, becasue primary key in presence table is by device id
    @getAllDevices().then (devices)=>
      retrievePresencePromises = []
      for device in devices
        retrievePresencePromises.push @getPresenceForDevice(device["id"])
      q.allSettled(retrievePresencePromises).then (results)=>
        deviceWithData = []
        for result in results
          result = result["value"]
          if Object.keys(result).length != 0
            deviceWithData.push result
        #Only use things within around 5mins?
        #for item,i in readyForCal
          #if (Date.now() - item["timestamp"]) > (1000 * 60 * 5)
          #  readyForCal = readyForCal.splice(item, 1)
          #  console.log "Removed"
        #get the three points of beacon, perform trileration
        performTrilateration = (beacon)=>
            console.log "tri"
            defer = q.defer();
            if beacon["beaconDataList"]
              beaconDataList = beacon["beaconDataList"]
              distances = []
              beaconsPoints = []
              for beaconData in beaconDataList
                distances.push beaconData["distance"]
                #Cannot get beacon info here?
                beaconsPoints.push @getBeaconInfo(beaconData, beacon['deviceId'])
              q.allSettled(beaconsPoints).then (beaconPromise)=>
                beaconsAssembled = []
                deviceId = beaconPromise.then
                for assembleBeacon in beaconPromise
                  assembleBeacon = assembleBeacon["value"]
                  beaconsAssembled.push assembleBeacon
                for beaconWithValue, i in beaconsAssembled
                  tri.addBeacon i, tri.vector(parseFloat(beaconWithValue['presence']['beacon']['x'] ), parseFloat(beaconWithValue['presence']['beacon']['y']) )
                  tri.setDistance i, parseFloat(beaconWithValue['presence']['distance'] )

                pos = tri.calculatePosition();
                console.log "tri"
                console.log beacon
                defer.resolve {position : pos, deviceId  : beacon['deviceId'], nearestBeacon : beacon['nearestBeaconId']}
            return defer.promise

        allDevicesTrilateration = []
        for beacon in deviceWithData
          if Object.keys(beacon).length != 0
            allDevicesTrilateration.push performTrilateration(beacon)

        q.allSettled(allDevicesTrilateration).then (result)=>
          console.log "result"
          return @App.sendContent req, res, result

  getPresenceForDeviceTri : (deviceID)=>
    defer = q.defer();
    @App.dynamoDB.query {
      TableName : "hdb_presence",
      KeyConditionExpression : "#id = :device_id"
      ExpressionAttributeNames : {
        "#id" : "deviceId"
      },
      ExpressionAttributeValues : {
        ":device_id" : {
          "S" : deviceID
        }
      }
    },(err, result)=>
      console.log result
      result = @App.convertFromArray result["Items"]
      promises = [] #Promise to get beacon Info
      for item in result
        #Remove the M mapping
        parsedBeaconDataList = []
        for beacon in item["beaconDataList"]
          newItem = beacon["M"]
          parsedBeaconDataList.push @App.convertToObject newItem
        item["beaconDataList"] = parsedBeaconDataList
      if result.length is 0
        console.log "resolve no length"
        defer.resolve {}
      else
        defer.resolve result[0]

    return defer.promise
  getAllDevices : ()=>
    defer = q.defer()
    @App.dynamoDB.scan {
      TableName : "hdb_device_status"
    }, (err, devicesResult)=>
      devices = @App.convertFromArray devicesResult["Items"]

      console.log "hello"
      defer.resolve devices
    return defer.promise
  getBeaconInfo : (presence, deviceId) =>
    defer = q.defer();
    beaconID = presence["beaconId"]
    if @beaconMap[beaconID]
      presence["beacon"] = @beaconMap[beaconID]
      defer.resolve {presence : presence, deviceId : deviceId}
    else
      @App.dynamoDB.query {
        TableName : "hdb_beacons",
        KeyConditionExpression : "#id = :beacon_id"
        ExpressionAttributeNames : {
          "#id" : "id"
        },
        ExpressionAttributeValues : {
          ":beacon_id": {
            "S" : beaconID
          }
        }
      }, (err, singleBeaconResult)=>
        singleBeaconResult = @App.convertFromArray singleBeaconResult["Items"]
        @beaconMap[beaconID] = singleBeaconResult[0]
        presence["beacon"] = singleBeaconResult[0]
        defer.resolve {presence : presence, deviceId : deviceId}
    return defer.promise

  resolveArrayOfPromises : (array)=>
    defer = q.defer();
    results = []
    array.forEach (item, index)=>
      item.then (result)=>
        results[index] = result
    interval = setInterval ()=>
      console.log array.length
      if results.length is array.length
        clearInterval interval
        defer.resolve results
    ,100
    return defer.promise
  getAllPresence : (req, res)=>
    #Get all the devices, becasue primary key in presence table is by device id
    @getAllDevices().then (devices)=>
        for device in devices
          retrievePresencePromises = []
          for device in devices
            retrievePresencePromises.push @getPresenceForDevice(device["id"])
          q.allSettled(retrievePresencePromises).then (results)=>
            items = []
            for result in results
              if result['state'] is "fulfilled" and Object.keys(result['value']).length != 0
                items.push result['value']
            @getBeaconsByIdForPresence(items , req).then (presenceWithInfo)=>
              moved = []
              for presence in presenceWithInfo
                moved.push presence["value"]
              @calculateAngles moved
              return @App.sendContent req, res,moved

  getPresenceForDevice : (deviceID)=>
    defer = q.defer();
    @App.dynamoDB.query {
      TableName : "hdb_presence",
      KeyConditionExpression : "#id = :device_id"
      ExpressionAttributeNames : {
        "#id" : "deviceId"
      },
      ExpressionAttributeValues : {
        ":device_id" : {
          "S" : deviceID
        }
      }
    },(err, result)=>
      #defer.resolve(@App.convertFromArray result["Items"])
      result = @App.convertFromArray result["Items"]

      promises = [] #Promise to get beacon Info
      for item in result
        #Remove the M mapping
        parsedBeaconDataList = []
        for beacon in item["beaconDataList"]
          newItem = beacon["M"]
          parsedBeaconDataList.push @App.convertToObject newItem
        item["beaconDataList"] = parsedBeaconDataList
      if result.length is 0
        defer.resolve {}
      else
        defer.resolve result[0]

    return defer.promise
  getAllDevices : ()=>
    defer = q.defer()
    @App.dynamoDB.scan {
      TableName : "hdb_device_status"
    }, (err, devicesResult)=>
      devices = @App.convertFromArray devicesResult["Items"]

      console.log "hello"
      defer.resolve devices
    return defer.promise
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
            data["y"] = data["y"]#(parseInt(req.query.height)/100)* parseInt(data["y"])
            data["x"] = data["x"]#(parseInt(req.query.width)/100)* parseInt(data["x"])
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
      sinResult = Math.abs(Math.sin(refAngle  * Math.PI / 180));#This is y since, sin angle = y
      cosResult = Math.abs(Math.cos(refAngle  * Math.PI / 180));#This is x since cos angle = x

      item["y"] = (directionFactorY * (sinResult * item["distance"]))
      item["x"] = (directionFactorX * (cosResult * item["distance"]))
module.exports = Presence

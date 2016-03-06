q = require "q"
tri = require "trilateration"
class Presence
  constructor : (@App)->
    @beaconMap = {}
    @App.router.get "/api/presence", @getAllPresence
  getAllPresence : (req, res)=>
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

                defer.resolve {position : pos, deviceId  : beacon['deviceId']}
            return defer.promise

        allDevicesTrilateration = []
        console.log deviceWithData
        for beacon in deviceWithData
          if Object.keys(beacon).length != 0
            allDevicesTrilateration.push performTrilateration(beacon)

        q.allSettled(allDevicesTrilateration).then (result)=>
          console.log "result"
          return @App.sendContent req, res, result


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
module.exports = Presence

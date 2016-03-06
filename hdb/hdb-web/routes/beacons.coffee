
class Beacon
  constructor : (@App)->

    @App.router.get "/api/beacons", @getAllBeacons
  getAllBeacons : (req, res)=>
    dynamoDB = new @App.aws.DynamoDB();
    dynamoDB.scan {
      TableName : "hdb_beacons"
      #Select : "ALL_ATTRIBUTES",
    }, (err, result)=>
      result["Items"].sort (a,b)=>
        return a["id"] > b["id"]
      @App.sendContent req, res, @App.convertFromArray result["Items"]
module.exports = Beacon

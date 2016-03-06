express = require "express"
aws = require "aws-sdk"
Config = require "./config"
Beacon = require "./routes/beacons"
Presence = require "./routes/presence"
class App
	constructor : ()->
		@router = new express()
		@config = Config

		@router.use "/", express.static(__dirname+"/client")
		@router.use (req, res, next)=>
			res.setHeader('Access-Control-Allow-Origin', "*");
			next()
		@router.listen 3003,()=>
			console.log "running"

			aws.config.update @config.aws
			@aws = aws
			@dynamoDB = new @aws.DynamoDB();


			beaconRoute = new Beacon(@)
			presenceRoute = new Presence(@)
	sendContent : (req, res,content)=>
      res.status 200
      return res.json content
	sendError: (req, res, error, content)=>
      res.status error
      return res.end content
	convertFromArray : (array)=>
		parsedArray = []
		for item in array
			parsedArray.push @convertToObject item
		return parsedArray
	convertToObject : (item)=>
		parsed = {}
		for key in Object.keys(item)
			objectWithTypeAsKey = item[key]
			typeKeys = Object.keys objectWithTypeAsKey
			parsed[key] = item[key][typeKeys[0]]
		return parsed
module.exports = App
new App()

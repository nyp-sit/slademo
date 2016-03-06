express = require "express"
aws = require "aws-sdk"
Config = require "./config"
Presence = require "./routes/presence"
json = require "jsonfile"
class App
	constructor : ()->
		@router = new express()
		@config = Config

		#Handling cache
		@cache = {
			time : -1,
			value : {
			}
		}
		@getCache()

		@router.use express.static(__dirname+"/client")

		@router.use (req, res, next)=>
			res.setHeader('Access-Control-Allow-Origin', "*")
			next()
		@router.listen 3002,()=>
			console.log "running"

			aws.config.update @config.aws
			@aws = aws
			@dynamoDB = new @aws.DynamoDB();

			presenceRoute = new Presence(@)
			@router.get '*', (req, res) =>
	        res.sendFile(__dirname+'/client/index.html'); # load the single view file (angular will handle the page changes on the front-end)
	getCache : ()=>
		json.readFile "cache.json", (err, obj)=>
			if err and err.code is 'ENOENT'
				@saveCache()
			@cache = obj

	saveCache : ()=>
		json.writeFile "cache.json", @cache#, (err)=>
			#console.log err
	sendContent : (req, res,content)=>
      res.status 200
      return res.json content
	sendError: (res, error, content)=>
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

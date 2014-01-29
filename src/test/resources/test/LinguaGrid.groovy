@Grab('it.celi.research:langrid-manager:0.1')
import it.celi.services.grid.LinguaGridConnector
import it.celi.services.grid.data.*

println "Connecting to the database."
def manager = new LinguaGridConnector('lapps', 'localhost', 'langrid', 'langrid')

println "Registered services."
manager.services().each { service ->
	def endpoint = service.uri[0]
	println "${service?.name} ${endpoint?.uri} ${endpoint?.realm}"
}
return

/*
println "Registered service types."
manager.serviceTypes().each { println it }

return
*/
Endpoint endpoint = new Endpoint()
endpoint.username = 'operator'
endpoint.password = 'operator'
endpoint.uri = "http://localhost:8080/GateServices/services/Tokenizer"
endpoint.realm = null // Any realm

Service service = new Service()
service.name = 'Gate Tokenizer'
service.description = 'ANNIE Tokenizer from Gate'
service.uri << endpoint
service.support = "Don't know what the support field is for..."

println "Registering ${service.name}"
manager.registerService(service)

println "Done."

/* This is what we want a config file to look like.

gridID = 'anc'
serverName = 'localhost'

database {
	username = 'langrid'
	password = 'langrid'
}

service {
	name = Gate Sentence Splitter'
	description = 'Sentence splitter from Gate"
	uri = 'http://localhost:8080/GateServices/services/SentenceSplitter'
	support = ''
}

service {
	name = Gate Tokenizer'
	description = 'Tokenizer from Gate"
	uri = 'http://localhost:8080/GateServices/services/Tokenizer'
	support = ''
}


*/
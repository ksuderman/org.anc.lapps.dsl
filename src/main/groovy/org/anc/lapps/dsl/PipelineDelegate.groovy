package org.anc.lapps.dsl

import org.lappsgrid.client.ServiceClient
import org.anc.lapps.pipeline.Pipeline
import org.lappsgrid.api.DataSource
import org.lappsgrid.core.DataFactory
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer

//import org.lappsgrid.discriminator.DiscriminatorRegistry
import static org.lappsgrid.discriminator.Discriminators.Uri


/**
 * @author Keith Suderman
 */
class PipelineDelegate {
//    def services = []
    DataSource datasource
    Pipeline pipeline = new Pipeline()
    boolean performValidation = false
    boolean beVerbose = false
    def destination
    def extension

    void add(ServiceClient service) {
        //services << service
//        def url = service.getServiceUrl();
//        def user = service.server.username
//        def password = service.server.password
//        pipeline.add(new RemoteService(url, user, password))
//        services << new RemoteService(url, user, password)
        pipeline.add(service)
    }

    void validate()
    {
        //return pipeline.validate()
        performValidation = true
//        return false;
    }

    void validate(boolean performValidation) {
        this.performValidation = performValidation
    }

    void verbose(boolean beVerbose) {
        this.beVerbose = beVerbose
    }

    void extension(String extension) {
        this.extension = extension
    }

    void run() {
        println "Running pipeline"
        println "Destination: ${destination.path}"
        if (!destination.exists()) {
            println "Creating destination directory"
            if (!destination.mkdirs()) {
                println "Unable to create ${destination.path}"
                return
            }
        }
        String json = datasource.execute(DataFactory.list());
        Data listData = Serializer.parse(json, Data);
        if (listData.discriminator == Uri.ERROR) {
            println "Datasource.list() returned an error:"
            println listData.getPayload()
            return
        }
        Object payload = listData.payload
        if (payload == null) {
            println "DataSource returned a null payload"
            return
        }
        List<String> keys;
        if (payload instanceof List) {
            keys = (List<String>) payload
        }
        else if (payload instanceof String) {
            keys = Serializer.parse(payload.toString(), List)
        }
        else {
            println "Unknown payload type: ${payload.class.name}"
        }
//        String[] keys = listData.payload.split("\\s+")
        keys.each { key ->
            Data data = datasource.execute(DataFactory.get(key))
            if (data.discriminator == Uri.ERROR) {
                println "Unable to fetch ${key}: ${data.payload}"
            }
            else
            {
                data = pipeline.execute(data, performValidation);
                if (data.payload == null) {
                    println "ERROR: data.payload is NULL!"
                    println "Return type is ${data.discriminator}"
                }
                else if (data.discriminator == Uri.ERROR) {
                    println "ERROR: ${data.payload}"
                }
                else {
                    String filename = key
                    if (extension) {
                        filename = "${key}.${extension}"
                    }
                    File file = new File(destination, filename)
                    println "Writing ${file.path}"
                    file.setText(data.payload, "UTF-8")
                }
                println ""
            }
        }
    }

    void datasource(source) {
        datasource = source
    }

    void destination(String path) {
        destination = new File(path)
    }

    void destination(File file) {
        destination = file
    }
}

package org.anc.lapps.dsl

import org.lappsgrid.client.service.ServiceClient
import org.anc.lapps.pipeline.Pipeline
import org.lappsgrid.api.Data
import org.lappsgrid.api.DataSource
import org.lappsgrid.core.DataFactory
import org.lappsgrid.discriminator.DiscriminatorRegistry
import org.lappsgrid.discriminator.Types

/**
 * @author Keith Suderman
 */
class PipelineDelegate {
//    def services = []
    DataSource datasource
    Pipeline pipeline = new Pipeline()
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

    boolean validate()
    {
        return pipeline.validate()
//        return false;
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
        Data listData = datasource.query(DataFactory.list());
        if (listData.discriminator == Types.ERROR) {
            println "Datasource.list() returned an error:"
            println listData.getPayload()
            return
        }
        String[] keys = listData.payload.split("\\s+")
        keys.each { key ->
            Data data = datasource.query(DataFactory.get(key))
            if (data.discriminator == Types.ERROR) {
                println "Unable to fetch ${key}: ${data.payload}"
            }
            else
            {
                data = pipeline.execute(data);
                if (data.payload == null) {
                    println "ERROR: data.payload is NULL!"
                    String name = DiscriminatorRegistry.get(data.discriminator);
                    println "Return type is ${name} (${data.discriminator})"
                }
                else if (data.discriminator == Types.ERROR) {
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

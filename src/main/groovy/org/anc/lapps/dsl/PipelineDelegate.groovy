package org.anc.lapps.dsl

import org.anc.lapps.client.RemoteService
import org.lappsgrid.api.Data
import org.lappsgrid.api.DataSource
import org.lappsgrid.core.DataFactory
import org.lappsgrid.discriminator.Types

/**
 * @author Keith Suderman
 */
class PipelineDelegate {
    def services = []
    DataSource datasource
//    Pipeline pipeline = new Pipeline()
    def destination

    void add(Service service) {
        //services << service
        def url = service.getServiceUrl();
        def user = service.server.username
        def password = service.server.password
//        pipeline.add(new RemoteService(url, user, password))
        services << new RemoteService(url, user, password)
    }

    boolean validate()
    {
//        return pipeline.validate()
        return false;
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
//        DataSourceClient dataSourceClient = new DataSourceClient(datasource.getServiceUrl(), datasource.server.username, datasource.server.password)
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
                boolean save = true
                services.each { service ->
                    println "Running service ${service.getEndpoint()}"
                    data = service.execute(data)
                    if (data.discriminator == Types.ERROR) {
                        println "ERROR: Processing ${key} : ${data.payload}"
                        save = false
                        return
                    }
                }
//                data = pipeline.execute(data);
//                if (data.payload == null) {
//                    println "ERROR: data.payload is NULL!"
//                    String name = DiscriminatorRegistry.get(data.discriminator);
//                    println "Return type is ${name} (${data.discriminator})"
//                }
//                else if (save) {
                if (save) {
                    File file = new File(destination, key)
                    println "Writing ${file.path}"
                    file.setText(data.payload, "UTF-8")
                }
                println ""
            }
        }
    }

    void dataSource(source) {
//        def url = source.getServiceUrl()
//        def user = source.server.username
//        def pass = source.server.password
//        println "DataSource: ${url}"
//        println "Username  : ${user}"
//        println "Password  : ${pass}"
//        datasource = new DataSourceClient(url, user, pass)
        datasource = source
    }

    void destination(String path) {
        destination = new File(path)
    }

    void destination(File file) {
        destination = file
    }
}

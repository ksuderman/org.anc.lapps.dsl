package org.anc.lapps.dsl

import org.anc.grid.data.masc.client.DataSourceClient
import org.anc.lapps.client.RemoteService
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * @author Keith Suderman
 */
class LappsDsl {
//    def servers = [:]
//    def services = [:]

    void run(File file, args) {
        run(file.text, args)
    }

    void run(String scriptString, args) {
        ClassLoader loader = Thread.currentThread().contextClassLoader;
        if (loader == null) {
            loader = this.class.classLoader
        }
        ImportCustomizer customizer = new ImportCustomizer()
        def packages = [
            'org.lappsgrid.api',
            'org.lappsgrid.core',
            'org.lappsgrid.discriminator',
            'org.anc.grid.masc.data.client',
            'org.anc.lapps.client',
            'org.anc.lapps.pipeline',
            'org.anc.io',
            'org.anc.util',
            'org.anc.xml'
        ]
        packages.each {
            customizer.addStarImports(it)
        }

        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(customizer)
        Binding bindings = new Binding()
        GroovyShell shell = new GroovyShell(loader, bindings, configuration)

        Script script = shell.parse(scriptString)
        if (args != null && args.size() > 0) {
            script.binding.setVariable("args", args)
        }
        ExpandoMetaClass meta = new ExpandoMetaClass(script.class, false)
        def includes = []
        def included = [:]
        meta.include = { String fileName ->
            //throw new UnsupportedOperationException("Include statements are not supported at this time.")
            if (included[fileName] != null) {
                return
            }
            File file = new File(fileName)
            if (!file.exists()) {
                throw new FileNotFoundException(fileName)
            }
            println "Including ${fileName} from ${file.path}"


            script.evaluate(file.text)
//            shell.evaluate(file.text, 'Servers')
//            def includedScript = shell.parse(file)
//            includedScript.run(file, null)
//            includedScript.binding = script.binding
//            includedScript.metaClass = delegate.metaClass
//            includedScript.run()
            included[fileName] = true
        }

        meta.dataSource = { Closure cl ->
            cl.delegate = new DataSourceDelegate()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            String url = cl.delegate.getServiceUrl()
            String user = cl.delegate.server.username
            String pass = cl.delegate.server.password
            return new DataSourceClient(url, user, pass)
//            return cl.delegate
        }

        meta.server = { Closure cl ->
            cl.delegate = new ServerDelegate()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return new Server(cl.delegate)
        }

        meta.service = { Closure cl ->
            cl.delegate = new ServiceDelegate()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            def service = new Service(cl.delegate)
            def url = service.getServiceUrl();
            def user = service.server.username
            def pass = service.server.password
            return new RemoteService(url, user, pass)
        }

        meta.pipeline = { Closure cl ->
            cl.delegate = new PipelineDelegate()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return cl.delegate
        }

        meta.initialize()
        println "Running main."
        script.metaClass = meta
        script.run()
    }

    static void main(args) {
        if (args.size() == 0) {
            println """
USAGE

java -jar lapps.jar /path/to/script"

"""
            return
        }

        def argv = null
        if (args.size() > 1) {
            argv = args[1..-1]
        }
        new LappsDsl().run(new File(args[0]), argv)
    }
}



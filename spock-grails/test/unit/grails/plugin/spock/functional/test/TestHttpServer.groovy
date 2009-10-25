package grails.plugin.spock.functional.test

import java.io.IOException

import javax.servlet.Servlet
import org.mortbay.jetty.Server
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.ServletHolder

class TestHttpServer {

  final protected server 
  final boolean started
  
  Closure get
  Closure post
  Closure put
  Closure delete
   
  void start() {
    if (!started) {
      server = new Server(0)
      def context = new Context(server, "/")
      context.addServlet(new ServletHolder(new TestHttpServerServlet(this)), "/*")
      server.start()
      started = true
    }
  }
  
  void stop() {
    if (started) {
      server.stop()
      started = false
    }
  }
  
  def getPort() {
    server?.connectors[0].localPort
  }
  
  def getBaseUrl() {
      "http://localhost:$port/"
  }

}
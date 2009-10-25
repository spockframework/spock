package grails.plugin.spock.functional.test

import java.io.IOException

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestHttpServerServlet extends HttpServlet {

	private server
	
	TestHttpServerServlet(server) {
		this.server = server
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	  if (server.get) {
	    server.get?.call(req, res)
	  } else {
	   super.doGet(req, res)
	  }
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	  if (server.post) {
	    server.post?.call(req, res)
	  } else {
	   super.doPost(req, res)
	  }
	}
	
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	  if (server.put) {
	    server.put?.call(req, res)
	  } else {
	   super.doPut(req, res)
	  }
	}

	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	  if (server.delete) {
	    server.delete?.call(req, res)
	  } else {
	   super.doDelete(req, res)
	  }
	}
	
}
/* Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
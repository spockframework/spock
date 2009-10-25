package grails.plugin.spock.functional

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebResponse
import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.HttpMethod
import com.gargoylesoftware.htmlunit.WebWindow
import com.gargoylesoftware.htmlunit.WebRequestSettings

import java.net.MalformedURLException

import grails.plugin.spock.functional.util.URLUtils

import grails.plugin.spock.functional.htmlunit.configurer.WebRequestSettingsConfigurer

class WebSession {

  final WebClient client
  String base
  
  WebSession(String base = null) {
    this(new WebClient(), base)
  }
  
  WebSession(WebClient client, String base = null) {
    this.client = client
    this.setBase(base)
    configureWebClient(client)
  }
      
  void setBase(String base) {
    if (base) this.base = URLUtils.forceTrailingSlash(base)
  }
  
  Page get(url, Closure requestConfiguration = null) {
    request(HttpMethod.GET, url, requestConfiguration)
  }

  Page post(url, Closure requestConfiguration = null) {
    request(HttpMethod.POST, url, requestConfiguration)
  }
  
  Page delete(url, Closure requestConfiguration = null) {
    request(HttpMethod.DELETE, url, requestConfiguration)
  }
  
  Page put(url, Closure requestConfiguration = null) {
    request(HttpMethod.PUT, url, requestConfiguration)
  }

  Page request(HttpMethod method, url, Closure requestConfiguration = null) {
      def reqURL = makeRequestURL(url)
          
      def requestSettings = new WebRequestSettings(reqURL, method)
      
      if (requestConfiguration) {
          requestConfiguration.delegate = new WebRequestSettingsConfigurer(requestSettings)
          requestConfiguration.resolveStrategy = Closure.DELEGATE_FIRST
          requestConfiguration.call()
      }

      def requestPage = client.getPage(requestSettings)
      
      if (redirectEnabled && didReceiveRedirect) {
        doFollowRedirect()
      } else {
        requestPage
      }
  }
  
  WebWindow getCurrentWindow() {
    client.currentWindow
  }
  
  Page getPage() {
    currentWindow?.enclosedPage
  }

  WebResponse getResponse() {
    page?.webResponse
  }
  
  WebRequestSettings getRequestSettings() {
    response?.requestSettings
  }
  
  URL getRequestURL() {
    requestSettings.url
  }
  
  void setRedirectEnabled(boolean flag) {
    client.redirectEnabled = flag
  }
  
  boolean isRedirectEnabled() {
    client.redirectEnabled
  }
  
  boolean isDidReceiveRedirect() {
    (page) ? isRedirectStatus(response.statusCode) : false
  }
  
  Page followRedirect() {
    if (redirectEnabled || didReceiveRedirect == false) {
      null
    } else {
      doFollowRedirect()
    }
  }
  
  String getRedirectURL() {
    (didReceiveRedirect) ? response.getResponseHeaderValue('Location') : null
  }
  
  protected void configureWebClient(WebClient client) {
    client.with {
      redirectEnabled = false // we need to do this ourselves
      throwExceptionOnFailingStatusCode = false
    }
  }
      
  protected doFollowRedirect() {
    get(redirectURL)
  }
  
  protected makeRequestURL(targetURL, Page relativePage = null) {
    targetURL = targetURL as String

    try {
      new URL(targetURL)
    } catch (MalformedURLException e) {
      def requestURLPath = URLUtils.relativize(targetURL)
      
      if (URLUtils.isAbsolutePath(targetURL)) {
        makeRequestURLRelativeToBase(requestURLPath)
      } else {
        if (relativePage) {
          makeRequestURLRelativeToPage(requestURLPath, relativePage)
        } else {
          makeRequestURLRelativeToCurrentPageOrBase(requestURLPath)
        }
      }
    }
  }

  protected makeRequestURLRelativeToBase(String urlPath) {
    if (this.base) {
      new URL(new URL(base), urlPath)
    } else {
      throw new IllegalArgumentException("Cannot makeRequestURLRelativeToBase for $urlPath as 'base' is not set".toString())
    }
  }
  
  protected makeRequestURLRelativeToPage(String urlPath, Page pageBase) {
    new URL(pageBase.webResponse.requestSettings.url, urlPath)
  }
  
  protected makeRequestURLRelativeToCurrentPageOrBase(String urlPath) {
    if (page) {
      new URL(requestURL, urlPath)
    } else {
      new URL(new URL(base), urlPath)
    }
  }
  
  protected boolean isRedirectStatus(code) {
    code in [300, 301, 302, 303, 307]
  }
  
}
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

/**
 * A Groovier wrapper for HTMLUnit's WebClient that provides an API
 * suited for a series of sequenced requests.
 */
class WebSession {

  /**
   * The underlying client used.
   */
  final WebClient client
  
  /**
   * The optional base for all requests made to URL paths
   */
  String base
  
  /**
   * Creates session with a default client, and base if provided.
   */
  WebSession(String base = null) {
    this(new WebClient(), base)
  }

  /**
   * Creates a session with the given client, and base if provided.
   */  
  WebSession(WebClient client, String base = null) {
    this.client = client
    this.setBase(base)
    configureWebClient(client)
  }
  
  void setBase(String base) {
    if (base) this.base = URLUtils.forceTrailingSlash(base)
  }

  /**
   * Makes a HTTP GET request
   * 
   * @see #request(HttpMethod, Object, Closure)
   */  
  Page get(url, Closure requestConfiguration = null) {
    request(HttpMethod.GET, url, requestConfiguration)
  }

  /**
   * Makes a HTTP POST request
   * 
   * @see #request(HttpMethod, Object, Closure)
   */
  Page post(url, Closure requestConfiguration = null) {
    request(HttpMethod.POST, url, requestConfiguration)
  }

  /**
   * Makes a HTTP DELETE request
   * 
   * @see #request(HttpMethod, Object, Closure)
   */  
  Page delete(url, Closure requestConfiguration = null) {
    request(HttpMethod.DELETE, url, requestConfiguration)
  }

  /**
   * Makes a HTTP PUT request
   * 
   * @see #request(HttpMethod, Object, Closure)
   */  
  Page put(url, Closure requestConfiguration = null) {
    request(HttpMethod.PUT, url, requestConfiguration)
  }
  
  /**
   * Makes a request of type {@code method} to {@code url} after optionally configuring the 
   * request settings.
   * 
   * @param method the HTTP method of the request
   * @param url a psuedo url to be converted to a real URL by {@link #makeRequestURL(Object)}
   * @param requestConfiguration if provided, will be evaluated against a {@link WebRequestSettingsConfigurer} 
   *        with the request settings
   * @return The page object created by the request
   */
  Page request(HttpMethod method, url, Closure requestConfiguration = null) {
    def requestSettings = new WebRequestSettings(makeRequestURL(url), method)
    
    if (requestConfiguration) {
      requestConfiguration.delegate = new WebRequestSettingsConfigurer(requestSettings)
      requestConfiguration.resolveStrategy = Closure.DELEGATE_FIRST
      requestConfiguration.call()
    }

    client.getPage(requestSettings)
    
    if (redirectEnabled && didReceiveRedirect) {
      doFollowRedirect()
    } else {
      page
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
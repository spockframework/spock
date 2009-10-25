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
  
  /**
   * @return the current window of the client
   * @see WebClient#getCurrentWindow()
   */
  WebWindow getCurrentWindow() {
    client.currentWindow
  }

  /**
   * @return the enclosed page of the current window
   * @see WebWindow#getEnclosedPage()
   */  
  Page getPage() {
    currentWindow?.enclosedPage
  }

  /**
   * @return the response associated with the current page
   * @see Page#getWebResponse()
   */  
  WebResponse getResponse() {
    page?.webResponse
  }

  /**
   * @return the request settings used to generate the current response
   * @see WebResponse#getRequestSettings()
   */    
  WebRequestSettings getRequestSettings() {
    response?.requestSettings
  }

  /**
   * @return the URL of the current request
   * @see WebRequestSettings#getURL()
   */
  URL getRequestURL() {
    requestSettings.url
  }
  
  /**
   * Should the client automatically follow redirects?
   * 
   * @see WebClient#setRedirectEnabled()
   */
  void setRedirectEnabled(boolean flag) {
    client.redirectEnabled = flag
  }

  /**
   * Does the client automatically follow redirects?
   * 
   * @see WebClient#isRedirectEnabled()
   */  
  boolean isRedirectEnabled() {
    client.redirectEnabled
  }
  
  /**
   * Is the current response a redirect?
   * 
   * @see isRedirectStatus(int)
   * @return true, if the response was a redirect, false if it wasn't or no request has been made
   */
  boolean isDidReceiveRedirect() {
    (page) ? isRedirectStatus(response.statusCode) : false
  }

  /**
   * If the previous request resulted in a redirect, follow it.
   * 
   * Noop if the client is automatically following redirects.
   * 
   * @see isDidReceiveRedirect()
   * @see doFollowRedirect()
   * @return the page redirected to, or {@code null} if not in a position to redirect
   */
  Page followRedirect() {
    if (redirectEnabled || didReceiveRedirect == false) {
      null
    } else {
      doFollowRedirect()
    }
  }
  
  /**
   * If did receive a redirect, the url of the redirect request.
   * 
   * This is obtained by getting the 'Location' header of the response.
   * 
   * @see isDidReceiveRedirect()
   * @return the url if did receive a redirect, otherwise {@code null}
   */
  String getRedirectURL() {
    (didReceiveRedirect) ? response.getResponseHeaderValue('Location') : null
  }

  /**
   * Make a new get request to the redirect URL.
   * 
   * Note: this should _only_ be called when isDidReceiveRedirect() returns true. This
   * method does not check. Behaviour is unspecified if getRedirectURL() returns {@code null}.
   * 
   * @see getRedirectURL()
   * @see get(Object, Closure)
   */
  protected doFollowRedirect() {
    get(redirectURL)
  }

  /**
   * Sets the client to a state suitable for use with this API.
   * 
   * The 'throwExceptionOnFailingStatusCode' is set to false.
   */
  protected void configureWebClient(WebClient client) {
    client.with {
      throwExceptionOnFailingStatusCode = false
    }
  }
      
  /**
   * Convert a psuedo URL into a URL object.
   * 
   * If targetURL is a valid URL, it is used as is. If it is an absolute path, then 
   * a URL is constructed with it relative to base. Otherwise if relativePage is provided 
   * then a URL relative to that is constructed, else a URL is constructed relative to the 
   * current page if there is one or base is there is not.
   */
  protected URL makeRequestURL(targetURL, Page relativePage = null) {
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

  /**
   * @return A URL of base + urlPath
   */
  protected URL makeRequestURLRelativeToBase(String urlPath) {
    if (this.base) {
      new URL(new URL(base), urlPath)
    } else {
      throw new IllegalArgumentException("Cannot makeRequestURLRelativeToBase for $urlPath as 'base' is not set".toString())
    }
  }

  /**
   * @return A URL that is urlPath relative to pageBase
   */  
  protected URL makeRequestURLRelativeToPage(String urlPath, Page pageBase) {
    new URL(pageBase.webResponse.requestSettings.url, urlPath)
  }

  /**
   * @return A URL that is urlPath relative to the current page if there is one, or
   *         relative to base if there is no current page.
   */    
  protected URL makeRequestURLRelativeToCurrentPageOrBase(String urlPath) {
    if (page) {
      new URL(requestURL, urlPath)
    } else {
      new URL(new URL(base), urlPath)
    }
  }
  
  /**
   * Convenience method for determining if the status is a redirect or not.
   */
  protected boolean isRedirectStatus(int code) {
    code in [300, 301, 302, 303, 307]
  }
  
}
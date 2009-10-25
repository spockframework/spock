package grails.plugin.spock.functional

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebResponse
import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.HttpMethod
import com.gargoylesoftware.htmlunit.WebWindow
import com.gargoylesoftware.htmlunit.WebRequestSettings
import com.gargoylesoftware.htmlunit.CookieManager
import org.apache.commons.httpclient.Cookie
import com.gargoylesoftware.htmlunit.html.HtmlForm
import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.gargoylesoftware.htmlunit.html.ClickableElement

import java.net.MalformedURLException

import grails.plugin.spock.functional.util.URLUtils

import grails.plugin.spock.functional.htmlunit.configurer.WebRequestSettingsConfigurer

import grails.plugin.spock.functional.htmlunit.form.FormWrapper
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
   * @return The cookie manager of the client
   * @see WebClient#getCookieManager()
   */
  CookieManager getCookieManager() {
    client.cookieManager
  }

  /**
   * @return The cookies of the cookie manager
   * @see CookieManager#getCookies()
   */
  Set<Cookie> getCookies() {
    cookieManager.cookies
  }
  
  /**
   * @return the value for the header of the current response
   * @see WebResponse#getResponseHeaderValue(String)
   */
  String getHeader(String header) {
    response.getResponseHeaderValue(header)
  }
  
  /**
   * @return the content value for the meta item
   */
  String getMeta(String name) {
    page.getElementsByTagName('meta')?.find { it.attributes?.getNamedItem('name')?.nodeValue == name }?.contentAttribute
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
    (didReceiveRedirect) ? getHeader('Location') : null
  }
  
  /**
   * @return a FormWrapper for the first form on the page
   * @throws IllegalArgumentException if the form cannot be found
   */
  FormWrapper form() {
    form(0, null)
  }
  
  /**
   * Creates a FormWrapper for the first form on the page, and
   * passes the processor to {@link FormWrapper#call(Closure)} 
   *
   * @return a FormWrapper for the first form on the page
   * @throws IllegalArgumentException if the form cannot be found
   */
  FormWrapper form(Closure processor) {
    form(0, processor)
  }

  /**
   * Creates a FormWrapper for the form at the given index on the page, and
   * passes the processor to {@link FormWrapper#call(Closure)} if provided 
   *
   * @return a FormWrapper for form
   * @throws IllegalArgumentException if the form cannot be found
   */  
  FormWrapper form(Integer formIndex, Closure processor = null) {
    def formElement = page.forms?.getAt(formIndex)
    if (!formElement) {
      throw new IllegalArgumentException("There are no forms in the current response")
    }

    form(formElement, processor)
  }

  /**
   * Creates a FormWrapper for the form with the given id or name, and
   * passes the processor to {@link FormWrapper#call(Closure)} if provided
   *
   * @return a FormWrapper for form
   * @throws IllegalArgumentException if the form cannot be found
   */ 
  FormWrapper form(String idOrName, Closure processor = null) {
    def formElement

    try {
      formElement = page.getHtmlElementById(idOrName.toString())
    } catch (ElementNotFoundException e) {}
    
    if (!formElement) {
      try {
        formElement = page.getFormByName(idOrName)
      } catch (ElementNotFoundException e) {}
    
      if (!formElement) {
        throw new IllegalArgumentException("There is no form with id/name [$idOrName]")
      }
    }
    
    this.form(formElement, processor)
  }
  
  /**
   * Creates a FormWrapper for the form , and
   * passes the processor to {@link FormWrapper#call(Closure)} if provided
   *
   * @return a FormWrapper for form
   */  
  FormWrapper form(HtmlForm form, Closure processor = null) {
    def wrapper = new FormWrapper(form)
    if (processor) {
      wrapper.call(processor)
    }
    wrapper
  }
  
  /**
   * @return a list of elements matching the expression if multiple, the lone element if only one was matched, otherwise null
   */
  def byXPath(expr) {
    try {
      def results = page.getByXPath(expr as String)
      (results.size() > 1) ? results : results[0]
    } catch (ElementNotFoundException e) {
      null
    }
  }
  
  /**
   * @return the first element that has the id, otherwise null
   */  
  def byId(id) {
    try {
      page.getHtmlElementById(id as String)
    } catch (ElementNotFoundException e) {
      null
    }
  }

  /**
   * @return a list of elements with the class if multiple, the lone element if only one was found, otherwise null
   */
  def byClass(cssClass) {
    try {
      def results = page.getByXPath("//*[@class]").findAll {
        it.attributes?.getNamedItem('class')?.value?.split().any { it == cssClass }
      }

      (results.size() > 1) ? results : results[0]
    } catch (ElementNotFoundException e) {
      null
    }
  }

  /**
   * @return a list of elements with the name if multiple, the lone element if only one was found, otherwise null
   */
  def byName(name) {
    def elems = page.getElementsByName(name.toString())
    if (elems) {
      (elems.size() > 1) ? elems : elems[0]
    } else {
      null
    }
  }
  
  /**
   * Performs a click on the anchor with the given id or text.
   * 
   * @return the page generated by the click (may not be different to the current)
   * @throws IllegalArgumentException if no anchor could be found
   */
  Page click(anchorIdentifier) {
    def anchor = byId(anchorIdentifier)
    
    if (!anchor) {
      try { anchor = page.getFirstAnchorByText(anchorIdentifier) } catch (ElementNotFoundException e) {}
    }
      
    if (!anchor) {
      throw new IllegalArgumentException("No such element for id or anchorIdentifier text [${anchorIdentifier}]")
    }
	  
    if (anchor instanceof ClickableElement) {
      anchor.click()
    } else {
      throw new IllegalArgumentException("Found element for id or anchorIdentifier text [${anchorIdentifier}] but it is not clickable: ${anchor}")
    }
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
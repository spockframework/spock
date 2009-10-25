package grails.plugin.spock.functional.util

class URLUtils {

  static boolean isAbsolutePath(urlPath) {
    urlPath.startsWith('/')
  }

  static String relativize(String urlPath) {
    if (urlPath.startsWith('/')) {
      (urlPath.size() > 1) ? urlPath.substring(1) : ""
    } else {
      urlPath
    }
  }
    
  static String forceTrailingSlash(String url) {
    (url.endsWith('/')) ? url : "$url/"
  }
  


}


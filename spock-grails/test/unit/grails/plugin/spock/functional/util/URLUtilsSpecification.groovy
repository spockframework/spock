package grails.plugin.spock.functional.util

import spock.lang.*

class URLUtilsSpecification extends Specification { 

  def relativize() {
    expect:
    URLUtils.relativize(input) == output
    
    where:
    input << ["abc", "/abc", "/", ""]
    output << ["abc", "abc", "", ""]
  }
  
  def forceTrailingSlash() {
    expect:
    URLUtils.forceTrailingSlash(input) == output
    
    where:
    input << ["abc", "abc/", "/", ""]
    output << ["abc/", "abc/", "/", "/"]
  }
  
  def isAbsolutePath() {
    expect:
    URLUtils.isAbsolutePath(input) == output
    
    where:
    input << ["abc", "/abc", "/", ""]
    output << [false, true, true, false] 
  }
  
}
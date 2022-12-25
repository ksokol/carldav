package testutil.helper

import groovy.xml.XmlSlurper

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.notNullValue

class XmlHelper {

  static String getctag(def result) {
    def getctag = new XmlSlurper().parseText(result).response[0].propstat.prop.getctag.text()
    assertThat(getctag, notNullValue())
    return getctag
  }

  static String getetag(def result) {
    return getetag(result, 0)
  }

  static String getetag(def result, def idx) {
    def getetag = new XmlSlurper().parseText(result).response[idx].propstat.prop.getetag.text()
    assertThat(getetag, notNullValue())
    return getetag
  }
}

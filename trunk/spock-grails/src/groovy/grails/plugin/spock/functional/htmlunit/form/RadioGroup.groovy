/*
 * Copyright 2008-2009 the original author or authors.
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
 *
 * The original code of this plugin was developed by Historic Futures Ltd.
 * (www.historicfutures.com) and open sourced.
 */

package grails.plugin.spock.functional.htmlunit.form

import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput

class RadioGroup {
  final String fieldName
  final List<HtmlRadioButtonInput> radioButtons
  
  RadioGroup(String fieldName, List<HtmlRadioButtonInput> radioButtons) {
    this.fieldName = fieldName
    this.radioButtons = radioButtons
  }
    
  void setChecked(String value) {
    def radio = radioButtons.find { it.valueAttribute == value }
    if (radio) {
      radio.checked = true
    } else {
      throw new IllegalArgumentException("No radio button with name '$fieldName' for value '$value' found")
    }
  }

  String getChecked() {
    radioButtons.find { it.checked }?.valueAttribute
  }
}

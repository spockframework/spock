/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import com.gargoylesoftware.htmlunit.html.ClickableElement
import com.gargoylesoftware.htmlunit.html.HtmlSelect
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput
import com.gargoylesoftware.htmlunit.html.HtmlInput
import com.gargoylesoftware.htmlunit.html.HtmlTextArea
import com.gargoylesoftware.htmlunit.html.HtmlButton
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput
import com.gargoylesoftware.htmlunit.html.HtmlResetInput
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput
import com.gargoylesoftware.htmlunit.html.HtmlFileInput
import com.gargoylesoftware.htmlunit.html.HtmlImageInput
import com.gargoylesoftware.htmlunit.html.HtmlForm

import com.gargoylesoftware.htmlunit.ElementNotFoundException

class FormWrapper {

  final HtmlForm form
  
  FormWrapper(HtmlForm form) {
    this.form = form
  }
  
  void click(String idOrNameOrValue) {
    handleFormClick(idOrNameOrValue)
  }
  
  FormWrapper call(Closure processor) {
    processor.delegate = this
    processor.resolveStrategy = Closure.DELEGATE_FIRST
    processor.call()
  }
    
  protected void handleFormClick(String idOrNameOrValue) {
    def field = form.page.getElementById(idOrNameOrValue) 

    if (!field) {
      def fieldsByName = form.getInputsByName(idOrNameOrValue)
      if (fieldsByName.size() > 1) {
        throw new IllegalArgumentException("Unable to 'click' element named '$idOrNameOrValue', multiple elements with that name found, try clicking by value")
      } else if (fieldsByName.size()) {
        field = fieldsByName[0]
      }
    }

    if (!field) {
      field = form.getInputsByValue(idOrNameOrValue)?.find { it instanceof ClickableElement }
    }
    
    if (field && (field instanceof ClickableElement)) {
      field.click() 
    } else {
      throw new IllegalArgumentException("Unable to 'click' element named '$idOrNameOrValue', clickable element not found")
    }
  }

  void setValue(String name, value) {
    def field = find(name)
    if (field) {
      setValue(field, value)
    } else {
      throw new IllegalArgumentException("Unable to set field value, there is no element with name or id [$name]")
    }
  }
  
  void setValue(HtmlSelect select, value) {
    select.setSelectedAttribute(value?.toString(), true)
  }
  
  void setValue(HtmlRadioButtonInput radioButton, value) {
    radioButton.checked = Boolean.valueOf(value)
  }
  
  void setValue(HtmlFileInput fileInput, value) {
    throw new IllegalArgumentException("You cannot set elements of type HtmlFileInput, call methods or set properties on them instead")
  }

  void setValue(HtmlCheckBoxInput checkBox, value) {
    checkBox.checked = Boolean.valueOf(value)
  }

  void setValue(HtmlInput input, value) {
    input.valueAttribute = value
  }

  void setValue(RadioGroup radioButtons, value) {
    radioButtons.checked = value as String
  }

  void setValue(HtmlTextArea textarea, value) {
    textarea.text = value as String
  }
  
  void setValue(field, value) {
    throw new IllegalArgumentException("You cannot set elements of type [${field.class}], call methods or set properties on them instead")
  }  
  
  def find(name) {
    def field
    ['findRadioButtons', 'findSelect', 'findInput', 'findTextArea'].find {
      field = this.metaClass.pickMethod(it, String).invoke(this, name)
      field != null
    }
    field
  }
  
  RadioGroup findRadioButtons(name) {
    def radioButtons = form.getRadioButtonsByName(name)
    (radioButtons) ? new RadioGroup(name, radioButtons) : null
  }
  
  HtmlSelect findSelect(name) {
    try {
      form.getSelectByName(name) 
    } catch (ElementNotFoundException e) {
      null
    }
  }
  
  HtmlInput findInput(name) {
    try {
      form.getInputByName(name) 
    } catch (ElementNotFoundException e) {
      null
    }
  }
  
  HtmlTextArea findTextArea(name) {
    try {
      form.getTextAreaByName(name)
    } catch (ElementNotFoundException ex) {
      null
    }
  }
  
  Object getValue(String name) {
    def field = find(name)
    if (field) {
      getValue(field)
    } else {
      throw new IllegalArgumentException("Unable to get field value, there is no element with name or id [$name]")
    } 
  }
  
  List<String> getValue(HtmlSelect select) {
    select.getSelectedOptions()?.collect { it.valueAttribute }
  }
  
  Boolean getValue(HtmlRadioButtonInput radioButton) {
    radioButton.checked
  }
  
  HtmlFileInput getValue(HtmlFileInput fileInput) {
    fileInput
  }

  Boolean getValue(HtmlCheckBoxInput checkBox) {
    field.checked
  }

  String getValue(HtmlTextArea textarea) {
    textarea.text
  }
  
  String getValue(HtmlInput input) {
    input.valueAttribute
  }

  String getValue(RadioGroup radioButtons) {
    radioButtons.checked
  }
  
  def getValue(field) {
    throw new IllegalStateException("Don't know how to get a value from form element of type [${field}]")
  }
  
  def propertyMissing(String name) {
    getValue(name)
  }

  void propertyMissing(String name, value) {
    setValue(name, value)
  }
  
}

/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock

import org.junit.runner.RunWith
import org.spockframework.sample.IOrderService
import org.spockframework.sample.ShoppingCart
import static spock.lang.Predef.*
import spock.lang.*

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class BasicMocking {
  ShoppingCart cart = new ShoppingCart()
  IOrderService service = Mock()

  def shop() {
    given: "a shopping cart connected to an order service"
    cart.orderService = service
    
    when: "three items are added to the shopping cart"
    cart.addItem "ipod"
    cart.addItem "imac"
    cart.addItem "iphone"

    cart.checkOut 1
    
    then: "the order service dispatches the order"
    service.isOnline() >> true
    1 * service.dispatch(["ipod","imac","iphone"])
    // _._(_:_) >> _ // YEAH!
  }

}


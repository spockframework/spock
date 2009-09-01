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

package spock.tapestry;

import java.lang.annotation.*;

import org.spockframework.runtime.intercept.Directive;
import org.spockframework.tapestry.TapestryProcessor;

/**
 * Activates support for the Tapestry 5 inversion-of-control container. Except for
 * <tt>&#64;TapestrySupport</tt>, this extension relies solely on Tapestry's
 * own annotations. In particular, 
 *
 * <ul>
 * <li><tt>&#64;SubModule</tt> indicates which Tapestry module(s) should be started
 *  (and subsequently shut down)</li>
 * <li><tt>&#64;Inject</tt> marks fields which should be injected with a Tapestry service or
 * symbol</li>
 * </ul>
 *
 * Related annotations like <tt>&#64;Service</tt> and <tt>&#64;Symbol</tt> are also supported.
 * For more information, see the <a href="http://tapestry.apache.org/tapestry5/tapestry-ioc/">
 * Tapestry IoC documentation</a>.
 *
 * <p>For every specification annotated with <tt>&#64;TapestrySupport</tt>, the Tapestry registry
 * is started up and shut down once. Regular fields are injected once before every iteration,
 * <tt>&#64;Shared</tt> fields once before the first iteration.
 *
 * <p><b>Usage example:</b>
 *
 * <pre>
 * &#64;TapestrySupport
 * &#64;SubModule(UniverseModule) 
 * class UniverseSpec extends Specification {
 *   &#64;Inject
 *   UniverseService service
 *
 *   def "service knows the answer to the universe"() {
 *     expect:
 *     service.answer() == 42
 *   }
 * }
 * </pre>
 *
 * <b>Limitations:</b><br>
 * Currently, fields are injected <em>after</em> <tt>setup()</tt>/<tt>setupSpeck()</tt> is called. Therefore,
 * the values to be injected cannot be accessed from these methods.
 *
 * @author Peter Niederwieser
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Directive(TapestryProcessor.class)
public @interface TapestrySupport {}

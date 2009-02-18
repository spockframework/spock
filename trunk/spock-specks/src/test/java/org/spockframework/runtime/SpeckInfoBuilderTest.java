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

//package org.spockframework.runtime;
//
//import java.util.List;
//
//import org.junit.*;
//import static org.junit.Assert.*;
//
//import org.spockframework.compiler.Constants;
//import org.spockframework.runtime.model.BlockInfo;
//import org.spockframework.runtime.model.BlockKind;
//import org.spockframework.runtime.model.MethodInfo;
//import org.spockframework.runtime.model.MethodKind;
//import org.spockframework.runtime.model.SpeckInfo;
//
///**
// * A ...
// *
// * @author Peter Niederwieser
// */
//public class SpeckInfoBuilderTest {
//  SpeckInfo speck;
//
//  @Before
//  public void setup() throws Exception {
//    speck = new SpeckInfoBuilder(AnnotatedSpeckClass.class).build();
//  }
//
//  @Test
//  public void checkFeatureMethods() throws NoSuchMethodException {
//    assertEquals(2, speck.getFeatureMethods().size());
//  }
//
//  @Test
//  public void checkLifecyleMethods() {
//    assertNotNull(speck.getSetupMethod());
//    assertNotNull(speck.getCleanupMethod());
//    assertNotNull(speck.getSetupSpeckMethod());
//    assertNotNull(speck.getCleanupSpeckMethod());
//  }
//
//  @Test
//  public void checkFeatureMethod1() throws NoSuchMethodException {
//    MethodInfo feature = speck.getFeatureMethods().get(0);
//    assertEquals("someFeature", feature.getName());
//    assertEquals(AnnotatedSpeckClass.class.getDeclaredMethod("someFeature"), feature.getReflection());
//  }
//
//  @Test
//  public void checkBlocks() {
//    MethodInfo feature = speck.getFeatureMethods().get(0);
//    List<? extends BlockInfo> blocks = feature.getBlocks();
//    assertEquals(2, blocks.size());
//    checkBlock1(blocks.get(0));
//    checkBlock2(blocks.get(1));
//  }
//
//  private void checkBlock1(BlockInfo block) {
//    assertEquals("A call is made", block.getName());
//    Assert.assertEquals(BlockKind.WHEN, block.getKind());
//  }
//
//  private void checkBlock2(BlockInfo block) {
//    assertEquals("the phone rings", block.getName());
//    assertEquals(BlockKind.THEN, block.getKind());
//  }
//
//  @Test
//  public void checkDataProviders() throws NoSuchMethodException {
//    MethodInfo feature = speck.getFeatureMethods().get(0);
//    List<? extends MethodInfo> dataProviders = feature.getDataProviders();
//    assertEquals(2, dataProviders.size());
//    checkDataProvider1(dataProviders.get(0));
//    checkDataProvider2(dataProviders.get(1));
//  }
//
//  private void checkDataProvider1(MethodInfo param) throws NoSuchMethodException {
//    //assertNull(param.getName());
//    assertEquals(AnnotatedSpeckClass.class.getDeclaredMethod("someFeature" +
//      Constants.DATA_PROVIDER_SUFFIX + "0"), param.getReflection());
//    Assert.assertEquals(MethodKind.DATA_PROVIDER, param.getKind());
//    assertEquals(0, param.getBlocks().size());
//    assertEquals(0, param.getDataProviders().size());
//    assertEquals(null, param.getArgumentComputer());
//  }
//
//  private void checkDataProvider2(MethodInfo param) throws NoSuchMethodException {
//    //assertNull(param.getName());
//    assertEquals(AnnotatedSpeckClass.class.getDeclaredMethod("someFeature" +
//      Constants.DATA_PROVIDER_SUFFIX + "1"), param.getReflection());
//    assertEquals(MethodKind.DATA_PROVIDER, param.getKind());
//    assertEquals(0, param.getBlocks().size());
//    assertEquals(0, param.getDataProviders().size());
//    assertEquals(null, param.getArgumentComputer());
//  }
//
//  private void checkArgumentComputer(MethodInfo computer) throws NoSuchMethodException {
//    // TODO
//  }
//
//  @Test
//  public void checkFeatureMethod2() throws NoSuchMethodException {
//    MethodInfo feature = speck.getFeatureMethods().get(1);
//    assertEquals("anotherFeature", feature.getName());
//    assertEquals(AnnotatedSpeckClass.class.getDeclaredMethod("anotherFeature"), feature.getReflection());
//    assertEquals(0, feature.getBlocks().size());
//    assertEquals(0, feature.getDataProviders().size());
//    assertEquals(null, feature.getArgumentComputer());
//  }
//}

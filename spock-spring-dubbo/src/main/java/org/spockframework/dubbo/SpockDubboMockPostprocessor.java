package org.spockframework.dubbo;

import org.spockframework.mock.MockUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author zhangfanghui
 * @since 2020-04-09
 *
 *
 *  The main function of this class is to replace the member variable that depends on the annotation @Reference with
 *  a mock bean。
 *
 *
 *
 */
public class SpockDubboMockPostprocessor implements BeanPostProcessor,ApplicationContextAware {
  private ApplicationContext applicationContext;
  MockUtil mockUtil = new MockUtil();
  Object springBean;
  List<Annotation> referenceAnn = null;
  final String  reference = "Reference";

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    Class<?> cls = bean.getClass();
    List<Field> fields = new ArrayList<>();
    while (cls != null) {
      fields.addAll(Arrays.asList(cls.getDeclaredFields()));
      cls = cls.getSuperclass();
    }
    for (Field field : fields) {
      try {
        //Get the mock bean from the spring container by name
        springBean = applicationContext.getBean(getBeanName(field));
        referenceAnn = Arrays.asList(field.getAnnotations()).stream().filter(f -> f.annotationType().getName().contains(reference)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(referenceAnn) && Objects.nonNull(springBean) && mockUtil.isMock(springBean)) {
          //If there is a member variable with annotation @Reference in the bean and the spring container has a
          // mock bean for that variable，the variable in the bean is replaced with the mock variable
          field.setAccessible(true);
          ReflectionUtils.setField(field, bean, springBean);
        }
      } catch (Exception e) {

      }
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return null;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  private String getBeanName(Field field){
    String generatedBeanName = field.getGenericType().getTypeName();
    BeanDefinitionRegistry registry = (BeanDefinitionRegistry)applicationContext;
    String id = generatedBeanName;
    List<String> beanNames = new ArrayList<>();
    for(int counter = -1; counter == -1 || registry.containsBeanDefinition(id); id = generatedBeanName + "#" + counter) {
      ++counter;
      beanNames.add(id);
    }
    return beanNames.get(beanNames.size()-1);
  }

}

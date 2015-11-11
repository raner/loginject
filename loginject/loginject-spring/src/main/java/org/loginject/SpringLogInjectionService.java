//                                                                          //
// Copyright 2015 Mirko Raner                                               //
//                                                                          //
// Licensed under the Apache License, Version 2.0 (the "License");          //
// you may not use this file except in compliance with the License.         //
// You may obtain a copy of the License at                                  //
//                                                                          //
//     http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                          //
// Unless required by applicable law or agreed to in writing, software      //
// distributed under the License is distributed on an "AS IS" BASIS,        //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. //
// See the License for the specific language governing permissions and      //
// limitations under the License.                                           //
//                                                                          //

package org.loginject;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;

/**
 * The {@link SpringLogInjectionService} implements log injection for the Spring Framework. It exposes logger
 * bindings as a {@link BeanFactoryPostProcessor}.
 *
 * @param <_Logger_> the logger type
 *
 * @author Mirko Raner
 */
public class SpringLogInjectionService<_Logger_> implements LogInjectionService<BeanFactoryPostProcessor, _Logger_>
{
    private Field declaringClassField;
    private Field containingClassField;

    /**
     * Creates a new instance of the {@link SpringLogInjectionService}.
     */
    public SpringLogInjectionService()
    {
        try
        {
            declaringClassField = DependencyDescriptor.class.getDeclaredField("declaringClass");
            containingClassField = DependencyDescriptor.class.getDeclaredField("containingClass");
            Stream.of(declaringClassField, containingClassField).forEach(field -> field.setAccessible(true));
        }
        catch (NoSuchFieldException noSuchField)
        {
            throw new NoSuchFieldError(noSuchField.getMessage());
        }
    }

    public BeanFactoryPostProcessor getBindings(LogInject<_Logger_> logInject)
	{
		return new BeanFactoryPostProcessor()
		{
			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
			{
				DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)beanFactory;
				AutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver()
				{
					@Override
					public Object getSuggestedValue(DependencyDescriptor descriptor)
					{
						return logInject.createLogger(getInjecteeClass(descriptor));
					}
				};
				defaultListableBeanFactory.setAutowireCandidateResolver(resolver);
			}
		};
	}

    Class<?> getInjecteeClass(DependencyDescriptor descriptor)
    {
        Function<Field, Object> get = field ->
        {
            try
            {
                return field.get(descriptor);
            }
            catch (IllegalAccessException illegalAccess)
            {
                throw new IllegalAccessError(illegalAccess.getMessage());
            }
        };
        Stream<Field> fields = Stream.of(containingClassField, declaringClassField);
        return fields.map(get).filter(Class.class::isInstance).map(Class.class::cast).findFirst().get();
    }
}

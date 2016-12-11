//                                                                          //
// Copyright 2016 Mirko Raner                                               //
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.DependencyAndSource;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.util.Modules;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
* The class {@link GuiceLogInjectionService} implements a {@link LogInjectionService} for the
* {@link com.google.inject.Guice Guice} dependency injection framework. It binds a
* {@link javax.inject.Provider Provider} for the logger class and a {@link ProvisionListener} for obtaining the
* injection context.
*
* @param <_Logger_> the logger type
*
* @author Mirko Raner
**/
public class GuiceLogInjectionService<_Logger_> implements LogInjectionService<Module, _Logger_>
{
    @Override
    public Module getBindings(LogInject<_Logger_> logInject)
    {
        Stream<Class<_Logger_>> bindings = getBindingClasses(logInject);
        return Modules.combine(bindings.map(binding -> getBindings(logInject, binding)).toArray(Module[]::new));
    }

    private Module getBindings(LogInject<_Logger_> logInject, Class<_Logger_> loggerClass)
    {
        TypeLiteral<_Logger_> loggerType = TypeLiteral.get(loggerClass);
        GuiceLoggerProvider<_Logger_> provider = new GuiceLoggerProvider<>();
        Predicate<Dependency<?>> matchesLogger = dependency -> loggerType.equals(dependency.getKey().getTypeLiteral());
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                ProvisionListener provisionListener = new ProvisionListener()
                {
                    @Override
                    public <_Target_> void onProvision(ProvisionInvocation<_Target_> provision)
                    {
                        Binding<_Target_> binding = provision.getBinding();
                        if (loggerType.equals(binding.getKey().getTypeLiteral()))
                        {
                            Dependency<?> loggerDependency;
                            Stream<Dependency<?>> stream;
                            stream = provision.getDependencyChain().stream().map(DependencyAndSource::getDependency);
                            Iterator<Dependency<?>> dependencies = reverse(stream.collect(toList())).iterator();
                            if (dependencies.hasNext() && matchesLogger.test(loggerDependency = dependencies.next()))
                            {
                                InjectionPoint injectionPoint = loggerDependency.getInjectionPoint();
                                TypeLiteral<?> declaringType = injectionPoint.getDeclaringType();
                                TypeLiteral<?> targetType = null;
                                if (dependencies.hasNext())
                                {
                                    TypeLiteral<?> typeLiteral = dependencies.next().getKey().getTypeLiteral();
                                    if (declaringType.getRawType().isAssignableFrom(typeLiteral.getRawType()))
                                    {
                                        targetType = typeLiteral;
                                    }
                                }
                                Class<?> logger = (targetType != null? targetType:declaringType).getRawType();
                                BindingTargetVisitor<_Target_, Void> bindingTargetVisitor;
                                bindingTargetVisitor = new DefaultBindingTargetVisitor<_Target_, Void>()
                                {
                                    @Override
                                    public Void visit(ProviderInstanceBinding<? extends _Target_> instanceBinding)
                                    {
                                        if (provider.equals(instanceBinding.getUserSuppliedProvider()))
                                        {
                                            provider.setLogger(logInject.createLogger(logger));
                                        }
                                        return null;
                                    }
                                };
                                binding.acceptTargetVisitor(bindingTargetVisitor);
                            }
                        }
                    }
                };
                bind(loggerClass).toProvider(provider);
                bindListener(Matchers.any(), provisionListener);
            }
        };
    }

    private Stream<Class<_Logger_>> getBindingClasses(LogInject<_Logger_> logInject)
    {
        Class<_Logger_> loggerClass = logInject.getLoggerClass();
        switch (logInject.getLoggerClassType())
        {
            case INTERFACE: return Stream.of(loggerClass);
            case IMPLEMENTATION: return getAllBindingTypes(loggerClass);
            default: throw new IllegalArgumentException(String.valueOf(logInject.getLoggerClassType()));
        }
    }

    private void getAllSuperclassesAndInterfaces(Class<?> type, Set<Class<?>> collected)
    {
        if (type != null)
        {
            collected.add(type);
            Class<?> superclass = type.getSuperclass();
            getAllSuperclassesAndInterfaces(superclass, collected);
            for (Class<?> implementedInterface: type.getInterfaces())
            {
                getAllSuperclassesAndInterfaces(implementedInterface, collected);
            }
        }
    }

    private boolean noJDKClasses(Class<?> type)
    {
        String name = type.getName();
        return !name.startsWith("java.") || name.startsWith("java.util.logging.");
    }

    <_Type_> List<_Type_> reverse(List<_Type_> list)
    {
        Collections.reverse(list);
        return list;
    }

    Stream<Class<_Logger_>> getAllBindingTypes(Class<?> type)
    {
        return getAllPotentialBindingTypes(type).stream().filter(this::noJDKClasses);
    }

    Set<Class<_Logger_>> getAllPotentialBindingTypes(Class<?> type)
    {
        Class<Class<_Logger_>> loggerClass = new LogLiteral<Class<_Logger_>>(Class.class).getLiteral();
        Set<Class<?>> bindings = new HashSet<>();
        getAllSuperclassesAndInterfaces(type, bindings);
        return bindings.stream().map(loggerClass::cast).collect(toSet());
    }
}

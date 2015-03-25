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

import java.util.Optional;
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

public class GuiceLogInjectionService<_Logger_> implements LogInjectionService<Module, _Logger_>
{
    @Override
    public Class<Module> getBindingType()
    {
        return Module.class;
    }

    @Override
    public Module getBindings(LogInject<_Logger_> logInject)
    {
        Class<_Logger_> loggerClass = logInject.getLoggerClass();
        TypeLiteral<_Logger_> loggerType = TypeLiteral.get(loggerClass);
        Predicate<Dependency<?>> matchesLogger = dependency -> loggerType.equals(dependency.getKey().getTypeLiteral());
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                GuiceLoggerProvider<_Logger_> provider = new GuiceLoggerProvider<_Logger_>();
                ProvisionListener provisionListener = new ProvisionListener()
                {
                    @Override
                    public <_Target_> void onProvision(ProvisionInvocation<_Target_> provision)
                    {
                        Binding<_Target_> binding = provision.getBinding();
                        if (loggerType.equals(binding.getKey().getTypeLiteral()))
                        {
                            Stream<DependencyAndSource> stream = provision.getDependencyChain().stream();
                            Stream<Dependency<?>> dependencies = stream.map(DependencyAndSource::getDependency);
                            Optional<Dependency<?>> loggerDependency = dependencies.filter(matchesLogger).findFirst();
                            if (loggerDependency.isPresent())
                            {
                                InjectionPoint injectionPoint = loggerDependency.get().getInjectionPoint();
                                TypeLiteral<?> declaringType = injectionPoint.getDeclaringType();
                                BindingTargetVisitor<_Target_, Void> bindingTargetVisitor;
                                bindingTargetVisitor = new DefaultBindingTargetVisitor<_Target_, Void>()
                                {
                                    @Override
                                    public Void visit(ProviderInstanceBinding<? extends _Target_> binding)
                                    {
                                        if (provider.equals(binding.getUserSuppliedProvider()))
                                        {
                                            provider.setLogger(logInject.createLogger(declaringType.getRawType()));
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
}

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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Predicate;
import java.util.stream.Stream;
import static java.util.stream.Stream.of;

/**
* The {@link LogInjectionService} interface is loginject's main service provider interface (SPI). To use loginject with
* a particular dependency injection framework a corresponding implementation of {@link LogInjectionService} is
* required. Service implementations are loaded and instantiated via the {@link java.util.ServiceLoader}.
*
* @param <_Bindings_> the type of binding to be generated (e.g., an HK2 {@code Binder} or a Guice {@code Module})
* @param <_Logger_> the type of logger (implementations of {@link LogInjectionService} will keep this parameter unbound)
*
* @author Mirko Raner
**/
interface LogInjectionService<_Bindings_, _Logger_>
{
    /**
    * Returns the framework-specific bindings that bind a logger a generic fashion (i.e., so that the appropriate logger
    * instance is injected in each individual class).
    *
    * @param loginject the {@link LogInject} object that describes the generic logger binding
    * @return the specific binding object for the particular dependency injection framework
    **/
    _Bindings_ getBindings(LogInject<_Logger_> loginject);

    /**
    * Determines if this {@link LogInjectionService} implementation supports a particular type of binding. This method
    * is used by {@link LogInject} to find the appropriate service implementation in the list of implementations
    * returned by the {@link java.util.ServiceLoader}. Typically, implementors will not need to override the default
    * implementation, because an implementation that implements {@code LogInjectionService<X, _Logger_>} will typically
    * only support class {@code X}. Also, most implementors will not need the {@link LogInject} object, as they
    * typically support a binding type independent of the particular logger binding (e.g., the
    * {@code HK2LogInjectionService} will support creating a {@code Binder}, independent of whether the binding is for
    * a Log4J logger or a {@link java.util.logging.Logger}). However, certain implementations (notably the one for
    * Dagger) may rely on information from the {@link LogInject} object to determine whether they support a binding
    * type.
    *
    * @param loginject the {@link LogInject} object that describes the generic logger binding
    * @param bindingType the target binding type for a specific DI framework (e.g., a {@code Binder} for HK2)
    * @return {@code true} if the binding type is support by this service implementation, {@code false} otherwise
    **/
    default boolean supports(LogInject<_Logger_> loginject, Class<?> bindingType)
    {
        Type[] genericInterfaces = getClass().getGenericInterfaces();
        Class<ParameterizedType> parameter = ParameterizedType.class;
        Stream<ParameterizedType> interfaces = of(genericInterfaces).filter(parameter::isInstance).map(parameter::cast);
        Predicate<ParameterizedType> logInjectionService = type -> type.getRawType().equals(LogInjectionService.class);
        ParameterizedType parameterizedInterface = interfaces.filter(logInjectionService).findFirst().get();
        return parameterizedInterface.getActualTypeArguments()[0].equals(bindingType);
    }
}

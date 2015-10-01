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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceHandle;

/**
* The class {@link HK2LogInjectionResolver} implements an {@link InjectionResolver} for the HK2 dependency injection
* framework. It detects injection requests whose required type is compatible with {@link LogInject#getLoggerClass()}
* and creates an appropriate logger.
* All other requests are passed on to HK2's JSR-330 system resolver.
*
* @author Mirko Raner
**/
@Rank(1)
@Singleton
public class HK2LogInjectionResolver implements InjectionResolver<Inject>
{
    @Inject
    private LogInject<?> logInject;

    @Inject
    @Named(SYSTEM_RESOLVER_NAME)
    private InjectionResolver<Inject> systemResolver;

    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root)
    {
        if ((injectee.getRequiredType() instanceof Class)
        && ((Class<?>)injectee.getRequiredType()).isAssignableFrom(logInject.getLoggerClass()))
        {
            Class<?> injecteeClass = injectee.getInjecteeClass();
            Class<?> implementationClass = injectee.getInjecteeDescriptor().getImplementationClass();
            Class<?> logger = (injecteeClass.isAssignableFrom(implementationClass))? implementationClass:injecteeClass;
            return logInject.createLogger(logger);
        }
        return systemResolver.resolve(injectee, root);
    }

    @Override
    public boolean isConstructorParameterIndicator()
    {
        return false;
    }

    @Override
    public boolean isMethodParameterIndicator()
    {
        return false;
    }
}

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
        if (logInject.getLoggerClass().equals(injectee.getRequiredType()))
        {
            Class<?> injecteeClass = injectee.getInjecteeClass();
            return logInject.createLogger(injecteeClass);
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

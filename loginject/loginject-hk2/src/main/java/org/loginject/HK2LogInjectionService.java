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

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.loginject.LogInjectionService;

public class HK2LogInjectionService<_Logger_> implements LogInjectionService<Binder, _Logger_>
{
    @Override
    public Binder getBindings(LogInject<_Logger_> logInject)
	{
		return new AbstractBinder()
		{
			@Override
			protected void configure()
			{
				bind(logInject).to(LogInject.class);
				addActiveDescriptor(HK2LogInjectionResolver.class);
			}
		};
	}
}

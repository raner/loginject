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

import java.util.function.Function;

public class LogParameter<_Type_>
{
    private final static Class<Class<?>> CLASS = new LogLiteral<Class<?>>(Class.class).getLiteral();
    private final static LogParameter<Class<?>> CURRENT_CLASS = new LogParameter<>(CLASS, Function.identity());
    private final static LogParameter<String> CURRENT_CLASS_NAME = new LogParameter<>(String.class, Class::getName);

    private Class<_Type_> type;
    private Function<Class<?>, _Type_> function;

    private LogParameter(Class<_Type_> type, Function<Class<?>, _Type_> function)
    {
        this.type = type;
        this.function = function;
    }

    public static LogParameter<Class<?>> currentClass()
    {
        return CURRENT_CLASS;
    }

    public static LogParameter<String> currentClassName()
    {
        return CURRENT_CLASS_NAME;
    }

    public static LogParameter<String> constantString(String string)
    {
        return new LogParameter<>(String.class, always -> string);
    }

    public Class<_Type_> getParameterType()
    {
        return type;
    }

    public _Type_ getValue(Class<?> currentClass)
    {
        return function.apply(currentClass);
    }
}

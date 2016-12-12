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

/**
* A {@link LogParameter} encapsulates a parameter that is passed to a logger factory method at the of injection.
* It does not necessarily represent a constant value but may vary according to the injection point. For example,
* {@link #currentClass()} and {@link #currentClassName()} refer to the {@link Class} object and class name at the
* injection point, not to class that creates the {@link LogParameter}.
*
* @param <_Type_> the parameter type (must match the parameter type in the factory method)
*
* @author Mirko Raner
**/
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

    /**
    * @return a {@link LogParameter} representing the {@link Class} into which the logger is injected.
    **/
    public static LogParameter<Class<?>> currentClass()
    {
        return CURRENT_CLASS;
    }

    /**
    * @return a {@link LogParameter} representing the name of the {@link Class} into which the logger is injected.
    **/
    public static LogParameter<String> currentClassName()
    {
        return CURRENT_CLASS_NAME;
    }

    /**
    * Returns a {@link LogParameter} representing a constant string (always the same string, independent of injection
    * point).
    *
    * @param string the constant string
    * @return the {@link LogParameter}
    **/
    public static LogParameter<String> constantString(String string)
    {
        return new LogParameter<>(String.class, always -> string);
    }

    /**
    * Returns a {@link LogParameter} representing a constant value (always the same value, independent of injection
    * point).
    *
    * @param <_Parameter_> the log parameter type
    * @param parameter the value
    * @return the {@link LogParameter}
    **/
    public static <_Parameter_> LogParameter<_Parameter_> parameter(_Parameter_ parameter)
    {
        @SuppressWarnings("unchecked")
        Class<_Parameter_> parameterType = (Class<_Parameter_>)parameter.getClass();
        return new LogParameter<>(parameterType, always -> parameter);
    }

    /**
    * @return the parameter's type
    **/
    public Class<_Type_> getParameterType()
    {
        return type;
    }

    /**
    * Determines the parameter's value for the current injection point.
    *
    * @param currentClass the class into which the logger is injected
    * @return the parameter value
    **/
    public _Type_ getValue(Class<?> currentClass)
    {
        return function.apply(currentClass);
    }
}

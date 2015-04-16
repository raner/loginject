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

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.junit.Test;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

public class GuiceLogInjectionServiceTest
{
    private GuiceLogInjectionService<?> service = new GuiceLogInjectionService<>();

    @Test
    public void testPotentialBindingsForObject()
    {
        assertEquals(set(Object.class), service.getAllPotentialBindingTypes(Object.class));
    }

    @Test
    public void testPotentialBindingsForHashSet()
    {
        assertEquals(set(Object.class, HashSet.class, AbstractSet.class, Set.class, Cloneable.class, Serializable.class,
            AbstractCollection.class, Collection.class, Iterable.class),
            service.getAllPotentialBindingTypes(HashSet.class));
    }

    @Test
    public void testBindingsForLogger()
    {
        assertEquals(set(Logger.class), service.getAllBindingTypes(Logger.class).collect(toSet()));
    }

    private Set<Class<?>> set(Class<?>... classes)
    {
        return new HashSet<>(Arrays.asList(classes));
    }
}

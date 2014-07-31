/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.builder.AstAssert

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
@CompileStatic
class SimpleMacroTest extends GroovyTestCase {
    
    public void testMatch() {

        assertScript '''
            class SomeJob {
                SomeJob(Map flags, String key) {
                    flags[key] = true
                }
            }

            Map<String, Boolean> flags = [:];
            
            assert match(123, 
                (String)  : new SomeJob(flags, "foo"),
                (Integer) : new SomeJob(flags, "bar")
            ) instanceof SomeJob
            
            assert !flags["foo"]
            assert flags["bar"]
'''
    }
}

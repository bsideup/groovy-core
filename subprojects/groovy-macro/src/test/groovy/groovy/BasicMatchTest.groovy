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

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
class BasicMatchTest extends GroovyTestCase {

    public void testMatchFact() {
        assertScript '''
        def fact(num) {
            return match(num) {
                String >> fact(num.toInteger())
                (0 | 1) >> 1
                2 >> 2
                _ >> _ * fact(_ - 1)
            }
        }
        
        assert fact("5") == 120
'''
    }

    public void testMatchVariable() {
        assertScript '''
        def matcher(num) {
            return match(num) {
                (1 | 2 | 3) >> _ * _
                _ >> _
            }
        }
         
        assert matcher(1) == 1
        assert matcher(2) == 4
        assert matcher(3) == 9
        assert matcher(10) == 10
'''
    }

    public void testCascadeMatch() {
        assertScript '''
        def matcher(num) {
            return match(num) {
                (1 | 2 | 3) >> _ * _
                _ >> match(_) {
                        4 >> 10
                        _ >> _
                    }
            }
        }
         
        assert matcher(1) == 1
        assert matcher(2) == 4
        assert matcher(3) == 9
        assert matcher(4) == 10
        assert matcher(10) == 10
'''
    }

    public void testMultiMatch() {
        assertScript '''
        def matcher(int num) {
            return match(num) {
                (0 | 1 | 2 | 3) >> 1
                (4 | 5) >> 2
                _ >> 3
            }
        }
        
        assert matcher(0) == 1
        assert matcher(1) == 1
        assert matcher(2) == 1
        assert matcher(3) == 1
        
        assert matcher(4) == 2
        assert matcher(5) == 2
        
        assert matcher(6) == 3
        assert matcher(100500) == 3
        assert matcher(-1) == 3
'''
    }

    public void testClassMatch() {
        assertScript '''
        def matcher(Object it) {
            return match(it) {
                (String | Integer) >> "string or integer"
                Date >> "date"
                _ >> "unknown type"
            }
        }
         
        assert matcher("") == "string or integer"
        assert matcher(1) == "string or integer"
        assert matcher(String) == "string or integer"
        assert matcher(new Date()) == "date"
        assert matcher(1L) == "unknown type"
'''
    }
}

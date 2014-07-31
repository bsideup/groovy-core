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
package org.codehaus.groovy.macro.runtime;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.ASTTest;
import groovy.transform.Memoized;
import groovy.transform.TypeChecked;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
public class MacroGroovyMethods {

    public static class MacroValuePlaceholder {
        public static Object $v(Closure cl) {
            // replaced with AST transformations
            return null; 
        }
    }

    public static <T> T macro(Object self, @DelegatesTo(MacroValuePlaceholder.class) Closure cl) {
        return null;
    }

    public static <T> T macro(Object self, boolean asIs, @DelegatesTo(MacroValuePlaceholder.class) Closure cl) {
        return null;
    }

    public static <T> T macro(Object self, CompilePhase compilePhase, @DelegatesTo(MacroValuePlaceholder.class) Closure cl) {
        return null;
    }

    public static <T> T macro(Object self, CompilePhase compilePhase, boolean asIs, @DelegatesTo(MacroValuePlaceholder.class) Closure cl) {
        return null;
    }

    @Macro
    public static Expression match(Object self, MapExpression mapExpression, Expression it) {
        return generateMatcher(it, mapExpression.getMapEntryExpressions().iterator());
    }
    
    private static Expression generateMatcher(Expression it, Iterator<MapEntryExpression> iterator) {
        if(iterator.hasNext()) {
            MapEntryExpression mapEntryExpression = iterator.next();
            return ternaryX(
                    isInstanceOfX(it, ((ClassExpression) mapEntryExpression.getKeyExpression()).getType()),
                    mapEntryExpression.getValueExpression(),
                    generateMatcher(it, iterator)
            );
        } else {
            return constX(null);
        }
    }
}

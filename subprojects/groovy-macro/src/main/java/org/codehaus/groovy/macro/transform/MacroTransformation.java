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
package org.codehaus.groovy.macro.transform;

import groovy.lang.GroovyShell;
import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.macro.runtime.Macro;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MacroTransformation extends MethodCallTransformation implements CompilationUnitAware {

    public static final String DOLLAR_VALUE = "$v";
    public static final String MACRO_METHOD = "macro";
    
    CompilationUnit unit;

    @Override
    protected GroovyCodeVisitor getTransformer(ASTNode[] nodes, final SourceUnit sourceUnit) {
        return new ClassCodeExpressionTransformer() {
            @Override
            protected SourceUnit getSourceUnit() {
                return sourceUnit;
            }

            @Override
            public Expression transform(Expression exp) {
                if(!(exp instanceof MethodCallExpression)) {
                    return super.transform(exp);
                }

                MethodCallExpression call = (MethodCallExpression) exp;

                super.transform(call.getObjectExpression());
                super.transform(call.getArguments());

                List<ClassNode> argumentsList = new ArrayList<ClassNode>();

                for (Expression argument : ((TupleExpression) call.getArguments()).getExpressions()) {
                    argumentsList.add(ClassHelper.make(argument.getClass()));
                }
                
                List<MethodNode> dgmMethods = StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments(unit.getTransformLoader(), ClassHelper.OBJECT_TYPE, call.getMethodAsString(), argumentsList.toArray(new ClassNode[argumentsList.size()]));

                if(dgmMethods.size() == 0) {
                    return super.transform(exp);
                }

                ExtensionMethodNode extensionMethodNode = (ExtensionMethodNode) dgmMethods.get(0);
                
                if(extensionMethodNode.getExtensionMethodNode().getAnnotations(ClassHelper.make(Macro.class)).isEmpty()) {
                    return super.transform(exp);
                }

                GroovyShell shell = new GroovyShell(unit.getTransformLoader());
                
                List<Expression> macroArguments = new ArrayList<Expression>();
                macroArguments.add(null);
                macroArguments.addAll(((TupleExpression) call.getArguments()).getExpressions());
                
                return (Expression) InvokerHelper.invokeMethod(shell.evaluate(extensionMethodNode.getExtensionMethodNode().getDeclaringClass().getName()), call.getMethodAsString(), macroArguments.toArray());
            }
        };
    }

    @Override
    public void setCompilationUnit(CompilationUnit unit) {
        this.unit = unit;
    }
}


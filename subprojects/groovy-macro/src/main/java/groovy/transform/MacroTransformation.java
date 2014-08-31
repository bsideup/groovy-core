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
package groovy.transform;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO validation
 * TODO multiple phases support
 * 
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MacroTransformation extends MethodCallTransformation implements CompilationUnitAware {
    
    public static final ClassNode macroAnnotationClassNode = ClassHelper.make(Macro.class);

    CompilationUnit unit;

    @Override
    public void setCompilationUnit(CompilationUnit unit) {
        this.unit = unit;
    }

    @Override
    protected GroovyCodeVisitor getTransformer(ASTNode[] nodes, final SourceUnit sourceUnit) {
        return new MacroCallsTransformer(sourceUnit);
    }
    
    protected class MacroCallsTransformer extends ClassCodeExpressionTransformer {
        
        private SourceUnit sourceUnit;
        
        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        public MacroCallsTransformer(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        public Expression transform(Expression exp) {
            if(!(exp instanceof MethodCallExpression)) {
                return super.transform(exp);
            }
            
            MethodCallExpression call = (MethodCallExpression) exp;

            super.visitMethodCallExpression(call);

            List<ClassNode> argumentsList = new ArrayList<ClassNode>();
            argumentsList.add(ClassHelper.make(SourceUnit.class));

            for (Expression argument : ((TupleExpression) call.getArguments()).getExpressions()) {
                argumentsList.add(ClassHelper.make(argument.getClass()));
            }

            List<MethodNode> dgmMethods = StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments(
                    unit.getTransformLoader(), ClassHelper.OBJECT_TYPE, call.getMethodAsString(),
                    argumentsList.toArray(new ClassNode[argumentsList.size()]));

            if(dgmMethods.size() != 1) {
                return super.transform(exp);
            }

            ExtensionMethodNode extensionMethodNode = (ExtensionMethodNode) dgmMethods.get(0);

            if(extensionMethodNode.getExtensionMethodNode().getAnnotations(macroAnnotationClassNode).isEmpty()) {
                return super.transform(exp);
            }

            GroovyShell shell = new GroovyShell(unit.getTransformLoader());

            List<Object> macroArguments = new ArrayList<Object>();
            macroArguments.add(null);
            macroArguments.add(sourceUnit);
            macroArguments.addAll(((TupleExpression) call.getArguments()).getExpressions());

            Object clazz = shell.evaluate(extensionMethodNode.getExtensionMethodNode().getDeclaringClass().getName());
            return (Expression) InvokerHelper.invokeMethod(clazz, call.getMethodAsString(), macroArguments.toArray());
        }
    }
}


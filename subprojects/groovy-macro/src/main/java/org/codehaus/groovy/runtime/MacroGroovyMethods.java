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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.transform.Macro;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.Arrays;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

/**
 * @author Sergei Egorov <bsideup@gmail.com>
 */
public class MacroGroovyMethods {

    public static final String MATCH_PARAMETER_NAME = "_";

    // Only for auto-complete, can be removed
    public static <T> T match(Object self, Object it, Closure cl) {
        throw new GroovyRuntimeException("not available at runtime");
    }
    
    public static <T> T match(Object self, Closure cl) {
        throw new GroovyRuntimeException("not available at runtime");
    }

    // For obj.match {} syntax
    @Macro
    public static Expression match(Expression self, SourceUnit sourceUnit, ClosureExpression cl) {
        return match(self, sourceUnit, self, cl);
    }

    @Macro
    public static Expression match(Expression self, SourceUnit sourceUnit, Expression it, ClosureExpression cl) {
        List<Statement> statements;

        Statement originalCode = cl.getCode();
        if(originalCode instanceof BlockStatement) {
            statements = ((BlockStatement) originalCode).getStatements();
        } else {
            statements = Arrays.asList(originalCode);
        }

        BlockStatement resultBlock = block();
        for (Statement statement : statements) {
            if(!(statement instanceof ExpressionStatement)) {
                SyntaxException syntaxException = new SyntaxException("only ExpressionStatement is allowed as case",
                        statement.getLineNumber(),
                        statement.getColumnNumber(),
                        statement.getLastLineNumber(),
                        statement.getLastColumnNumber());
                sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(syntaxException, sourceUnit));
                continue;
            }
            
            Expression caseExpression = ((ExpressionStatement) statement).getExpression();
            
            if(!(caseExpression instanceof BinaryExpression)) {
                SyntaxException syntaxException = new SyntaxException("case should be BinaryExpression",
                        caseExpression.getLineNumber(),
                        caseExpression.getColumnNumber(),
                        caseExpression.getLastLineNumber(),
                        caseExpression.getLastColumnNumber());
                sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(syntaxException, sourceUnit));
                continue;
            }
            
            BinaryExpression binaryExpression = (BinaryExpression) caseExpression;

            Token operation = binaryExpression.getOperation();
            if (!operation.isA(Types.RIGHT_SHIFT)) {
                SyntaxException syntaxException = new SyntaxException("case expressions should be divided by >>",
                        operation.getStartLine(),
                        operation.getStartColumn());
                sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(syntaxException, sourceUnit));
                continue;
            }

            resultBlock.addStatement(
                    ifS(
                            getCaseExpression(binaryExpression.getLeftExpression()),
                            returnS(binaryExpression.getRightExpression())
                    )
            );
        }

        ClosureExpression closureExpression = closureX(
                params(param(ClassHelper.OBJECT_TYPE, MATCH_PARAMETER_NAME)),
                resultBlock
        );

        closureExpression.setVariableScope(cl.getVariableScope());

        return callX(closureExpression, "call", it);
    }

    public static Expression getCaseExpression(Expression leftExpression) {
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression binaryCaseExpression = (BinaryExpression) leftExpression;

            if (binaryCaseExpression.getOperation().isA(Types.BITWISE_OR)) {
                return orX(
                        getCaseExpression(binaryCaseExpression.getLeftExpression()),
                        getCaseExpression(binaryCaseExpression.getRightExpression())
                );
            }
        }

        VariableExpression parameterExpression = varX(MATCH_PARAMETER_NAME);
        BinaryExpression eqCheck = eqX(parameterExpression, leftExpression);

        if (leftExpression instanceof ClassExpression) {
            return andX(
                    notNullX(parameterExpression),
                    orX(
                            eqCheck,
                            eqX(callX(parameterExpression, "getClass"), leftExpression)
                    )
            );
        }

        if(leftExpression instanceof RangeExpression) {
            return callX(leftExpression, "contains", parameterExpression);
        }
        
        if(leftExpression instanceof ClosureExpression) {
            return callX(leftExpression, "call", parameterExpression);
        }
        
        return eqCheck;
    }
}

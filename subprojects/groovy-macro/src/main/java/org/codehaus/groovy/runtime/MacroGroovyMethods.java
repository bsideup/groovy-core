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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Types;

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

    @Macro
    public static Expression match(Object self, SourceUnit sourceUnit, Expression it, ClosureExpression cl) {
        BlockStatement blockStatement = (BlockStatement) cl.getCode();

        BlockStatement resultBlock = block();
        for (Statement statement : blockStatement.getStatements()) {
            BinaryExpression binaryExpression = (BinaryExpression) ((ExpressionStatement) statement).getExpression();

            if (!binaryExpression.getOperation().isA(Types.RIGHT_SHIFT)) {
                SyntaxException syntaxException = new SyntaxException("match expressions should be divided by >>",
                        binaryExpression.getOperation().getStartLine(),
                        binaryExpression.getOperation().getStartColumn());
                sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(syntaxException, sourceUnit));
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

        return eqCheck;
    }
}

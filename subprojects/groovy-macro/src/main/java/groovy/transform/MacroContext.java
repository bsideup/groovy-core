package groovy.transform;

import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

public class MacroContext {
    
    private final MethodCallExpression call;
    
    private final SourceUnit sourceUnit;
    
    private final CompilationUnit compilationUnit;

    public MacroContext(CompilationUnit compilationUnit, SourceUnit sourceUnit, MethodCallExpression call) {
        this.compilationUnit = compilationUnit;
        this.sourceUnit = sourceUnit;
        this.call = call;
    }

    public MethodCallExpression getCall() {
        return call;
    }

    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
}

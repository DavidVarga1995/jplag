package jplag.java19;


import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavacAdapter {
    private static final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private static final Logger LOGGER = Logger.getLogger(JavacAdapter.class.getName());

    public int parseFiles(File dir, File[] pathedFiles, final Parser parser) {
        final StandardJavaFileManager jfm = javac.getStandardFileManager(null, null, null);
        DiagnosticCollector<? super JavaFileObject> diagListen = new DiagnosticCollector<>();
        final JavaCompiler.CompilationTask task = javac.getTask(null, jfm, diagListen, null, null,
                jfm.getJavaFileObjects(pathedFiles));
        Iterable<? extends CompilationUnitTree> asts = null;
        try {
            asts = ((JavacTask) task).parse();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception occur", e);
        }
        final Trees trees = Trees.instance(task);
        final SourcePositions positions = trees.getSourcePositions();
        CompilationUnitTree last;
        assert asts != null;
        for (final CompilationUnitTree ast : asts) {
            final String filename;
            if (dir == null)
                filename = ast.getSourceFile().getName();
            else {
                filename = Paths.get(dir.toURI()).relativize(Paths.get(ast.getSourceFile().toUri())).toString();
            }
            final LineMap map = ast.getLineMap();
            last = ast;
            ast.accept(new TreeScanner<>() {
                @Override
                public Object visitBlock(BlockTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_INIT_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 1);
                    Object ret = super.visitBlock(node, p);
                    parser.add(JavaTokenConstants.J_INIT_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitClass(ClassTree node, Object p) {
                    boolean enu = false;
                    boolean interf = false;
                    boolean anno = false;
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);

                    if (node.getKind() == Tree.Kind.ENUM) enu = true;
                    if (node.getKind() == Tree.Kind.INTERFACE) interf = true;
                    if (node.getKind() == Tree.Kind.ANNOTATION_TYPE) anno = true;
                    if (enu)
                        parser.add(JavaTokenConstants.J_ENUM_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 4);
                    if (interf)
                        parser.add(JavaTokenConstants.J_INTERFACE_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 9);
                    if (anno)
                        parser.add(JavaTokenConstants.J_ANNO_T_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 10);
                    if (!enu && !interf && !anno)
                        parser.add(JavaTokenConstants.J_CLASS_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 5);
                    Object ret = super.visitClass(node, p);
                    if (!enu && !interf && !anno)
                        parser.add(JavaTokenConstants.J_CLASS_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    if (anno)
                        parser.add(JavaTokenConstants.J_ANNO_T_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    if (enu)
                        parser.add(JavaTokenConstants.J_ENUM_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    if (interf)
                        parser.add(JavaTokenConstants.J_INTERFACE_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitImport(ImportTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_IMPORT, filename, map.getLineNumber(n), map.getColumnNumber(n), 6);
                    return super.visitImport(node, p);
                }

                @Override
                public Object visitPackage(PackageTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_PACKAGE, filename, map.getLineNumber(n), map.getColumnNumber(n), 7);
                    return super.visitPackage(node, p);
                }

                @Override
                public Object visitMethod(MethodTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_METHOD_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), node.getName().length());
                    Object ret = super.visitMethod(node, p);
                    parser.add(JavaTokenConstants.J_METHOD_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitSynchronized(SynchronizedTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_SYNC_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 12);
                    Object ret = super.visitSynchronized(node, p);
                    parser.add(JavaTokenConstants.J_SYNC_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitDoWhileLoop(DoWhileLoopTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_DO_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 2);
                    Object ret = super.visitDoWhileLoop(node, p);
                    parser.add(JavaTokenConstants.J_DO_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitWhileLoop(WhileLoopTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_WHILE_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 5);
                    Object ret = super.visitWhileLoop(node, p);
                    parser.add(JavaTokenConstants.J_WHILE_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitForLoop(ForLoopTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_FOR_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 3);
                    Object ret = super.visitForLoop(node, p);
                    parser.add(JavaTokenConstants.J_FOR_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitEnhancedForLoop(EnhancedForLoopTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_FOR_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 3);
                    Object ret = super.visitEnhancedForLoop(node, p);
                    parser.add(JavaTokenConstants.J_FOR_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitSwitch(SwitchTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_SWITCH_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 6);
                    Object ret = super.visitSwitch(node, p);
                    parser.add(JavaTokenConstants.J_SWITCH_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitCase(CaseTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_CASE, filename, map.getLineNumber(n), map.getColumnNumber(n), 4);
                    return super.visitCase(node, p);
                }

                @Override
                public Object visitTry(TryTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    if (node.getResources().isEmpty())
                        parser.add(JavaTokenConstants.J_TRY_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 3);
                    else
                        parser.add(JavaTokenConstants.J_TRY_WITH_RESOURCE, filename, map.getLineNumber(n), map.getColumnNumber(n), 3);
                    if (node.getFinallyBlock() != null)
                        parser.add(JavaTokenConstants.J_FINALLY, filename, map.getLineNumber(n), map.getColumnNumber(n), 3);
                    return super.visitTry(node, p);
                }

                @Override
                public Object visitCatch(CatchTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_CATCH_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 5);
                    Object ret = super.visitCatch(node, p);
                    parser.add(JavaTokenConstants.J_CATCH_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitIf(IfTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_IF_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 2);
                    node.getCondition().accept(this, p);
                    node.getThenStatement().accept(this, p);
                    if (node.getElseStatement() != null) {
                        n = positions.getStartPosition(ast, node.getElseStatement());
                        parser.add(JavaTokenConstants.J_ELSE, filename, map.getLineNumber(n), map.getColumnNumber(n), 4);
                        node.getElseStatement().accept(this, p);
                    }
                    parser.add(JavaTokenConstants.J_IF_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return null;
                }

                @Override
                public Object visitBreak(BreakTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_BREAK, filename, map.getLineNumber(n), map.getColumnNumber(n), 5);
                    return super.visitBreak(node, p);
                }

                @Override
                public Object visitContinue(ContinueTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_CONTINUE, filename, map.getLineNumber(n), map.getColumnNumber(n), 8);
                    return super.visitContinue(node, p);
                }

                @Override
                public Object visitReturn(ReturnTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_RETURN, filename, map.getLineNumber(n), map.getColumnNumber(n), 6);
                    return super.visitReturn(node, p);
                }

                @Override
                public Object visitThrow(ThrowTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_THROW, filename, map.getLineNumber(n), map.getColumnNumber(n), 5);
                    return super.visitThrow(node, p);
                }

                @Override
                public Object visitNewClass(NewClassTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    if (!node.getTypeArguments().isEmpty()) {
                        parser.add(JavaTokenConstants.J_GENERIC, filename, map.getLineNumber(n), map.getColumnNumber(n),
                                (long) 3 + node.getIdentifier().toString().length());
                    }
                    parser.add(JavaTokenConstants.J_NEWCLASS, filename, map.getLineNumber(n), map.getColumnNumber(n), 3);
                    return super.visitNewClass(node, p);
                }

                @Override
                public Object visitTypeParameter(TypeParameterTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    // This is odd, but also done like this in Java17
                    parser.add(JavaTokenConstants.J_GENERIC, filename, map.getLineNumber(n), map.getColumnNumber(n), 1);
                    return super.visitTypeParameter(node, p);
                }

                @Override
                public Object visitNewArray(NewArrayTree node, Object arg1) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_NEWARRAY, filename, map.getLineNumber(n), map.getColumnNumber(n), 3);
                    if (node.getInitializers() != null && !node.getInitializers().isEmpty()) {
                        n = positions.getStartPosition(ast, node.getInitializers().get(0));
                        parser.add(JavaTokenConstants.J_ARRAY_INIT_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 1);
                        parser.add(JavaTokenConstants.J_ARRAY_INIT_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    }
                    return super.visitNewArray(node, arg1);
                }

                @Override
                public Object visitAssignment(AssignmentTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_ASSIGN, filename, map.getLineNumber(n), map.getColumnNumber(n), 1);
                    return super.visitAssignment(node, p);
                }

                @Override
                public Object visitAssert(AssertTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_ASSERT, filename, map.getLineNumber(n), map.getColumnNumber(n), 6);
                    return super.visitAssert(node, p);
                }

                @Override
                public Object visitVariable(VariableTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_VARDEF, filename, map.getLineNumber(n), map.getColumnNumber(n), node.toString().length());
                    return super.visitVariable(node, p);
                }

                @Override
                public Object visitConditionalExpression(ConditionalExpressionTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_COND, filename, map.getLineNumber(n), map.getColumnNumber(n), 1);
                    return super.visitConditionalExpression(node, p);
                }

                @Override
                public Object visitMethodInvocation(MethodInvocationTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_APPLY, filename, map.getLineNumber(n), map.getColumnNumber(n), positions.getEndPosition(ast, node.getMethodSelect()) - n);
                    return super.visitMethodInvocation(node, p);
                }

                @Override
                public Object visitAnnotation(AnnotationTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_ANNO, filename, map.getLineNumber(n), map.getColumnNumber(n), 1);
                    return super.visitAnnotation(node, p);
                }

                @Override
                public Object visitModule(ModuleTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    long m = positions.getEndPosition(ast, node);
                    parser.add(JavaTokenConstants.J_MODULE_BEGIN, filename, map.getLineNumber(n), map.getColumnNumber(n), 6);
                    Object ret = super.visitModule(node, p);
                    parser.add(JavaTokenConstants.J_MODULE_END, filename, map.getLineNumber(m - 1), map.getColumnNumber(m - 1), 1);
                    return ret;
                }

                @Override
                public Object visitRequires(RequiresTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_REQUIRES, filename, map.getLineNumber(n), map.getColumnNumber(n), 8);
                    return super.visitRequires(node, p);
                }

                @Override
                public Object visitProvides(ProvidesTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_PROVIDES, filename, map.getLineNumber(n), map.getColumnNumber(n), 8);
                    return super.visitProvides(node, p);
                }

                @Override
                public Object visitExports(ExportsTree node, Object p) {
                    long n = positions.getStartPosition(ast, node);
                    parser.add(JavaTokenConstants.J_EXPORTS, filename, map.getLineNumber(n), map.getColumnNumber(n), 7);
                    return super.visitExports(node, p);
                }

                @Override
                public Object visitErroneous(ErroneousTree node, Object p) {
                    parser.errorsInc();
                    return super.visitErroneous(node, p);
                }
            }, null);
            positions.getEndPosition(last, last);
            parser.add(JavaTokenConstants.FILE_END, filename, 1, -1, -1);
        }
        int errors = 0;
        for (Diagnostic<?> diagItem : diagListen.getDiagnostics()) {
            if (diagItem.getKind() == javax.tools.Diagnostic.Kind.ERROR) {
                errors++;
            }
            diagItem.getKind();
            diagItem.getKind();
        }
        return errors;
    }

}

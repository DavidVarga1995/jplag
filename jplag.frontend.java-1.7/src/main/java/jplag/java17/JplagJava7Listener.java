package jplag.java17;

import jplag.java17.grammar.*;
import jplag.java17.grammar.Java7Parser.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class JplagJava7Listener implements Java7Listener, JavaTokenConstants {

    private final jplag.java17.Parser jplagParser;

    public JplagJava7Listener(jplag.java17.Parser jplag) {
        jplagParser = jplag;
    }

    @Override
    public void enterEveryRule(ParserRuleContext arg0) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitEveryRule(ParserRuleContext arg0) {
// Do nothing because of X and Y.
    }

    @Override
    public void visitErrorNode(ErrorNode arg0) {
// Do nothing because of X and Y.
    }

    @Override
    public void visitTerminal(TerminalNode arg0) {
        if (arg0.getText().equals("else")) {
            jplagParser.add(J_ELSE, arg0.getSymbol());
        }

    }

    @Override
    public void enterInnerCreator(InnerCreatorContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitInnerCreator(InnerCreatorContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
        jplagParser.add(J_ANNO_T_BEGIN, ctx.getStart());
    }

    @Override
    public void exitAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
        jplagParser.add(J_ANNO_T_END, ctx.getStop());
    }

    @Override
    public void enterVariableDeclarator(VariableDeclaratorContext ctx) {
        jplagParser.add(J_VARDEF, ctx.getStart());
    }

    @Override
    public void exitVariableDeclarator(VariableDeclaratorContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterResources(ResourcesContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitResources(ResourcesContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterExpressionList(ExpressionListContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitExpressionList(ExpressionListContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterQualifiedIdentifier(QualifiedIdentifierContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitQualifiedIdentifier(QualifiedIdentifierContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterTypeDeclaration(TypeDeclarationContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitTypeDeclaration(TypeDeclarationContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterForUpdate(ForUpdateContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitForUpdate(ForUpdateContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterFormalParameterVariables(FormalParameterVariablesContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitFormalParameterVariables(FormalParameterVariablesContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterElementValueArrayInitializer(ElementValueArrayInitializerContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitElementValueArrayInitializer(ElementValueArrayInitializerContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterAnnotation(AnnotationContext ctx) {
        jplagParser.add(J_ANNO, ctx.getStart());
    }

    @Override
    public void exitAnnotation(AnnotationContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterMemberDecl(MemberDeclContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitMemberDecl(MemberDeclContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterEnumConstant(EnumConstantContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitEnumConstant(EnumConstantContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterAnnotationName(AnnotationNameContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitAnnotationName(AnnotationNameContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterEnhancedForControl(EnhancedForControlContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitEnhancedForControl(EnhancedForControlContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterPrimary(PrimaryContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitPrimary(PrimaryContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterNormalClassDeclaration(NormalClassDeclarationContext ctx) {
        jplagParser.add(J_CLASS_BEGIN, ctx.getStart());
    }

    @Override
    public void exitNormalClassDeclaration(NormalClassDeclarationContext ctx) {
        jplagParser.add(J_CLASS_END, ctx.getStop());

    }

    @Override
    public void enterClassBody(ClassBodyContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitClassBody(ClassBodyContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterDefaultValue(DefaultValueContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitDefaultValue(DefaultValueContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterImportDeclaration(ImportDeclarationContext ctx) {
        jplagParser.add(J_IMPORT, ctx.getStart());
    }

    @Override
    public void exitImportDeclaration(ImportDeclarationContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterVariableModifier(VariableModifierContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitVariableModifier(VariableModifierContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterEnumConstantName(EnumConstantNameContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitEnumConstantName(EnumConstantNameContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterCreatedName(CreatedNameContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitCreatedName(CreatedNameContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterQualifiedIdentifierList(QualifiedIdentifierListContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitQualifiedIdentifierList(QualifiedIdentifierListContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterPackageDeclaration(PackageDeclarationContext ctx) {
        if (ctx.annotation().isEmpty()) {
            jplagParser.add(J_PACKAGE, ctx.getStart());
        }
    }

    @Override
    public void exitPackageDeclaration(PackageDeclarationContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterTypeRef(TypeRefContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitTypeRef(TypeRefContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterConstantDeclarator(ConstantDeclaratorContext ctx) {
        jplagParser.add(J_VARDEF, ctx.getStart());
    }

    @Override
    public void exitConstantDeclarator(ConstantDeclaratorContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterConstantDeclaratorRest(ConstantDeclaratorRestContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitConstantDeclaratorRest(ConstantDeclaratorRestContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterElementValuePairs(ElementValuePairsContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitElementValuePairs(ElementValuePairsContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterVariableDeclarators(VariableDeclaratorsContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitVariableDeclarators(VariableDeclaratorsContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterTypeArguments(TypeArgumentsContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitTypeArguments(TypeArgumentsContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterClassCreatorRest(ClassCreatorRestContext ctx) {
        if (ctx.classBody() != null) {
            jplagParser.add(J_IN_CLASS_BEGIN, ctx.getStart());
        }
    }

    @Override
    public void exitClassCreatorRest(ClassCreatorRestContext ctx) {
        if (ctx.classBody() != null) {
            jplagParser.add(J_IN_CLASS_END, ctx.getStop());
        }
    }

    @Override
    public void enterSwitchBlock(SwitchBlockContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitSwitchBlock(SwitchBlockContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterAnnotationMethod(AnnotationMethodContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitAnnotationMethod(AnnotationMethodContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterModifier(ModifierContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitModifier(ModifierContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterCatchClause(CatchClauseContext ctx) {
        jplagParser.add(J_CATCH_BEGIN, ctx.getStart());
    }

    @Override
    public void exitCatchClause(CatchClauseContext ctx) {
        jplagParser.add(J_CATCH_END, ctx.getStop());
    }


    @Override
    public void enterEnumConstants(EnumConstantsContext ctx) {
        jplagParser.add(J_ENUM_CLASS_BEGIN, ctx.getStop());
    }

    @Override
    public void exitEnumConstants(EnumConstantsContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterInterfaceBody(InterfaceBodyContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitInterfaceBody(InterfaceBodyContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterConstantExpression(ConstantExpressionContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void exitConstantExpression(ConstantExpressionContext ctx) {
// Do nothing because of X and Y.
    }

    @Override
    public void enterPackageOrTypeName(PackageOrTypeNameContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitPackageOrTypeName(PackageOrTypeNameContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterForControl(ForControlContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitForControl(ForControlContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterEnumDeclaration(EnumDeclarationContext ctx) {
        jplagParser.add(J_ENUM_BEGIN, ctx.getStart());
    }

    @Override
    public void exitEnumDeclaration(EnumDeclarationContext ctx) {
        jplagParser.add(J_ENUM_END, ctx.getStop());
    }

    @Override
    public void enterLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterTypeList(TypeListContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitTypeList(TypeListContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterTypeParameter(TypeParameterContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitTypeParameter(TypeParameterContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExplicitConstructorInvocation(ExplicitConstructorInvocationContext ctx) {
        jplagParser.add(J_APPLY, ctx.start);
    }

    @Override
    public void exitExplicitConstructorInvocation(ExplicitConstructorInvocationContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterInterfaceMethodDeclaratorRest(InterfaceMethodDeclaratorRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitInterfaceMethodDeclaratorRest(InterfaceMethodDeclaratorRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterElementValue(ElementValueContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitElementValue(ElementValueContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterCompilationUnit(CompilationUnitContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitCompilationUnit(CompilationUnitContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterStatementExpression(StatementExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitStatementExpression(StatementExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterFormalParameterDeclarations(FormalParameterDeclarationsContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitFormalParameterDeclarations(FormalParameterDeclarationsContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterBlock(BlockContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitBlock(BlockContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterVariableInitializer(VariableInitializerContext ctx) {
        if (!(ctx.parent instanceof ArrayInitializerContext)) {
            // dont print assignment, as this is part of an array initialization
            jplagParser.add(J_ASSIGN, ctx.getStart());
        }
    }

    @Override
    public void exitVariableInitializer(VariableInitializerContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterBlockStatement(BlockStatementContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitBlockStatement(BlockStatementContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterIntegerLiteral(IntegerLiteralContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitIntegerLiteral(IntegerLiteralContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterInterfaceMemberDecl(InterfaceMemberDeclContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitInterfaceMemberDecl(InterfaceMemberDeclContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterCreator(CreatorContext ctx) {
        if (ctx.classCreatorRest() != null) {
            if (// @formatter:off
                // "normal" generic
                    ctx.createdName().typeArguments().size() > 0
                            // allow diamond operator
                            || ctx.createdName().children.size() > 1 && (ctx.createdName().getChild(1).getText().equals("<") && ctx.createdName().getChild(2).getText().equals(">"))
                            || ctx.createdName().children.size() > 3 && (ctx.createdName().getChild(3).getText().equals("<") && ctx.createdName().getChild(4).getText().equals(">"))) {
                // @formatter: on
                jplagParser.add(J_GENERIC, ctx.start);
            }
            jplagParser.add(J_NEWCLASS, ctx.start);
        } else if (ctx.arrayCreatorRest() != null) {
            jplagParser.add(J_NEWARRAY, ctx.start);

        }

    }

    @Override
    public void exitCreator(CreatorContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterConstantDeclaratorsRest(ConstantDeclaratorsRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitConstantDeclaratorsRest(ConstantDeclaratorsRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterInterfaceGenericMethodDecl(InterfaceGenericMethodDeclContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitInterfaceGenericMethodDecl(InterfaceGenericMethodDeclContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterInterfaceMethodOrFieldDecl(InterfaceMethodOrFieldDeclContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitInterfaceMethodOrFieldDecl(InterfaceMethodOrFieldDeclContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterTryStatement(TryStatementContext ctx) {
        jplagParser.add(J_TRY_BEGIN, ctx.getStart());
    }

    @Override
    public void exitTryStatement(TryStatementContext ctx) {
        if (hasFinally(ctx)) {
            jplagParser.add(J_FINALLY, ctx.start);
        }
    }

    private boolean hasFinally(TryStatementContext ctx) {
        for (ParseTree pt : ctx.children) {
            if (pt instanceof TerminalNode) {
                if (pt.getText().equals("finally")) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void enterFieldDeclaration(FieldDeclarationContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitFieldDeclaration(FieldDeclarationContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterNormalInterfaceDeclaration(NormalInterfaceDeclarationContext ctx) {
        jplagParser.add(J_INTERFACE_BEGIN, ctx.start);
    }

    @Override
    public void exitNormalInterfaceDeclaration(NormalInterfaceDeclarationContext ctx) {
        jplagParser.add(J_INTERFACE_END, ctx.start);
    }

    @Override
    public void enterExplicitGenericInvocation(ExplicitGenericInvocationContext ctx) {
        jplagParser.add(J_APPLY, ctx.start);
    }

    @Override
    public void exitExplicitGenericInvocation(ExplicitGenericInvocationContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterMethodDeclaration(MethodDeclarationContext ctx) {
        if (ctx.start.getText().equals("void")) {
            jplagParser.add(J_VOID, ctx.start);
        }
        jplagParser.add(J_METHOD_BEGIN, ctx.start);
    }

    @Override
    public void exitMethodDeclaration(MethodDeclarationContext ctx) {
        jplagParser.add(J_METHOD_END, ctx.getStop());
    }

    @Override
    public void enterParExpression(ParExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitParExpression(ParExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterSwitchLabel(SwitchLabelContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitSwitchLabel(SwitchLabelContext ctx) {
        jplagParser.add(J_CASE, ctx.getStop());
    }

    @Override
    public void enterConstructorDeclaration(ConstructorDeclarationContext ctx) {
        jplagParser.add(J_CONSTR_BEGIN, ctx.getStart());
    }

    @Override
    public void exitConstructorDeclaration(ConstructorDeclarationContext ctx) {
        jplagParser.add(J_CONSTR_END, ctx.getStop());
    }

    @Override
    public void enterTypeParameters(TypeParametersContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitTypeParameters(TypeParametersContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterAnnotationTypeElement(AnnotationTypeElementContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitAnnotationTypeElement(AnnotationTypeElementContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterResource(ResourceContext ctx) {
        jplagParser.add(J_TRY_WITH_RESOURCE, ctx.getStart());
    }

    @Override
    public void exitResource(ResourceContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterClassDeclaration(ClassDeclarationContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitClassDeclaration(ClassDeclarationContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterElementValuePair(ElementValuePairContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitElementValuePair(ElementValuePairContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterBooleanLiteral(BooleanLiteralContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitBooleanLiteral(BooleanLiteralContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterVoidInterfaceMethodDeclaratorRest(VoidInterfaceMethodDeclaratorRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitVoidInterfaceMethodDeclaratorRest(VoidInterfaceMethodDeclaratorRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterInterfaceMethodOrFieldRest(InterfaceMethodOrFieldRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitInterfaceMethodOrFieldRest(InterfaceMethodOrFieldRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterTypeName(TypeNameContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitTypeName(TypeNameContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterArguments(ArgumentsContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitArguments(ArgumentsContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterMethodBody(MethodBodyContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitMethodBody(MethodBodyContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterArrayInitializer(ArrayInitializerContext ctx) {
        jplagParser.add(J_ARRAY_INIT_BEGIN, ctx.getStart());
    }

    @Override
    public void exitArrayInitializer(ArrayInitializerContext ctx) {
        jplagParser.add(J_ARRAY_INIT_END, ctx.getStop());
    }

    @Override
    public void enterFormalParameters(FormalParametersContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitFormalParameters(FormalParametersContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterPrimitiveType(PrimitiveTypeContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitPrimitiveType(PrimitiveTypeContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterNonWildcardTypeArguments(NonWildcardTypeArgumentsContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitNonWildcardTypeArguments(NonWildcardTypeArgumentsContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterTypeArgument(TypeArgumentContext ctx) {
        if (!(ctx.parent.parent instanceof CreatedNameContext)) {
            // the generic token has already been emitted by the class emitter
            jplagParser.add(J_GENERIC, ctx.getStart());
        }
    }

    @Override
    public void exitTypeArgument(TypeArgumentContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterClassOrInterfaceDeclaration(ClassOrInterfaceDeclarationContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitClassOrInterfaceDeclaration(ClassOrInterfaceDeclarationContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterForInit(ForInitContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitForInit(ForInitContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterArrayCreatorRest(ArrayCreatorRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitArrayCreatorRest(ArrayCreatorRestContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterBound(BoundContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitBound(BoundContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterSwitchBlockStatementGroup(SwitchBlockStatementGroupContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitSwitchBlockStatementGroup(SwitchBlockStatementGroupContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterLiteral(LiteralContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitLiteral(LiteralContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterDoWhileStmt(DoWhileStmtContext ctx) {
        jplagParser.add(J_DO_BEGIN, ctx.getStart());
    }

    @Override
    public void exitDoWhileStmt(DoWhileStmtContext ctx) {
        jplagParser.add(J_DO_END, ctx.getStop());
    }

    @Override
    public void enterBreak(BreakContext ctx) {
        jplagParser.add(J_BREAK, ctx.getStart());
    }

    @Override
    public void exitBreak(BreakContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterIfStmt(IfStmtContext ctx) {
        jplagParser.add(J_IF_BEGIN, ctx.getStart());
    }

    @Override
    public void exitIfStmt(IfStmtContext ctx) {
        jplagParser.add(J_IF_END, ctx.getStop());
    }

    @Override
    public void enterThrowStmt(ThrowStmtContext ctx) {
        jplagParser.add(J_THROW, ctx.getStart());
    }

    @Override
    public void exitThrowStmt(ThrowStmtContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterSynchronizedStmt(SynchronizedStmtContext ctx) {
        jplagParser.add(J_SYNC_BEGIN, ctx.getStart());
    }

    @Override
    public void exitSynchronizedStmt(SynchronizedStmtContext ctx) {
        jplagParser.add(J_SYNC_END, ctx.getStop());
    }

    @Override
    public void enterStatementExpressStmt(StatementExpressStmtContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void exitStatementExpressStmt(StatementExpressStmtContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterBlockStmt(BlockStmtContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitBlockStmt(BlockStmtContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterReturnStmt(ReturnStmtContext ctx) {
        jplagParser.add(J_RETURN, ctx.getStart());
    }

    @Override
    public void exitReturnStmt(ReturnStmtContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterSemicStmt(SemicStmtContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitSemicStmt(SemicStmtContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterIdentifiedStmt(IdentifiedStmtContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitIdentifiedStmt(IdentifiedStmtContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterTryStmt(TryStmtContext ctx) {
        // nothing happens here see enterTryStatement
    }

    @Override
    public void exitTryStmt(TryStmtContext ctx) {
        // nothing happens here see exitTryStatement
    }

    @Override
    public void enterContinueStmt(ContinueStmtContext ctx) {
        jplagParser.add(J_CONTINUE, ctx.getStart());
    }

    @Override
    public void exitContinueStmt(ContinueStmtContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterAssertStmt(AssertStmtContext ctx) {
        jplagParser.add(J_ASSERT, ctx.getStart());
    }

    @Override
    public void exitAssertStmt(AssertStmtContext ctx) {
        // Do nothing because of X and Y.
    }

    @Override
    public void enterSwitchStmt(SwitchStmtContext ctx) {
        jplagParser.add(J_SWITCH_BEGIN, ctx.getStart());
    }

    @Override
    public void exitSwitchStmt(SwitchStmtContext ctx) {
        jplagParser.add(J_SWITCH_END, ctx.getStop());
    }

    @Override
    public void enterForStmt(ForStmtContext ctx) {
        jplagParser.add(J_FOR_BEGIN, ctx.getStart());
    }

    @Override
    public void exitForStmt(ForStmtContext ctx) {
        jplagParser.add(J_FOR_END, ctx.getStop());
    }

    @Override
    public void enterWhileStmt(WhileStmtContext ctx) {
        jplagParser.add(J_WHILE_BEGIN, ctx.getStart());
    }

    @Override
    public void exitWhileStmt(WhileStmtContext ctx) {
        jplagParser.add(J_WHILE_END, ctx.getStop());
    }

    @Override
    public void enterExprBinaryOperatorInstanceof(ExprBinaryOperatorInstanceofContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryOperatorInstanceof(ExprBinaryOperatorInstanceofContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryOperatorGT(ExprBinaryOperatorGTContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryOperatorGT(ExprBinaryOperatorGTContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprNotExpression(ExprNotExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprNotExpression(ExprNotExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprNewCreator(ExprNewCreatorContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprNewCreator(ExprNewCreatorContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryOperatorAdd(ExprBinaryOperatorAddContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryOperatorAdd(ExprBinaryOperatorAddContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryOperatorMult(ExprBinaryOperatorMultContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryOperatorMult(ExprBinaryOperatorMultContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprPrimary(ExprPrimaryContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprPrimary(ExprPrimaryContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprExplicitGenericInvocation(ExprExplicitGenericInvocationContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprExplicitGenericInvocation(ExprExplicitGenericInvocationContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprSuperIdentifier(ExprSuperIdentifierContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprSuperIdentifier(ExprSuperIdentifierContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprSuper(ExprSuperContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprSuper(ExprSuperContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprThis(ExprThisContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprThis(ExprThisContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryBoolAnd(ExprBinaryBoolAndContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryBoolAnd(ExprBinaryBoolAndContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryAnd(ExprBinaryAndContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryAnd(ExprBinaryAndContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprIncDecExpression(ExprIncDecExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprIncDecExpression(ExprIncDecExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryBoolOr(ExprBinaryBoolOrContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryBoolOr(ExprBinaryBoolOrContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryEquals(ExprBinaryEqualsContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryEquals(ExprBinaryEqualsContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprAssignment(ExprAssignmentContext ctx) {
        jplagParser.add(J_ASSIGN, ctx.getStart());
    }

    @Override
    public void exitExprAssignment(ExprAssignmentContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryNot(ExprBinaryNotContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryNot(ExprBinaryNotContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprNewIdentidier(ExprNewIdentidierContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprNewIdentidier(ExprNewIdentidierContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprMethodExpressionList(ExprMethodExpressionListContext ctx) {
        jplagParser.add(J_APPLY, ctx.getStart());
    }

    @Override
    public void exitExprMethodExpressionList(ExprMethodExpressionListContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprCastExpression(ExprCastExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprCastExpression(ExprCastExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryOperatorComp(ExprBinaryOperatorCompContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryOperatorComp(ExprBinaryOperatorCompContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprBinaryOr(ExprBinaryOrContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprBinaryOr(ExprBinaryOrContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprExpressionIncDec(ExprExpressionIncDecContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprExpressionIncDec(ExprExpressionIncDecContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprArrayExpression(ExprArrayExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprArrayExpression(ExprArrayExpressionContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterExprIdentifier(ExprIdentifierContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitExprIdentifier(ExprIdentifierContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterCbdMember(CbdMemberContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitCbdMember(CbdMemberContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterCbdSemicolon(CbdSemicolonContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void exitCbdSemicolon(CbdSemicolonContext ctx) {
        // Do nothing because of X and Y.

    }

    @Override
    public void enterCbdBlock(CbdBlockContext ctx) {
        jplagParser.add(J_INIT_BEGIN, ctx.getStart());
    }

    @Override
    public void exitCbdBlock(CbdBlockContext ctx) {
        jplagParser.add(J_INIT_END, ctx.getStop());
    }

    @Override
    public void enterExprConditionalExpression(ExprConditionalExpressionContext ctx) {
        jplagParser.add(J_COND, ctx.getStart());
    }

    @Override
    public void exitExprConditionalExpression(ExprConditionalExpressionContext ctx) {
        // Do nothing because of X and Y.
    }
}

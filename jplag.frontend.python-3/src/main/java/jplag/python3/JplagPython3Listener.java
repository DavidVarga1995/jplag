package jplag.python3;

import jplag.python3.grammar.Python3Listener;
import jplag.python3.grammar.Python3Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class JplagPython3Listener implements Python3Listener, Python3TokenConstants {

    private final jplag.python3.Parser jplagParser;

    public JplagPython3Listener(jplag.python3.Parser jplag) {
        jplagParser = jplag;
    }

    @Override
    public void enterTestlist(Python3Parser.@NonNull TestlistContext ctx) {
    }

    @Override
    public void exitTestlist(Python3Parser.@NonNull TestlistContext ctx) {
    }

    @Override
    public void enterAssert_stmt(Python3Parser.@NonNull Assert_stmtContext ctx) {
        jplagParser.add(ASSERT, ctx.getStart());
    }

    @Override
    public void exitAssert_stmt(Python3Parser.@NonNull Assert_stmtContext ctx) {
    }

    @Override
    public void enterArgument(Python3Parser.@NonNull ArgumentContext ctx) {
    }

    @Override
    public void exitArgument(Python3Parser.@NonNull ArgumentContext ctx) {
    }

    @Override
    public void enterNot_test(Python3Parser.@NonNull Not_testContext ctx) {
    }

    @Override
    public void exitNot_test(Python3Parser.@NonNull Not_testContext ctx) {
    }

    @Override
    public void enterFile_input(Python3Parser.@NonNull File_inputContext ctx) {
    }

    @Override
    public void exitFile_input(Python3Parser.@NonNull File_inputContext ctx) {
    }

    @Override
    public void enterXor_expr(Python3Parser.@NonNull Xor_exprContext ctx) {
    }

    @Override
    public void exitXor_expr(Python3Parser.@NonNull Xor_exprContext ctx) {
    }

    @Override
    public void enterImport_from(Python3Parser.@NonNull Import_fromContext ctx) {
    }

    @Override
    public void exitImport_from(Python3Parser.@NonNull Import_fromContext ctx) {
    }

    @Override
    public void enterSingle_input(Python3Parser.@NonNull Single_inputContext ctx) {
    }

    @Override
    public void exitSingle_input(Python3Parser.@NonNull Single_inputContext ctx) {
    }

    @Override
    public void enterDecorated(Python3Parser.@NonNull DecoratedContext ctx) {
        jplagParser.add(DEC_BEGIN, ctx.getStart());
    }

    @Override
    public void exitDecorated(Python3Parser.@NonNull DecoratedContext ctx) {
        jplagParser.addEnd(DEC_END, ctx.getStart());
    }

    @Override
    public void enterWith_item(Python3Parser.@NonNull With_itemContext ctx) {
    }

    @Override
    public void exitWith_item(Python3Parser.@NonNull With_itemContext ctx) {
    }

    @Override
    public void enterRaise_stmt(Python3Parser.@NonNull Raise_stmtContext ctx) {
        jplagParser.add(RAISE, ctx.getStart());
    }

    @Override
    public void exitRaise_stmt(Python3Parser.@NonNull Raise_stmtContext ctx) {
    }

    @Override
    public void enterImport_as_name(Python3Parser.@NonNull Import_as_nameContext ctx) {
    }

    @Override
    public void exitImport_as_name(Python3Parser.@NonNull Import_as_nameContext ctx) {
    }

    @Override
    public void enterExcept_clause(Python3Parser.@NonNull Except_clauseContext ctx) {
        jplagParser.add(EXCEPT_BEGIN, ctx.getStart());
    }

    @Override
    public void exitExcept_clause(Python3Parser.@NonNull Except_clauseContext ctx) {
        jplagParser.addEnd(EXCEPT_END, ctx.getStart());
    }

    @Override
    public void enterCompound_stmt(Python3Parser.@NonNull Compound_stmtContext ctx) {
    }

    @Override
    public void exitCompound_stmt(Python3Parser.@NonNull Compound_stmtContext ctx) {
    }

    @Override
    public void enterAnd_expr(Python3Parser.@NonNull And_exprContext ctx) {
    }

    @Override
    public void exitAnd_expr(Python3Parser.@NonNull And_exprContext ctx) {
    }

    @Override
    public void enterLambdef_nocond(Python3Parser.@NonNull Lambdef_nocondContext ctx) {
    }

    @Override
    public void exitLambdef_nocond(Python3Parser.@NonNull Lambdef_nocondContext ctx) {
    }

    @Override
    public void enterDictorsetmaker(Python3Parser.@NonNull DictorsetmakerContext ctx) {
        jplagParser.add(ARRAY, ctx.getStart());
    }

    @Override
    public void exitDictorsetmaker(Python3Parser.@NonNull DictorsetmakerContext ctx) {
    }

    @Override
    public void enterReturn_stmt(Python3Parser.@NonNull Return_stmtContext ctx) {
        jplagParser.add(RETURN, ctx.getStart());
    }

    @Override
    public void exitReturn_stmt(Python3Parser.@NonNull Return_stmtContext ctx) {
    }

    @Override
    public void enterDotted_name(Python3Parser.@NonNull Dotted_nameContext ctx) {
    }

    @Override
    public void exitDotted_name(Python3Parser.@NonNull Dotted_nameContext ctx) {
    }

    @Override
    public void enterFlow_stmt(Python3Parser.@NonNull Flow_stmtContext ctx) {
    }

    @Override
    public void exitFlow_stmt(Python3Parser.@NonNull Flow_stmtContext ctx) {
    }

    @Override
    public void enterWhile_stmt(Python3Parser.@NonNull While_stmtContext ctx) {
        jplagParser.add(WHILE_BEGIN, ctx.getStart());
    }

    @Override
    public void exitWhile_stmt(Python3Parser.@NonNull While_stmtContext ctx) {
        jplagParser.addEnd(WHILE_END, ctx.getStart());
    }

    @Override
    public void enterOr_test(Python3Parser.@NonNull Or_testContext ctx) {
    }

    @Override
    public void exitOr_test(Python3Parser.@NonNull Or_testContext ctx) {
    }

    @Override
    public void enterComparison(Python3Parser.@NonNull ComparisonContext ctx) {
    }

    @Override
    public void exitComparison(Python3Parser.@NonNull ComparisonContext ctx) {
    }

    @Override
    public void enterTest(Python3Parser.@NonNull TestContext ctx) {
    }

    @Override
    public void exitTest(Python3Parser.@NonNull TestContext ctx) {
    }

    @Override
    public void enterSubscript(Python3Parser.@NonNull SubscriptContext ctx) {
    }

    @Override
    public void exitSubscript(Python3Parser.@NonNull SubscriptContext ctx) {
    }

    @Override
    public void enterComp_for(Python3Parser.@NonNull Comp_forContext ctx) {
    }

    @Override
    public void exitComp_for(Python3Parser.@NonNull Comp_forContext ctx) {
    }

    @Override
    public void enterYield_arg(Python3Parser.@NonNull Yield_argContext ctx) {
        jplagParser.add(YIELD, ctx.getStart());
    }

    @Override
    public void exitYield_arg(Python3Parser.@NonNull Yield_argContext ctx) {
    }

    @Override
    public void enterYield_expr(Python3Parser.@NonNull Yield_exprContext ctx) {
    }

    @Override
    public void exitYield_expr(Python3Parser.@NonNull Yield_exprContext ctx) {
    }

    @Override
    public void enterImport_stmt(Python3Parser.@NonNull Import_stmtContext ctx) {
        jplagParser.add(IMPORT, ctx.getStart());
    }

    @Override
    public void exitImport_stmt(Python3Parser.@NonNull Import_stmtContext ctx) {
    }

    @Override
    public void enterShift_expr(Python3Parser.@NonNull Shift_exprContext ctx) {
    }

    @Override
    public void exitShift_expr(Python3Parser.@NonNull Shift_exprContext ctx) {
    }

    @Override
    public void enterLambdef(Python3Parser.@NonNull LambdefContext ctx) {
        jplagParser.add(LAMBDA, ctx.getStart());
    }

    @Override
    public void exitLambdef(Python3Parser.@NonNull LambdefContext ctx) {
    }

    @Override
    public void enterAnd_test(Python3Parser.@NonNull And_testContext ctx) {
    }

    @Override
    public void exitAnd_test(Python3Parser.@NonNull And_testContext ctx) {
    }

    @Override
    public void enterGlobal_stmt(Python3Parser.@NonNull Global_stmtContext ctx) {
    }

    @Override
    public void exitGlobal_stmt(Python3Parser.@NonNull Global_stmtContext ctx) {
    }

    @Override
    public void enterImport_as_names(Python3Parser.@NonNull Import_as_namesContext ctx) {
    }

    @Override
    public void exitImport_as_names(Python3Parser.@NonNull Import_as_namesContext ctx) {
    }

    @Override
    public void enterDecorators(Python3Parser.@NonNull DecoratorsContext ctx) {
    }

    @Override
    public void exitDecorators(Python3Parser.@NonNull DecoratorsContext ctx) {
    }

    @Override
    public void enterTry_stmt(Python3Parser.@NonNull Try_stmtContext ctx) {
        jplagParser.add(TRY_BEGIN, ctx.getStart());
    }

    @Override
    public void exitTry_stmt(Python3Parser.@NonNull Try_stmtContext ctx) {
    }

    @Override
    public void enterComp_op(Python3Parser.@NonNull Comp_opContext ctx) {
    }

    @Override
    public void exitComp_op(Python3Parser.@NonNull Comp_opContext ctx) {
    }

    @Override
    public void enterStar_expr(Python3Parser.@NonNull Star_exprContext ctx) {
    }

    @Override
    public void exitStar_expr(Python3Parser.@NonNull Star_exprContext ctx) {
    }

    @Override
    public void enterBreak_stmt(Python3Parser.@NonNull Break_stmtContext ctx) {
        jplagParser.add(BREAK, ctx.getStart());
    }

    @Override
    public void exitBreak_stmt(Python3Parser.@NonNull Break_stmtContext ctx) {
    }

    @Override
    public void enterParameters(Python3Parser.@NonNull ParametersContext ctx) {
    }

    @Override
    public void exitParameters(Python3Parser.@NonNull ParametersContext ctx) {
    }

    @Override
    public void enterDecorator(Python3Parser.@NonNull DecoratorContext ctx) {
    }

    @Override
    public void exitDecorator(Python3Parser.@NonNull DecoratorContext ctx) {
    }

    @Override
    public void enterTfpdef(Python3Parser.@NonNull TfpdefContext ctx) {
    }

    @Override
    public void exitTfpdef(Python3Parser.@NonNull TfpdefContext ctx) {
    }

    @Override
    public void enterTestlist_comp(Python3Parser.@NonNull Testlist_compContext ctx) {
        if (ctx.getText().contains(",")) {
            jplagParser.add(ARRAY, ctx.getStart());
        }
    }

    @Override
    public void exitTestlist_comp(Python3Parser.@NonNull Testlist_compContext ctx) {
    }

    @Override
    public void enterIf_stmt(Python3Parser.@NonNull If_stmtContext ctx) {
        jplagParser.add(IF_BEGIN, ctx.getStart());
    }

    @Override
    public void exitIf_stmt(Python3Parser.@NonNull If_stmtContext ctx) {
        jplagParser.addEnd(IF_END, ctx.getStart());
    }

    @Override
    public void enterWith_stmt(Python3Parser.@NonNull With_stmtContext ctx) {
        jplagParser.add(WITH_BEGIN, ctx.getStart());
    }

    @Override
    public void exitWith_stmt(Python3Parser.@NonNull With_stmtContext ctx) {
        jplagParser.addEnd(WITH_END, ctx.getStart());
    }

    @Override
    public void enterClassdef(Python3Parser.@NonNull ClassdefContext ctx) {
        jplagParser.add(CLASS_BEGIN, ctx.getStart());
    }

    @Override
    public void exitClassdef(Python3Parser.@NonNull ClassdefContext ctx) {
        jplagParser.addEnd(CLASS_END, ctx.getStart());
    }

    @Override
    public void enterExprlist(Python3Parser.@NonNull ExprlistContext ctx) {
    }

    @Override
    public void exitExprlist(Python3Parser.@NonNull ExprlistContext ctx) {
    }

    @Override
    public void enterSmall_stmt(Python3Parser.@NonNull Small_stmtContext ctx) {
    }

    @Override
    public void exitSmall_stmt(Python3Parser.@NonNull Small_stmtContext ctx) {
    }

    @Override
    public void enterTrailer(Python3Parser.@NonNull TrailerContext ctx) {
        if (ctx.getText().charAt(0)=='(') {
            jplagParser.add(APPLY, ctx.getStart());
        } else {
            jplagParser.add(ARRAY, ctx.getStart());
        }
    }

    @Override
    public void exitTrailer(Python3Parser.@NonNull TrailerContext ctx) {
    }

    @Override
    public void enterDotted_as_names(Python3Parser.@NonNull Dotted_as_namesContext ctx) {
    }

    @Override
    public void exitDotted_as_names(Python3Parser.@NonNull Dotted_as_namesContext ctx) {
    }

    @Override
    public void enterArith_expr(Python3Parser.@NonNull Arith_exprContext ctx) {
    }

    @Override
    public void exitArith_expr(Python3Parser.@NonNull Arith_exprContext ctx) {
    }

    @Override
    public void enterArglist(Python3Parser.@NonNull ArglistContext ctx) {
    }

    @Override
    public void exitArglist(Python3Parser.@NonNull ArglistContext ctx) {
    }

    @Override
    public void enterSimple_stmt(Python3Parser.@NonNull Simple_stmtContext ctx) {
    }

    @Override
    public void exitSimple_stmt(Python3Parser.@NonNull Simple_stmtContext ctx) {
    }

    @Override
    public void enterTypedargslist(Python3Parser.@NonNull TypedargslistContext ctx) {
    }

    @Override
    public void exitTypedargslist(Python3Parser.@NonNull TypedargslistContext ctx) {
    }

    @Override
    public void enterExpr(Python3Parser.@NonNull ExprContext ctx) {
    }

    @Override
    public void exitExpr(Python3Parser.@NonNull ExprContext ctx) {
    }

    @Override
    public void enterTerm(Python3Parser.@NonNull TermContext ctx) {
    }

    @Override
    public void exitTerm(Python3Parser.@NonNull TermContext ctx) {
    }

    @Override
    public void enterPower(Python3Parser.@NonNull PowerContext ctx) {
    }

    @Override
    public void exitPower(Python3Parser.@NonNull PowerContext ctx) {
    }

    @Override
    public void enterDotted_as_name(Python3Parser.@NonNull Dotted_as_nameContext ctx) {
    }

    @Override
    public void exitDotted_as_name(Python3Parser.@NonNull Dotted_as_nameContext ctx) {
    }

    @Override
    public void enterFactor(Python3Parser.@NonNull FactorContext ctx) {
    }

    @Override
    public void exitFactor(Python3Parser.@NonNull FactorContext ctx) {
    }

    @Override
    public void enterSliceop(Python3Parser.@NonNull SliceopContext ctx) {
    }

    @Override
    public void exitSliceop(Python3Parser.@NonNull SliceopContext ctx) {
    }

    @Override
    public void enterFuncdef(Python3Parser.@NonNull FuncdefContext ctx) {
        jplagParser.add(METHOD_BEGIN, ctx.getStart());
    }

    @Override
    public void exitFuncdef(Python3Parser.@NonNull FuncdefContext ctx) {
        jplagParser.addEnd(METHOD_END, ctx.getStart());
    }

    @Override
    public void enterSubscriptlist(Python3Parser.@NonNull SubscriptlistContext ctx) {
    }

    @Override
    public void exitSubscriptlist(Python3Parser.@NonNull SubscriptlistContext ctx) {
    }

    @Override
    public void enterTest_nocond(Python3Parser.@NonNull Test_nocondContext ctx) {
    }

    @Override
    public void exitTest_nocond(Python3Parser.@NonNull Test_nocondContext ctx) {
    }

    @Override
    public void enterComp_iter(Python3Parser.@NonNull Comp_iterContext ctx) {
    }

    @Override
    public void exitComp_iter(Python3Parser.@NonNull Comp_iterContext ctx) {
    }

    @Override
    public void enterNonlocal_stmt(Python3Parser.@NonNull Nonlocal_stmtContext ctx) {
    }

    @Override
    public void exitNonlocal_stmt(Python3Parser.@NonNull Nonlocal_stmtContext ctx) {
    }

    @Override
    public void enterEval_input(Python3Parser.@NonNull Eval_inputContext ctx) {
    }

    @Override
    public void exitEval_input(Python3Parser.@NonNull Eval_inputContext ctx) {
    }

    @Override
    public void enterVfpdef(Python3Parser.@NonNull VfpdefContext ctx) {
    }

    @Override
    public void exitVfpdef(Python3Parser.@NonNull VfpdefContext ctx) {
    }

    @Override
    public void enterImport_name(Python3Parser.@NonNull Import_nameContext ctx) {
    }

    @Override
    public void exitImport_name(Python3Parser.@NonNull Import_nameContext ctx) {
    }

    @Override
    public void enterComp_if(Python3Parser.@NonNull Comp_ifContext ctx) {
    }

    @Override
    public void exitComp_if(Python3Parser.@NonNull Comp_ifContext ctx) {
    }

    @Override
    public void enterAugassign(Python3Parser.@NonNull AugassignContext ctx) {
        jplagParser.add(ASSIGN, ctx.getStart());
    }

    @Override
    public void exitAugassign(Python3Parser.@NonNull AugassignContext ctx) {
    }

    @Override
    public void enterPass_stmt(Python3Parser.@NonNull Pass_stmtContext ctx) {
    }

    @Override
    public void exitPass_stmt(Python3Parser.@NonNull Pass_stmtContext ctx) {
    }

    @Override
    public void enterExpr_stmt(Python3Parser.@NonNull Expr_stmtContext ctx) {
    }

    @Override
    public void exitExpr_stmt(Python3Parser.@NonNull Expr_stmtContext ctx) {
    }

    @Override
    public void enterYield_stmt(Python3Parser.@NonNull Yield_stmtContext ctx) {
        jplagParser.add(YIELD, ctx.getStart());
    }

    @Override
    public void exitYield_stmt(Python3Parser.@NonNull Yield_stmtContext ctx) {
    }

    @Override
    public void enterSuite(Python3Parser.@NonNull SuiteContext ctx) {
    }

    @Override
    public void exitSuite(Python3Parser.@NonNull SuiteContext ctx) {
    }

    @Override
    public void enterContinue_stmt(Python3Parser.@NonNull Continue_stmtContext ctx) {
        jplagParser.add(CONTINUE, ctx.getStart());
    }

    @Override
    public void exitContinue_stmt(Python3Parser.@NonNull Continue_stmtContext ctx) {
    }

    @Override
    public void enterTestlist_star_expr(Python3Parser.@NonNull Testlist_star_exprContext ctx) {
    }

    @Override
    public void exitTestlist_star_expr(Python3Parser.@NonNull Testlist_star_exprContext ctx) {
    }

    @Override
    public void enterVarargslist(Python3Parser.@NonNull VarargslistContext ctx) {
    }

    @Override
    public void exitVarargslist(Python3Parser.@NonNull VarargslistContext ctx) {
    }

    @Override
    public void enterFor_stmt(Python3Parser.@NonNull For_stmtContext ctx) {
        jplagParser.add(FOR_BEGIN, ctx.getStart());
    }

    @Override
    public void exitFor_stmt(Python3Parser.@NonNull For_stmtContext ctx) {
        jplagParser.addEnd(FOR_END, ctx.getStart());
    }

    @Override
    public void enterDel_stmt(Python3Parser.@NonNull Del_stmtContext ctx) {
        jplagParser.add(DEL, ctx.getStart());
    }

    @Override
    public void exitDel_stmt(Python3Parser.@NonNull Del_stmtContext ctx) {
    }

    @Override
    public void enterAtom(Python3Parser.@NonNull AtomContext ctx) {
    }

    @Override
    public void exitAtom(Python3Parser.@NonNull AtomContext ctx) {
    }

    @Override
    public void enterStmt(Python3Parser.@NonNull StmtContext ctx) {
    }

    @Override
    public void exitStmt(Python3Parser.@NonNull StmtContext ctx) {
    }

    @Override
    public void enterEveryRule(@NonNull ParserRuleContext ctx) {
    }

    @Override
    public void exitEveryRule(@NonNull ParserRuleContext ctx) {
    }

    @Override
    public void visitTerminal(@NonNull TerminalNode node) {
        if (node.getText().equals("=")) {
            jplagParser.add(ASSIGN, node.getSymbol());
        } else if (node.getText().equals("finally")) {
            jplagParser.add(FINALLY, node.getSymbol());
        }
    }

    @Override
    public void visitErrorNode(@NonNull ErrorNode node) {
    }

	@Override
	public void enterAnnassign(Python3Parser.AnnassignContext ctx) {
	}

	@Override
	public void exitAnnassign(Python3Parser.AnnassignContext ctx) {
	}

	@Override
	public void enterEncoding_decl(Python3Parser.Encoding_declContext ctx) {
	}

	@Override
	public void exitEncoding_decl(Python3Parser.Encoding_declContext ctx) {
	}

	@Override
	public void enterAtom_expr(Python3Parser.Atom_exprContext ctx) {
	}

	@Override
	public void exitAtom_expr(Python3Parser.Atom_exprContext ctx) {
	}

	@Override
	public void enterAsync_funcdef(Python3Parser.Async_funcdefContext ctx) {
	}

	@Override
	public void exitAsync_funcdef(Python3Parser.Async_funcdefContext ctx) {
	}

	@Override
	public void enterAsync_stmt(Python3Parser.Async_stmtContext ctx) {
	}

	@Override
	public void exitAsync_stmt(Python3Parser.Async_stmtContext ctx) {
	}
}

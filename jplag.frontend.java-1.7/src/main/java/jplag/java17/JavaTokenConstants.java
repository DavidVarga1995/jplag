package jplag.java17;

public interface JavaTokenConstants extends jplag.TokenConstants {
    int FILE_END = 0;

    // Used to optionally separate methods from each other
    // with an always marked token (for 1.5 grammar)
    int SEPARATOR_TOKEN = 1;// @formatter:off

    int J_PACKAGE = 2;                //  a // at the end of the constant means that there is a test case covering the token
    int J_IMPORT = 3;                //
    int J_CLASS_BEGIN = 4;            //
    int J_CLASS_END = 5;            //
    int J_METHOD_BEGIN = 6;        //
    int J_METHOD_END = 7;            //
    int J_VARDEF = 8;                //
    int J_SYNC_BEGIN = 9;            //
    int J_SYNC_END = 10;            //
    int J_DO_BEGIN = 11;            //
    int J_DO_END = 12;                //
    int J_WHILE_BEGIN = 13;        //
    int J_WHILE_END = 14;            //
    int J_FOR_BEGIN = 15;            //
    int J_FOR_END = 16;            //
    int J_SWITCH_BEGIN = 17;        //
    int J_SWITCH_END = 18;            //
    int J_CASE = 19;                //
    int J_TRY_BEGIN = 20;            //
    int J_CATCH_BEGIN = 21;        //
    int J_CATCH_END = 22;        //
    int J_FINALLY = 23;            //
    int J_IF_BEGIN = 24;            //
    int J_ELSE = 25;                //
    int J_IF_END = 26;                //
    int J_COND = 27;                //
    int J_BREAK = 28;                //
    int J_CONTINUE = 29;            //
    int J_RETURN = 30;                //
    int J_THROW = 31;                //
    int J_IN_CLASS_BEGIN = 32;        //
    int J_IN_CLASS_END = 33;        //
    int J_APPLY = 34;                //
    int J_NEWCLASS = 35;            //
    int J_NEWARRAY = 36;            //
    int J_ASSIGN = 37;                //
    int J_INTERFACE_BEGIN = 38;    //
    int J_INTERFACE_END = 39;        //
    int J_CONSTR_BEGIN = 40;        //
    int J_CONSTR_END = 41;            //
    int J_INIT_BEGIN = 42;            //
    int J_INIT_END = 43;            //
    int J_VOID = 44;                //
    int J_ARRAY_INIT_BEGIN = 45;    //
    int J_ARRAY_INIT_END = 46;    //

    // new in 1.5:
    int J_ENUM_BEGIN = 47;            //
    int J_ENUM_CLASS_BEGIN = 48;    //
    int J_ENUM_END = 49;            //
    int J_GENERIC = 50;            //
    int J_ASSERT = 51;                //

    int J_ANNO = 52;                //
    int J_ANNO_MARKER = 53;        //
    int J_ANNO_M_BEGIN = 54;        //
    int J_ANNO_M_END = 55;            //
    int J_ANNO_T_BEGIN = 56;        //
    int J_ANNO_T_END = 57;            //
    int J_ANNO_C_BEGIN = 58;        //
    int J_ANNO_C_END = 59;            //

    // new in 1.7
    int J_TRY_WITH_RESOURCE = 60;    //

    int NUM_DIFF_TOKENS = 61;        // @formatter:on
}

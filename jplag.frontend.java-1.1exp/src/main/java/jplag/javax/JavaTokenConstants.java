package jplag.javax;

//import java.io.*;

public interface JavaTokenConstants extends jplag.TokenConstants {
    int FILE_END = 0;

    int J_PACKAGE = 1;
    int J_IMPORT = 2;
    int J_CLASS_BEGIN = 3;
    int J_CLASS_END = 4;
    int J_METHOD_BEGIN = 5;
    int J_METHOD_END = 6;
    int J_VARDEF = 7;
    int J_SYNC_BEGIN = 8;
    int J_SYNC_END = 9;
    int J_DO_BEGIN = 10;
    int J_DO_END = 11;
    int J_WHILE_BEGIN = 12;
    int J_WHILE_END = 13;
    int J_FOR_BEGIN = 14;
    int J_FOR_END = 15;
    int J_SWITCH_BEGIN = 16;
    int J_SWITCH_END = 17;
    int J_CASE = 18;
    int J_TRY_BEGIN = 19;
    int J_CATCH_BEGIN = 20;
    int J_CATCH_END = 21;
    int J_FINALLY = 22;
    int J_IF_BEGIN = 23;
    int J_ELSE = 24;
    int J_IF_END = 25;
    int J_COND = 26;
    int J_BREAK = 27;
    int J_CONTINUE = 28;
    int J_RETURN = 29;
    int J_THROW = 30;
    int J_IN_CLASS_BEGIN = 31;
    int J_IN_CLASS_END = 32;
    int J_APPLY = 33;
    int J_NEWCLASS = 34;
    int J_NEWARRAY = 35;
    int J_ASSIGN = 36;
    int J_INTERFACE_BEGIN = 37;
    int J_INTERFACE_END = 38;
    int J_CONSTR_BEGIN = 39;
    int J_CONSTR_END = 40;
    int J_INIT_BEGIN = 41;
    int J_INIT_END = 42;
    int J_VOID = 43;
    // new tokens:
    int J_ABSTRACT = 44;
    int J_FINAL = 45;
    int J_PUBLIC = 46;
    int J_STATIC = 47;
    int J_PROTECTED = 48;
    int J_PRIVATE = 49;
    int J_EXTENDS = 50;
    int J_TRANSIENT = 51;
    int J_VOLANTILE = 52;
    int J_ARRAY_INIT = 53;
    int J_NATIVE = 54;
    int J_SYNCHRONIZED = 55;
    int J_THROWS = 56;
    int J_THIS = 57;
    int J_BOOLEAN_TYPE = 58;
    int J_CHAR_TYPE = 59;
    int J_BYTE_TYPE = 60;
    int J_SHORT_TYPE = 61;
    int J_INT_TYPE = 62;
    int J_LONG_TYPE = 63;
    int J_FLOAT_TYPE = 64;
    int J_DOUBLE_TYPE = 65;
    int J_ASSIGNOP = 66;
    int J_ASSIGNBITOP = 67;
    int J_COND_OR = 68;
    int J_COND_AND = 69;
    int J_COND_IOR = 70;
    int J_COND_XOR = 71;
    int J_AND = 72;
    int J_EQUALITY = 73;
    int J_INSTANCEOF = 74;
    int J_SHIFT = 75;
    int J_RELATIONAL = 76;
    int J_ADD = 77;
    int J_MULT = 78;
    int J_DECINC = 79;
    int J_CAST = 80;
    int J_SUPER = 81;
    int J_NULL = 83;
    int J_LABEL = 84;
    int J_INT = 85;
    int J_FLOAT = 86;
    int J_CHAR = 87;
    int J_STRING = 88;
    int J_BOOLEAN = 89;

    int NUM_DIFF_TOKENS = 90;
}

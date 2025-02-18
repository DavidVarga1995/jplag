package jplag.csharp;

public interface CSharpTokenConstants extends jplag.TokenConstants {
    int FILE_END = 0;

    // Used to optionally separate methods from each other
    // with an always marked token
    int SEPARATOR_TOKEN = 1;

    int _INVOCATION =       2;
    int _OBJECT_CREATION =  3;
    int _ARRAY_CREATION =   4;
    int _ASSIGNMENT =       5;
    int _L_BRACE =          6;
    int _R_BRACE =          7;
    int _DECLARE_VAR =      8;
    int _DECLARE_CONST =    9;
    int _IF =              10;
    int _ELSE =            11;
    int _SWITCH_BEGIN =    12;
    int _SWITCH_END =      13;
    int _CASE =            14;
    int _DO =              15;
    int _WHILE =           16;
    int _FOR =             17;
    int _FOREACH =         18;
    int _BREAK =           19;
    int _CONTINUE =        20;
    int _GOTO =            21;
    int _RETURN =          22;
    int _THROW =           23;
    int _CHECKED =         24;
    int _UNCHECKED =       25;
    int _LOCK =            26;
    int _USING =           27;
    int _TRY =             28;
    int _CATCH =           29;
    int _FINALLY =         30;
    int _NAMESPACE_BEGIN = 31;
    int _NAMESPACE_END =   32;
    int _USING_DIRECTIVE = 33;
    int _CLASS_BEGIN =     34;
    int _CLASS_END =       35;
    int _METHOD =          36;
    int _PROPERTY =        37;
    int _EVENT =           38;
    int _INDEXER =         39;
    int _OPERATOR =        40;
    int _CONSTRUCTOR =     41;
    int _STATIC_CONSTR =   42;
    int _DESTRUCTOR =      43;
    int _STRUCT_BEGIN =    44;
    int _STRUCT_END =      45;
    int _INTERFACE_BEGIN = 46;
    int _INTERFACE_END =   47;
    int _ENUM =            48;
    int _DELEGATE =        49;
    int _ATTRIBUTE =       50;
    int _END_IF =          51;
    int _UNSAFE =          52;
    int _FIXED =           53;

    int NUM_DIFF_TOKENS = 54;
}
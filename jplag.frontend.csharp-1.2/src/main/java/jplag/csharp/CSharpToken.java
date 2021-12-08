package jplag.csharp;


public class CSharpToken extends jplag.Token implements CSharpTokenConstants {
  private static final long serialVersionUID = 1L;
  private int line, column, length;

  public CSharpToken(int type, String file, int line, int column, int length) {
    super(type, file, line, column, length);
 }

  public int getLine() { return line; }
  public int getColumn() { return column; }
  public int getLength() { return length; }
  public void setLine(int line) { this.line = line; }
  public void setColumn(int column) { this.column = column; }
  public void setLength(int length) { this.length = length; }

  public static String type2string(int type) {
    switch (type) {
    case CSharpTokenConstants.FILE_END:
                                   return "**********";
    case INVOCATION:              return "INVOCATION";
    case OBJECT_CREATION:         return "OBJECT_CRE";
    case ARRAY_CREATION:          return "ARRAY_CREA";
    case ASSIGNMENT:              return "ASSIGNMENT";
    case L_BRACE:                 return "L_BRACE { ";
    case R_BRACE:                 return "R_BRACE } ";
    case DECLARE_VAR:             return "DECLAREVAR";
    case DECLARE_CONST:           return "DECLARE_CO";
    case IF:                      return "IF {      ";
    case ELSE:                    return "ELSE      ";
    case END_IF:                  return "IF }      ";
    case SWITCH_BEGIN:            return "SWITCH {  ";
    case SWITCH_END:              return "SWITCH }  ";
    case CASE:                    return "CASE      ";
    case DO:                      return "DO        ";
    case WHILE:                   return "WHILE     ";
    case FOR:                     return "FOR       ";
    case FOREACH:                 return "FOREACH   ";
    case BREAK:                   return "BREAK     ";
    case CONTINUE:                return "CONTINUE  ";
    case GOTO:                    return "GOTO      ";
    case RETURN:                  return "RETURN    ";
    case THROW:                   return "THROW     ";
    case CHECKED:                 return "CHECKED   ";
    case UNCHECKED:               return "UNCHECKED ";
    case LOCK:                    return "LOCK      ";
    case USING:                   return "USING     ";
    case TRY:                     return "TRY       ";
    case CATCH:                   return "CATCH     ";
    case FINALLY:                 return "FINALLY   ";
    case NAMESPACE_BEGIN:         return "NAMESPACE{";
    case NAMESPACE_END:           return "NAMESPACE}";
    case USING_DIRECTIVE:         return "USING_DIR ";
    case CLASS_BEGIN:             return "CLASS {   ";
    case CLASS_END:               return "CLASS }   ";
    case METHOD:                  return "METHOD    ";
    case PROPERTY:                return "PROPERTY  ";
    case EVENT:                   return "EVENT     ";
    case INDEXER:                 return "INDEXER   ";
    case OPERATOR:                return "OPERATOR  ";
    case CONSTRUCTOR:             return "CONSTR    ";
    case STATIC_CONSTR:           return "ST_CONSTR ";
    case DESTRUCTOR:              return "DESTRUCTOR";
    case STRUCT_BEGIN:            return "STRUCT {  ";
    case STRUCT_END:              return "STRUCT }  ";
    case INTERFACE_BEGIN:         return "INTERFACE{";
    case INTERFACE_END:           return "INTERFACE}";
    case ENUM:                    return "ENUM      ";
    case DELEGATE:                return "DELEGATE  ";
    case ATTRIBUTE:               return "ATTRIBUTE ";
    case UNSAFE:                  return "UNSAFE    ";
    case FIXED:                   return "FIXED     ";

    default:                      return "<UNKNOWN> ";
    }
  }
  
  public static int numberOfTokens() { 
    return NUM_DIFF_TOKENS;
  } 
}


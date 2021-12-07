package jplag.java17;

import jplag.StrippedProgram;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ParserTest {

    private static File srcTestResources;

    @BeforeAll
    public static void getPaths() {

        srcTestResources = new File(System.getProperty("user.dir"), "src/test/resources");
    }

    @Disabled
    public void j7Template() {
        File file = new File(srcTestResources, "J7StringSwitch.java");
        String expected = ""// @formatter:off
                + "********\n"; // @formatter:on
        String tokens = parseWithJ7Parser(file, false);
        assertEquals(expected, tokens);
    }

    /**
     * added for ticket #58
     */
    @Test
    public void emptyFileOnlyComments() {
        compareWithParser15(new File(srcTestResources, "EmptyFileOnlyComments.java"));
    }

    @Test
    public void emptyFile() {
        compareWithParser15(new File(srcTestResources, "EmptyFile.java"));
    }

    @Test
    public void j7StringSwitchTest() {
        File file = new File(srcTestResources, "J7StringSwitch.java");
        String expected = "" // @formatter:off
                + "CLASS{  \n"
                + "VOID    \n"
                + "METHOD{ \n"
                + "SWITCH{ \n"
                + "CASE    \n"
                + "BREAK   \n"
                + "CASE    \n"
                + "CASE    \n"
                + "BREAK   \n"
                + "CASE    \n"
                + "BREAK   \n"
                + "}SWITCH \n"
                + "}METHOD \n"
                + "}CLASS  \n"
                + "********\n"; // @formatter:on
        String tokens = parseWithJ7Parser(file, false);
        assertEquals(expected, tokens);
    }

    @Test
    public void j7TryCatchTest() {
        File file = new File(srcTestResources, "J7TryCatch.java");
        String expected = "IMPORT  \n" // @formatter:off
                + "IMPORT  \n"
                + "IMPORT  \n"
                + "CLASS{  \n"
                + "VOID    \n"
                + "METHOD{ \n"
                + "TRY{    \n"
                + "CATCH{  \n"
                + "}CATCH  \n"
                + "FINALLY \n"
                + "}METHOD \n"
                + "VOID    \n"
                + "METHOD{ \n"
                + "TRY{    \n"
                + "TRY_RES \n"
                + "NEWCLASS\n"
                + "APPLY   \n"
                + "CATCH{  \n"
                + "}CATCH  \n"
                + "}METHOD \n"
                + "VOID    \n"
                + "METHOD{ \n"
                + "TRY{    \n"
                + "TRY_RES \n"
                + "NEWCLASS\n"
                + "TRY_RES \n"
                + "NEWCLASS\n"
                + "APPLY   \n"
                + "CATCH{  \n"
                + "}CATCH  \n"
                + "}METHOD \n"
                + "VOID    \n"
                + "METHOD{ \n"
                + "TRY{    \n"
                + "APPLY   \n"
                + "CATCH{  \n"
                + "}CATCH  \n"
                + "}METHOD \n"
                + "VOID    \n"
                + "METHOD{ \n"
                + "TRY{    \n"
                + "APPLY   \n"
                + "CATCH{  \n"
                + "}CATCH  \n"
                + "CATCH{  \n"
                + "}CATCH  \n"
                + "}METHOD \n"
                + "}CLASS  \n"
                + "********\n";// @formatter:on

        String tokens = parseWithJ7Parser(file, false);
        assertEquals(expected, tokens);
    }

    @Test
    public void compArray() {
        compareWithParser15(new File(srcTestResources, "ArrayTest.java"));
    }

    @Test
    public void compSimpleClass() {
        compareWithParser15(new File(srcTestResources, "SimpleClass.java"));
    }

    @Test
    public void compEnum() {
        compareWithParser15(new File(srcTestResources, "EnumTest.java"));
    }

    @Test
    public void compGeneric() {
        compareWithParser15(new File(srcTestResources, "GenericPocket.java"));
    }

    @Test
    public void compInnerClass() {
        compareWithParser15(new File(srcTestResources, "InnerClass.java"));
    }

    @Test
    public void compAnonymousClass() {
        compareWithParser15(new File(srcTestResources, "AnonymousClass.java"));
    }

    @Test
    public void compAnnotations_Usage() {
        compareWithParser15(new File(srcTestResources, "AnnotationText.java"));
    }

    @Test
    public void compAnnotations_PackageUsage() {
        compareWithParser15(new File(srcTestResources, "package-info.java"));
    }

    @Test
    public void compAnnotations_Definition() {
        compareWithParser15(new File(srcTestResources, "OptimizeAnnotation.java"));
    }

    @Test
    public void j7Generics() {
        File file = new File(srcTestResources, "J7Generics.java");
        String expected = ""// @formatter:off
                + "CLASS{  \n"
                + "VOID    \n"
                + "METHOD{ \n"
                + "GENERIC \n"
                + "VARDEF  \n"
                + "ASSIGN  \n"
                + "GENERIC \n"
                + "NEWCLASS\n"
                + "}METHOD \n"
                + "}CLASS  \n"
                + "********\n"; // @formatter:on
        String tokens = parseWithJ7Parser(file, false);
        assertEquals(expected, tokens);
    }

    @Test
    public void assureBackwardsCompatibility2() {
        compareWithParser15(new File(srcTestResources, "ExceptionTwo.java"));
    }

    @Test
    public void assureBackwardsCompatibility3() {
        compareWithParser15(new File(srcTestResources, "ExceptionThree.java"));
    }

    @Test
    public void assureBackwardsCompatibility4() {
        compareWithParser15(new File(srcTestResources, "Kalender.java"));
    }

    /**
     * Compare results (token sequence) with Java 1.5 parser (without method
     * separators ; if you know why they exist, PLEASE tell us or document at
     * https://svn.ipd.kit.edu/trac/jplag/wiki/Server/Frontends/Java-1.5)
     *
     * @param javaFile java file
     */
    private void compareWithParser15(File javaFile) {
        if (!javaFile.exists()) {
            fail("Test not implemented - cannot find file >" + javaFile + "<");
        }
        String newTokens = parseWithJ7Parser(javaFile, true);
        String oldTokens = parseWithJ5Parser(javaFile);
        // compare token sequence?
        assertEquals(oldTokens, newTokens);
    }

    private String parseWithJ5Parser(File javaFile) {

        // parse with old parser
        jplag.java15.Parser oldParser = new jplag.java15.Parser(false);
        oldParser.setProgram(new StrippedProgram());
        jplag.Structure oldStruct = oldParser.parse(javaFile.getParentFile(), new String[]{javaFile.getName()});

        return buildTokenString(oldStruct, true);
    }

    /**
     * assumes that the token types have not changed from java 1.5 to Java 1.7
     * (meaning the integer values are the same but Java 1.7 has more than Java
     * 1.5)
     *
     * @param oldStruct   old struction
     * @param withDetails with details
     * @return return
     * @see jplag.java17.JavaToken
     */
    private String buildTokenString(jplag.Structure oldStruct, boolean withDetails) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < oldStruct.size(); i++) {
            sb.append(jplag.java17.JavaToken.type2string(oldStruct.getTokens()[i].type));
            if (withDetails) {
                sb.append(" L:").append(oldStruct.getTokens()[i].getLine()).append(" C:")
                        .append(oldStruct.getTokens()[i].getLine()).append(" l:")
                        .append(oldStruct.getTokens()[i].getLine());
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String parseWithJ7Parser(File javaFile, boolean withDetails) {
        // parse with new parser
        jplag.java17.Parser newParser = new Parser();
        newParser.setProgram(new StrippedProgram());
        jplag.Structure newStruct = newParser.parse(javaFile.getParentFile(), new String[]{javaFile.getName()});

        return buildTokenString(newStruct, withDetails);
    }
}

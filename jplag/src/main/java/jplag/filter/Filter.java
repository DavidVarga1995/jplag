package jplag.filter;

public class Filter {
    private final int[] table;

    public Filter(String fileName) {
        table = FilterParser.parse(fileName, new jplag.javax.JavaToken(0, null, 0),
                null);
    }

    public final jplag.Structure filter(jplag.Structure struct) {
        if (struct == null) return null;
        jplag.Structure res = new jplag.Structure();
        for (int i = 0; i < struct.size(); i++) {
            jplag.Token oldToken = struct.getTokens()[i];
            if (table[oldToken.type] != -1)
                res.addToken(new jplag.javax.JavaToken(table[oldToken.type],
                        oldToken.file,
                        oldToken.getLine()));
        }
        return res;
    }
}

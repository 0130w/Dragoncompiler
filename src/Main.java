import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import java.io.IOException;
import java.util.List;

public class Main
{    
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        sysYLexer.removeErrorListeners();
        MyErrorListener myErrorListener = new MyErrorListener();
        sysYLexer.addErrorListener(myErrorListener);
        List<? extends Token> myTokens = sysYLexer.getAllTokens();
        if (!myErrorListener.hasError()) {
            for (Token token : myTokens) {
                printSysYTokenInformation(token);
            }
        }
    }

    private static void printSysYTokenInformation(Token token) {
        String tokenType = SysYLexer.VOCABULARY.getSymbolicName(token.getType());
        String tokenText = token.getText();
        int tokenLine = token.getLine();
        if (tokenType.equals("INTEGER_CONST")) {
            int value = parseTokenValue(tokenText);
            System.out.println(tokenType + " " + value + " at Line " + tokenLine + ".");
        } else {
            System.out.println(tokenType + " " + tokenText + " at Line " + tokenLine + ".");
        }
    }

    private static int parseTokenValue(String tokenText) {
        try {
            if (tokenText.startsWith("0x") || tokenText.startsWith("0X")) {
                return Integer.parseInt(tokenText.substring(2), 16);
            } else if (tokenText.startsWith("0") && tokenText.length() > 1) {
                return Integer.parseInt(tokenText, 8);
            } else {
                return Integer.parseInt(tokenText);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + tokenText);
            return 0;
        }
    }
}
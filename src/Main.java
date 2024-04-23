import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.util.Arrays;

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
        TokenErrorListener myTokenErrorListener = new TokenErrorListener();
        sysYLexer.addErrorListener(myTokenErrorListener);
        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);
        ParseErrorListener myParseErrorListener = new ParseErrorListener();
        sysYParser.removeErrorListeners();
        sysYParser.addErrorListener(myParseErrorListener);
        ParserListener parseListener = new ParserListener(tokens);
        TypeCheckListener typeListener = new TypeCheckListener();
        ProxyParseTreeListener proxyListener = new ProxyParseTreeListener(Arrays.asList(parseListener, typeListener));
        ParseTreeWalker.DEFAULT.walk(proxyListener, sysYParser.program());
    }
}
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ErrorNode;

public class ParseErrorListener extends BaseErrorListener {
    boolean hasError = false;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        System.out.println("Error type B at Line " + line + ": " + msg + ".");
        hasError = true;
    }

    public boolean hasError() {
        return hasError;
    }
}

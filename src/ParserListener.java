import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class ParserListener extends SysYParserBaseListener{

    BufferedTokenStream tokens;
    TokenStreamRewriter rewriter;
    int brace_counts = 0;
    int indent_level = 0;

    public ParserListener(BufferedTokenStream tokens) {
        this.tokens = tokens;
        rewriter = new TokenStreamRewriter(tokens);
    }

    // TODO! use more efficient structure
    String[] keywords = {"const", "int", "void", "if",
            "else", "while", "break", "continue", "return"};
    String[] operands = {"+", "-", "*", "/", "%", "=", "==", "!=",
            "<", ">", "<=", ">=", "!", "&&", "||", ",", ";"};
    String[] ws_keywords = {"const", "int", "void", "if",
            "else", "while", "return"};
    String[] binary_operands = {"+", "-", "*", "/", "%", "=", "==", "!=",
            "<", ">", "<=", ">=", "&&", "||"};
    private static final Map<Integer, Color> BRACE_COLOR_MAP = new HashMap<>();
    private static final Set<Integer> L_BRACE_COLOR_LIST = new HashSet<>();
    private static final Set<Integer> R_BRACE_COLOR_LIST = new HashSet<>();
    static {
        BRACE_COLOR_MAP.put(0, Color.LightRed);
        BRACE_COLOR_MAP.put(1, Color.LightGreen);
        BRACE_COLOR_MAP.put(2, Color.LightYellow);
        BRACE_COLOR_MAP.put(3, Color.LightBlue);
        BRACE_COLOR_MAP.put(4, Color.LightMagenta);
        BRACE_COLOR_MAP.put(5, Color.LightCyan);

        // See SysYParser.tokens
        L_BRACE_COLOR_LIST.add(25);
        L_BRACE_COLOR_LIST.add(27);
        L_BRACE_COLOR_LIST.add(29);

        R_BRACE_COLOR_LIST.add(26);
        R_BRACE_COLOR_LIST.add(28);
        R_BRACE_COLOR_LIST.add(30);
    }

    Optional<Effect> last_effect = Optional.empty();
    Stack<Color> color_stack = new Stack<>();
    Stack<Effect> effect_stack = new Stack<>();

    @Override
    public void visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        String text = node.getText();
        int token_type = token.getType();
        boolean changeFlag = false;

        if(Arrays.asList(keywords).contains(text)) {
            color_stack.push(Color.LightCyan);
            changeFlag = true;
        } else if(Arrays.asList(operands).contains(text)) {
            color_stack.push(Color.LightRed);
            changeFlag = true;
        } else if(token_type == 34) {
            // 34 represents INTEGER_CONST, see SysYParser.tokens
            color_stack.push(Color.Magenta);
            changeFlag = true;
        } else if(L_BRACE_COLOR_LIST.contains(token_type)) {
            color_stack.push(getBraceColor(brace_counts));
            brace_counts = (brace_counts + 1) % getBraceColorNumber();
            changeFlag = true;
        } else if(R_BRACE_COLOR_LIST.contains(token_type)) {
            brace_counts = (brace_counts - 1 + getBraceColorNumber()) % getBraceColorNumber();
            color_stack.push(getBraceColor(brace_counts));
            changeFlag = true;
        }

        // process function def empty line
        ParserRuleContext parent = (ParserRuleContext) node.getParent();
        if(parent.getRuleIndex() == SysYParser.RULE_func_type) {
            rewriter.insertBefore(token, "\u001B[" + Color.Reset.getCode() + "m" + "\n");
        }

        // process whitespace after COMMA
        if(token.getType() == 31) {
            rewriter.insertAfter(token, "\u001B[" + Color.Reset.getCode() + "m" + " ");
        }

        // process indentation of else
        if(token.getType() == 5) {
            int prev_index = token.getTokenIndex() - 1;
            boolean flag = false;
            if(prev_index >= 0) {
                Token prev_token = rewriter.getTokenStream().get(prev_index);
                if(prev_token.getType() != 28) {
                    rewriter.insertBefore(token, "\u001B[" + Color.Reset.getCode() + "m" + "\n" + getIndentWS());
                    flag = true;
                }
            }
            if(!flag) {
                rewriter.insertBefore(token, "\u001B[" + Color.Reset.getCode() + "m" + "\n" + getIndentWS());
            }
        }

        // process Left Brace
        processLeftBrace(token, node);

        // add a whitespace after ws_keywords
        addWSAfterWSKeywords(token);

        // add whitespaces between binary operands
        addWSBetweenBinaryOps(token, node);

        changeToken(node.getText(), token);
        if(changeFlag) {
            color_stack.pop();
        }
    }

    @Override
    public void exitProgram(SysYParser.ProgramContext ctx) {
        rewriter.replace(ctx.EOF().getSymbol(), "");
    }

    @Override
    public void enterStmt(SysYParser.StmtContext ctx) {
        color_stack.push(Color.White);
        Token start = ctx.getStart();
        int prev_index = start.getTokenIndex() - 1;

        if(checkElse(ctx)) {
            indent_level++;
        }

        if(checkIfElifWhileCases(ctx)) {
            indent_level++;
        }

        indentStmt(ctx);

        if(start.getType() != 27)
        {
            boolean check_elif_rbrace = (prev_index >= 0
                    && ((rewriter.getTokenStream().get(prev_index).getType() == 5
                    && start.getType() == 4) || rewriter.getTokenStream().get(prev_index).getType() == 28));
            if(!check_elif_rbrace) {
                rewriter.insertBefore(start, "\u001B[" + Color.Reset.getCode() + "m" + "\n");
            }
        }
    }

    @Override
    public void exitStmt(SysYParser.StmtContext ctx) {
        color_stack.pop();
        if(checkIfElifWhileCases(ctx)) {
            indent_level--;
        }
        if(checkElse(ctx)) {
            indent_level--;
        }
    }

    @Override
    public void enterBlock(SysYParser.BlockContext ctx) {
        Token start = ctx.getStart();
        Token end = ctx.getStop();
        ParserRuleContext parent = ctx.getParent();
        ParserRuleContext ancestor = parent.getParent();
        boolean check_while_if = (parent.getRuleIndex() == SysYParser.RULE_stmt
                && ancestor.getRuleIndex() == SysYParser.RULE_stmt);
        if(!check_while_if) {
            rewriter.insertBefore(start, getIndentWS());
        }
        rewriter.insertBefore(end, getIndentWS());
        indent_level++;
    }

    @Override
    public void exitBlock(SysYParser.BlockContext ctx) {
        indent_level--;
        Token end = ctx.getStop();
        rewriter.insertBefore(end, "\u001B[" + Color.Reset.getCode() + "m" + "\n");
    }

    @Override
    public void enterDecl(SysYParser.DeclContext ctx) {
        last_effect = Optional.of(Effect.Underlined);
        effect_stack.push(Effect.Underlined);
        color_stack.push(Color.LightMagenta);
        Token start = ctx.getStart();
        rewriter.insertBefore(start, "\u001B[" + Color.Reset.getCode() + "m" + "\n" + getIndentWS());
    }

    @Override
    public void exitDecl(SysYParser.DeclContext ctx) {
        color_stack.pop();
        effect_stack.pop();
    }

    @Override
    public void enterFunc_name(SysYParser.Func_nameContext ctx) { color_stack.push(Color.LightYellow); }

    @Override
    public void exitFunc_name(SysYParser.Func_nameContext ctx) { color_stack.pop(); }

    @Override
    public void enterFunc_def(SysYParser.Func_defContext ctx) {
        Token start = ctx.getStart();
        int prev_index = start.getTokenIndex() - 1;
        if(prev_index >= 0) {
            rewriter.insertBefore(start, "\u001B[" + Color.Reset.getCode() + "m" + "\n");
        }
    }

    public String getFormattedText() {
        // delete consecutive white spaces
        String modified_text = rewriter.getText();
        String regex = "^\u001B\\[[;\\d]*m\\s*\\n+";
        modified_text = modified_text.replaceAll(regex, "");
        return modified_text;
    }

    private Color getColor() {
        return color_stack.isEmpty() ? Color.ResetFore : color_stack.peek();
    }
    private Optional<Effect> getEffect() { return effect_stack.isEmpty()? Optional.empty() : Optional.of(effect_stack.peek()); }
    private Optional<Effect> getResetEffect() { return last_effect; }
    public Color getBraceColor(int number) {
        return BRACE_COLOR_MAP.get((number % BRACE_COLOR_MAP.size()));
    }
    public int getBraceColorNumber() { return BRACE_COLOR_MAP.size(); }
    private void changeToken(String text, Token token) {
        Color color = getColor();
        String new_text = "\u001B[" + color.getCode() + "m";
        if(getEffect().isPresent()) {
            new_text += "\u001B[" + getEffect().get().getCode() + "m";
        }
        new_text += text;
        if(getResetEffect().isPresent()) {
            new_text += "\u001B[" + getResetEffect().get().getResetCode() + "m";
        }
        rewriter.replace(token, new_text);
    }
    private void processLeftBrace(Token token, TerminalNode node) {
        if(token.getType() != 27)
            return;
        int prev_index = token.getTokenIndex() - 1;
        if(prev_index < 0) { return; }
        Token prev_token = rewriter.getTokenStream().get(prev_index);
        if(prev_token == null) { return; }
        int prev_type = prev_token.getType();

        if(prev_type == 32 ||
                (prev_type == 27
                        && ((ParserRuleContext) node.getParent()).getRuleIndex() != SysYParser.RULE_init_val
                        && ((ParserRuleContext) node.getParent()).getRuleIndex() != SysYParser.RULE_const_init_val)
                || prev_type == 28) {
            // Type 32 represents SEMICOLON, see SysYParser.tokens
            rewriter.insertBefore(token, "\u001B[" + Color.Reset.getCode() + "m" + "\n");
            // if prev is an/a ASSIGN/COMMA, no need to add ws before
        } else if(!prev_token.getText().endsWith(" ") && prev_type != 15
                && prev_type != 31){
            ParserRuleContext parent = (ParserRuleContext) node.getParent();
            if(parent.getRuleIndex() == SysYParser.RULE_block)
            rewriter.insertBefore(token, "\u001B[" + Color.Reset.getCode() + "m" + " ");
        }
    }

    // add a whitespace after ws_keywords
    private void addWSAfterWSKeywords(Token token) {
        if(Arrays.asList(ws_keywords).contains(token.getText())) {
            Token next_token = rewriter.getTokenStream().get(token.getTokenIndex() + 1);
            boolean check_semi = token.getType() == 9 && next_token.getType() == 32;
            if((!check_semi && next_token != null) && !next_token.getText().startsWith(" ")) {
                boolean single_else_flag = (token.getType() == 5 && next_token.getType() != 4);
                if(!single_else_flag) {
                    rewriter.insertAfter(token, "\u001B[" + Color.Reset.getCode() + "m" + " ");
                }
            }
        }
    }

    // Check if stmt is followed by else without braces
    // True if yes, false otherwise
    private boolean checkElse(SysYParser.StmtContext ctx) {
        Token token = ctx.getStart();
        int start_type = token.getType();
        int prev_index = token.getTokenIndex() - 1;
        if(prev_index >= 0) {
            Token prev_token = rewriter.getTokenStream().get(prev_index);
            return start_type != 4 && start_type != 27 && prev_token.getType() == 5;
        }
        return false;
    }

    // Check if stmt is followed by if, else if or while without braces
    // True if yes, false otherwise
    private boolean checkIfElifWhileCases(SysYParser.StmtContext ctx) {
        Token token = ctx.getStart();
        int prev_index = token.getTokenIndex() - 1;
        if(prev_index >= 0) {
            Token prev_token = rewriter.getTokenStream().get(prev_index);
            int prev_type = prev_token.getType();
            if(prev_type == 26 && token.getType() != 27) {
                ParserRuleContext parent = ctx.getParent();
                int parent_start_type = parent.getStart().getType();
                return parent.getRuleIndex() == SysYParser.RULE_stmt && (parent_start_type == 4 || parent_start_type == 6);
            }
        }
        return false;
    }

    // add whitespaces between binary operands
    private void addWSBetweenBinaryOps(Token token, TerminalNode node) {
        // process unary operands
        ParserRuleContext parent = (ParserRuleContext) node.getParent();
        if(parent.getRuleIndex() == SysYParser.RULE_unary_op) {
            return;
        }

        if(Arrays.asList(binary_operands).contains(token.getText())) {
            Token next_token = rewriter.getTokenStream().get(token.getTokenIndex() + 1);
            Token prev_token = rewriter.getTokenStream().get(token.getTokenIndex() - 1);
            if(next_token != null && !next_token.getText().startsWith(" ")) {
                rewriter.insertAfter(token, "\u001B[" + Color.Reset.getCode() + "m" + " ");
            }
            if(prev_token != null && !prev_token.getText().endsWith(" ")) {
                rewriter.insertBefore(token, "\u001B[" + Color.Reset.getCode() + "m" + " ");
            }
        }
    }
    private String getIndentWS() {
        String reset = "\u001B[" + Color.Reset.getCode() + "m";
        String ws = " ".repeat(indent_level * 4);
        return reset + ws;
    }

    private void indentStmt(SysYParser.StmtContext ctx) {
        Token start = ctx.getStart();
        int prev_index = start.getTokenIndex() - 1;
        if(ctx.getChild(0) instanceof SysYParser.BlockContext) {
            return;
        }
        // process if indentation in else if
        if(start.getType() == 4) {
            if(prev_index >= 0) {
                Token prev_token = rewriter.getTokenStream().get(prev_index);
                if(prev_token.getType() == 5) {
                    return;
                }
            }
        }
        if(prev_index >= 0) {
           Token prev_token = rewriter.getTokenStream().get(prev_index);
           if(prev_token.getType() == 28) {
               rewriter.insertBefore(start, "\u001B[" + Color.Reset.getCode() + "m" + "\n" + getIndentWS());
               return;
           }
        }
        rewriter.insertBefore(start, getIndentWS());
    }
}
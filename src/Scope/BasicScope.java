package Scope;

import Symbol.Symbol;

import java.util.HashMap;

public class BasicScope implements Scope{
    private String scopeName;
    private final HashMap<String, Symbol> symbols = new HashMap<String, Symbol>();
    private final Scope enclosingScope;

    public BasicScope(String name, Scope enclosingScope) {
        this.scopeName = name;
        this.enclosingScope = enclosingScope;
    }

    @Override
    public String getName() {
        return scopeName;
    }

    @Override
    public void setName(String name) {
        scopeName = name;
    }

    @Override
    public void define(Symbol symbol) {
        if(symbols.containsKey(symbol.getName())) {
            // TODO: handler redefine error
        } else {
            symbols.put(symbol.getName(), symbol);
        }
    }

    @Override
    public Symbol resolve(String name) {
        Symbol result = symbols.get(name);
        if(result == null) {
           if(enclosingScope == null) {
               // TODO: Report symbol is not defined
               return null;
           }
           return enclosingScope.resolve(name);
        }
        return result;
    }

    @Override
    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public HashMap<String, Symbol> getSymbols() {
        return symbols;
    }
}

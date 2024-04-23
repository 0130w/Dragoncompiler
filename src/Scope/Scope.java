package Scope;

import Symbol.Symbol;

import java.util.HashMap;

public interface Scope {
    String getName();
    void setName(String name);
    void define(Symbol symbol);
    Symbol resolve(String name);
    Scope getEnclosingScope();
    HashMap<String, Symbol> getSymbols();
}

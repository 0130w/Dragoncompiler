parser grammar SysYParser;

options {
    tokenVocab = SysYLexer;
}

program:
    compUnit EOF
    ;

compUnit : ( decl | func_def )+;

decl : const_decl
    | var_decl
    ;

const_decl : CONST b_type const_def (COMMA const_def)* SEMICOLON ;

b_type : INT;

const_def : IDENT (L_BRACKT const_exp R_BRACKT)* ASSIGN const_init_val;

const_init_val : const_exp
            | L_BRACE (const_init_val (COMMA const_init_val)*)? R_BRACE
            ;

var_decl : b_type var_def (COMMA var_def)* SEMICOLON;

var_def : IDENT (L_BRACKT const_exp R_BRACKT)*
    | IDENT (L_BRACKT const_exp R_BRACKT)* ASSIGN init_val
    ;

init_val : exp
    | L_BRACE (init_val (COMMA init_val)*)? R_BRACE
    ;

func_name: IDENT;

func_def : func_type func_name L_PAREN (func_f_params)? R_PAREN block
    ;

func_type : VOID
        | INT
        ;

func_f_params : func_f_param (COMMA func_f_param)*;

func_f_param : b_type IDENT (L_BRACKT R_BRACKT (L_BRACKT exp R_BRACKT)*)?;

block : L_BRACE (block_item)* R_BRACE;

block_item : decl
        | stmt
        ;

stmt : l_val ASSIGN exp SEMICOLON
    | exp? SEMICOLON
    | block
    | IF L_PAREN cond R_PAREN stmt (ELSE stmt)?
    | WHILE L_PAREN cond R_PAREN stmt
    | BREAK SEMICOLON
    | CONTINUE SEMICOLON
    | RETURN exp? SEMICOLON
    ;

exp : L_PAREN exp R_PAREN
    | l_val
    | number
    | func_name L_PAREN func_r_params? R_PAREN
    | unary_op exp
    | exp (MUL | DIV | MOD) exp
    | exp (PLUS | MINUS) exp
    ;

cond : exp
    | cond (LT | GT | LE | GE) cond
    | cond (EQ | NEQ) cond
    | cond AND cond
    | cond OR cond
    ;

l_val : IDENT (L_BRACKT exp R_BRACKT)*;

number : INTEGER_CONST;

unary_op : PLUS
    | MINUS
    | NOT
    ;

func_r_params : param (COMMA param)*;

param: exp;

const_exp: exp;
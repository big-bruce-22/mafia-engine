package mafia.engine.expression.lexer;


public record Token(Type type, String value) {

    public static final Token END = new Token(Type.END, "");

    public enum Type {
        NUMBER,
        IDENTIFIER,

        DOT,

        OPEN_PARENTHESIS,
        CLOSE_PARENTHESIS,

        LOGICAL_OPERATOR,       // and, or, not, &&, ||, !
        ARITHMETIC_OPERATOR,    // +, -, *, /
        RELATIONAL_OPERATOR,             // ==, !=, <, >, <=, >=
        KEYWORD,                // is, in

        SEPARATOR,              // ,
        FUNCTION,               // count
        CALL,                   

        END
    }    

    public Token(Type type, char c) {
        this(type, String.valueOf(c));
    }

    public String toString() {
        return "type: %s value: '%s'".formatted(type, value);
    }
}


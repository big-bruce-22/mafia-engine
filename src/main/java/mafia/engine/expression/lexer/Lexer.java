package mafia.engine.expression.lexer;

import static java.lang.Character.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import mafia.engine.expression.lexer.Token.Type;
import mafia.engine.expression.parser.Parser;

public class Lexer {

    private static final Pattern NUMBER = Pattern.compile("\\d+(\\.\\d+)?");
    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern ARITHMETIC = Pattern.compile("\\+|-|\\*|/");
    private static final Pattern LOGICAL = Pattern.compile("&&|\\|\\||!|\\b(and|or|not)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RELATIONAL = Pattern.compile("==|!=|<=|>=|<|>");
    private static final Pattern KEYWORD = Pattern.compile("\\b(is in|is not|is)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern FUNCTIONS;
    
    private static final String[] functionNames = {"count"};

    static {
        String s = Arrays.stream(functionNames).collect(Collectors.joining("|"));
        FUNCTIONS = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
    }

    public static void main(String[] args) {
        var lexer = new Lexer();
        var parser = new Parser();

        System.out.println(parser.parse(lexer.tokenize(
            """
                count(players, player.state is Alive and player.alignment is Good) + 
                count(players, player.state is Alive and player.alignment is Neutral) < 2
            """
        )));
    }
 
    public Queue<Token> tokenize(String s) {
        Queue<Token> tokens = new LinkedList<>();

        int i = 0;
        int length = s.length();

        while (i < length) {
            char c = s.charAt(i);

            // Skip whitespace
            if (isWhitespace(c)) {
                i++;
                continue;
            }

            // Single-character tokens
            switch (c) {
                case '(' -> {
                    tokens.add(new Token(Type.OPEN_PARENTHESIS, c));
                    i++;
                    continue;
                }
                case ')' -> {
                    tokens.add(new Token(Type.CLOSE_PARENTHESIS, c));
                    i++;
                    continue;
                }
                case '.' -> {
                    tokens.add(new Token(Type.DOT, c));
                    i++;
                    continue;
                }
                case ',' -> {
                    tokens.add(new Token(Type.SEPARATOR, c));
                    i++;
                    continue;
                }
            }

            // try regex-based tokens
            String remaining = s.substring(i);

            // FUNCTIONS
            var matcher = FUNCTIONS.matcher(remaining);
            if (matcher.lookingAt()) {
                String value = matcher.group();
                tokens.add(new Token(Type.FUNCTION, value));
                i += value.length();
                continue;
            }

            // LOGICAL operators
            matcher = ARITHMETIC.matcher(remaining);
            if (matcher.lookingAt()) {
                String value = matcher.group();
                tokens.add(new Token(Type.ARITHMETIC_OPERATOR, value));
                i += value.length();
                continue;
            }

            // LOGICAL operators
            matcher = LOGICAL.matcher(remaining);
            if (matcher.lookingAt()) {
                String value = matcher.group();
                tokens.add(new Token(Type.LOGICAL_OPERATOR, value));
                i += value.length();
                continue;
            }

            // RELATIONAL operators
            matcher = RELATIONAL.matcher(remaining);
            if (matcher.lookingAt()) {
                String value = matcher.group();
                tokens.add(new Token(Type.RELATIONAL_OPERATOR, value));
                i += value.length();
                continue;
            }

            // KEYWORDS
            matcher = KEYWORD.matcher(remaining);
            if (matcher.lookingAt()) {
                String value = matcher.group();
                tokens.add(new Token(Type.KEYWORD, value));
                i += value.length();
                continue;
            }

            // NUMBER
            matcher = NUMBER.matcher(remaining);
            if (matcher.lookingAt()) {
                String value = matcher.group();
                tokens.add(new Token(Type.NUMBER, value));
                i += value.length();
                continue;
            }

            // IDENTIFIER
            matcher = IDENTIFIER.matcher(remaining);
            if (matcher.lookingAt()) {
                String value = matcher.group();
                tokens.add(new Token(Type.IDENTIFIER, value));
                i += value.length();
                continue;
            }

            throw new IllegalArgumentException(
                "Unexpected character at position " + i + ": '" + c + "'"
            );
        }

        tokens.add(Token.END);
        return tokens;
    }   
}

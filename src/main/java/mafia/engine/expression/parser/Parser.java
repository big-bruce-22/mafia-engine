package mafia.engine.expression.parser;

import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;

import mafia.engine.expression.lexer.Token;
import mafia.engine.expression.lexer.Token.Type;

public class Parser {
    
    public Node parse(Queue<Token> tokens) {
        return parse(tokens, 0.0f, false);
    }

    private Node parse(final Queue<Token> tokens, final float minBindingPower, final boolean requireClosingParenthesis) {
        if (tokens.peek() == null || tokens.peek() == Token.END) {
            return null;    
        }
        var token = tokens.peek();
        var type = token.type();
        var value = token.value();
        Node lhs = switch (type) {
            case NUMBER, IDENTIFIER -> new Node(tokens.poll());
            case OPEN_PARENTHESIS -> parseParenthesisBody(tokens);
            case FUNCTION -> parseFunctionBody(tokens,token);
            // unary operators
            case ARITHMETIC_OPERATOR -> {
                var condition = value.equals("-");
                yield parseUnaryRight(tokens, requireClosingParenthesis, value, condition, type);
            }
            case LOGICAL_OPERATOR -> {
                var condition = value.equals("!") || value.equalsIgnoreCase("not");
                yield parseUnaryRight(tokens, requireClosingParenthesis, value, condition, type);
            }
            case END -> null;
            default -> throw unexpectedToken(tokens);
        };

        while (true) {
            if (tokens.peek() == null || tokens.peek() == Token.END) {
                break;
            }

            value = tokens.peek().value();
            type = tokens.peek().type();

            if (type == Type.END || type == Type.SEPARATOR) {
                break;
            }

            if (type == Type.CLOSE_PARENTHESIS) {
                if (requireClosingParenthesis) {
                    break;
                }
                throw unexpectedToken(tokens);
            }

            var operator = switch (type) {
                case DOT,
                    LOGICAL_OPERATOR,
                    ARITHMETIC_OPERATOR,
                    RELATIONAL_OPERATOR,
                    KEYWORD -> new Node(type, value);
                default -> throw unexpectedToken(tokens);
            };

            var bindingPower = getBindingPower(operator.value());
            if (bindingPower.getLeft() < minBindingPower) {
                break;
            }

            tokens.poll();
            var rhs = parse(tokens, bindingPower.getRight(), requireClosingParenthesis);

            if (rhs == null) {
                throw new IllegalStateException("right hand side of (" + operator.value() + ") is null");
            }

            operator.left(lhs);
            operator.right(rhs);
            lhs = operator;
        }
        return lhs;
    }

    private Node parseFunctionBody(final Queue<Token> tokens, Token token) {
        var functionNode = new Node(token);
        if (tokens.peek() == null || tokens.peek().type() == Type.END) {
            throw new IllegalStateException("Missing function body: " + functionNode.value());
        }

        if (tokens.peek().type() != Type.FUNCTION) {
            throw unexpectedToken(tokens);
        }

        var callee = new Node(tokens.poll());
        return parseCall(callee, tokens);
    }

    private Node parseParenthesisBody(final Queue<Token> tokens) {
        tokens.poll();
        var lhs = parse(tokens, 0.0f, true);
        var close = tokens.poll();
        if (close == null || close.type() != Type.CLOSE_PARENTHESIS) {
            throw new IllegalStateException("Expected: ')'");
        }
        return lhs;
    }

    private Node parseCall(Node callee, Queue<Token> tokens) {
        tokens.poll(); // '('

        Node args = null;

        if (tokens.peek().type() != Type.CLOSE_PARENTHESIS) {
            args = parse(tokens, 0.0f, false);

            while (tokens.peek().type() == Type.SEPARATOR) {
                tokens.poll();
                args = new Node(
                    Type.SEPARATOR,
                    ",",
                    args,
                    parse(tokens, 0.0f, true)
                );
            }
        }

        if (tokens.poll().type() != Type.CLOSE_PARENTHESIS) {
            throw new IllegalStateException("Expected ')'");
        }

        return new Node(Type.CALL, "call", callee, args);
    }

    private Node parseUnaryRight(
        Queue<Token> tokens,
        boolean requireClosingParenthesis,
        String value,
        boolean condition,
        Type type
    ) {
        if (condition) {
            tokens.poll();
            var rbp = getBindingPower(value).getRight();
            var rhs = parse(tokens, rbp, requireClosingParenthesis);
            var op = new Node(type, value);
            op.right(rhs);
            return op;
        }
        throw unexpectedToken(tokens);
    }

    private Pair<Float, Float> getBindingPower(String value) {
        return switch (value.toLowerCase()) {
            // postfix
            case "." -> Pair.of(100.0f, 100.1f);

            // arithmetic
            case "*", "/" -> Pair.of(60.0f, 60.1f);
            case "+", "-" -> Pair.of(50.0f, 50.1f);

            // relational
            case "<", ">", "<=", ">=", "==", "!=", "is", "is in", "is not" ->
                Pair.of(40.0f, 40.1f);

            // prefix logical
            case "not", "!" ->
                Pair.of(35.0f, 35.1f);

            // logical infix
            case "and" -> Pair.of(30.0f, 30.1f);
            case "or"  -> Pair.of(20.0f, 20.1f);

            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    private IllegalStateException unexpectedToken(Queue<Token> tokens) {
        return new IllegalStateException("Unexpected token: " + tokens.peek().value() + " with type: " + tokens.peek().type());
    }
}

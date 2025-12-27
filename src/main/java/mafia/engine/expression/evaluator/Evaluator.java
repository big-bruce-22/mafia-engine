package mafia.engine.expression.evaluator;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import mafia.engine.expression.parser.Node;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;

public class Evaluator {
    
    private final FunctionsEvaluator functionsEvaluator = new FunctionsEvaluator(this);

    public EvaluationResult evaluate(Node node, Properties properties, String parentPropertyName) {
        if (node == null) {
            return new EvaluationResult(EvaluationType.VOID, null);
        }
        
        var value = node.value();
        var type = node.type();
        return switch (node.type()) {
            case IDENTIFIER             -> {
                if (properties.containsProperty(value)) {
                    yield inferValue(properties.getProperty(value));
                }
                yield new EvaluationResult(EvaluationType.LITERAL, value);
            }
            case FUNCTION               -> new EvaluationResult(EvaluationType.LITERAL, value);
            case NUMBER                 -> new EvaluationResult(EvaluationType.NUMBER, Float.valueOf(value));
            case CALL                   -> evaluateCall(node, properties, parentPropertyName);
            case DOT                    -> evaluateDot(node, properties, parentPropertyName);
            case ARITHMETIC_OPERATOR    -> evaluateArithmetic(value, node, properties, parentPropertyName);
            case LOGICAL_OPERATOR       -> evaluateLogical(value, node, properties, parentPropertyName);
            case RELATIONAL_OPERATOR    -> evaluateRelational(value, node, properties, parentPropertyName);
            case KEYWORD                -> evaluateKeyword(value, node, properties, parentPropertyName);
            default                     -> throw new IllegalStateException("Unexpected type: " + type);
        };
    }

    private EvaluationResult evaluateCall(Node node, Properties properties, String parentPropertyName) {
        Node callee = node.left();
        Node argsNode = node.right();

        Object result = functionsEvaluator.evaluateFunction(callee, argsNode, properties);

        return switch (result) {
            case Number _ -> new EvaluationResult(EvaluationType.NUMBER, result);
            case List<?> _ -> new EvaluationResult(EvaluationType.LIST, result);
            // default -> new EvaluationResult(EvaluationType.ANY, result);
            default -> throw new IllegalStateException(
                "Unexpected return type for call " + callee.value() + " : " + result.getClass()
            );
        };
    }

    private EvaluationResult evaluateDot(Node node, Properties properties, String parentPropertyName) {
        var left = evaluate(node.left(), properties, parentPropertyName);

        if (left.type() != EvaluationType.LIST && left.type() != EvaluationType.LITERAL) {
            throw new IllegalStateException("Left side of '.' must be an identifier");
        }

        if (left.result() instanceof PropertyHolder ph) {
            properties = ph.getProperties();
        }

        var propertyNode = node.right();
        var propertyName = propertyNode.value();
        
        if (left.type() == EvaluationType.LIST) {
            if (!propertyName.equals("size")) {
                throw new IllegalStateException("Only 'size' property is supported for lists");
            }
            var list = (List<?>) left.result();
            return new EvaluationResult(EvaluationType.NUMBER, Float.valueOf(list.size()));
        }

        if (!properties.containsProperty(propertyName)) {
            throw new IllegalStateException(
                "Property '%s' not found for %s".formatted(
                    propertyName,
                    properties.propertyName()
                )
            );
        }

        var raw = properties.getProperty(propertyName);

        return inferValue(raw);
    }

    private EvaluationResult inferValue(Object raw) {
        if (raw == null) {
            return new EvaluationResult(EvaluationType.LITERAL, null);            
        }
        return switch (raw) {
            case Number n   -> new EvaluationResult(EvaluationType.NUMBER, n.floatValue());
            case Boolean b  -> new EvaluationResult(EvaluationType.BOOLEAN, b);
            case List<?> l  -> new EvaluationResult(EvaluationType.LIST, l);
            case String s   -> new EvaluationResult(EvaluationType.LITERAL, s);
            default         -> new EvaluationResult(EvaluationType.LITERAL, raw);
        };
    }

    private EvaluationResult evaluateArithmetic(
        String value,
        Node node,
        Properties properties,
        String parentPropertyName
    ) {
        // Unary minus
        if (value.equals("-") && node.left() == null) {
            var right = evaluate(node.right(), properties, parentPropertyName);

            if (right.type() != EvaluationType.NUMBER) {
                throw new IllegalStateException("Unary '-' requires a number");
            }

            return new EvaluationResult(
                EvaluationType.NUMBER,
                -((Float) right.result())
            );
        }

        // Binary arithmetic
        var left = evaluate(node.left(), properties, parentPropertyName);
        var right = evaluate(node.right(), properties, parentPropertyName);

        if (left.type() != EvaluationType.NUMBER || right.type() != EvaluationType.NUMBER) {
            throw new IllegalStateException(
                "Arithmetic operator '" + value + "' requires numeric operands"
            );
        }

        float l = Float.valueOf(left.result().toString());
        float r = Float.valueOf(right.result().toString());

        return switch (value) {
            case "+" -> new EvaluationResult(EvaluationType.NUMBER, l + r);
            case "-" -> new EvaluationResult(EvaluationType.NUMBER, l - r);
            case "*" -> new EvaluationResult(EvaluationType.NUMBER, l * r);
            case "/" -> {
                if (r == 0f) {
                    throw new ArithmeticException("Division by zero");
                }
                yield new EvaluationResult(EvaluationType.NUMBER, l / r);
            }
            default -> throw new IllegalStateException("Unexpected arithmetic: " + value);
        };
    }

    private EvaluationResult evaluateLogical(
        String value,
        Node node,
        Properties properties,
        String parentPropertyName
    ) {
        var left = evaluate(node.left(), properties, parentPropertyName);
        requireBoolean(left);
        var b = switch (value.toLowerCase()) {
            case "&&", "and" -> {
                if (!(Boolean) left.result()) {
                    yield false;
                }

                var right = evaluate(node.right(), properties, parentPropertyName);
                requireBoolean(right);
                yield (Boolean) right.result();
            }
            case "||", "or" -> {
                if ((Boolean) left.result()) {
                    yield true;
                }

                var right = evaluate(node.right(), properties, parentPropertyName);
                requireBoolean(right);
                yield (Boolean) right.result();
            }
            case "!", "not" -> {
                var right = evaluate(node.right(), properties, parentPropertyName);
                requireBoolean(right);
                yield !(Boolean) right.result();
            }
            default -> throw new IllegalStateException("Unexpected logical: " + value);
        };
        return new EvaluationResult(EvaluationType.BOOLEAN, b);
    }

    private void requireBoolean(EvaluationResult r) {
        if (r.type() != EvaluationType.BOOLEAN) {
            throw new IllegalStateException("Expected boolean but got " + r.type());
        }
    }

    private EvaluationResult evaluateRelational(
        String value,
        Node node,
        Properties properties,
        String parentPropertyName
    ) {
        var left = evaluate(node.left(), properties, parentPropertyName);
        var right = evaluate(node.right(), properties, parentPropertyName);

        return switch (value) {
            case "==" -> new EvaluationResult(EvaluationType.BOOLEAN,
                    left.result().equals(right.result()));

            case "!=" -> new EvaluationResult(EvaluationType.BOOLEAN,
                    !left.result().equals(right.result()));

            case "<" -> compareNumbers(left, right, (a, b) -> a < b);
            case ">" -> compareNumbers(left, right, (a, b) -> a > b);
            case "<=" -> compareNumbers(left, right, (a, b) -> a <= b);
            case ">=" -> compareNumbers(left, right, (a, b) -> a >= b);

            default -> throw new IllegalStateException("Unexpected relational: " + value);
        };
    }

    private EvaluationResult compareNumbers(
        EvaluationResult l,
        EvaluationResult r,
        BiPredicate<Float, Float> op
    ) {
        if (l.type() != EvaluationType.NUMBER || r.type() != EvaluationType.NUMBER) {
            throw new IllegalStateException("Numeric comparison required");
        }
        return new EvaluationResult(
            EvaluationType.BOOLEAN,
            op.test(Float.valueOf(l.result().toString()), Float.valueOf(r.result().toString()))
        );
    }

    private EvaluationResult evaluateKeyword(
        String value, 
        Node node, 
        Properties properties, 
        String parentPropertyName
    ) {
        var left = evaluate(node.left(), properties, parentPropertyName);
        var right = evaluate(node.right(), properties, parentPropertyName);
        var b =  switch (value) {
            case "is in" -> {
                if (!(right.result() instanceof String s)) {
                    throw new IllegalStateException("'in' requires a list");
                }

                yield Arrays.stream(s.split(","))
                    .map(String::trim)
                    .anyMatch(v -> v.equals(left.result().toString()));
            }
            case "is"       ->   left.type() == right.type() && left.result().toString().equals(right.result().toString());
            case "is not"   -> !(left.type() == right.type() && left.result().toString().equals(right.result().toString()));
            default -> throw new IllegalStateException("Unexpected keyword: " + value);
        };
        return new EvaluationResult(EvaluationType.BOOLEAN, b);
    }
}

package mafia.engine.expression.evaluator;

import java.util.Collection;

import mafia.engine.expression.lexer.Token.Type;
import mafia.engine.expression.parser.Node;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;

public class FunctionsEvaluator {

    private final Evaluator evaluator;

    public FunctionsEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public Object evaluateFunction(Node callee, Node argsNode, Properties properties) {
        String functionName = callee.value().toString().toLowerCase();
        Node[] args = flattenArgs(argsNode);

        return switch (functionName) {
            case "count" -> count(args, properties);
            case "filter" -> filter(args, properties);
            default -> throw new IllegalArgumentException("Unknown function: " + functionName);
        };
    }

    private Collection<?> filter(Node[] args, Properties properties) {
        if (args.length != 2) {
            throw new IllegalArgumentException("filter() expects 2 arguments: list, condition");
        }

        Object listObj = evaluator.evaluate(args[0], properties, null).result();

        if (!(listObj instanceof Collection<?> list)) {
            throw new IllegalArgumentException(
                "First argument of filter() must be a collection, instead got " + listObj.getClass()
            );
        }

        Node conditionNode = args[1];
        Collection<Object> result = new java.util.ArrayList<>();
        for (Object item : list) {
            if (item instanceof PropertyHolder p) {
                Properties itemProps = p.getProperties();
                var name = itemProps.propertyName();
                Object condResult = evaluator.evaluate(conditionNode, itemProps, name).result();
                if (condResult instanceof Boolean b && b) {
                    result.add(item);
                }
            } else {
                throw new IllegalStateException(
                    "item " + item + " of type " + item.getClass() + "  does not contain any properties"
                );
            }
        }
        return result;
    }

    private int count(Node[] args, Properties properties) {
        if (args.length != 2) {
            throw new IllegalArgumentException("count() expects 2 arguments: list, condition");
        }

        Object listObj = evaluator.evaluate(args[0], properties, null).result();

        if (!(listObj instanceof Collection<?> list)) {
            throw new IllegalArgumentException(
                "First argument of count() must be a collection, instead got " + listObj.getClass()
            );
        }

        Node conditionNode = args[1];
        int counter = 0;
        for (Object item : list) {
            if (item instanceof PropertyHolder p) {
                Properties itemProps = p.getProperties();
                var name = itemProps.propertyName();
                Object condResult = evaluator.evaluate(conditionNode, itemProps, name).result();
                if (condResult instanceof Boolean b && b) {
                    counter++;
                }
            } else {
                throw new IllegalStateException(
                    "item " + item + " of type " + item.getClass() + "  does not contain any properties"
                );
            }
        }
        return counter;
    }

    private Node[] flattenArgs(Node argsNode) {
        if (argsNode == null) return new Node[0];

        
        Node curr = argsNode;
        int count = 1;
        for (; curr.type() == Type.SEPARATOR; count++) {
            curr = curr.right();
        }
    
        Node[] result = new Node[count];
        curr = argsNode;
        for (int i = 0; i < count; i++) {
            if (curr.type() == Type.SEPARATOR) {
                result[i] = curr.left();
                curr = curr.right();
            } else {
                result[i] = curr;
            }
        }

        return result;
    }

}

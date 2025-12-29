package mafia.engine.expression.evaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            case "contains" -> contains(args, properties);
            default -> 
                throw new IllegalArgumentException("Unknown function: " + functionName);
        };
    }

    private boolean contains(Node[] args, Properties properties) {
        if (args.length != 2) {
            throw new ArgumentMismatchException(
                "contains", 
                "list", "condition"
            );
        }

        var listObj = evaluator.evaluate(
                args[0], 
                properties, 
                null
            ).result();

        if (!(listObj instanceof Collection<?> list)) {
            throw new ArgumentMismatchException(
                "contains", 
                List.class, 
                1, 
                listObj
            );
        }

        var conditionNode = args[1];
        for (var item : list) {
            if (item instanceof PropertyHolder p) {
                var itemProps = p.getProperties();
                var name = itemProps.propertyName();
                var condResult = evaluator.evaluate(
                        conditionNode, 
                        itemProps, 
                        name
                    )
                    .result();
                if (condResult instanceof Boolean b && b) {
                    return true;
                }
            } else {
                itemNonPropertyHolder(item);
            }
        }
        return false;
    }

    private Collection<?> filter(Node[] args, Properties properties) {
        if (args.length != 2) {
            throw new ArgumentMismatchException(
                "filter", 
                "list", "condition"
            );
        }

        var listObj = evaluator.evaluate(
                args[0], 
                properties, 
                null
            ).result();

        if (!(listObj instanceof Collection<?> list)) {
            throw new ArgumentMismatchException(
                "filter", 
                List.class, 
                1, 
                listObj
            );
        }
        
        var conditionNode = args[1];
        var result = new ArrayList<Object>();
        for (var item : list) {
            if (item instanceof PropertyHolder p) {
                var itemProps = p.getProperties();
                var name = itemProps.propertyName();
                var condResult = evaluator.evaluate(
                        conditionNode, 
                        itemProps, 
                        name
                    )
                    .result();
                if (condResult instanceof Boolean b && b) {
                    result.add(item);
                }
            } else {
                itemNonPropertyHolder(item);
            }
        }
        return result;
    }

    private int count(Node[] args, Properties properties) {
        if (args.length != 2) {
            throw new ArgumentMismatchException(
                "count",
                "list", "condition"
            );
        }

        var listObj = evaluator.evaluate(
                args[0], 
                properties, 
                null
            ).result();

        if (!(listObj instanceof Collection<?> list)) {
            throw new ArgumentMismatchException(
                "count", 
                List.class, 
                1, 
                listObj
            );
        }

        if (list.isEmpty()) {
            return 0;
        }

        var conditionNode = args[1];
        var counter = 0;
        for (var item : list) {
            if (item instanceof PropertyHolder p) {
                var itemProps = p.getProperties();
                var name = itemProps.propertyName();
                var condResult = evaluator.evaluate(
                        conditionNode, 
                        itemProps, 
                        name
                    )
                    .result();
                if (condResult instanceof Boolean b && b) {
                    counter++;
                }
            } else {
                itemNonPropertyHolder(item);
            }
        }
        return counter;
    }

    private void itemNonPropertyHolder(Object item) {
        throw new IllegalStateException(
            "item " + item + " of type " + item.getClass() + "  does not contain any properties"
        );
    }

    private Node[] flattenArgs(Node argsNode) {
        if (argsNode == null) {
            return new Node[0];
        }
    
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

package mafia.engine.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mafia.engine.expression.evaluator.EvaluationResult;
import mafia.engine.expression.evaluator.Evaluator;
import mafia.engine.expression.lexer.Lexer;
import mafia.engine.expression.parser.Node;
import mafia.engine.expression.parser.Parser;
import mafia.engine.property.Properties;

public class ExpressionEngine {
    
    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();
    private final Evaluator evaluator = new Evaluator();

    private Map<String, Node> cache = new HashMap<>();

    public void loadExpressions(List<String> expressions) {
        for (var expr : expressions) {
            cache.put(expr, parse(expr));
        }
    }

    public EvaluationResult evalaute(String expression, Properties properties) {
        if (!cache.containsKey(expression)) {
            cache.put(expression, parse(expression));
        }
        
        return evaluator.evaluate(cache.get(expression), properties, properties.propertyName());
    }

    private Node parse(String s) {
        return parser.parse(lexer.tokenize(s));
    }
}

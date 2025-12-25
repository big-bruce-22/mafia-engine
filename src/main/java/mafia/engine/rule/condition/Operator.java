package mafia.engine.rule.condition;

import java.util.Arrays;

public enum Operator {
    AND("and", "&", "&&"),
    OR("or", "|", "||"),
    NOT("not", "!"),
    IS("is", "==", "=");

    private final String operator;
    private final String[] symbolicRepresentations;

    Operator(String operator, String... symbolicRepresentations) {
        this.operator = operator;
        this.symbolicRepresentations = symbolicRepresentations;
    }

    public static Operator parse(String s) {
        if (s == null) return null;

        String input = s.toLowerCase();

        for (var op : values()) {
            if (op.operator.equals(input)) {
                return op;
            }

            if (Arrays.stream(op.symbolicRepresentations)
                      .anyMatch(sym -> sym.equals(input))) {
                return op;
            }
        }

        return null;
    }
}

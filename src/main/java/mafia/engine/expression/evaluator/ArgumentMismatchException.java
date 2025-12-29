package mafia.engine.expression.evaluator;

public class ArgumentMismatchException extends RuntimeException {

    public ArgumentMismatchException(String message) {
        super(message);
    }

    public ArgumentMismatchException(String functionName, Class<?> clazz, int argumentCount, Object mismatchedArgument) {
        super(
            "%s argument of %s() must be of type %s, instead got %s".formatted(
                ordinal(argumentCount),
                functionName,
                clazz.getSimpleName(),
                mismatchedArgument.getClass().getSimpleName()
            )
        );
    }
    
    public ArgumentMismatchException(String functionName, String... arguments) {
        super(
            "%s() expects %d arguments: %s".formatted(
                functionName,
                arguments.length,
                String.join(", ", arguments)
            )
        );
    }

    private static String ordinal(int n) {
        int mod100 = n % 100;
        int mod10 = n % 10;

        if (mod100 >= 11 && mod100 <= 13) {
            return n + "th";
        }

        return switch (mod10) {
            case 1 -> n + "st";
            case 2 -> n + "nd";
            case 3 -> n + "rd";
            default -> n + "th";
        };
    }
}

package mafia.engine.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class providing common operations for working with Java Streams and Collections.
 * <p>
 * This class offers a variety of static methods to simplify filtering, mapping, collecting,
 * and searching within collections using the Stream API. It also provides convenient methods
 * for exception handling and index finding based on predicates.
 * </p>
 * <p>
 * All methods are static and the class cannot be instantiated.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Find elements in collections with optional exception throwing or default values</li>
 *   <li>Map and flatMap collections to lists or arrays</li>
 *   <li>Filter collections and obtain results as lists or streams</li>
 *   <li>Combine mapping and filtering operations</li>
 *   <li>Collect mapped or flat-mapped results using custom collectors</li>
 *   <li>Find the index of elements matching a predicate</li>
 *   <li>Filter and sort collections in a single operation</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * <pre>
 *     List&lt;String&gt; names = List.of("Alice", "Bob", "Charlie");
 *     String firstWithB = StreamUtils.findOrElse(names, s -&gt; s.startsWith("B"), "Default");
 * </pre>
 * </p>
 *
 * @since 1.0
 */
public final class StreamUtils {

    private StreamUtils() {}

    /**
     * Creates a map by associating elements from two lists, where each element from the first list
     * is mapped to the corresponding element from the second list by index.
     *
     * @param <K> the type of keys in the resulting map
     * @param <V> the type of values in the resulting map
     * @param <M> the type of map to return (must extend Map<T, R>)
     * @param col1 the list of keys
     * @param col2 the list of values
     * @return a map associating each element of {@code col1} with the corresponding element of {@code col2}
     * @throws IllegalStateException if the sizes of {@code col1} and {@code col2} do not match
     */
    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<K, V>> M mapEach(List<K> col1, List<V> col2) {
        if (col1.size() != col2.size()) {
            throw new IllegalStateException("Both collection size must be the same");
        }

        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < col1.size(); i++) {
            map.put(col1.get(i), col2.get(i));
        }
        return (M) map;
    }

    /**
     * Returns the first element in the given collection that matches the specified condition,
     * or throws the provided exception if no such element is found.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .filter(condition)
     *         .findFirst()
     *         .orElseThrow(exceptionFunction);
     * </pre>
     * 
     * @param <T> the type of elements in the collection
     * @param col the collection to search
     * @param condition the predicate to apply to elements to determine a match
     * @param exceptionFunction a supplier that provides the exception to be thrown if no match is found
     * @return the first matching element in the collection
     * @throws RuntimeException if no element matches the condition
     */
    public static final <T> T findOrElseThrow(Collection<T> col, Predicate<T> condition, Supplier<? extends RuntimeException> exceptionFunction) {
        return col.stream()
            .filter(condition)
            .findFirst()
            .orElseThrow(exceptionFunction);
    }

    /**
     * Returns the first element in the given collection that matches the specified condition,
     * or the provided default value if no such element is found.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .filter(condition)
     *         .findFirst()
     *         .orElse(other);
     * </pre>
     *
     * @param <T>        the type of elements in the collection
     * @param col the collection to search
     * @param condition  the predicate to apply to elements to determine a match
     * @param other      the default value to return if no matching element is found
     * @return the first matching element, or {@code other} if none match
     */
    public static final <T> T findOrElse(Collection<T> col, Predicate<T> condition, T other) {
        return col.stream()
            .filter(condition)
            .findFirst()
            .orElse(other);
    }

    public static final <T> T findOrElse(T[] arr, Predicate<T> condition, T other) {
        return Stream.of(arr)
            .filter(condition)
            .findFirst()
            .orElse(other);
    }

    public static <T, R> R findMappedFirstOrDefault(Collection<T> collection,  Predicate<T> filter,  Function<T, R> mapper,  R defaultValue) {
        return collection.stream()
            .filter(filter)
            .map(mapper)
            .findFirst()
            .orElse(defaultValue);
    }

    /**
     * Checks if any element in the given collection matches the specified condition.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream().anyMatch(condition);
     * </pre>
     *
     * @param <T>        the type of elements in the collection
     * @param col the collection to search
     * @param condition  the predicate to apply to elements of the collection
     * @return {@code true} if any element matches the condition, {@code false} otherwise
     */
    public static final <T> boolean containsIn(Collection<T> col, Predicate<T> condition) {
        return col.stream().anyMatch(condition);    
    }

    /**
     * Throws a runtime exception generated by the provided exception function if any element
     * in the given collection matches the specified condition.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .filter(condition)
     *         .findFirst()
     *         .ifPresent(t -&gt; {
     *             throw exceptionFunction.apply(t);
     *         });
     * </pre>
     *
     * @param <T> the type of elements in the collection
     * @param col the collection to be checked
     * @param condition the predicate to test elements against
     * @param exceptionFunction a function that produces a {@link RuntimeException} for a matching element
     * @throws RuntimeException if any element in the collection matches the condition
     */
    public static final <T> void throwIfMatch(Collection<T> col, Predicate<T> condition, Function<T, ? extends RuntimeException> exceptionFunction) {
        col.stream()
            .filter(condition)
            .findFirst()
            .ifPresent(t -> {
                throw exceptionFunction.apply(t);
            });
    }

    /**
     * Transforms a collection of elements of type {@code T} into a list of elements of type {@code R}
     * by applying the provided mapping function to each element.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .map(mapper)
     *         .toList();
     * </pre>
     *
     * @param <T> the type of elements in the input collection
     * @param <R> the type of elements in the resulting list
     * @param col the input collection to be transformed
     * @param mapper the function to apply to each element in the collection
     * @return a list containing the results of applying the mapper function to the elements of the collection
     * @throws NullPointerException if either {@code collection} or {@code mapper} is {@code null}
     */
    public static final <T, R> List<R> mapToList(Collection<T> col, Function<T, R> mapper) {
        return col.stream()
            .map(mapper)
            .toList();
    }

    /**
     * Applies a mapping function to each element of the given collection, flattens the resulting streams into a single stream,
     * and collects the results into a list.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .flatMap(mapper)
     *         .toList();
     * </pre>
     *
     * @param <T> the type of elements in the input collection
     * @param <R> the type of elements in the resulting list
     * @param col the input collection whose elements are to be mapped and flattened
     * @param mapper a function to apply to each element, producing a stream of results
     * @return a list containing the results of applying the mapping function and flattening the streams
     */
    public static final <T, R> List<R> flatMapToList(Collection<T> col, Function<? super T, ? extends Stream<? extends R>> mapper) {
        return col.stream()
            .flatMap(mapper)
            .toList();
    }

    /**
     * Transforms a collection of elements into an array by applying a mapping function to each element.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .map(mapper)
     *         .toArray(generator);
     * </pre>
     *
     * @param <T>       the type of elements in the input collection
     * @param <R>       the type of elements in the resulting array
     * @param col the collection of elements to be mapped
     * @param mapper     the function to apply to each element in the collection
     * @param generator  a function which produces a new array of the desired type and length
     * @return           an array containing the results of applying the mapper function to the elements of the collection
     */
    public static final <T, R> R[] mapToArray(Collection<T> col, Function<T, R> mapper, IntFunction<R[]> generator) {
        return col.stream()
            .map(mapper)
            .toArray(generator);
    }

    /**
     * Filters the given collection using the specified predicate and returns a list of elements
     * that match the filter condition.
     *
     * @param <T>        the type of elements in the collection
     * @param col the collection to be filtered
     * @param filter     the predicate used to filter elements
     * @return a list containing elements from the collection that satisfy the predicate
     * 
     * @see StreamUtils#filterStream
     */
    public static final <T> List<T> filter(Collection<T> col, Predicate<? super T> filter) {
        return col.stream().filter(filter).toList();
    }

    /**
     * Returns a stream consisting of the elements of the given collection that match the provided predicate.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .filter(filter);
     * </pre>
     *
     * @param <T> the type of elements in the collection
     * @param col the collection whose elements are to be filtered
     * @param filter the predicate to apply to each element to determine if it should be included
     * @return a stream containing the elements that match the filter
     * @throws NullPointerException if the collection or filter is null
     */
    public static final <T> Stream<T> filterStream(Collection<T> col, Predicate<? super T> filter) {
        return col.stream()
            .filter(filter);
    }

    /**
     * Transforms each element of the given collection using the provided mapper function,
     * then filters the resulting elements using the specified predicate, and collects the results into a list.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .map(mapper)
     *         .filter(filter)
     *         .toList();
     * </pre>
     *
     * @param <T> the type of elements in the input collection
     * @param <R> the type of elements after mapping
     * @param col the input collection to be processed
     * @param mapper a function to apply to each element of the collection
     * @param filter a predicate to apply to each mapped element to determine if it should be included
     * @return a list containing the mapped and filtered elements
     */
    public static final <T, R> List<R> mapToListThenFilter(Collection<T> col, Function<T, R> mapper, Predicate<? super R> filter) {
        return col.stream()
            .map(mapper)
            .filter(filter)
            .toList();
    }

    /**
     * Filters the given collection using the specified predicate, then maps the filtered elements
     * using the provided mapping function, and collects the results into a list.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .filter(filter)
     *         .map(mapper)
     *         .toList();
     * </pre>
     *
     * @param <T> the type of elements in the input collection
     * @param <R> the type of elements in the resulting list
     * @param col the collection to be processed
     * @param filter the predicate used to filter elements
     * @param mapper the function used to map filtered elements
     * @return a list containing the mapped results of the filtered elements
     * @throws NullPointerException if any of the arguments are null
     */
    public static final <T, R> List<R> filterThenMapToList(Collection<T> col, Predicate<? super T> filter, Function<T, R> mapper) {
        return col.stream()
            .filter(filter)
            .map(mapper)
            .toList();
    }

    /**
     * Transforms the elements of the given collection using the provided mapping function,
     * then collects the results using the specified collector.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .map(mapper)
     *         .collect(collector);
     * </pre>
     *
     * @param <T>        the type of input elements in the collection
     * @param <R>        the type of elements after applying the mapping function
     * @param <A>        the intermediate accumulation type of the collector
     * @param <R2>       the final result type of the collector
     * @param col the input collection to be processed
     * @param mapper     the function to apply to each element of the collection
     * @param collector  the collector used to accumulate the mapped results
     * @return the result of collecting the mapped elements
     */
    public static final <T, R, A, R2> R2 mapAndCollect(Collection<T> col, Function<T, R> mapper, Collector<R, A, R2> collector) {
        return col.stream()
            .map(mapper)
            .collect(collector);
    }


    public static final <T, R, A, R2> R2 flatMapAndCollect(Collection<T> col, Function<? super T, ? extends Stream<? extends R>> mapper, Collector<R, A, R2> collector) {
        return col.stream()
            .flatMap(mapper)
            .collect(collector);
    }

    /**
     * Returns the index of the first element in the given collection that matches the specified condition.
     * If no element matches, returns the provided default value.
     * 
     *
     * @param <T> the type of elements in the collection
     * @param col the collection to search
     * @param condition the predicate to apply to elements to determine a match
     * @param defaultValue the value to return if no matching element is found
     * @return the index of the first matching element, or defaultValue if none match
     */
    public static final <T> int findIndex(Collection<T> col, Predicate<T> condition, int defaultValue) {
		int index = 0;
		for (T e : col) {
			if (condition.test(e)) {
				return index;
			}
			index++;
		}
		return defaultValue;
	}

    /**
     * Returns the index of the first element in the given collection that matches the specified predicate.
     * If no element matches, returns the provided default value.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .filter(predicate)
     *         .findFirst()
     *         .orElse(defaultValue);
     * </pre>
     *
     * @param col   the list to search through
     * @param predicate    an {@link IntPredicate} that tests element indices
     * @param defaultValue the value to return if no matching element is found
     * @return the index of the first matching element, or {@code defaultValue} if none match
     */
    public static final int findIndex(int size, IntPredicate predicate, int defaultValue) {
        return IntStream.range(0, size)
            .filter(predicate)
            .findFirst()
            .orElse(defaultValue);
    }

    /**
     * Filters and sorts the given collection based on the provided predicate and comparator,
     * returning the results as a new list.
     * 
     * @implNote
     * this is the same as
     * <pre>
     *     collection.stream()
     *         .filter(filter)
     *         .sorted(comparator)
     *         .toList();
     * </pre>
     *
     * @param <T>        the type of elements in the collection
     * @param col the collection to be filtered and sorted
     * @param filter     the predicate used to filter elements
     * @param comparator the comparator used to sort the filtered elements
     * @return a list containing the filtered and sorted elements
     */
    public static final <T> List<T> filterAndSortToList(Collection<T> col, Predicate<T> filter, Comparator<T> comparator) {
        return col.stream()
            .filter(filter)
            .sorted(comparator)
            .toList();
    }

    public static final <T> List<T> filterArrayToList(T[] arr, Predicate<T> filter) {
        return Arrays.stream(arr)
            .filter(filter)
            .toList();
    }

    public static final <T> List<T> sortToList(Collection<T> col, Comparator<T> comparator) {
        return col.stream()
            .sorted(comparator)
            .toList();
    }

    /**
     * Sorts the given collection and returns it as a new list.
     * 
     * Impl: collection.stream().sorted().toList();
     * @param <T>
     * @param col
     * @return
     */
    public static final <T> List<T> sort(Collection<T> col) {
        return col.stream().sorted().toList();
    }

    public static final <T, C extends Collection<T>> C sortTo(Collection<T> col, Comparator<T> comparator, Supplier<C> collectionFactory) {
        return col.stream()
            .sorted(comparator)
            .collect(Collectors.toCollection(collectionFactory));
    }

    public static final <T> boolean anyMatch(Collection<T> col, Predicate<T> condition) {
        return col.stream().anyMatch(condition);
    }

    public static final <T> List<T> distinct(Collection<T> col) {
        return col.stream().distinct().toList();
    }
}

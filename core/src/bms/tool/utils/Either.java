package bms.tool.utils;

import java.util.function.Function;

public class Either<T, U> {
    private final boolean isLeft;
    private final T left;
    private final U right;

    private Either(T left, U right) {
        this.isLeft = left != null;
        this.left = left;
        this.right = right;
    }

    public static <T, U> Either<T, U> of(T left, U right) {
        if (left != null && right != null) {
            throw new IllegalArgumentException();
        }
        return new Either<>(left, right);
    }

    public <K, V> Either<K, V> apply(Function<T, K> onLeft, Function<U, V> onRight) {
        return isLeft
                ? Either.of(onLeft.apply(left), null)
                : Either.of(null, onRight.apply(right));
    }

    public static Either<Integer, String> parseInteger(String maybeInteger) {
        try {
            Integer r = Integer.parseInt(maybeInteger);
            return Either.of(r, null);
        } catch (NumberFormatException e) {
            // Do nothing
        }
        return Either.of(null, maybeInteger);
    }

    public T getLeft() {
        return left;
    }

    public U getRight() {
        return right;
    }

    public static <T> T unwrap(Either<T, T> either) {
        return either.isLeft ? either.left : either.right;
    }

    public boolean isLeft() {
        return isLeft;
    }
}

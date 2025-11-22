package bms.tool.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.*;

public final class Pair<K, V> {
	private final K first;
	private final V second;

	public static <K, V> Pair<K, V> of(K first, V second) {
		return new Pair<>(first, second);
	}

	public Pair(K first, V second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		return "Pair(first=" + this.getFirst() + ", second=" + this.getSecond() + ")";
	}

	public <U, P> Pair<U, P> apply(Function<K, U> onFirst, Function<V, P> onSecond) {
		return of(onFirst.apply(this.first), onSecond.apply(this.second));
	}

	public <U, P> Pair<U, P> apply(Function<Pair<K, V>, Pair<U, P>> transfer) {
		return transfer.apply(this);
	}

	public <U> U apply(BiFunction<K, V, U> transfer) {
		return transfer.apply(this.getFirst(), this.getSecond());
	}

	public <U> U partiallyApplyFirst(Function<K, U> onFirst) {
		return onFirst.apply(this.first);
	}

	public <U> Pair<U, V> applyFirstKeepSecond(Function<K, U> onFirst) {
		return of(onFirst.apply(this.first), second);
	}

	public <P> P partiallyApplySecond(Function<V, P> onSecond) {
		return onSecond.apply(this.second);
	}

	public <P> Pair<K, P> applySecondKeepFirst(Function<V, P> onSecond) {
		return of(this.first, onSecond.apply(this.second));
	}

	public void consume(Consumer<K> onFirst, Consumer<V> onSecond) {
		onFirst.accept(this.first);
		onSecond.accept(this.second);
	}

	public void consume(Consumer<Pair<K, V>> consumer) {
		consumer.accept(this);
	}

	public void consume(BiConsumer<K, V> consumer) {
		consumer.accept(this.getFirst(), this.getSecond());
	}

	public void partiallyConsumeFirst(Consumer<K> onFirst) {
		onFirst.accept(this.first);
	}

	public void partiallyConsumeSecond(Consumer<V> onSecond) {
		onSecond.accept(this.second);
	}

	public boolean predicate(Predicate<K> onFirst, Predicate<V> onSecond) {
		return onFirst.test(this.first) && onSecond.test(this.second);
	}

	public boolean predicate(BiPredicate<K, V> condition) {
		return condition.test(this.first, this.second);
	}

	public <T extends Comparable<T>, U extends Comparable<U>> Comparator<Pair<T, U>> DEFAULT_COMPARATOR() {
		return (o1, o2) -> o1.first.equals(o2.first)
				? o1.second.compareTo(o2.second)
				: o1.first.compareTo(o2.first);
	}

	public <T, U> boolean equalsOnFirst(Pair<T, U> rhs) {
		return this.first.equals(rhs.first);
	}

	public static <T, U> List<T> projectFirst(Collection<Pair<T, U>> col) {
		return col.stream().map(Pair::getFirst).toList();
	}

	public static <T, U> List<U> projectSecond(Collection<Pair<T, U>> col) {
		return col.stream().map(Pair::getSecond).toList();
	}

	public static <T> Pair<List<T>, List<T>> shunt(Collection<T> col, Predicate<T> filter) {
		Pair<List<T>, List<T>> ret = of(new ArrayList<>(), new ArrayList<>());
		for (T elem : col) {
			if (filter.test(elem)) {
				ret.getFirst().add(elem);
			} else {
				ret.getSecond().add(elem);
			}
		}
		return ret;
	}

	public K getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}
}

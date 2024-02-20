package io.mangoo.test.concurrent;

import org.cactoos.Func;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.llorllale.cactoos.shaded.org.cactoos.iterable.Mapped;
import org.llorllale.cactoos.shaded.org.cactoos.scalar.SumOf;
import org.llorllale.cactoos.shaded.org.cactoos.scalar.Ternary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class ConcurrentRunner<T> extends TypeSafeDiagnosingMatcher<Func<? super T, Boolean>> {
    private final T input;
    private final int total;

    public ConcurrentRunner(final T object) {
        this(object, Runtime.getRuntime().availableProcessors() << 4);
    }

    public ConcurrentRunner(final T object, final int threads) {
        super();
        this.input = object;
        this.total = threads;
    }

    @Override
    public boolean matchesSafely(final Func<? super T, Boolean> func, final Description desc) {
        final int matching;
        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final List<Future<Boolean>> futures = new ArrayList<>(this.total);
            final Callable<Boolean> task = () -> {
                latch.await();
                return func.apply(this.input);
            };
            for (int thread = 0; thread < this.total; ++thread) {
                futures.add(service.submit(task));
            }
            latch.countDown();
            matching = new SumOf(
                    new Mapped<>(
                            f -> new Ternary<>(f.get(), 1, 0).value(),
                            futures
                    )
            ).intValue();
            service.shutdown();
        }
        if (matching != this.total) {
            desc
                    .appendText("ran successfuly in ")
                    .appendValue(matching)
                    .appendText(" threads");
        }
        return matching == this.total;
    }

    @Override
    public void describeTo(final Description description) {
        description
                .appendText("runs in ")
                .appendValue(this.total)
                .appendText(" threads successfuly");
    }
}
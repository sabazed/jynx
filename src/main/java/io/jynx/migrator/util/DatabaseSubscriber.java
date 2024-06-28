package io.jynx.migrator.util;

import com.mongodb.MongoTimeoutException;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class DatabaseSubscriber<T> implements Subscriber<T> {

	private final List<T> received;
	private final List<Throwable> errors;
	private final CountDownLatch latch;
	private volatile Subscription subscription;

	public DatabaseSubscriber() {
		this.received = new ArrayList<>();
		this.errors = new ArrayList<>();
		this.latch = new CountDownLatch(1);
	}

	@Override
	public void onSubscribe(final Subscription s) {
		subscription = s;
		s.request(Integer.MAX_VALUE);
	}

	@Override
	public void onNext(final T t) {
		received.add(t);
	}

	@Override
	public void onError(final Throwable t) {
		errors.add(t);
		onComplete();
	}

	@Override
	public void onComplete() {
		latch.countDown();
	}

	public List<T> getReceived() {
		return received;
	}

	public List<Throwable> getErrors() {
		return errors;
	}

	public void await() throws Throwable {
		await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}

	public void await(final long timeout, final TimeUnit unit) throws Throwable {
		subscription.request(Integer.MAX_VALUE);
		if (!latch.await(timeout, unit)) {
			throw new MongoTimeoutException("Publisher onComplete timed out");
		}
		if (!errors.isEmpty()) {
			throw errors.get(0);
		}
	}

}

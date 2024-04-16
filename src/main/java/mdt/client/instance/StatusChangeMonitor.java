package mdt.client.instance;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import utils.Polling;
import utils.Throwables;
import utils.func.CheckedRunnable;
import utils.func.CheckedSupplier;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class StatusChangeMonitor implements CheckedRunnable {
	private final MDTInstance m_instance;
	private final Function<MDTInstanceStatus,Boolean> m_matcher;
	private Duration m_pollingInterval;
	private Optional<Duration> m_timeout = Optional.empty();
	private MDTInstanceStatus m_status;
	
	public StatusChangeMonitor(MDTInstance instance, Function<MDTInstanceStatus,Boolean> matcher) {
		m_instance = instance;
		m_matcher = matcher;
	}
	
	public void setPollingInterval(Duration interval) {
		m_pollingInterval = interval;
	}
	
	public void setTimeout(Duration timeout) {
		m_timeout = Optional.ofNullable(timeout);
	}
	
	public void run() throws TimeoutException, InterruptedException {
		Preconditions.checkState(m_pollingInterval != null, "PollingInterval is null");
		
		Polling polling = Polling.builder()
								.setEndOfPollingPredicate(m_endOfPollingPredicate)
								.setPollingInterval(m_pollingInterval)
								.setTimeout(m_timeout)
								.build();
		try {
			polling.run();
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw Throwables.toRuntimeException(cause);
		}
	}
	
	public MDTInstanceStatus getStatus() {
		return m_status;
	}
	
	private final CheckedSupplier<Boolean> m_endOfPollingPredicate = new CheckedSupplier<>() {
		@Override
		public Boolean get() throws Throwable {
			m_status = m_instance.getStatus();
			return m_matcher.apply(m_status);
		}
	};
}
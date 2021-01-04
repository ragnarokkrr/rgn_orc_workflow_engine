package ragna.wf.orc.common.events.spring;

import java.util.StringJoiner;
import org.springframework.context.ApplicationEvent;
import ragna.wf.orc.common.events.DomainEvent;

public class ApplicationEventWrapper extends ApplicationEvent {
  /**
   * Create a new {@code ApplicationEvent}.
   *
   * @param source the object on which the event initially occurred or with which the event is
   *     associated (never {@code null})
   */
  public ApplicationEventWrapper(Object source) {
    super(source);
  }

  public DomainEvent unwrap() {
    return (DomainEvent) source;
  }

  public static ApplicationEventWrapper wrap(final DomainEvent source) {
    return new ApplicationEventWrapper(source);
  }

  @Override
  public String toString() {

    return new StringJoiner(", ", ApplicationEventWrapper.class.getSimpleName() + "[", "]")
        .toString();
  }
}

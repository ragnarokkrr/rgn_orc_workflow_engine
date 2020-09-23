package ragna.wf.utils;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

// TODO move this class to module common-test
// https://stackoverflow.com/questions/7095871/mockito-spy-how-to-gather-return-values
public class ResultCaptor<T> implements Answer {
  private T result = null;

  public T getResult() {
    return result;
  }

  @Override
  public T answer(InvocationOnMock invocationOnMock) throws Throwable {
    result = (T) invocationOnMock.callRealMethod();
    return result;
  }
}

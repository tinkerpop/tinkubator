package gov.lanl.cnls.linkedprocess;
import static org.easymock.EasyMock.reportMatcher;

import java.util.Collection;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

public class MultiCaptureMatcher<T> implements IArgumentMatcher {
 
    Collection<T> captureDestination;
 
    public MultiCaptureMatcher(Collection<T> captureDestination) {
        this.captureDestination = captureDestination;
    }
 
    @Override
    public void appendTo(StringBuffer buffer) {
        buffer.append("multiCapture(").append(captureDestination.toString()).append(")");
    }
 
    @Override
    public boolean matches(Object actual) {
        captureDestination.add((T) actual);
        return true;
    }
 
    public static <S> S multiCapture(Collection<S> destination) {
      reportMatcher(new MultiCaptureMatcher<S>(destination));
      return null;
  }
}

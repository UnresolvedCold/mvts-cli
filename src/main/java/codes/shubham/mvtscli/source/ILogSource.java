package codes.shubham.mvtscli.source;

import java.io.IOException;
import java.util.stream.Stream;

public interface ILogSource extends AutoCloseable {
  Stream<LogLine> logLines() throws IOException;
}

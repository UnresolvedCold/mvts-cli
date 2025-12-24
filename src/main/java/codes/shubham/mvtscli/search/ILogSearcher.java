package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.ILogSource;

import java.util.List;

public interface ILogSearcher {
  public void search(ILogSource source, IOSearchMode mode,
                             String searchTerm, ILogHandler matchHandler, ILogHandler lineHandler);
}

package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.ILogSource;

import java.util.List;

public interface ILogSearcher {
  public List<String> search(ILogSource source, IOSearchMode mode, String searchTerm);
}

package codes.shubham.mvtscli.index;

public record FileCursor(
    long offset,
    long size,
    long lastModified
) {}

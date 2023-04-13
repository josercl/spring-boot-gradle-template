package common;

import java.util.List;

public record ValidationError(
    String field,
    List<String> errors
) { }

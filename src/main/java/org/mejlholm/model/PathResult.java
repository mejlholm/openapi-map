package org.mejlholm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PathResult {

    private final String name;
    private final String openapiUrl;
    private final String openapiUiUrl;
    private final String path;
    private final List<String> operations;
    private final List<Annotation> annotations;

}

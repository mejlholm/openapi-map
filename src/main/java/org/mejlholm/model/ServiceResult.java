package org.mejlholm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ServiceResult {

    private final String name;
    private final String openapiUrl;
    private final String openapiUiUrl;
    private final List<PathResult> pathResults;

}

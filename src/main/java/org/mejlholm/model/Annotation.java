package org.mejlholm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Annotation {
    private final String name;
    private final String value;
}

package org.mejlholm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PathResult {

    final String path;
    final List<String> operations;

}

package de.leidenheit.steeldartdetectormvp.steps;

import java.util.List;

import static de.leidenheit.steeldartdetectormvp.steps.Page.*;

public final class Pagination {

    private static final List<PageRelation> pageRelations = List.of(
            new PageRelation(MASK_GREEN, MASK_RED, null),
            new PageRelation(MASK_RED, MASK_MULTIPLIERS, MASK_GREEN),
            new PageRelation(MASK_MULTIPLIERS, MASK_MULTIRINGS, MASK_RED),
            new PageRelation(MASK_MULTIRINGS, MASK_DARTBOARD, MASK_MULTIPLIERS),
            new PageRelation(MASK_DARTBOARD, MASK_SINGLE, MASK_MULTIRINGS),
            new PageRelation(MASK_SINGLE, MASK_DOUBLE, MASK_DARTBOARD),
            new PageRelation(MASK_DOUBLE, MASK_TRIPLE, MASK_SINGLE),
            new PageRelation(MASK_TRIPLE, MASK_OUTERBULL, MASK_DOUBLE),
            new PageRelation(MASK_OUTERBULL, MASK_INNERBULL, MASK_TRIPLE),
            new PageRelation(MASK_INNERBULL, MASK_MISS, MASK_OUTERBULL),
            new PageRelation(MASK_MISS, MASK_SEGMENT_ADJUSTMENT, MASK_INNERBULL),
            new PageRelation(MASK_SEGMENT_ADJUSTMENT, MASK_SEGMENTS, MASK_MISS),
            new PageRelation(MASK_SEGMENTS, DARTBOARD_SUMMARY, MASK_SEGMENT_ADJUSTMENT),
            new PageRelation(DARTBOARD_SUMMARY, null, MASK_SEGMENTS),
            new PageRelation(DART_DARTS, DART_TIP, null),
            new PageRelation(DART_TIP, DART_SUMMARY, DART_DARTS),
            new PageRelation(DART_SUMMARY, null, DART_TIP),
            new PageRelation(EVALUATION, null, null),
            new PageRelation(HOME, null, null)
    );

    public static boolean hasNext(final Page currentPage) {
        var pageRelation = pageRelations.stream()
                .filter(pr -> pr.current().getIndex() == currentPage.getIndex())
                .findFirst()
                .orElseThrow();
        return pageRelation.next() != null;
    }

    public static boolean hasPrevious(final Page currentPage) {
        var pageRelation = pageRelations.stream()
                .filter(pr -> pr.current().getIndex() == currentPage.getIndex())
                .findFirst()
                .orElseThrow();
        return pageRelation.previous() != null;
    }

    public static Page nextPage(final Page currentPage) {
        return pageRelations.stream()
                .filter(pr -> pr.current() == currentPage)
                .findFirst()
                .orElseThrow()
                .next();
    }

    public static Page previousPage(final Page currentPage) {
        return pageRelations.stream()
                .filter(pr -> pr.current() == currentPage)
                .findFirst().orElseThrow()
                .previous();
    }
}

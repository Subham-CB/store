package com.example.store.util;

import com.example.store.api.model.SortEnumDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

public class PageableBuilderTest {

    private PageableBuilder pageableBuilder;

    // Default values
    private static final int DEFAULT_SIZE = 30;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "asc";

    @BeforeEach
    void setUp() {
        pageableBuilder = new PageableBuilder();
    }

    @Test
    @DisplayName("buildPageable - uses all provided values when nothing is null")
    void buildPageable_allParamsProvided_usesProvidedValues() {
        Pageable result = pageableBuilder.buildPageable(
                2, 10, "name", SortEnumDTO.DESC, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getPageNumber()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getSort().getOrderFor("name")).isNotNull();
        assertThat(result.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("buildPageable - defaults page to 0 when page param is null")
    void buildPageable_nullPage_defaultsToZero() {
        Pageable result = pageableBuilder.buildPageable(
                null, 10, "name", SortEnumDTO.ASC, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("buildPageable - uses provided size when not null")
    void buildPageable_providedSize_usesProvidedSize() {
        Pageable result = pageableBuilder.buildPageable(
                0, 15, "id", SortEnumDTO.ASC, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getPageSize()).isEqualTo(15);
    }

    @Test
    @DisplayName("buildPageable - defaults size to defaultSize when size param is null")
    void buildPageable_nullSize_defaultsToConfiguredLimit() {
        Pageable result = pageableBuilder.buildPageable(
                0, null, "id", SortEnumDTO.ASC, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    @DisplayName("buildPageable - uses provided sortBy when not null")
    void buildPageable_providedSortBy_usesProvidedField() {
        Pageable result = pageableBuilder.buildPageable(
                0, 10, "name", SortEnumDTO.ASC, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getSort().getOrderFor("name")).isNotNull();
    }

    @Test
    @DisplayName("buildPageable - defaults sortBy to defaultSortBy when sortBy param is null")
    void buildPageable_nullSortBy_defaultsToConfiguredSortField() {
        Pageable result = pageableBuilder.buildPageable(
                0, 10, null, SortEnumDTO.ASC, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    @DisplayName("buildPageable - uses ASC direction when SortEnumDTO.ASC is provided")
    void buildPageable_sortDirAsc_appliesAscDirection() {
        Pageable result = pageableBuilder.buildPageable(
                0, 10, "id", SortEnumDTO.ASC, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("buildPageable - uses DESC direction when SortEnumDTO.DESC is provided")
    void buildPageable_sortDirDesc_appliesDescDirection() {
        Pageable result = pageableBuilder.buildPageable(
                0, 10, "id", SortEnumDTO.DESC, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("buildPageable - defaults sort direction to defaultSortDir when sortDir param is null")
    void buildPageable_nullSortDir_defaultsToConfiguredDirection() {

        Pageable result = pageableBuilder.buildPageable(0, 10, "id", null, DEFAULT_SIZE, DEFAULT_SORT_BY, "asc");

        assertThat(result.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("buildPageable - defaults sort direction to DESC when defaultSortDir is 'desc'")
    void buildPageable_nullSortDir_defaultsToDescWhenConfiguredAsDesc() {
        Pageable result = pageableBuilder.buildPageable(0, 10, "id", null, DEFAULT_SIZE, DEFAULT_SORT_BY, "desc");

        assertThat(result.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("buildPageable - all nullable params null uses all defaults")
    void buildPageable_allNullable_usesAllDefaults() {
        Pageable result =
                pageableBuilder.buildPageable(null, null, null, null, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(DEFAULT_SIZE);
        assertThat(result.getSort().getOrderFor(DEFAULT_SORT_BY)).isNotNull();
        assertThat(result.getSort().getOrderFor(DEFAULT_SORT_BY).getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}

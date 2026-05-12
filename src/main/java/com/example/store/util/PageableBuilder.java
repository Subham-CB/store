package com.example.store.util;

import com.example.store.api.model.SortEnumDTO;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageableBuilder {

    /**
     * Builds a Pageable object using provided parameters or defaults from configuration.
     *
     * @param page the page number (0-indexed) or null to use default
     * @param size the page size or null to use default from configuration
     * @param sortBy the field to sort by or null to use default from configuration
     * @param sortDir the sort direction ("asc" or "desc") or null to use default from configuration
     * @param defaultSize the default page size from configuration
     * @param defaultSortBy the default sort field from configuration
     * @param defaultSortDir the default sort direction from configuration
     * @return a Pageable object configured with the provided parameters or defaults
     */
    public Pageable buildPageable(
            final Integer page,
            final Integer size,
            final String sortBy,
            final SortEnumDTO sortDir,
            final int defaultSize,
            final String defaultSortBy,
            final String defaultSortDir) {

        // Use values from configuration if not provided in request
        final int pageValue = (page != null) ? page : 0;
        final int sizeValue = (size != null) ? size : defaultSize;
        final String sortByValue = (sortBy != null) ? sortBy : defaultSortBy;
        final String sortDirValue = (sortDir != null) ? sortDir.name() : defaultSortDir;

        final Sort sort =
                Sort.by(sortDirValue.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortByValue);

        return PageRequest.of(pageValue, sizeValue, sort);
    }
}

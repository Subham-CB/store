package com.example.store.controller;

import com.example.store.component.GlobalSearchProp;
import com.example.store.dto.ProductDTO;
import com.example.store.dto.SortEnumDTO;
import com.example.store.service.ProductService;
import com.example.store.util.PageableBuilder;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final PageableBuilder pageableBuilder;
    private final GlobalSearchProp globalSearchProp;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> findAllProducts(
            @RequestParam(required = false) @Min(value = 0, message = "Min page number is 0") final Integer page,
            @RequestParam(required = false) @Min(value = 5, message = "Min limit is 5") final Integer limit,
            @RequestParam(required = false) final String sortBy,
            @RequestParam(required = false) final SortEnumDTO sortDir
    ){

        Pageable pageable = pageableBuilder.buildPageable(page,limit,sortBy,sortDir,
                globalSearchProp.getLimit(),
                globalSearchProp.getSortField(),
                globalSearchProp.getDirection());

        return ResponseEntity.status(HttpStatus.OK).body(productService.findAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> findProductById(@PathVariable final Long id){
        return ResponseEntity.status(HttpStatus.OK).body(productService.findProductById(id));
    }
}

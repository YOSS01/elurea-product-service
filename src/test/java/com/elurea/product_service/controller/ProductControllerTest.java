package com.elurea.product_service.controller;

import com.elurea.product_service.config.AppProperties;
import com.elurea.product_service.exception.ResourceNotFoundException;
import com.elurea.product_service.security.SecurityConfig;
import com.elurea.product_service.security.SecurityProblemHandler;
import com.elurea.product_service.service.ProductService;
import com.elurea.product_service.service.ProductVariantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductController.class, ProductVariantController.class},
        properties = "app.internal-api-key=test-internal-key")
@Import({SecurityConfig.class, SecurityProblemHandler.class, ProductControllerTest.Config.class})
class ProductControllerTest {

    private static final String VALID_PRODUCT = """
            {"categoryId": "6f1c1a52-6a39-4c3b-9d0e-1f2a3b4c5d6e", "title": "Dress", "description": "Nice"}
            """;
    private static final String STOCK_BODY = """
            {"items": [{"variantId": "6f1c1a52-6a39-4c3b-9d0e-1f2a3b4c5d6e", "quantity": 2}]}
            """;

    @TestConfiguration
    @EnableConfigurationProperties(AppProperties.class)
    static class Config {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductVariantService variantService;

    private static RequestPostProcessor admin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor customer() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void catalogIsPublicToRead() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void creatingProductWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(productService);
    }

    @Test
    void creatingProductAsCustomerReturns403() throws Exception {
        mockMvc.perform(post("/api/products").with(customer())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(productService);
    }

    @Test
    void createProductWithInvalidBodyReturns400WithFieldErrors() throws Exception {
        String body = """
                {"title": " ", "description": "", "thumbnail": "not a url", "slug": "Bad Slug"}
                """;

        mockMvc.perform(post("/api/products").with(admin()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.categoryId").exists())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.thumbnail").exists())
                .andExpect(jsonPath("$.errors.slug").exists());

        verifyNoInteractions(productService);
    }

    @Test
    void createVariantRejectsInvalidPriceStockAndSku() throws Exception {
        String body = """
                {"productId": "6f1c1a52-6a39-4c3b-9d0e-1f2a3b4c5d6e", "sku": "bad sku!", "size": "M",
                 "price": 12.345, "stock": -1}
                """;

        mockMvc.perform(post("/api/product-variants").with(admin()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists());

        verifyNoInteractions(variantService);
    }

    @Test
    void stockReservationAcceptsInternalApiKey() throws Exception {
        when(variantService.reserveStock(any())).thenReturn(List.of());

        mockMvc.perform(post("/api/product-variants/stock/reserve").header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON).content(STOCK_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void stockReservationRejectsWrongApiKey() throws Exception {
        mockMvc.perform(post("/api/product-variants/stock/reserve").header("X-Internal-Api-Key", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content(STOCK_BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(variantService);
    }

    @Test
    void stockReservationRejectsCustomers() throws Exception {
        mockMvc.perform(post("/api/product-variants/stock/reserve").with(customer())
                        .contentType(MediaType.APPLICATION_JSON).content(STOCK_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(variantService);
    }

    @Test
    void serviceKeyCannotModifyCatalog() throws Exception {
        mockMvc.perform(post("/api/products").header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isForbidden());
        verifyNoInteractions(productService);
    }

    @Test
    void stockReservationValidatesQuantities() throws Exception {
        String body = """
                {"items": [{"variantId": "6f1c1a52-6a39-4c3b-9d0e-1f2a3b4c5d6e", "quantity": 0}]}
                """;

        mockMvc.perform(post("/api/product-variants/stock/reserve").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['items[0].quantity']").exists());
    }

    @Test
    void listVariantsWithoutProductIdReturns400() throws Exception {
        mockMvc.perform(get("/api/product-variants"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchTermTooLongReturns400() throws Exception {
        mockMvc.perform(get("/api/products").param("search", "x".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.search").exists());
    }

    @Test
    void unknownSlugReturns404() throws Exception {
        when(productService.getBySlug("missing")).thenThrow(new ResourceNotFoundException("Product", "missing"));

        mockMvc.perform(get("/api/products/slug/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }
}

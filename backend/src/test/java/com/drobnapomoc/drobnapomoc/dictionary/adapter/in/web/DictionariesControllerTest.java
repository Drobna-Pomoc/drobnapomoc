package com.drobnapomoc.drobnapomoc.dictionary.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.DictionariesApi;
import com.drobnapomoc.drobnapomoc.api.DictionariesApiController;
import com.drobnapomoc.drobnapomoc.api.model.CityRest;
import com.drobnapomoc.drobnapomoc.api.model.DistrictRest;
import com.drobnapomoc.drobnapomoc.api.model.ServiceCategoryRest;
import com.drobnapomoc.drobnapomoc.dictionary.application.port.in.ListCitiesUseCase;
import com.drobnapomoc.drobnapomoc.dictionary.application.port.in.ListServiceCategoriesUseCase;
import com.drobnapomoc.drobnapomoc.dictionary.domain.City;
import com.drobnapomoc.drobnapomoc.dictionary.domain.District;
import com.drobnapomoc.drobnapomoc.dictionary.domain.ServiceCategory;
import com.drobnapomoc.drobnapomoc.shared.security.RestAccessDeniedHandler;
import com.drobnapomoc.drobnapomoc.shared.security.RestAuthenticationEntryPoint;
import com.drobnapomoc.drobnapomoc.shared.security.SecurityConfig;
import com.drobnapomoc.drobnapomoc.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(DictionariesApiController.class)
@Import({
        DictionariesApiDelegateImpl.class,
        DictionaryWebMapperImpl.class,
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class DictionariesControllerTest {

    /** Prefix from {@code openapi.dictionaries.base-path}; the generated path constants don't include it. */
    private static final String BASE_PATH = "/v1";
    private static final String CITIES_PATH = BASE_PATH + DictionariesApi.PATH_LIST_CITIES;
    private static final String CATEGORIES_PATH = BASE_PATH + DictionariesApi.PATH_LIST_SERVICE_CATEGORIES;

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private ListCitiesUseCase listCitiesUseCase;

    @MockitoBean
    private ListServiceCategoriesUseCase listServiceCategoriesUseCase;

    @Test
    void testListCities_givenNoSession_shouldReturnCities() {
        // given
        given(listCitiesUseCase.listCities())
                .willReturn(List.of(new City(1L, "Lublin", List.of(new District(2L, 1L, "Czechów")))));

        // when
        final var result = mockMvc.get().uri(CITIES_PATH).exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .convertTo(CityRest[].class)
                .satisfies(body -> assertThat(body).containsExactly(
                        new CityRest(1L, "Lublin", List.of(new DistrictRest(2L, "Czechów")))
                ));
    }

    @Test
    void testListServiceCategories_givenNoSession_shouldReturnCategories() {
        // given
        given(listServiceCategoriesUseCase.listServiceCategories())
                .willReturn(List.of(new ServiceCategory(3L, "Hydraulik")));

        // when
        final var result = mockMvc.get().uri(CATEGORIES_PATH).exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .convertTo(ServiceCategoryRest[].class)
                .satisfies(body -> assertThat(body).containsExactly(new ServiceCategoryRest(3L, "Hydraulik")));
    }

    @Test
    void testListCities_givenNoCities_shouldReturnEmptyArray() {
        // given
        given(listCitiesUseCase.listCities()).willReturn(List.of());

        // when
        final var result = mockMvc.get().uri(CITIES_PATH).exchange();

        // then
        assertThat(result).hasStatusOk().bodyJson().isEqualTo("[]");
    }

    @Test
    void testListServiceCategories_givenLoggedInUser_shouldReturnOk() {
        // given
        given(listServiceCategoriesUseCase.listServiceCategories()).willReturn(List.of());

        // when
        final var result = mockMvc.get().uri(CATEGORIES_PATH)
                .with(user("anna@example.com").roles("CONTRACTOR"))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
    }

    @Test
    void testListServiceCategories_givenNoSession_shouldIssueCsrfCookie() {
        // given
        given(listServiceCategoriesUseCase.listServiceCategories()).willReturn(List.of());

        // when
        final var result = mockMvc.get().uri(CATEGORIES_PATH).exchange();

        // then
        assertThat(result).hasStatusOk().cookies().containsCookie("XSRF-TOKEN");
    }
}

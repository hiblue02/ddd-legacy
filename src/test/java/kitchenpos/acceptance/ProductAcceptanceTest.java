package kitchenpos.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.AcceptanceTest;
import kitchenpos.acceptance.steps.ProductSteps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("상품")
public class ProductAcceptanceTest extends AcceptanceTest {

    private String NAME = "상품";
    private BigDecimal PRICE = BigDecimal.valueOf(1000);

    @DisplayName("상품 등록")
    @Test
    void create() {
        //when
        ExtractableResponse<Response> response
                = ProductSteps.상품_생성(NAME, PRICE);
        //then
        assertAll(
                () -> assertThat(response.statusCode())
                        .isEqualTo(HttpStatus.CREATED.value())
                , () -> assertThat(response.jsonPath()
                        .getString("name")).isEqualTo(NAME)
                , () -> assertThat(response.jsonPath()
                        .getObject("price", BigDecimal.class))
                        .isEqualTo(PRICE)
        );

    }

    @DisplayName("상품 가격 수정")
    @Test
    void priceChange() {
        //given
        ExtractableResponse<Response> created
                = ProductSteps.상품_생성(NAME, PRICE);
        UUID productId = created.response().jsonPath().getObject("id", UUID.class);

        //when
        BigDecimal changePrice = BigDecimal.valueOf(500);
        ExtractableResponse<Response> response
                = ProductSteps.상품_가격_수정(productId, BigDecimal.valueOf(500));
        //then
        assertAll(
                () -> assertThat(response.statusCode())
                        .isEqualTo(HttpStatus.OK.value())
                , () -> assertThat(response.jsonPath()
                        .getString("name")).isEqualTo(NAME)
                , () -> assertThat(response.jsonPath()
                        .getObject("price", BigDecimal.class))
                        .isEqualTo(changePrice)
        );

    }

}
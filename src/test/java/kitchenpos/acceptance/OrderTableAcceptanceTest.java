package kitchenpos.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.AcceptanceTest;
import kitchenpos.acceptance.steps.*;
import kitchenpos.domain.*;
import kitchenpos.fixture.MenuProductFixture;
import kitchenpos.fixture.OrderLineItemFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("주문테이블")
public class OrderTableAcceptanceTest extends AcceptanceTest {

    private static final String NAME = "주문테이블";

    private Menu menu;

    @BeforeEach
    void setup() {
        MenuGroup menuGroup = MenuGroupSteps.메뉴그룹_생성("메뉴그룹").as(MenuGroup.class);
        Product product = ProductSteps.상품_생성("상품", BigDecimal.valueOf(1000)).as(Product.class);
        MenuProduct menuProduct = MenuProductFixture.create(product, 1);
        menu = MenuSteps.메뉴_생성(NAME, BigDecimal.valueOf(900), menuGroup.getId(), List.of(menuProduct))
                .as(Menu.class);
        MenuSteps.메뉴_보이기(menu.getId());
    }

    @DisplayName("[성공] 주문테이블 등록")
    @Test
    void createTest1() {
        //when
        ExtractableResponse<Response> response = OrderTableSteps.주문테이블_생성(NAME);
        //then
        assertAll(
                () -> assertThat(response.statusCode())
                        .isEqualTo(HttpStatus.CREATED.value())
                , () -> assertThat(response.jsonPath().getString("name"))
                        .isEqualTo(NAME)
        );
    }


    @DisplayName("[성공] 주문테이블 앉기")
    @Test
    void sitTest1() {
        //given
        OrderTable orderTable = OrderTableSteps.주문테이블_생성(NAME).as(OrderTable.class);
        //when
        ExtractableResponse<Response> response = OrderTableSteps.주문테이블_앉기(orderTable.getId());
        //then
        assertAll(
                () -> assertThat(response.statusCode())
                        .isEqualTo(HttpStatus.OK.value())
                , () -> assertThat(response.jsonPath().getBoolean("occupied"))
                        .isTrue()
        );
    }

    @DisplayName("[성공] 주문테이블 인원수 변경")
    @Test
    void changeNumberOfGuestsTest1() {
        //given
        OrderTable orderTable = OrderTableSteps.주문테이블_생성(NAME).as(OrderTable.class);
        OrderTableSteps.주문테이블_앉기(orderTable.getId());
        //when
        int numberOfGuests = 5;
        ExtractableResponse<Response> response = OrderTableSteps.주문테이블_인원수_변경(orderTable.getId(), numberOfGuests);
        //then
        assertAll(
                () -> assertThat(response.statusCode())
                        .isEqualTo(HttpStatus.OK.value())
                , () -> assertThat(response.jsonPath().getInt("numberOfGuests"))
                        .isEqualTo(numberOfGuests)
        );
    }

    @DisplayName("[예외] 사용중이 아닌 테이블은 인원수를 변경할 수 없다.")
    @Test
    void changeNumberOfGuestsTest2() {
        //given
        OrderTable orderTable = OrderTableSteps.주문테이블_생성(NAME).as(OrderTable.class);
        //when
        int numberOfGuests = 5;
        ExtractableResponse<Response> response = OrderTableSteps.주문테이블_인원수_변경(orderTable.getId(), numberOfGuests);
        //then
        assertThat(response.statusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("[성공] 주문테이블 치우기")
    @Test
    void clearTest1() {
        //given
        OrderTable orderTable = OrderTableSteps.주문테이블_생성(NAME).as(OrderTable.class);
        OrderTableSteps.주문테이블_앉기(orderTable.getId());
        OrderTableSteps.주문테이블_인원수_변경(orderTable.getId(), 5);

        OrderLineItem orderLineItem = OrderLineItemFixture.create(menu, menu.getPrice(), 1);
        Order order = OrderSteps.매장_주문_생성(orderTable.getId(), List.of(orderLineItem)).as(Order.class);
        OrderSteps.주문_접수(order.getId());
        OrderSteps.주문_서빙(order.getId());
        OrderSteps.주문_완료(order.getId());

        //when
        ExtractableResponse<Response> response = OrderTableSteps.주문_테이블_치우기(orderTable.getId());
        //then
        assertAll(
                () -> assertThat(response.statusCode())
                        .isEqualTo(HttpStatus.OK.value())
                , () -> assertThat(response.jsonPath().getBoolean("occupied"))
                        .isFalse()
                , () -> assertThat(response.jsonPath().getInt("numberOfGuests"))
                        .isZero()
        );
    }

    @DisplayName("[성공] 주문테이블 전체 조회")
    @Test
    void findAllTest1() {
        //given
        OrderTable orderTable1 = OrderTableSteps.주문테이블_생성(NAME).as(OrderTable.class);
        OrderTable orderTable2 = OrderTableSteps.주문테이블_생성(NAME).as(OrderTable.class);
        //when
        ExtractableResponse<Response> response = OrderTableSteps.주문테이블_전체_조회();
        //then
        assertAll(
                () -> assertThat(response.statusCode())
                        .isEqualTo(HttpStatus.OK.value())
                , () -> assertThat(response.jsonPath().getList("id", UUID.class))
                        .hasSize(2)
                        .contains(orderTable1.getId(), orderTable2.getId())
        );
    }
}
package wooteco.subway.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("구간 관련 기능")
public class SectionTest {

    @DisplayName("상행선과 하행선은 같은 역으로 할 수 없다.")
    @Test
    void duplicateStationException() {
        Station 강남 = new Station("강남");
        assertThatThrownBy(() -> new Section(강남, 강남, new Line("2호선", "green"), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구간에서 상행선과 하행선은 같은 역으로 할 수 없습니다.");
    }

    @DisplayName("상행선과 하행선의 거리는 1 이상이어야 한다.")
    @Test
    void distanceException() {
        Station 강남 = new Station("강남");
        Station 선릉 = new Station("선릉");
        assertThatThrownBy(() -> new Section(강남, 선릉, new Line("2호선", "green"), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상행선과 하행선의 거리는 1 이상이어야 합니다.");
    }
}

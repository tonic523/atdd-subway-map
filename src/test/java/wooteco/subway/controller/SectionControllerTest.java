package wooteco.subway.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static wooteco.subway.controller.AcceptanceFixture.post;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.LineRequest;
import wooteco.subway.dto.LineResponse;
import wooteco.subway.dto.SectionRequest;
import wooteco.subway.dto.StationResponse;
import wooteco.subway.service.LineService;
import wooteco.subway.service.SectionService;

public class SectionControllerTest extends AcceptanceTest {

    @Autowired
    StationDao stationDao;
    @Autowired
    LineService lineService;
    @Autowired
    SectionService sectionService;

    @DisplayName("등록 `POST /lines/{id}/sections`")
    @Nested
    @Sql("/init.sql")
    class post {

        private Station 동천역;
        private Station 정자역;
        private Station 판교역;
        private LineResponse lineResponse;

        @BeforeEach
        void setUp() {
            동천역 = stationDao.save(new Station("동천역"));
            정자역 = stationDao.save(new Station("정자역"));
            판교역 = stationDao.save(new Station("판교역"));
            lineResponse = lineService.save(new LineRequest("신분당선", "red", 판교역.getId(),
                    동천역.getId(), 20));
        }

        @DisplayName("성공")
        @Test
        void success() {
            Map<String, Object> params = new HashMap<>();
            params.put("upStationId", 정자역.getId());
            params.put("downStationId", 동천역.getId());
            params.put("distance", 10);

            ExtractableResponse<Response> response = post(params,
                    "/lines/" + lineResponse.getId() + "/sections");

            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        }

        @DisplayName("노선, 상행선, 하행선을 찾지 못하면 404를 응답한다.")
        @Test
        void notFoundLineOrUpStationOrDownStation() {
            Map<String, Object> params = new HashMap<>();
            params.put("upStationId", 1000L);
            params.put("downStationId", 1001L);
            params.put("distance", 10);

            ExtractableResponse<Response> response = post(params,
                    "/lines/" + 1002L + "/sections");

            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }

        @DisplayName("상행선, 하행선이 둘 중에 하나만 노선에 있지 않으면 400을 응답한다.")
        @Test
        void existOneStationInLine() {
            Map<String, Object> params = new HashMap<>();
            params.put("upStationId", 동천역.getId());
            params.put("downStationId", 판교역.getId());
            params.put("distance", 10);

            ExtractableResponse<Response> response = post(params,
                    "/lines/" + lineResponse.getId() + "/sections");

            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @DisplayName("기존 구간의 거리보다 크면 400을 응답한다.")
        @Test
        void longerThanDistanceOfOverlappingSection() {
            Map<String, Object> params = new HashMap<>();
            params.put("upStationId", 판교역.getId());
            params.put("downStationId", 정자역.getId());
            params.put("distance", 30);

            ExtractableResponse<Response> response = post(params,
                    "/lines/" + lineResponse.getId() + "/sections");

            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }
    }


    @Nested
    @DisplayName("제거 `DELETE /lines/{id}/sections?stationId=?`")
    class delete {

        private Station 동천역;
        private Station 정자역;
        private Station 판교역;
        private Station 미금역;
        private LineResponse lineResponse;

        @BeforeEach
        void setUp() {
            동천역 = stationDao.save(new Station("동천역"));
            정자역 = stationDao.save(new Station("정자역"));
            판교역 = stationDao.save(new Station("판교역"));
            미금역 = stationDao.save(new Station("미금역"));
            lineResponse = lineService.save(new LineRequest("신분당선", "red", 판교역.getId(),
                    동천역.getId(), 20));
            sectionService.save(new SectionRequest(판교역.getId(), 정자역.getId(), 5), lineResponse.getId());
            sectionService.save(new SectionRequest(정자역.getId(), 미금역.getId(), 5), lineResponse.getId());
        }

        @DisplayName("성공")
        @Test
        void delete() {
            Map<String, Object> params = new HashMap<>();
            params.put("stationId", 정자역.getId());
            ExtractableResponse<Response> response = AcceptanceFixture.delete(params,
                    "/lines/" + lineResponse.getId() + "/sections");

            List<StationResponse> allStationResponseByLineId = lineService.findAllStationResponseByLineId(
                    lineResponse.getId());
            assertAll(
                    () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                    () -> assertThat(allStationResponseByLineId).hasSize(3)
            );
        }

        @DisplayName("노선, 역이 존재하지 않은 id이면 404을 응답한다.")
        @Test
        void notFoundLineAndStation() {
            Map<String, Object> params = new HashMap<>();
            params.put("stationId", 1000L);
            ExtractableResponse<Response> response = AcceptanceFixture.delete(params,
                    "/lines/" + 1001L + "/sections");

            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }

        @DisplayName("노선에 역이 포함되지 않으면 400을 응답한다.")
        @Test
        void notContainsSection() {
            Station 선릉역 = stationDao.save(new Station("선릉역"));
            Map<String, Object> params = new HashMap<>();
            params.put("stationId", 선릉역.getId());
            ExtractableResponse<Response> response = AcceptanceFixture.delete(params,
                    "/lines/" + lineResponse.getId() + "/sections");

            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }
    }
}

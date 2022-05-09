package wooteco.subway.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import wooteco.subway.domain.Station;
import wooteco.subway.exception.NotFoundException;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class StationDao {

    private static final int UPDATE_QUERY_EMPTY_RESULT = 0;
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public StationDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("STATION")
                .usingGeneratedKeyColumns("id");
    }

    public Station save(Station station) {
        final SqlParameterSource parameters = new BeanPropertySqlParameterSource(station);
        final Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Station(id, station.getName());
    }

    private RowMapper<Station> rowMapper() {
        return (rs, rowNum) -> {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            return new Station(id, name);
        };
    }

    public List<Station> findAll() {
        String SQL = "select * from station;";
        return jdbcTemplate.query(SQL, rowMapper());
    }

    public void deleteById(Long id) {
        String SQL = "delete from station where id = ?";
        validateExistById(jdbcTemplate.update(SQL, id), id);
    }

    private void validateExistById(int updateQueryResult, Long id) {
        if (updateQueryResult == UPDATE_QUERY_EMPTY_RESULT) {
            throw new NotFoundException(id + "에 해당하는 지하철 역을 찾을 수 없습니다.");
        }
    }
}

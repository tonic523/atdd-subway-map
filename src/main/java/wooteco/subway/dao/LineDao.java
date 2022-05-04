package wooteco.subway.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import wooteco.subway.domain.Line;
import wooteco.subway.exception.DuplicateNameException;
import wooteco.subway.exception.NotFoundException;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class LineDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public LineDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("LINE")
                .usingGeneratedKeyColumns("id");
    }

    public Line save(Line line) {
        final SqlParameterSource parameters = new BeanPropertySqlParameterSource(line);
        try {
            final Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
            return new Line(id, line.getName(), line.getColor());
        } catch (DuplicateKeyException e) {
            throw new DuplicateNameException("중복된 지하철 노선 이름이 있습니다.");
        }
    }

    public List<Line> findAll() {
        String SQL = "select * from line;";
        return jdbcTemplate.query(SQL, rowMapper());
    }

    private RowMapper<Line> rowMapper() {
        return (rs, rowNum) -> {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String color = rs.getString("color");
            return new Line(id, name, color);
        };
    }

    public Line findById(Long id) {
        String SQL = "select * from line where id = ?;";
        try {
            return jdbcTemplate.queryForObject(SQL, rowMapper(), id);
        } catch (DataAccessException e) {
            throw new NotFoundException("id에 맞는 지하철 노선이 없습니다.");
        }
    }

    public void update(Line line) {
        findById(line.getId());
        String SQL = "update line set name = ?, color = ? where id = ?;";
        jdbcTemplate.update(SQL, line.getName(), line.getColor(), line.getId());
    }

    public void delete(Long id) {
        findById(id);
        String SQL = "delete from line where id = ?";
        jdbcTemplate.update(SQL, id);
    }
}

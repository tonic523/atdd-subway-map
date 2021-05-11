package wooteco.subway.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.domain.line.Line;
import wooteco.subway.domain.line.LineDao;
import wooteco.subway.domain.line.SortedStationIds;
import wooteco.subway.domain.section.Section;
import wooteco.subway.domain.section.SectionDao;
import wooteco.subway.domain.station.StationDao;
import wooteco.subway.web.dto.LineResponse;
import wooteco.subway.web.dto.SectionRequest;
import wooteco.subway.web.dto.StationResponse;
import wooteco.subway.web.exception.NotFoundException;

@Service
@Transactional
public class LineService {

    private final LineDao lineDao;
    private final SectionDao sectionDao;
    private final StationDao stationDao;

    public LineService(LineDao lineDao, SectionDao sectionDao, StationDao stationDao) {
        this.lineDao = lineDao;
        this.sectionDao = sectionDao;
        this.stationDao = stationDao;
    }

    public LineResponse add(Line line, SectionRequest request) {
        Long lineId = addLine(line);
        addSection(new Section(
                lineId,
                request.getUpStationId(),
                request.getDownStationId(),
                request.getDistance()
        ));
        return findById(lineId);
    }

    private Long addLine(Line line) {
        return lineDao.save(line);
    }

    private Long addSection(Section section) {
        return sectionDao.save(section);
    }

    public List<Line> findAll() {
        return lineDao.findAll();
    }

    public LineResponse findById(Long id) {
        Line line = findLine(id);

        List<Section> sections = sectionDao.listByLineId(line.getId());
        List<Long> stationIds = new SortedStationIds(sections).get();
        List<StationResponse> stations = stationDao.stationsFilteredById(stationIds)
                .stream()
                .map(StationResponse::new)
                .collect(Collectors.toList());

        return new LineResponse(line, stations);
    }

    public void update(Long id, Line line) {
        findLine(id);
        lineDao.update(id, line);
    }

    public void delete(Long id) {
        findLine(id);
        lineDao.delete(id);
    }

    private Line findLine(Long id) {
        return lineDao.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 노선입니다"));
    }
}

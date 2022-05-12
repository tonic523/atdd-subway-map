<p align="center">
    <img width="200px;" src="https://raw.githubusercontent.com/woowacourse/atdd-subway-admin-frontend/master/images/main_logo.png"/>
</p>
<p align="center">
  <a href="https://techcourse.woowahan.com/c/Dr6fhku7" alt="woowacourse subway">
    <img alt="Website" src="https://img.shields.io/website?url=https%3A%2F%2Fedu.nextstep.camp%2Fc%2FR89PYi5H">
  </a>
  <img alt="GitHub" src="https://img.shields.io/github/license/woowacourse/atdd-subway-map">
</p>

<br>

# 지하철 노선도 미션
스프링 과정 실습을 위한 지하철 노선도 애플리케이션

# 요구사항 정리
- 지하철 역
  - 역 관리 API 기능 완성한다.
  - 지하철역 생성 시 이미 등록된 이름으로 요청한다면 에러를 응답한다.
- 지하철 노선
  - 노선 추가 시 3가지 정보도 추가로 입력받는다.
    - upStationId: 상행 종점
    - downStatiionId: 하행 종점
    - distance: 두 종점간의 거리
    - 두 종점간의 연결 정보를 이용하여 노선 추가 시 구간(Section) 정보도 함께 등록
  - [예외] 같은 이름의 노선은 생성 불가하다.
  - 노선 관리 API 구현
    - [API 문서](https://techcourse-storage.s3.ap-northeast-2.amazonaws.com/c682be69ae4e412c9e3905a59ef7b7ed#Line)
- 노선 기능에 대한 End to End 테스트 작성한다.
- 구간 관리 API 구현
  - 추가 기능
    - 노선에 구간을 추가
    - 노선에 포함된 구간 정보를 통해 상행 종점부터 하행 종점까지의 역 목록을 응답
    - 구간 제거
  - 구간 관리 API 스펙은 [API 문서](https://techcourse-storage.s3.ap-northeast-2.amazonaws.com/c682be69ae4e412c9e3905a59ef7b7ed#Section) 참고

# API
## 지하철 역

> 등록 - `POST /stations`
- 이름을 가진 역을 저장한다.
- 저장된 id와 이름을 응답한다. `Created 201`
  - [x] [예외] 이름은 빈값이면 안된다. `Bad Request 400`
  - [x] [예외] 이미 존재하는 이름이 있으면 안된다. `Bad Request 400`
> 목록 - `GET /stations`
- 역 목록을 응답한다. `OK 200`
> 삭제 - `DELETE /stations/{id}`
- id에 해당하는 역을 삭제한다. `No Content 204`
  - [x] [예외] id에 해당하는 역이 있어야 한다. `Not Found 404`

## 지하철 노선

> 등록 - `POST /lines`
- 노선과 동시에 상행선, 하행선에 관한 구간도 등록한다. `Created 201`
  - [x] [예외] 이름, 색깔은 빈값이면 안된다. `Bad Request 400`
  - [x] [예외] 이름, 색깔은 중복이 있으면 안된다. `Bad Request 400`
  - [x] [예외] 지하철이 있는 id 값이어야 한다. `Not Found 404`
  - [x] [예외] 상행역과 하행역은 같아선 안된다. `Bad Request 400`
  - [x] [예외] 거리는 1 이상이어야 한다. `Bad Request 400`
> 목록 - `GET /lines`
- 모든 노선 목록을 역과 함께 응답한다. `OK 200`
> 조회 - `GET /lines/{id}`
- id에 해당하는 노선을 역과 함께 응답한다. `OK 200`
  - [x]  [예외] 노선이 있는 id 값이어야 한다. `Not Found 404`
> 수정 - `PUT /lines/{id}`
- id에 해당하는 노선의 이름과 색깔을 수정한다. `OK 200`
- [x]  [예외] 노선이 있는 id 값이어야 한다. `Not Found 404`
- [x]  [예외] 이미 있는 이름, 색깔이면 안된다. `Bad Request 400`
> 삭제 - `DELETE /lines/{id}`
  - 해당 id 값을 가지는 line을 삭제하고 노선에 있는 모든 구간들을 삭제한다. `No Content 204`
  - [x]  [예외] 노선이 있는 id 값이어야 한다. `Not Found 404`

## 구간
> 등록 `POST /lines/{id}/sections`
- 상행선, 하행선, 거리를 가지는 구간을 등록한다. `OK 200`
  - [x] [예외] 노선, 상행선, 하행선이 존재하는 id여야 한다. `Bad Request 404`
  - [ ] [예외] 상행선, 하행선이 둘 중에 하나만 노선에 있어야 한다. `Bad Request 400`
  - 하행선이 노선의 상행 종점이면 상행 종점 구간으로 등록한다.
  - 상행선이 노선의 하행 종점이면 하행 종점 구간으로 등록한다.
  - [x] [예외] 노선의 중간 구간에 등록하면 기존 구간의 거리보다 작아야 한다. `Bad Request 400`
> 제거 `DELETE /lines/{id}/sections?stationId=?`
- 구간을 삭제한다. `OK 200`
  - [x] [예외] 노선, 역은 존재하는 id여야 한다. `Not Found 400`
  - [x] [예외] 노선에 역은 포함되어야 한다. `Bad Request 400`
  - [x] 역에 포함된 구간을 삭제하고 비어진 구간을 연결한다.

## 🚀 Getting Started
### Usage
#### application 구동
```
./gradlew bootRun
```
<br>

## ✏️ Code Review Process
[텍스트와 이미지로 살펴보는 온라인 코드 리뷰 과정](https://github.com/next-step/nextstep-docs/tree/master/codereview)

<br>

## 🐞 Bug Report

버그를 발견한다면, [Issues](https://github.com/woowacourse/atdd-subway-map/issues) 에 등록해주세요 :)

<br>

## 📝 License

This project is [MIT](https://github.com/woowacourse/atdd-subway-map/blob/master/LICENSE) licensed.

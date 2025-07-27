# takealook-backend

## Getting Started

### Submodule 초기화

이 프로젝트는 `takealook-taskmanager`를 서브모듈로 사용하고 있습니다. 프로젝트를 처음 클론하거나 서브모듈을 초기화하려면 다음 명령어들을 실행해주세요:

```bash
# 저장소 클론 (처음 클론하는 경우)
git clone --recurse-submodules https://github.com/takea-look/takealook-backend.git

# 또는 이미 클론한 경우
cd takealook-backend
git submodule init
git submodule update --init --recursive
```

### 서브모듈 업데이트

서브모듈의 최신 변경사항을 가져오려면:

```bash
git submodule update --remote --recursive
```

## install application via Docker
```sh
docker pull tklcat/takealook-backend:latest
docker run --name app -p 8080:8080 \
    -e DB_USERNAME=ENTER_YOUR_POSTGRESQL_USERNAME \
    -e DB_PASSWORD=ENTER_YOUR_POSTGRESQL_PASSWORD \
    -e DB_URL=ENTER_YOUR_POSTGRESQL_DB_URL \
    takealook-backend:latest > tkl.log 2<&1 &
```

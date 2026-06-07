# How to Start

## 1. Start Docker Dev (MySQL, Redis, Prometheus, Grafana, ELK...)
Dừng containers nếu đang chạy:

```bash
cd environment
docker compose -f docker-compose-dev.yml down
```
Nếu muốn xóa volumes (dữ liệu) luôn:

```bash
cd environment
rm -rf data/db_data
```
Lưu ý: rm -rf data/db_data sẽ xóa toàn bộ dữ liệu MySQL hiện tại. Nếu có data production/test cần giữ, hãy backup trước bằng mysqldump.

Khởi động lại
```bash
docker compose -f docker-compose-dev.yml up -d
```

Các service sẽ khởi động:
- MySQL → `localhost:3316`
- Redis → `localhost:6319`
- Prometheus → `http://localhost:9090`
- Grafana → `http://localhost:3000` (admin / admin)
- Elasticsearch → `http://localhost:9200`
- Kibana → `http://localhost:5601`

---

## 2. Start Docker Kafka
    
```bash
cd environment
docker compose -f docker-compose-kafka.yml up -d
```

Các service sẽ khởi động:
- Kafka → `localhost:9094`
- Kafka UI → `http://localhost:8989`

---

## 3. Run Start Application (Spring Boot)

Chạy class `StartApplication.java` từ module `xxxx-start`:

```bash
./mvnw spring-boot:run -pl xxxx-start
```

Hoặc chạy trực tiếp từ IntelliJ IDEA: `Run > StartApplication`.

---

## 4. Start Frontend

```bash
cd xxxx.fe.com
npm run dev
```

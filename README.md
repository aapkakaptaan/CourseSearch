# Course Search Spring Boot + Elasticsearch

A sample project demonstrating full-text search, fuzzy search, filtering, sorting, and autocomplete using Spring Boot and Elasticsearch.

---

## 1. Launch Elasticsearch

> This project requires a running Elasticsearch instance (tested with 8.x).

### With Docker Compose

```sh
docker-compose up -d
```

If you don't have a `docker-compose.yml`, use this:

```yaml
version: "3.8"
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.12.0
    container_name: elasticsearch
    environment:
      - node.name=elasticsearch
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - xpack.security.enabled=false
      - xpack.security.http.ssl.enabled=false
      - ES_JAVA_OPTS=-Xms1g -Xmx1g
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - "9200:9200"
    volumes:
      - es_data:/usr/share/elasticsearch/data:rw
    healthcheck:
      test:
        [
          "CMD-SHELL",
          "curl --silent --fail localhost:9200/_cluster/health || exit 1",
        ]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  es_data:
    driver: local

```

---

## 2. Build and Run the Spring Boot Application

```sh
./gradlew clean build
./gradlew bootRun
```

Or, to run the jar:

```sh
./gradlew bootJar
java -jar build/libs/coursesearch-*.jar
```

---

## 3. Populate the Index with Sample Data

On first startup, the application auto-loads sample data from `src/main/resources/sample-courses.json` if the `courses` index is empty.

To force reloading:
1. **Delete the index:**
    ```sh
    curl -XDELETE "localhost:9200/courses"
    ```
2. **Restart the Spring Boot app.**
    You should see console output like:
    ```
    ✔ Successfully indexed 50 courses into Elasticsearch
    ```

---

## 4. Example API Usage

All endpoints base URL: `http://localhost:8080/api/courses`

### a) List all courses

```sh
curl "http://localhost:8080/api/courses/search"
```

### b) Filter by category, price, page & size

```sh
curl "http://localhost:8080/api/courses/search?category=Math&minPrice=50&maxPrice=150&page=0&size=5"
```

### c) Fuzzy search (typo-tolerant)

```sh
curl "http://localhost:8080/api/courses/search?q=creative%20drwing"
```

### d) Sorted results

```sh
curl "http://localhost:8080/api/courses/search?sort=priceDesc"
```

### e) Autocomplete (completion suggester)

```sh
curl "http://localhost:8080/api/courses/search/suggest?q=crea"
```

---

## 5. (Bonus) Sample Responses

### a) Fuzzy search for a typo

```sh
curl "http://localhost:8080/api/courses/search?q=creative%20drwing"
```
**Response:**
```json
{
  "total": 2,
  "courses": [
    {
      "id": "course-026",
      "title": "Creative Drawing",
      "category": "Art",
      "price": 27.5,
      "nextSessionDate": 1756998586.348
    },
    ...
  ]
}
```

### b) Autocomplete

```sh
curl "http://localhost:8080/api/courses/search/suggest?q=crea"
```
**Response:**
```json
["Creative Drawing"]
```

---

## 6. Troubleshooting

- If you see no results for queries, ensure Elasticsearch is running (`localhost:9200`) and your data is loaded (`GET /courses/_search`).
- To re-index sample data, delete the index and restart the app.
- If you change `sample-courses.json`, always delete the index and restart.

---

## 7. Endpoints Reference

| Endpoint | Description |
|----------|-------------|
| `/api/courses/search` | Full-text, fuzzy search, filter, sort, and paginate |
| `/api/courses/search/suggest` | Autocomplete course titles |

---

Enjoy searching!

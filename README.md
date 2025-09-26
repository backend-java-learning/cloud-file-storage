# Cloud File Storage

Реализованный проект из роадмап Сергея Жукова: https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/

Облачное хранилище файлов на **Java Spring Boot** с использованием PostgreSQL, MinIO и Redis.

## Деплой
http://45.141.79.139:8080

Тестовый пользователь:
```
registrationTestUser
qwerty123
```

## Стек технологий
- Java Spring Boot
- Spring Data Redis
- MinIO
- PostgreSQL
- H2 (для интеграционных тестов)

## Переменные окружения

### Пример локального запуска
```env
DB_USER=
DB_PASSWORD=
DB_NAME=cloud_file_storage
DB_URL=jdbc:postgresql://localhost:5431/cloud_file_storage

MINIO_ROOT_USER=
MINIO_ROOT_PASSWORD=
MINIO_URL=http://localhost:9000
MINIO_BUCKET=user-files
TEST_MINIO_BUCKET=test-user-files

REDIS_PORT=6379
REDIS_HOST=localhost
```

### Пример запуска в docker-сети
``` env
DB_USER=
DB_PASSWORD=
DB_NAME=cloud_file_storage
DB_URL=jdbc:postgresql://postgres:5432/cloud_file_storage

MINIO_ROOT_USER=
MINIO_ROOT_PASSWORD=
MINIO_URL=http://minio:9000
MINIO_BUCKET=user-files
TEST_MINIO_BUCKET=test-user-files

REDIS_PORT=6379
REDIS_HOST=redis
```

## Фронтенд
Перед запуском убедитесь, что в родительской директории проекта склонирован репозиторий фронтенда:
```
git clone https://github.com/zhukovsd/cloud-storage-frontend.git
```

### Конфигурация Nginx
Для корректной работы API без ошибок CORS добавьте проксирование на Spring Boot в файл nginx.conf в разделе server:
```
# Проксирование API на Spring Boot
location /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

## Запуск проекта
### Локально

- Настройте .env с локальными переменными.
- Запустите PostgreSQL, MinIO и Redis.
- Соберите и запустите Spring Boot приложение

### В Docker

- Настройте .env с переменными для Docker-сети.
- Убедитесь, что контейнеры postgres, minio и redis доступны в сети Docker.
- Соберите и запустите приложение в Docker или через Docker Compose.
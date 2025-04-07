# ☁️ Проект "Погода"
<hr>

## Описание
<hr>
Веб-приложение для просмотра текущей погоды. 
Пользователь может зарегистрироваться и добавить в коллекцию одну или 
несколько локаций (городов, сёл, других пунктов), 
после чего главная страница приложения начинает отображать список локаций с
их текущей погодой.

### Деплой: http://37.252.19.242:8081 (пока не работает)

# Использованные технологии / инструменты
<hr/>

### Backend
![java-logo](https://github.com/VladislavLevchikIsAProger/tennis_scoreboard/assets/153897612/bc1ab298-7a78-42ec-8813-05b38668310e)
![hibernate-logo](https://github.com/VladislavLevchikIsAProger/tennis_scoreboard/assets/153897612/071df0a5-79ef-4435-9c98-5a9b2383d420)
![postgresql](https://github.com/VladislavLevchikIsAProger/weather_tracker/assets/153897612/8922bdba-ad57-4d69-b68c-ec505fff82e0)
![maven-logo](https://github.com/VladislavLevchikIsAProger/tennis_scoreboard/assets/153897612/159c5f30-83db-49a2-906a-fc92a071eeff)
![opeanweatherapi](https://github.com/VladislavLevchikIsAProger/weather_tracker/assets/153897612/78bce6ce-0faf-4d08-bf48-cc12cea9cc83)
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=black)
![Flyway](https://img.shields.io/badge/Flyway-CC0000?style=for-the-badge&logo=flyway&logoColor=white)

### Тестирование

![junit-logo](https://github.com/VladislavLevchikIsAProger/tennis_scoreboard/assets/153897612/a1a05826-fecb-4b7a-827c-946ffc72da32)
![mockito](https://github.com/VladislavLevchikIsAProger/weather_tracker/assets/153897612/c405a582-b268-4b82-b3e8-461d77b7f39c)
![h2-logo](https://github.com/VladislavLevchikIsAProger/tennis_scoreboard/assets/153897612/3e65f8a8-a9a7-44bc-85c8-42d173338c74)

### Фронтенд

![html-logo](https://github.com/VladislavLevchikIsAProger/tennis_scoreboard/assets/153897612/cf73900e-a565-405d-b7dd-cc05f9429c2f)
![css-logo](https://github.com/VladislavLevchikIsAProger/tennis_scoreboard/assets/153897612/d7d9ecf6-1cfb-4fe1-ba32-dd43d59921a8)
![thymeleaf](https://github.com/VladislavLevchikIsAProger/weather_tracker/assets/153897612/5c5cda5f-c5d6-42c8-893b-3737e8d04db2)

### Deploy

![dockerfile](https://github.com/VladislavLevchikIsAProger/weather_tracker/assets/153897612/e22a80da-ca5a-438b-a5f5-605393f3208d)
![docker-compose](https://github.com/VladislavLevchikIsAProger/weather_tracker/assets/153897612/82390fb8-e6d4-4b15-b175-78eead5bc360)

## Диаграмма базы данных

![diagram](https://github.com/VladislavLevchikIsAProger/weather_tracker/assets/153897612/06eab789-15ed-4dd5-b29a-70d48b3fd80a)


## Зависимости
+ Java 21
+ Apache Maven
+ Tomcat 11
+ Intellij IDEA
+ OpenWeather Api Key

## Функционал


### Регистрация

Адрес - `/auth/sign-up`
- Регистрация пользователя по уникальному логику и паролю
- Подтверждение пароля для успешной регистрации

![authorization.png](src/main/resources/view/static/images/registration.png)

### Авторизации

Адрес - `/auth/sign-in`
- Авторизация пользователя по существующему логину и паролю

![authorization.png](src/main/resources/view/static/images/authorization.png)


### Главная страница

Адрес - `/weather`

- Просмотр погоды сохранненых локаций
- Поиск локаций
- Выход из аккаунта

![img_2.png](src/main/resources/view/static/images/weather.png)

### Страница поиска локаций

Адрес - `/weather/search-results`

- Поиск локаций
- Добавление локаций на главную страницу

![img_1.png](src/main/resources/view/static/images/city.png)


<hr/>

## Установка и запуск через Docker

### 1. Клонирование репозитория
- Перейдите в папку, где хотите хранить проект.
- Откройте консоль и выполните команды:
  ```bash
  git clone https://github.com/0-Luntik-0/weather.git
  cd weather
  ```

### 2. Настройка файла `.env`

- API вы можете взять на сайте -> [openweathermap](https://openweathermap.org/api)
- Для этого придеться зарегестрироваться и получить API ключ (лимит 60 запросов в минуту)

<hr/>

- Переименуйте файл `.env.example` в `.env` и заполните своими данными:
- 
  ```properties
  # Database
  DB_NAME=your_db_name
  DB_USERNAME=your_user
  DB_PASSWORD=your_password

  # OpenWeather
  OPENWEATHER_API_KEY=your_api_key
  ```

### 3. Подготовка к запуску приложения
- Убедитесь, что установлены Docker и Docker Compose. Проверьте версии:
  ```bash
  docker --version
  docker compose version
  ```
- Если вывод примерно такой:
  ```bash
  Docker version 28.0.4, build b8034c0
  Docker Compose version v2.34.0
  ```
  переходите к следующему шагу. Иначе установите Docker и Docker Compose.

### Для Windows и macOS:

- Скачайте и установите Docker Desktop с [официального сайта Docker](https://www.docker.com/products/docker-desktop)
- Запустите Docker Desktop и дождитесь его полной загрузки.
- Проверьте установку в терминале:

```bash
docker --version
docker compose version
```

### Для Linux:
- Установите Docker [по официальной документации](https://docs.docker.com/engine/install/)
- Установите Docker Compose [по официальной документации](https://docs.docker.com/desktop/setup/install/linux/)
- Проверьте установку в терминале:

```bash
docker --version
docker compose version
```
### 4. Запуск приложения
- В папке проекта выполните:
  ```bash
  docker compose up --build
  ```

### 5. Доступ к приложению
- Откройте браузер и перейдите по адресу:
  ```
  http://localhost:8080
  ```

### Остановка и очистка
- Чтобы остановить контейнеры и удалить данные:
  ```bash
  docker compose down -v
  ```

### Что внутри Docker?
- **PostgreSQL**:
  - Данные в томе `postgres_data`.
  - Порт: `5433` (хост) → `5432` (контейнер).
- **Adminer**:
  - Веб-интерфейс для БД.
  - Доступ: `http://localhost:5050` (логин/пароль из `.env`).
- **Приложение**:
  - Собирается из `Dockerfile`.
  - Порт: `8080` (хост) → `8080` (контейнер).
  - Подключается к БД через сеть Docker.
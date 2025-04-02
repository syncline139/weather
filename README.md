# ☁️ Проект "Погода"
<hr>

## Описание
<hr>
Веб-приложение для просмотра текущей погоды. 
Пользователь может зарегистрироваться и добавить в коллекцию одну или 
несколько локаций (городов, сёл, других пунктов), 
после чего главная страница приложения начинает отображать список локаций с
их текущей погодой.

### Деплой: http://37.252.19.242:8081

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

## Диаграмма базы данных

![diagram](https://github.com/VladislavLevchikIsAProger/weather_tracker/assets/153897612/06eab789-15ed-4dd5-b29a-70d48b3fd80a)


## Зависимости
+ Java 21+
+ Apache Maven
+ Tomcat 11
+ Intellij IDEA
+ OpenWeather Api Key

## Функционал


### Регистрация

Адрес - `/auth/sign-up`
- Регистрация пользователя по уникальному логику и паролю
- Подтверждение пароля для успешной регистрации

![img.png](src/main/resources/view/static/images/img.png)

### Авторизации

Адрес - `/auth/sign-in`
- Авторизация пользователя по существующему логину и паролю

  ![img_1.png](src/main/resources/view/static/images/img_1.png)


### Главная страница

Адрес - `/weather`

- Просмотр погоды сохранненых локаций
- Поиск локаций
- Выход из аккаунта

![img_2.png](src/main/resources/view/static/images/img_2.png)

### Страница поиска локаций

Адрес - `/weather/search-results`

- Поиск локаций
- Добавление локаций на главную страницу

![img_3.png](src/main/resources/view/static/images/img_3.png)


<hr/>

## Установка проекта

Следуйте этим шагам, чтобы настроить и запустить проект на своей машине:

### 1. Клонирование репозитория:

   Откройте терминал и выполните команду:

 `git clone https://github.com/0-Luntik-0/weather.git`

  `cd ВАША-ПАПКА`
### 2. Настройка PostgreSQL

1. Установите PostgreSQL, если он ещё не установлен, и запустите сервер.
2. Создайте базу данных: 

```properties
CREATE DATABASE project_weather
```

3. Переименуйте файл src/main/resources/hibernate.properties.origin в hibernate.properties и укажите свои данные для подключения к PostgreSQL: 

```properties
hibernate.driver_class=org.postgresql.Driver
hibernate.connection.url=jdbc:postgresql://localhost:5432/project_weather
hibernate.connection.username=YOUR_USERNAME
hibernate.connection.password=YOUR_PASSWORD

hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.show_sql=true

API=YOUR_API_KEY
```
### Сборка и запуск приложения на Tomcat

1. **Перейдите в директорию проекта:**
   ```bash
   cd ВАША_ПАПКА
   ```

2. **Соберите проект и установите зависимости:**
   ```bash
   mvn clean install
   ```

3. **Развертывание на Tomcat**

   **Если используете внешний Tomcat:**

    - Найдите сгенерированный WAR-файл (обычно находится в `target/ВАШ_ПРОЕКТ.war`).
    - Скопируйте WAR-файл в папку `webapps` вашего Tomcat:
      ```bash
      cp target/ВАШ_ПРОЕКТ.war /путь_к_Tomcat/webapps/
      ```
    - Запустите Tomcat:
      ```bash
      cd /путь_к_Tomcat/bin
      ./startup.sh   # для Linux/macOS
      startup.bat    # для Windows
      ```
    - Перейдите в браузере по адресу:
      ```
      http://localhost:8080/
      ```

4. **Запуск в IntelliJ IDEA**

    - Откройте проект в IntelliJ IDEA.
    - Перейдите в **Run | Edit Configurations**.
    - Нажмите `+` и выберите **Tomcat Server | Local**.
    - Укажите путь к установленному Tomcat.
    - В разделе **Deployment** нажмите `+` и добавьте ваш WAR-файл.
    - Убедитесь, что Tomcat настроен на порт `8080`.
    - Нажмите **Apply** и **OK**.
    - Запустите конфигурацию с помощью **Run** или **Debug**.
    - Откройте в браузере:
      ```
      http://localhost:8080/
      ```



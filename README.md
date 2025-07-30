# Squad Backend

Bu proje Spring Boot 3.3.4 ve PostgreSQL kullanarak geliştirilmiştir.

## Teknolojiler

- **Java 17**
- **Spring Boot 3.3.4**
- **PostgreSQL** (MySQL değil!)
- **Spring Security**
- **Spring Data JPA**
- **MapStruct** (Object Mapping)
- **Lombok**
- **JWT Authentication**

## IDE Ayarları

### IntelliJ IDEA için:

1. **Database Tool Window'da PostgreSQL bağlantısı kurun:**

   - Host: `localhost` veya `127.0.0.1`
   - Port: `5432`
   - Database: `my_database`
   - Username: `${DB_USERNAME}` (environment variable)
   - Password: `${DB_PASSWORD}` (environment variable)

2. **Maven ayarları:**

   - Maven compiler plugin annotation processor path'leri doğru ayarlanmıştır
   - MapStruct ve Lombok annotation processor'ları birlikte çalışır

3. **Database Dialect:**
   - `application.properties` dosyasında: `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect`
   - IDE'nin PostgreSQL dialect'ini kullanmasını sağlar

### VS Code için:

1. **Java Extension Pack** yükleyin
2. **PostgreSQL** extension'ı yükleyin
3. **Spring Boot Extension Pack** yükleyin

## Çalıştırma

```bash
# Dependencies'leri yükle
mvn clean install

# Uygulamayı çalıştır
mvn spring-boot:run
```

## Environment Variables

Aşağıdaki environment variable'ları ayarlayın:

```bash
export DB_USERNAME=your_postgres_username
export DB_PASSWORD=your_postgres_password
export JWT_SECRET_KEY=your_jwt_secret_key
```

## Önemli Notlar

- Bu proje **PostgreSQL** kullanır, MySQL değil!
- IDE'nin MySQL syntax'ı göstermesi normaldir, ancak gerçekte PostgreSQL çalışır
- MapStruct annotation processor'ları doğru ayarlanmıştır
- Tüm mapper'lar PostgreSQL ile uyumludur

## Sorun Giderme

### IDE MySQL Syntax Hatası Gösteriyorsa:

1. IDE'nin database connection'ını PostgreSQL olarak ayarlayın
2. IDE'nin dialect ayarını PostgreSQL olarak değiştirin
3. Projeyi yeniden import edin

### MapStruct Implementation Hatası:

1. `mvn clean compile` çalıştırın
2. IDE'yi yeniden başlatın
3. Annotation processor'ların doğru çalıştığından emin olun

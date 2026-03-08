# 404 Squad — Backend

## Proje
Hali saha organizasyon uygulamasi. Java 17 + Spring Boot 3.3.4 + PostgreSQL.

## Dil
Turkce iletisim kur. Commit mesajlari Ingilizce.

## Komutlar
- Baslat: `cd ~/404/404-backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- Health check: `curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/swagger-ui.html`
- DB baglanti testi: `pg_isready -h localhost -p 5432`
- Test: `cd ~/404/404-backend && ./mvnw test`

## Veritabani
- PostgreSQL: `my_database`, user: `squad_dev`, port: 5432
- Env vars (~/.zshrc): DB_USERNAME, DB_PASSWORD, JWT_SECRET_KEY
- Hibernate ddl-auto: update, timezone: Europe/Istanbul

## Mimari & Konvansiyonlar
- Katmanli yapi: Entity -> Repository -> Service(Impl) -> Controller
- DTO kullan, Entity dogrudan response olarak donme
- Package: `com.squad.squad.{controller,service,entity,dto,repository,security,config,enums}`
- PascalCase class adlari, camelCase field/method adlari
- Jackson: boolean field'larda `is` prefix sorununa dikkat (@JsonProperty ile coz)

## Git Workflow
- Branch: master <- staging <- develop <- feature/xxx
- Aktif branch: `develop`
- Feature branch'ler develop'dan acilir
- Commit oncesi `git status` ve `git diff` kontrol et

## Port
8082 (dev profile)

## Onemli Dosyalar
- Config: src/main/resources/application-dev.properties
- Migrations: src/main/resources/db/migration/
- pom.xml: dependency yonetimi

## Gelistirme Standartlari

### BaseEntity — Yeni Entity Kurallari
Her yeni entity `BaseEntity`'yi extend etmeli:
```java
@Entity
public class YeniEntity extends BaseEntity { ... }
```
BaseEntity otomatik saglar: `id`, `active` (boolean, default true), `createdAt`, `updatedAt`, `createdBy` (User), `updatedBy` (User).
- Entity'de ayrica `@Id`, `createdAt`, `active` TANIMLAMA
- `createdAt` ve `createdBy` -> Spring Data JPA Auditing otomatik doldurur (`SecurityContext`'ten)
- Yeni tablo icin DB migration SQL'i de yazilmali (Flyway kullanimda degil, psql ile manuel uygula)

### Veri Izolasyonu — Squad Bazli Erisim Kontrolu
Her sorgu ve endpoint squad (group) context'ini dogrulamamali:
- `GroupContextFilter` her request'e `squadId` inject eder (Header: `X-Group-Id`)
- Service katmaninda squad'a ait olmayan veriyi asla dogrudan donemez
- Repository sorgularina `squadId` filtresi ekle: `.findBySquadId(squadId)`
- Super Admin haric hic bir endpoint baska squad'in verisine erisememeli
- Yeni listeleme endpointlerinde: `WHERE squad_id = :currentSquadId` sartini kontrol et

### Schema Degisikligi Yapildiginda
Flyway projede kullanimda degil. Schema degisikligi:
1. `src/main/resources/db/migration/` altina `VX__aciklama.sql` yaz (dokumantasyon icin)
2. SQL'i dogrudan DB'ye uygula: `psql -h localhost -p 5432 -U squad_dev -d my_database -f migration.sql`
3. Hibernate `ddl-auto=update` yeni kolonlari otomatik ekler ancak `NOT NULL` kolon icin once DEFAULT ekle

### Guvenlik Kontrol Listesi
- [ ] Yeni endpoint: Squad context filtresi var mi?
- [ ] Yeni entity: BaseEntity'den extend ediliyor mu?
- [ ] Yeni listeleme: Baska squad verisi siziyor mu?
- [ ] boolean field: `is` prefix Jackson sorunu var mi? (`@JsonProperty` ekle)

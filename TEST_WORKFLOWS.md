# 404 Squad Backend — Test Workflow Rehberi

> Projenin hiç test edilmeden production'a taşınmasını engellemek amacıyla hazırlanmıştır.
> Her workflow, gerçek hayat senaryoları üzerinden test edilmektedir.
> **Backend:** Spring Boot 3.3.4 + TestContainers (gerçek PostgreSQL) + JUnit 5

---

## Test Altyapısı

| Araç | Amaç |
|------|------|
| `@SpringBootTest(RANDOM_PORT)` | Gerçek HTTP + gerçek Spring context |
| `TestContainers (PostgreSQL 16)` | Mock yok — gerçek veritabanı |
| `@ServiceConnection` | TC datasource otomatik inject |
| `TestRestTemplate` | HTTP istemci |
| `JdbcTemplate` | Test setup + DB doğrulama |
| `ExecutorService + CountDownLatch` | Concurrent testler |

**Test Komutu:** `./mvnw test`
**Base Class:** `com.squad.squad.integration.BaseIntegrationTest`

---

## Grup A — Auth & Kullanıcı Yönetimi

| ID | Test | Dosya | Senaryo | Beklenti |
|----|------|-------|---------|----------|
| A1 | Başarılı kayıt | `AuthIntegrationTest` | Username + password POST → `/api/users/createUser` | 201 + token |
| A2 | Duplicate username | `AuthIntegrationTest` | Aynı username ikinci kez | 409 Conflict |
| A3 | Başarılı login | `AuthIntegrationTest` | Doğru credentials | 200 + token + squads |
| A4 | Yanlış şifre | `AuthIntegrationTest` | Hatalı password | 401 |
| A5 | Token koruması | `AuthIntegrationTest` | Token olmadan korumalı endpoint | 401 |
| A6 | Profil güncelleme | `AuthIntegrationTest` | Kendi username'ini güncelle | 200 |
| A7 | Başkasının profilini güncelleme | `AuthIntegrationTest` | Başka user path'i | 403 |
| A8 | Password sıfırlama (superAdmin) | `AuthIntegrationTest` | Normal user ile dene | 403 |

---

## Grup B — Squad Workflow

| ID | Test | Dosya | Senaryo | Beklenti |
|----|------|-------|---------|----------|
| B1 | Squad oluşturma talebi | `SquadWorkflowIntegrationTest` | Authenticated user talep gönderir | 200 OK |
| B2 | Çift talep engeli | `SquadWorkflowIntegrationTest` | PENDING varken ikinci talep | 409 Conflict |
| B3 | SuperAdmin onaylama | `SquadWorkflowIntegrationTest` | Onay sonrası Squad + Player + Membership oluştu mu | DB kontrol |
| B4 | SuperAdmin reddetme | `SquadWorkflowIntegrationTest` | Reddet → status=REJECTED | 200 + DB kontrol |
| B5 | Geçersiz invite code ile katılma | `SquadWorkflowIntegrationTest` | Rastgele code | 404 |
| B6 | Geçerli invite code ile katılma talebi | `SquadWorkflowIntegrationTest` | Doğru code | 200 + DB'de PENDING |
| B7 | Join talebi admin onayı | `SquadWorkflowIntegrationTest` | Onay → Player + Membership oluştu mu | DB kontrol |
| B8 | Join talebi admin reddi | `SquadWorkflowIntegrationTest` | Reddet → status=REJECTED | 200 |
| B9 | 3 red → bloke | `SquadWorkflowIntegrationTest` | 3 red sonrası aynı squad'a 4. talep | 409 Conflict |
| B10 | Son admin koruması | `SquadWorkflowIntegrationTest` | Tek admini düşür | 409 Conflict |
| B11 | Üye kaldırma | `SquadWorkflowIntegrationTest` | Kaldır → membership silindi mi | DB kontrol |
| B12 | Squad data izolasyonu | `SquadWorkflowIntegrationTest` | Squad B admin Squad A maçını görmeye çalışır | 403/404 |
| B13 | Invite code yenileme | `SquadWorkflowIntegrationTest` | Eski kod geçersiz oldu mu | 404 |

---

## Grup C — Maç Lifecycle

| ID | Test | Dosya | Senaryo | Beklenti |
|----|------|-------|---------|----------|
| C1 | Maç oluşturma | `GameLifecycleIntegrationTest` | Admin, roster listesiyle maç oluşturur | 200 + game.id |
| C2 | Oylanmamış maç varken yeni maç engeli | `GameLifecycleIntegrationTest` | 2. maç oluşturmaya çalış | 409 Conflict |
| C3 | Maç oluşturma → rating temizleme | `GameLifecycleIntegrationTest` | Maç oluşturulunca eski rating'ler silindi mi | DB: rating count = 0 |
| C4 | Maç güncelleme | `GameLifecycleIntegrationTest` | Tarih + roster değiştir | 200 |
| C5 | Oynanmış maç güncellenemez | `GameLifecycleIntegrationTest` | isPlayed=true sonra güncelle | 409 Conflict |
| C6 | Maç silme | `GameLifecycleIntegrationTest` | Delete → roster'lar da silindi mi | 200 + DB: roster count = 0 |
| C7 | Gol ekleme → skor güncelleme | `GameLifecycleIntegrationTest` | BLACK gol ekle → homeTeamScore++ + isPlayed=true | DB kontrol |
| C8 | Beyaz takım gol → awayScore | `GameLifecycleIntegrationTest` | WHITE gol → awayTeamScore++ | DB kontrol |
| C9 | Auto-mark played | `GameLifecycleIntegrationTest` | Geçmiş tarihli maç getir → isPlayed=true | DB kontrol |

---

## Grup D — Voting & Rating Cascade (En Kritik)

| ID | Test | Dosya | Senaryo | Beklenti |
|----|------|-------|---------|----------|
| D1 | Başarılı oy (happy path — tek takım) | `VotingCascadeIntegrationTest` | 2v2: BLACK takımı oylaması tamamlanır | DB: roster.rating hesaplandı |
| D2 | Her iki takım oylaması → full cascade | `VotingCascadeIntegrationTest` | 2v2: her iki takım da oy verir | game.is_voted=true, mvp_id set, player.rating güncellendi |
| D3 | Kendi oyunu verme engeli | `VotingCascadeIntegrationTest` | Kendi playerId'sini rate et | 400/422 |
| D4 | Karşı takıma oy verme engeli | `VotingCascadeIntegrationTest` | WHITE oyuncuya BLACK kişi oy verir | 400/422 |
| D5 | Duplikat oy engeli | `VotingCascadeIntegrationTest` | Aynı player için ikinci kez oy | 400/422 |
| D6 | Oynanmamış maçta oy engeli | `VotingCascadeIntegrationTest` | isPlayed=false olan maçta oy | 400/422 |
| D7 | Zaten oylanmış maçta oy engeli | `VotingCascadeIntegrationTest` | isVoted=true olan maçta oy | 400/422 |
| D8 | **Concurrent voting — race condition** | `VotingConcurrencyIntegrationTest` | 4 thread aynı anda oy gönderir (her takımdan 2) | DB: tam olarak N rating, isVoted=true, duplicate yok |
| D9 | **Duplicate submit — race condition** | `VotingConcurrencyIntegrationTest` | Aynı voter 5 thread ile aynı anda POST | DB: sadece 1 hasVote=true roster |

---

## Grup E — İstatistikler & Leaderboard

| ID | Test | Dosya | Senaryo | Beklenti |
|----|------|-------|---------|----------|
| E1 | Top scorers | `StatsIntegrationTest` | 3 oyuncuya farklı sayıda gol ekle | Doğru sıralama |
| E2 | Top rated players | `StatsIntegrationTest` | 2 maç sonrası rating ortalaması | En yüksek rating başta |
| E3 | Legendary duos | `StatsIntegrationTest` | Aynı takımda 2 maç oynayan çift | Çift tespit edildi |

---

## Grup F — Güvenlik & SSRF

| ID | Test | Dosya | Senaryo | Beklenti |
|----|------|-------|---------|----------|
| F1 | SSRF — localhost bloke | `SecurityIntegrationTest` | `http://localhost:6379` | 400 Bad Request |
| F2 | SSRF — internal IP bloke | `SecurityIntegrationTest` | `http://192.168.1.1` | 400 Bad Request |
| F3 | SSRF — HTTP bloke (HTTPS zorunlu) | `SecurityIntegrationTest` | `http://maps.app.goo.gl/x` | 400 Bad Request |
| F4 | SSRF — whitelist izin | `SecurityIntegrationTest` | `https://maps.app.goo.gl/x` | 200 veya 5xx (network erişimi yok ama whitelist geçti) |
| F5 | SSRF — başka domain bloke | `SecurityIntegrationTest` | `https://evil.com` | 400 Bad Request |
| F6 | Admin endpoint → normal user | `SecurityIntegrationTest` | MEMBER user admin endpoint'i çağırır | 403 |
| F7 | SuperAdmin endpoint → admin user | `SecurityIntegrationTest` | Admin user superAdmin endpoint çağırır | 403 |
| F8 | Cross-squad erişim | `SecurityIntegrationTest` | Squad B kullanıcısı Squad A maçını talep eder | 403 |

---

## Grup G — SuperAdmin

| ID | Test | Dosya | Senaryo | Beklenti |
|----|------|-------|---------|----------|
| G1 | Tüm squad'ları listeleme | `SuperAdminIntegrationTest` | 2 squad var, liste döner | 200 + count=2 |
| G2 | Squad deaktif etme | `SuperAdminIntegrationTest` | Deaktif → X-Group-Id ile erişim | 403 |
| G3 | Kullanıcı rolü değiştirme | `SuperAdminIntegrationTest` | USER → ADMIN yap | DB: super_admin=true |
| G4 | Kullanıcı silme | `SuperAdminIntegrationTest` | Kullanıcı sil → membership'ler de gitti mi | DB kontrol |

---

## Test Dosyası Haritası

```
src/test/java/com/squad/squad/
├── integration/
│   ├── BaseIntegrationTest.java              ← Ortak altyapı
│   ├── auth/
│   │   └── AuthIntegrationTest.java          ← Grup A
│   ├── squad/
│   │   └── SquadWorkflowIntegrationTest.java ← Grup B
│   ├── game/
│   │   └── GameLifecycleIntegrationTest.java ← Grup C
│   ├── voting/
│   │   ├── VotingCascadeIntegrationTest.java  ← Grup D (D1-D7)
│   │   └── VotingConcurrencyIntegrationTest.java ← Grup D (D8-D9)
│   ├── stats/
│   │   └── StatsIntegrationTest.java          ← Grup E
│   ├── security/
│   │   └── SecurityIntegrationTest.java       ← Grup F
│   └── superadmin/
│       └── SuperAdminIntegrationTest.java     ← Grup G
└── (mevcut unit testler)
    ├── service/
    │   ├── RatingServiceTest.java
    │   ├── GameServiceTest.java
    │   └── ...
    └── repository/
        └── RatingRepositoryTest.java
```

---

## Notlar

- **Concurrent testler** için `ExecutorService` + `CountDownLatch` kullanılır
- **SSRF testi** ağ erişimi gerektirmez — whitelist/blacklist logic HTTP isteği atmadan kontrol edilir
- **MVP testi** Persona tablosu gerektirir; testler `@BeforeEach` yerine Flyway migration ile gelen veriyi kullanır
- Tüm testler **birbirinden bağımsızdır** — her test `@BeforeEach` ile DB'yi temizler

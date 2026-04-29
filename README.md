# 🧴 Fregence - Premium Perfume E-Commerce API

Fregence, lüks ətir mağazaları üçün hazırlanmış, yüksək performanslı və təhlükəsiz RESTful API platformasıdır. Bu layihə müasir backend memarlığı və bulud texnologiyaları əsasında qurulmuşdur.

## 🚀 Texnologiyalar (Tech Stack)
- **Framework:** Java 17, Spring Boot 3.4.3
- **Database:** PostgreSQL (Neon.tech)
- **Security:** Spring Security & JWT (JSON Web Token)
- **Caching:** Redis (Upstash) - *Performansı 10 qat artırır*
- **Storage:** Cloudinary - *Məhsul şəkillərinin buludda saxlanılması*
- **Optimization:** Bucket4j (Rate Limiting), Docker, JVM Memory Management
- **Documentation:** Swagger UI / OpenAPI 3

## ✨ Əsas Funksiyalar
- 🔐 **Giriş Sistemi:** Rol əsaslı (Admin/User) giriş və qeydiyyat.
- 🛍 **Alış-veriş:** Tam funksional Səbət (Cart) və Sifariş (Order) idarəetməsi.
- 🚚 **Logistika:** Admin tərəfindən kuryer təyini və canlı status izləmə.
- 📊 **Dashboard:** Real-vaxt statistikası (Aylıq artım, Ümumi qazanc).
- ⚡ **Sürət:** Redis və React Query (polling) ilə optimallaşdırılmış sorğular.

## ⚙️ Lokal Quraşdırılması
1. Layihəni klonlayın.
2. `application.properties` daxilindəki Environment Variable-ları təyin edin (DB, JWT, Cloudinary, Redis).
3. Maven ilə build edin: `./mvnw clean install`
4. Başladın: `java -jar target/*.jar`

---
Developed by Toghrul

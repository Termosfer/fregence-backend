# 🧴 Fregence - Premium Perfume E-Commerce API

Fregence is a high-performance, secure, and cloud-native RESTful API platform tailored for luxury fragrance boutiques. This project is built with a modern backend architecture, focusing on scalability, data integrity, and advanced security standards.

## 🚀 Tech Stack
- **Framework:** Java 17, Spring Boot 3.4.3
- **Database:** PostgreSQL (Cloud-hosted via **Neon.tech**)
- **Security:** Spring Security & JWT (JSON Web Tokens)
- **Caching:** Redis (**Upstash**) - *Reduces database load and enhances performance*
- **Image Hosting:** **Cloudinary API** - *Automated image optimization and storage*
- **Traffic Control:** **Bucket4j** (Rate Limiting) - *DDoS and Bot protection*
- **Documentation:** Swagger UI / OpenAPI 3
- **Testing:** JUnit 5 & Mockito

## ✨ Key Features
- 🔐 **Advanced Authentication:** Secure Login/Register system using JWT and Role-Based Access Control (RBAC).
- 🛍 **E-Commerce Engine:** Full-featured Shopping Cart and Order Management with snapshot-based pricing logic to preserve historical data.
- 🚚 **Logistics Integration:** Admin capability to assign couriers and provide real-time delivery status updates to customers.
- 🔍 **Dynamic Filtering:** Multi-criteria filtering system (Brand, Price Range, Gender) powered by optimized JPQL queries.
- 📊 **Business Intelligence:** Admin Dashboard with real-time analytics including Total Revenue, Average Order Value (AOV), and Customer Growth Rate.
- ⚡ **Optimization:** Redis-backed caching and Cloudinary integration for automatic WebP conversion and image resizing.

## 🛡️ Security & Best Practices
- **Data Protection:** Passwords encrypted using BCrypt hashing.
- **Privacy:** Sensitive information hidden via DTO (Data Transfer Object) architecture.
- **Environment Safety:** All credentials (DB, JWT, Cloudinary) managed strictly through Environment Variables.
- **Resilience:** Implemented Rate Limiting to prevent API abuse and ensure server stability on Render.

## ⚙️ Local Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Termosfer/fregence-backend.git
2. **Configure Environment Variables:**
   ```bash
   DB_URL=jdbc:postgresql://your-db-host/neondb
   DB_USER=your_db_user
   DB_PASSWORD=your_db_password
   JWT_SECRET=your_64_char_secret_key
   CLOUDINARY_NAME=your_cloud_name
   CLOUDINARY_API_KEY=your_api_key
   CLOUDINARY_API_SECRET=your_api_secret
   REDIS_HOST=your_redis_host
   REDIS_PORT=6379
   REDIS_PASSWORD=your_redis_password
3. **Build the project:**
   ```bash
   ./mvnw clean install
4. **Run the application:**
   ```bash
   java -jar target/*.jar
--- 
   Developed by Toghrul 

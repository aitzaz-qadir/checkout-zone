# 🏢 Checkout Zone

An equipment checkout management system for tracking company equipment loans. Built with Spring Boot, PostgreSQL, and Thymeleaf for internal IT asset management.

## ✨ Planned Features

- **Equipment Management** - Track company equipment (laptops, monitors, peripherals, etc.)
- **Checkout/Checkin System** - Manage equipment loans to employees
- **Transaction History** - View complete checkout history and current assignments
- **Search & Filter** - Find equipment by type, status, or availability
- **Database Persistence** - PostgreSQL for reliable data storage

## 🛠️ Tech Stack

- ☕ **Java 25** - Modern Java with LTS support
- 🍃 **Spring Boot 3.5.6** - Backend framework
- 🗄️ **PostgreSQL 18+** - Relational database
- 🎨 **Thymeleaf** - Server-side templating engine
- 🎯 **Spring Data JPA** - Database interaction with Hibernate
- 📦 **Maven** - Dependency management

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/checkout/checkout_zone/
│   │   ├── model/          # Entity classes (Equipment, User, etc.)
│   │   ├── repository/     # Data access layer
│   │   ├── service/        # Business logic
│   │   └── controller/     # REST endpoints
│   └── resources/
│       ├── application.properties  # Database configuration
│       ├── templates/              # Thymeleaf HTML templates
│       └── static/                 # CSS, JS, images
└── test/                   # Unit and integration tests
```

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- PostgreSQL 18.0
- Maven (included via wrapper)

### Installation
```bash
# Clone the repository
git clone https://github.com/aitzaz-qadir/checkout-zone.git

# Navigate to project directory
cd checkout-zone

# Create PostgreSQL database
createdb -U postgres cz_data

# Run the application
./mvnw spring-boot:run
```

The application will start on http://localhost:8080

## 👨‍💻 Author

**Aitzaz Qadir**

- Portfolio: [aitzaz-qadir.netlify.app](https://aitzaz-qadir.netlify.app)
- GitHub: [@aitzaz-qadir](https://github.com/aitzaz-qadir)
- LinkedIn: [linkedin.com/in/aitzaz-qadir](https://www.linkedin.com/in/aitzaz-qadir/)
- Email: aitzazqk@gmail.com

📝 License

- This project is open source and available for educational purposes.

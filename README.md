<h1 align="center">Solaris – Renewable Energy Management Application</h1>
<p align="center">
  <img src="./src/main/resources/logo/logo.jpg" alt="Solaris Logo" width="200"/>
</p>

---

## 🌞 Overview

**Solaris** is a modern Java desktop application designed to centralize and simplify the management of renewable energy products, events, users, blogs, and waste tracking. The application aims to promote responsible consumption and smart energy management, supporting ecological sustainability.

---

## 🚀 Key Features

- **User Management**: Secure user registration, authentication, and role-based access control.
- **Waste Management**: Track and manage waste to reinforce ecological impact.
- **Event Management**: Plan, reserve, and manage workshops and events.
- **Blog Module**: Publish informative articles, news, and advice.
- **Product Management**: Maintain a catalog of green products (add, edit, delete).

---

## 🛠️ Technologies Used

| Technology           | Description                            |
|---------------------|----------------------------------------|
| **Java 17**         | Main programming language               |
| **JavaFX**          | Graphical user interface                |
| **Scene Builder**   | Drag-and-drop UI creation               |
| **IntelliJ IDEA**   | Development environment                 |
| **MySQL (WAMP)**    | Local database                          |
| **Maven**           | Dependency management & build           |
| **Git & GitHub**    | Version control and collaboration       |

---

## 📦 Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   ```
2. **Configure the database:**
   - Import the SQL scripts from the `sql/` directory into your local MySQL server.
   - Update database credentials in the configuration files if necessary.
3. **Build the project:**
   ```bash
   mvn clean install
   ```
4. **Configure credentials:**
   Copy `.env.example` to `.env` and fill in your own values. The application reads them from environment variables — no keys are stored in source control.
   ```bash
   cp .env.example .env
   ```
   Required variables: `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `STRIPE_SECRET_KEY`, `STRIPE_PUBLISHABLE_KEY`, `SENDGRID_API_KEY`, `GIPHY_API_KEY`, `GEMINI_API_KEY`, `SOLARIS_ADMIN_PHONE`.

5. **Run the application:**
   ```bash
   mvn javafx:run
   ```

---

## 🧑‍💻 Contribution

Contributions are welcome! Please fork the repository and submit a pull request. For major changes, open an issue first to discuss what you would like to change.

---

## 📄 License

This project is licensed under the MIT License.

---

## 👤 Author

**Nasri Ayari**  
Lead Developer & Project Owner  
Email: [nasri.ayari@email.com](mailto:nasri.ayari@email.com)

---

Thank you for using Solaris! Together, let's build a greener future.


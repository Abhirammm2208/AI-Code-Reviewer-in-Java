# AI Code Reviewer

A Spring Boot application that provides AI-powered code review using Google Gemini API.

## Features

- 🤖 AI-powered code analysis using Google Gemini
- 📊 Automated code review with suggestions
- 💾 PostgreSQL database for storing reviews
- 🔒 Secure credential management with environment variables
- 🚀 RESTful API endpoints

## Prerequisites

- Java 19 or higher
- Maven 3.6+
- PostgreSQL 17.5+
- Google Gemini API Key ([Get one here](https://aistudio.google.com/app/apikey))

## Quick Start

### 1. Clone the repository
```bash
git clone https://github.com/Abhirammm2208/AI-Code-Reviewer-in-Java.git
cd AI-Code-Reviewer-in-Java
```

### 2. Set up PostgreSQL Database
```sql
CREATE DATABASE ai_code_reviewer;
```

### 3. Configure Environment Variables

Copy `.env.example` to `.env` and fill in your credentials:
```bash
cp .env.example .env
```

Edit `.env` with your actual values:
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ai_code_reviewer
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
GENERATIVE_API_KEY=your_gemini_api_key
GENERATIVE_MODEL=gemini-2.0-flash-exp
AI_PROVIDER=gemini
```

### 4. Run the Application

**Windows PowerShell:**
```powershell
.\run-app.ps1
```

The application will start on `http://localhost:9090`

## API Usage

### Submit Code for Review

**Endpoint:** `POST /api/reviews`

**Request Body:**
```json
{
  "code": "public class Example {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n    }\n}",
  "language": "java"
}
```

**Response:**
```json
{
  "id": 1,
  "code": "...",
  "language": "java",
  "review": "AI-generated review...",
  "createdAt": "2025-10-11T17:52:45"
}
```

## Project Structure

```
ai-code-reviewer/
├── src/
│   ├── main/
│   │   ├── java/com/yourorg/aicode/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── model/          # JPA entities
│   │   │   ├── repository/     # Data repositories
│   │   │   └── service/        # Business logic
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/         # Web UI
│   └── test/
├── .env                        # Your local credentials (gitignored)
├── .env.example               # Template for environment variables
├── run-app.ps1                # Startup script with env loading
└── pom.xml                    # Maven dependencies

```

## Technologies Used

- **Spring Boot 3.5.6** - Application framework
- **Spring Data JPA** - Database access
- **PostgreSQL** - Database
- **Google Gemini API** - AI code review
- **Lombok** - Boilerplate reduction
- **Maven** - Build tool

## Security Notes

- ⚠️ Never commit `.env` file to version control
- 🔑 Keep your Gemini API key secure
- 🔐 Use strong database passwords
- ✅ `.gitignore` is configured to exclude `.env`

## Troubleshooting

### Database Connection Error
- Ensure PostgreSQL is running
- Verify database credentials in `.env`
- Check database exists: `ai_code_reviewer`

### API Key Error
- Verify your Gemini API key is valid
- Check API quota at [Google AI Studio](https://aistudio.google.com)

### Port Already in Use
- Change port in `application.properties`: `server.port=8080`

## Development

### Build
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is open source and available under the [MIT License](LICENSE).

## Author

**Abhiram**  
GitHub: [@Abhirammm2208](https://github.com/Abhirammm2208)

## Acknowledgments

- Google Gemini API for AI capabilities
- Spring Boot community
- PostgreSQL project

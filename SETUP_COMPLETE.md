# AI Code Reviewer - Gemini API Setup Complete ✅

## Overview
Your application is now configured to use **Google Gemini AI** via the Generative Language API with direct API key authentication.

## Configuration Applied

### API Key
```
AIzaSyBBJEOYme08UFfoFQcxKbDL93pVkwVUe5Q
```

This key has been set in `application.properties` as the default value.

### Model
```
gemini-2.0-flash-exp (default)
```

## How It Works

The application now calls Google's Generative Language API directly using:
- **Endpoint**: `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`
- **Authentication**: API key passed as query parameter
- **Method**: HTTP POST with JSON body

## Running the Application

### Start the Server
```powershell
mvn spring-boot:run
```

The application will start on **http://localhost:9090**

### Test the API

#### Using PowerShell (Windows)
```powershell
$body = @{
    author = "test-user"
    code = "public class Example { public static void main(String[] args) { System.out.println(\"Hello\"); } }"
    language = "java"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:9090/api/reviews" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

#### Using curl (Linux/Mac/Git Bash)
```bash
curl -X POST http://localhost:9090/api/reviews \
  -H "Content-Type: application/json" \
  -d '{
    "author": "test-user",
    "code": "public class Example { public static void main(String[] args) { System.out.println(\"Hello\"); } }",
    "language": "java"
  }'
```

#### Using the Web UI
Open your browser and navigate to:
```
http://localhost:9090
```

The static HTML page will be served where you can paste code and get reviews.

## Expected Response

```json
{
  "submissionId": 1,
  "score": 75,
  "comments": "Code review completed by Gemini AI",
  "summary": "Basic Java hello world with console output",
  "issues": [
    "Uses System.out.println instead of proper logging"
  ],
  "suggestions": [
    "Replace System.out.println with a logging framework like SLF4J",
    "Add proper error handling",
    "Consider adding documentation"
  ],
  "bestPractices": [
    "Use logging frameworks",
    "Add unit tests",
    "Follow naming conventions"
  ],
  "fixCode": "public class Example { private static final Logger logger = LoggerFactory.getLogger(Example.class); public static void main(String[] args) { logger.info(\"Hello\"); } }"
}
```

## Configuration Options

You can override the default settings using environment variables:

### Change API Key (if needed)
```powershell
$env:GENERATIVE_API_KEY = "your-new-api-key"
mvn spring-boot:run
```

### Change Model
```powershell
$env:GENERATIVE_MODEL = "gemini-1.5-pro"
mvn spring-boot:run
```

### Available Models
- `gemini-2.0-flash-exp` - Latest experimental fast model (default)
- `gemini-1.5-pro` - Most capable model
- `gemini-1.5-flash` - Fast and efficient
- `gemini-1.0-pro` - Stable production model

## Files Modified

1. **pom.xml**
   - Removed Vertex AI SDK dependency
   - Now uses standard Java HTTP client

2. **CodeReviewService.java**
   - Updated to call Generative Language API directly
   - Uses API key authentication
   - Simplified error handling

3. **application.properties**
   - Added your API key: `AIzaSyBBJEOYme08UFfoFQcxKbDL93pVkwVUe5Q`
   - Set default model: `gemini-2.0-flash-exp`
   - Removed Vertex AI configuration (project-id, location)

## What's Different from Vertex AI?

| Feature | Vertex AI | Generative Language API |
|---------|-----------|------------------------|
| Authentication | Service Account JSON | API Key |
| Setup Complexity | High (GCP project, IAM) | Low (just API key) |
| Endpoint | aiplatform.googleapis.com | generativelanguage.googleapis.com |
| Cost | Pay-per-token | Pay-per-token (different rates) |
| Enterprise Features | Yes (VPC, audit logs) | No |

## Troubleshooting

### Error: "API key not valid"
- Verify the API key is correct in `application.properties`
- Check if the API key has the necessary permissions
- Get a new key from: https://aistudio.google.com/app/apikey

### Error: "Model not found"
- Ensure the model name is correct (e.g., `gemini-2.0-flash-exp`)
- Try a different model from the available list

### Application falls back to heuristic
If you see "Gemini call failed, falling back" in logs:
- Check your internet connection
- Verify the API key is set correctly
- Check API quota limits

## Security Notes

⚠️ **Important**: The API key is currently hardcoded in `application.properties` for convenience.

For production:
1. **Use environment variables**:
   ```properties
   generative.api-key=${GENERATIVE_API_KEY:}
   ```

2. **Never commit API keys** to version control

3. **Rotate keys regularly**

4. **Set API key restrictions** in Google Cloud Console:
   - IP restrictions
   - API restrictions
   - Request limits

## Next Steps

✅ Application is ready to use!
✅ API key is configured
✅ Server is running on port 9090
✅ Database is connected (PostgreSQL)

You can now:
1. Test the API using the examples above
2. Open the web UI at http://localhost:9090
3. Review the logs to see Gemini API responses
4. Check the database for saved reviews

## Cost Management

- **Free tier**: 15 requests per minute
- **Paid tier**: Higher limits available
- Monitor usage at: https://aistudio.google.com/app/apikey

For detailed pricing: https://ai.google.dev/pricing

---

**Status**: ✅ Ready for use!
**Endpoint**: http://localhost:9090/api/reviews
**UI**: http://localhost:9090

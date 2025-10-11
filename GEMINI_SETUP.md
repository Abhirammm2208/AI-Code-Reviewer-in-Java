# Google Gemini / Vertex AI Setup Guide

This project now uses **Google Cloud Vertex AI** with the Gemini model for AI-powered code reviews.

## Prerequisites

1. **Google Cloud Project**: You need an active Google Cloud project with Vertex AI API enabled
2. **Service Account**: Create a service account with Vertex AI User role
3. **Authentication**: Download the service account JSON key file

## Setup Steps

### 1. Enable Vertex AI API

```bash
gcloud services enable aiplatform.googleapis.com
```

### 2. Create Service Account (if you don't have one)

```bash
gcloud iam service-accounts create ai-code-reviewer \
    --display-name="AI Code Reviewer Service Account"
```

### 3. Grant Vertex AI User Role

```bash
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
    --member="serviceAccount:ai-code-reviewer@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/aiplatform.user"
```

### 4. Download Service Account Key

```bash
gcloud iam service-accounts keys create ~/ai-code-reviewer-key.json \
    --iam-account=ai-code-reviewer@YOUR_PROJECT_ID.iam.gserviceaccount.com
```

### 5. Set Environment Variables

#### On Windows (PowerShell):
```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\path\to\ai-code-reviewer-key.json"
$env:GOOGLE_CLOUD_PROJECT = "your-project-id"
$env:GOOGLE_CLOUD_LOCATION = "us-central1"  # Optional, defaults to us-central1
```

#### On Linux/Mac (Bash):
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/ai-code-reviewer-key.json"
export GOOGLE_CLOUD_PROJECT="your-project-id"
export GOOGLE_CLOUD_LOCATION="us-central1"  # Optional
```

### 6. Update application.properties

The `application.properties` file already has the configuration ready. Just ensure these values match your setup:

```properties
# Google Cloud Project ID
generative.project-id=${GOOGLE_CLOUD_PROJECT:your-project-id-here}

# Region (default: us-central1)
generative.location=${GOOGLE_CLOUD_LOCATION:us-central1}

# Gemini model (options: gemini-2.0-flash-exp, gemini-1.5-pro, gemini-1.5-flash, etc.)
generative.model=${GENERATIVE_MODEL:gemini-2.0-flash-exp}
```

## Running the Application

### Option 1: Using Maven (with environment variables set)

```bash
mvn spring-boot:run
```

### Option 2: Using Java with inline environment variables (PowerShell)

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\key.json"
$env:GOOGLE_CLOUD_PROJECT="your-project-id"
mvn spring-boot:run
```

### Option 3: Packaged JAR

```bash
mvn clean package -DskipTests
java -jar target/ai-code-reviewer-0.0.1-SNAPSHOT.jar
```

## Available Gemini Models

- `gemini-2.0-flash-exp` - Latest experimental fast model (default)
- `gemini-1.5-pro` - Most capable model
- `gemini-1.5-flash` - Fast and efficient
- `gemini-1.0-pro` - Stable production model

You can change the model by setting the `GENERATIVE_MODEL` environment variable:

```powershell
$env:GENERATIVE_MODEL = "gemini-1.5-pro"
```

## Testing the API

Once the application is running on `http://localhost:9090`, you can test it:

```bash
curl -X POST http://localhost:9090/api/reviews \
  -H "Content-Type: application/json" \
  -d '{
    "author": "test-user",
    "code": "public class Example { public static void main(String[] args) { System.out.println(\"Hello\"); } }",
    "language": "java"
  }'
```

## Troubleshooting

### Error: "Application Default Credentials not found"
- Ensure `GOOGLE_APPLICATION_CREDENTIALS` points to a valid service account JSON key
- Verify the file exists and is readable

### Error: "Permission denied" or "403 Forbidden"
- Check that your service account has the `roles/aiplatform.user` role
- Verify Vertex AI API is enabled: `gcloud services enable aiplatform.googleapis.com`

### Error: "Project ID not found"
- Set `GOOGLE_CLOUD_PROJECT` environment variable
- Or update `generative.project-id` in `application.properties`

### Error: "Model not found"
- Ensure the model name is correct (e.g., `gemini-2.0-flash-exp`)
- Check that the model is available in your selected region

## Cost Considerations

Vertex AI charges per request based on:
- Input tokens (characters in your code)
- Output tokens (characters in the review response)

Pricing varies by model. See: https://cloud.google.com/vertex-ai/pricing

## Security Best Practices

1. **Never commit** service account keys to version control
2. Use **Secret Manager** in production environments
3. Set **IAM policies** with least privilege
4. Rotate service account keys regularly
5. Use **Workload Identity** for GKE deployments

## Additional Resources

- [Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)
- [Gemini API Reference](https://cloud.google.com/vertex-ai/docs/generative-ai/model-reference/gemini)
- [Java SDK Documentation](https://cloud.google.com/java/docs/reference/google-cloud-vertexai/latest/overview)

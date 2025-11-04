# run-app.ps1
# Complete startup script that loads environment variables and runs the application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI Code Reviewer - Starting Application" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Load environment variables from .env
$envFile = Join-Path $PSScriptRoot ".env"

if (Test-Path $envFile) {
    Write-Host "[1/3] Loading environment variables..." -ForegroundColor Yellow
    
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        
        # Skip empty lines and comments
        if ($line -and !$line.StartsWith("#")) {
            $parts = $line -split '=', 2
            
            if ($parts.Count -eq 2) {
                $key = $parts[0].Trim()
                $value = $parts[1].Trim()
                
                [Environment]::SetEnvironmentVariable($key, $value, "Process")
                Write-Host "  ✓ $key" -ForegroundColor Green
            }
        }
    }
    
    Write-Host "`n[2/3] Verifying configuration..." -ForegroundColor Yellow
    
    # Verify required environment variables
    $required = @(
        "SPRING_DATASOURCE_URL",
        "SPRING_DATASOURCE_USERNAME",
        "SPRING_DATASOURCE_PASSWORD",
        "GENERATIVE_API_KEY"
    )
    
    $allSet = $true
    foreach ($var in $required) {
        $value = [Environment]::GetEnvironmentVariable($var, "Process")
        if ([string]::IsNullOrWhiteSpace($value)) {
            Write-Host "  ✗ $var is not set!" -ForegroundColor Red
            $allSet = $false
        } else {
            Write-Host "  ✓ $var is configured" -ForegroundColor Green
        }
    }
    
    if (!$allSet) {
        Write-Host "`nError: Some required environment variables are missing!" -ForegroundColor Red
        Write-Host "Please update your .env file with the correct values." -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "`n[3/3] Starting Spring Boot application..." -ForegroundColor Yellow
    Write-Host "--------------------------------------`n" -ForegroundColor Gray
    
    # Build Maven arguments with environment variables
    $mavenArgs = @(
        "spring-boot:run",
        "-Dspring-boot.run.jvmArguments=""-DSPRING_DATASOURCE_URL=$([Environment]::GetEnvironmentVariable('SPRING_DATASOURCE_URL', 'Process')) -DSPRING_DATASOURCE_USERNAME=$([Environment]::GetEnvironmentVariable('SPRING_DATASOURCE_USERNAME', 'Process')) -DSPRING_DATASOURCE_PASSWORD=$([Environment]::GetEnvironmentVariable('SPRING_DATASOURCE_PASSWORD', 'Process')) -DGENERATIVE_API_KEY=$([Environment]::GetEnvironmentVariable('GENERATIVE_API_KEY', 'Process')) -DGENERATIVE_MODEL=$([Environment]::GetEnvironmentVariable('GENERATIVE_MODEL', 'Process')) -DAI_PROVIDER=$([Environment]::GetEnvironmentVariable('AI_PROVIDER', 'Process')) -DJWT_SECRET=$([Environment]::GetEnvironmentVariable('JWT_SECRET', 'Process')) -DGOOGLE_CLIENT_ID=$([Environment]::GetEnvironmentVariable('GOOGLE_CLIENT_ID', 'Process')) -DGOOGLE_CLIENT_SECRET=$([Environment]::GetEnvironmentVariable('GOOGLE_CLIENT_SECRET', 'Process'))"""
    )
    
    # Run Maven Spring Boot
    & mvn $mavenArgs
    
} else {
    Write-Host "Error: .env file not found!" -ForegroundColor Red
    Write-Host "`nPlease follow these steps:" -ForegroundColor Yellow
    Write-Host "  1. Copy .env.example to .env" -ForegroundColor White
    Write-Host "  2. Edit .env and fill in your actual credentials" -ForegroundColor White
    Write-Host "  3. Run this script again`n" -ForegroundColor White
    exit 1
}

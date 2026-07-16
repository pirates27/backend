$baseUrl = "http://landlens-production-alb-1919392235.ap-south-1.elb.amazonaws.com"
$email = "testgovt_" + (Get-Date -Format "HHmmss") + "@landlens.com"

$regBody = @{
    email = $email
    password = "Password123"
    firstName = "Govt"
    lastName = "Officer"
    phoneNumber = "9876543210"
    role = "GOVERNMENT_OFFICER"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
} catch {}

$loginBody = @{
    email = $email
    password = "Password123"
} | ConvertTo-Json

$session = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
Write-Host "Logged in as: $($session.role) with token length: $($session.accessToken.Length)"

$headers = @{ Authorization = "Bearer $($session.accessToken)" }

try {
    $dashboard = Invoke-RestMethod -Uri "$baseUrl/api/analytics/dashboard" -Headers $headers -Method Get
    Write-Host "SUCCESS! Dashboard returned:" -ForegroundColor Green
    $dashboard | ConvertTo-Json
} catch {
    Write-Host "FAILED with StatusCode:" $_.Exception.Response.StatusCode -ForegroundColor Red
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    Write-Host "Response Body:" $reader.ReadToEnd() -ForegroundColor Red
}

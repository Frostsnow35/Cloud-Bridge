$headers = @{
    "Content-Type" = "application/json"
}
$body = @{
    "message" = "我想找专利"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" -Method Post -Headers $headers -Body $body -ErrorAction Stop
    Write-Host "AI Chat Response:"
    Write-Host ($response | ConvertTo-Json -Depth 5)
} catch {
    Write-Host "AI Chat Request Failed: $_"
    Write-Host $_.Exception.Response
}

try {
    # Test Library Get (Expecting null or error if ES is down, but let's see if the endpoint is reachable)
    # We use a random ID, it will likely return null/empty string, but status code should be 200
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/libraries/policies/123" -Method Get -ErrorAction Stop
    Write-Host "Library Get Response Code: $($response.StatusCode)"
    Write-Host "Library Get Content: $($response.Content)"
} catch {
    Write-Host "Library Get Request Failed: $_"
}

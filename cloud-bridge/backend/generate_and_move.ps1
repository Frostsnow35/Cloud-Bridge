
Write-Host "Starting data generation..."
Set-Location "E:\数据要素大赛作品\cloud-bridge\backend"

# Run python script
python data/generate_smart_data.py

if ($LASTEXITCODE -eq 0) {
    Write-Host "Python script executed successfully."
    
    # Check if files exist in root
    if (Test-Path "smart_achievements.json") {
        Write-Host "Moving smart_achievements.json to data/..."
        Move-Item -Path "smart_achievements.json" -Destination "data/smart_achievements.json" -Force
    } else {
        Write-Host "smart_achievements.json not found in root."
    }

    if (Test-Path "smart_demands.json") {
        Write-Host "Moving smart_demands.json to data/..."
        Move-Item -Path "smart_demands.json" -Destination "data/smart_demands.json" -Force
    } else {
        Write-Host "smart_demands.json not found in root."
    }
    
    # Check if files exist in data (maybe script put them there?)
    if (Test-Path "data/smart_achievements.json") {
         Write-Host "smart_achievements.json exists in data/."
    }
} else {
    Write-Host "Python script failed."
}

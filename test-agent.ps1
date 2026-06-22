Add-Type -AssemblyName System.Net.Http

$url = "http://localhost:8080/api/ai/agent/chat"
$sessionId = [Guid]::NewGuid().ToString()

function Test-AgentMessage($message, $description) {
    Write-Host "`n===============================================" -ForegroundColor Cyan
    Write-Host "TEST: $description" -ForegroundColor Cyan
    Write-Host "INPUT: $message" -ForegroundColor Yellow
    Write-Host "SESSION: $sessionId" -ForegroundColor Gray
    Write-Host "-----------------------------------------------"
    
    $client = New-Object System.Net.Http.HttpClient
    $client.Timeout = [TimeSpan]::FromSeconds(120)
    
    $body = @{
        sessionId = $sessionId
        message = $message
    } | ConvertTo-Json -Compress
    
    $content = New-Object System.Net.Http.StringContent($body, [Text.Encoding]::UTF8, "application/json")
    
    try {
        $response = $client.SendAsync(
            [System.Net.Http.HttpRequestMessage]@{
                Method = [System.Net.Http.HttpMethod]::Post
                RequestUri = $url
                Content = $content
            },
            [System.Net.Http.HttpCompletionOption]::ResponseHeadersRead
        ).Result
        
        $stream = $response.Content.ReadAsStreamAsync().Result
        $reader = New-Object System.IO.StreamReader($stream)
        
        $fullResponse = ""
        $tokenCount = 0
        $maxLines = 200
        $lineCount = 0
        
        while (!$reader.EndOfStream -and $lineCount -lt $maxLines) {
            $line = $reader.ReadLine()
            if ($line -eq $null) { break }
            $lineCount++
            
            if ($line.StartsWith("event:")) {
                $evt = $line.Substring(6).Trim()
                Write-Host "[EVENT: $evt]" -ForegroundColor Green
                if ($evt -eq "done") { break }
            }
            elseif ($line.StartsWith("data:")) {
                $data = $line.Substring(5).Trim()
                try {
                    $json = $data | ConvertFrom-Json
                    if ($json.sessionId) {
                        Write-Host "  sessionId=$($json.sessionId)" -ForegroundColor Gray
                    } elseif ($json.message) {
                        Write-Host "  ERROR: $($json.message)" -ForegroundColor Red
                    }
                } catch {
                    # It's a token piece
                    $fullResponse += $data
                    $tokenCount++
                    if ($tokenCount % 30 -eq 0) {
                        Write-Host "  [tokens: $tokenCount]" -ForegroundColor DarkGray
                    }
                }
            }
        }
        
        $reader.Close()
        $stream.Close()
        $response.Dispose()
        $client.Dispose()
        
        Write-Host "-----------------------------------------------"
        Write-Host "RESPONSE ($tokenCount tokens):" -ForegroundColor Green
        if ($fullResponse.Length -gt 500) {
            Write-Host $fullResponse.Substring(0, 500) + "..." -ForegroundColor White
        } else {
            Write-Host $fullResponse -ForegroundColor White
        }
        return $fullResponse
        
    } catch {
        Write-Host "ERROR: $_" -ForegroundColor Red
        return $null
    }
}

# Test 1: 模糊输入 -- 期望 Agent 追问
Write-Host "`n=== PHASE 1: 模糊输入测试 ===" -ForegroundColor Magenta
$r1 = Test-AgentMessage "我想要一种新材料" "模糊需求 - 应触发 Agent 追问"

# Test 2: 明确需求 -- 期望 Agent 调用工具
Write-Host "`n=== PHASE 2: 明确需求测试 ===" -ForegroundColor Magenta
$r2 = Test-AgentMessage "我需要一种耐高温陶瓷基复合材料，用于航空发动机叶片，耐温300度以上" "明确需求 - 应触发工具调用编排"

Write-Host "`n=== ALL TESTS DONE ===" -ForegroundColor Magenta

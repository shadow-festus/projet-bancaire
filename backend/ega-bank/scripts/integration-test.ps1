$ErrorActionPreference = 'Stop'
$baseUrl = 'http://localhost:8080/api'

function LoginOrRegister {
    try {
        Write-Output "Attempting login..."
        $login = Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/login" -ContentType 'application/json' -Body (@{username='admin';password='password123'} | ConvertTo-Json) -ErrorAction Stop
        Write-Output "Login successful"
        return $login
    } catch {
        Write-Output "Login failed, registering admin..."
        Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/register" -ContentType 'application/json' -Body (@{username='admin';email='admin@egabank.com';password='password123'} | ConvertTo-Json) -ErrorAction Stop | Out-Null
        Start-Sleep -Milliseconds 300
        $login = Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/login" -ContentType 'application/json' -Body (@{username='admin';password='password123'} | ConvertTo-Json) -ErrorAction Stop
        Write-Output "Login after register successful"
        return $login
    }
}

try {
    $login = LoginOrRegister
    $token = $login.accessToken
    if (-not $token) { throw 'No access token received' }
    $headers = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }

    Write-Output "\n=== Creating client ==="
    $clientBody = @{ nom='Dupont'; prenom='Jean'; dateNaissance='1990-05-15'; sexe='MASCULIN'; adresse='123 Rue de Lomé'; telephone='+22890123456'; courriel='jean.dupont@email.com'; nationalite='Togolaise' }
    $client = Invoke-RestMethod -Method Post -Uri "$baseUrl/clients" -Headers $headers -Body (ConvertTo-Json $clientBody) -ErrorAction Stop
    Write-Output "CLIENT CREATED:"; $client | ConvertTo-Json -Depth 5
    $clientId = $client.id

    Write-Output "\n=== Creating account for client $clientId ==="
    $accBody = @{ typeCompte='EPARGNE'; clientId = $clientId }
    $account = Invoke-RestMethod -Method Post -Uri "$baseUrl/accounts" -Headers $headers -Body (ConvertTo-Json $accBody) -ErrorAction Stop
    Write-Output "ACCOUNT CREATED:"; $account | ConvertTo-Json -Depth 5
    $accountNumber = $account.numeroCompte
    if (-not $accountNumber) { $accountNumber = $account.numero; if (-not $accountNumber) { $accountNumber = $account.accountNumber } }

    Write-Output "\n=== Deposit to account $accountNumber ==="
    # Use ASCII description to avoid PowerShell/JSON encoding issues when posting to the API
    $depBody = @{ montant = 100000; description = 'Initial deposit' }
    $deposit = Invoke-RestMethod -Method Post -Uri "$baseUrl/transactions/$accountNumber/deposit" -Headers $headers -Body (ConvertTo-Json $depBody) -ErrorAction Stop
    Write-Output "DEPOSIT RESPONSE:"; $deposit | ConvertTo-Json -Depth 5

    Write-Output "\n=== Fetch accounts for client $clientId ==="
    $accountsClient = Invoke-RestMethod -Method Get -Uri "$baseUrl/accounts/client/$clientId" -Headers $headers -ErrorAction Stop
    $accountsClient | ConvertTo-Json -Depth 5

    Write-Output "\nAll steps completed successfully."
} catch {
    Write-Output "ERROR: $($_.Exception.Message)"
    exit 1
}

# Tes otomatis semua endpoint utama Ikobana Backend.
# Pastikan backend sudah jalan dulu (profil h2 disarankan), lalu jalankan:
#   powershell -ExecutionPolicy Bypass -File .\test-api.ps1

$Base = "http://localhost:8080/api"
$script:Pass = 0
$script:Fail = 0

# Kirim request dan kembalikan @{ Status; Body } tanpa melempar error untuk status 4xx/5xx.
function Send-Request($Method, $Path, $Body = $null, $Token = $null) {
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $params = @{ Uri = "$Base$Path"; Method = $Method; Headers = $headers; UseBasicParsing = $true }
    if ($Body) {
        $params["Body"] = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 5))
        $params["ContentType"] = "application/json; charset=utf-8"
    }
    try {
        $res = Invoke-WebRequest @params
        return @{ Status = [int]$res.StatusCode; Body = ($res.Content | ConvertFrom-Json) }
    } catch {
        $resp = $_.Exception.Response
        if ($null -eq $resp) { throw "Backend tidak bisa dihubungi di $Base. Sudah dijalankan?" }
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
        $text = $reader.ReadToEnd()
        $json = $null
        try { $json = $text | ConvertFrom-Json } catch {}
        return @{ Status = [int]$resp.StatusCode; Body = $json }
    }
}

function Check($Name, $Result, $Expected) {
    if ($Result.Status -eq $Expected) {
        Write-Host "  [OK]    $Name ($($Result.Status))" -ForegroundColor Green
        $script:Pass++
    } else {
        $msg = if ($Result.Body) { $Result.Body.message } else { "" }
        Write-Host "  [GAGAL] $Name - dapat $($Result.Status), harusnya $Expected. $msg" -ForegroundColor Red
        $script:Fail++
    }
}

$stamp = Get-Date -Format "HHmmss"

Write-Host "`n== Auth" -ForegroundColor Cyan
$r = Send-Request POST "/auth/login" @{ email = "admin@ikobana.com"; password = "Admin@12345" }
Check "Login admin" $r 200
$adminToken = $r.Body.data.data.token

$custEmail = "customer$stamp@ikobana.test"
$custPass = "Customer123"
$r = Send-Request POST "/auth/register" @{
    email = $custEmail; username = "customer$stamp"; password = $custPass; confirmPassword = $custPass
    fullName = "Customer Tes"; phone = "081234567891"
}
Check "Register customer baru" $r 201

$r = Send-Request POST "/auth/login" @{ email = $custEmail; password = $custPass }
Check "Login customer" $r 200
$custToken = $r.Body.data.data.token

Check "Login dengan password salah ditolak" (Send-Request POST "/auth/login" @{ email = $custEmail; password = "salah12345" }) 401
Check "Lihat profil sendiri (/auth/me)" (Send-Request GET "/auth/me" $null $custToken) 200
Check "Akses /auth/me tanpa token ditolak" (Send-Request GET "/auth/me") 401

Write-Host "`n== Produk" -ForegroundColor Cyan
$r = Send-Request POST "/products" @{
    sku = "TES-$stamp"; name = "Nugget Ayam Tes"; category = "OLAHAN_AYAM"
    priceRetail = 25000; priceModal = 18000; minimumStock = 5; warehouse = "Gudang A"
} $adminToken
Check "Admin tambah produk" $r 201
$productId = $r.Body.data.id

Check "Customer tidak boleh tambah produk" (Send-Request POST "/products" @{
    sku = "X-$stamp"; name = "Tidak Boleh"; category = "LAINNYA"; priceRetail = 1; priceModal = 1; minimumStock = 1
} $custToken) 403

Check "Admin tambah stok" (Send-Request PUT "/products/$productId/stock" @{ quantity = 20; reason = "Stok awal" } $adminToken) 200
Check "Lihat daftar produk (tanpa login)" (Send-Request GET "/products") 200
Check "Lihat detail produk" (Send-Request GET "/products/$productId") 200
Check "Cari produk" (Send-Request GET "/products/search?keyword=Nugget") 200
Check "Admin lihat produk stok menipis" (Send-Request GET "/products/low-stock" $null $adminToken) 200
Check "Produk yang tidak ada -> 404" (Send-Request GET "/products/999999") 404

Write-Host "`n== Transaksi" -ForegroundColor Cyan
$r = Send-Request POST "/transactions" @{
    customerName = "Customer Tes"; customerPhone = "081234567891"
    items = @(@{ productId = $productId; quantity = 2 }); paymentMethod = "QRIS"
} $custToken
Check "Customer buat transaksi" $r 201
$trxId = $r.Body.data.id

$stock = (Send-Request GET "/products/$productId").Body.data.stock
if ($stock -eq 18) { Write-Host "  [OK]    Stok berkurang jadi 18" -ForegroundColor Green; $script:Pass++ }
else { Write-Host "  [GAGAL] Stok harusnya 18, dapat $stock" -ForegroundColor Red; $script:Fail++ }

Check "Beli melebihi stok ditolak" (Send-Request POST "/transactions" @{
    customerName = "Customer Tes"; customerPhone = "081234567891"
    items = @(@{ productId = $productId; quantity = 9999 }); paymentMethod = "QRIS"
} $custToken) 400

Check "Customer lihat transaksinya" (Send-Request GET "/transactions" $null $custToken) 200
Check "Lihat detail transaksi" (Send-Request GET "/transactions/$trxId" $null $custToken) 200
Check "Admin cari transaksi" (Send-Request GET "/transactions/admin/search?keyword=Customer" $null $adminToken) 200
Check "Admin ubah status bayar" (Send-Request PUT "/transactions/$trxId/payment-status" @{ paymentStatus = "COMPLETED" } $adminToken) 200
Check "Admin ubah status kirim" (Send-Request PUT "/transactions/$trxId/delivery-status" @{ deliveryStatus = "SHIPPED" } $adminToken) 200
Check "Admin lihat laporan" (Send-Request GET "/transactions/report/date-range?startDate=2020-01-01T00:00:00&endDate=2099-12-31T23:59:59" $null $adminToken) 200

Write-Host ""
$color = if ($script:Fail -eq 0) { "Green" } else { "Red" }
Write-Host "Hasil: $($script:Pass) berhasil, $($script:Fail) gagal" -ForegroundColor $color
if ($script:Fail -gt 0) { exit 1 }

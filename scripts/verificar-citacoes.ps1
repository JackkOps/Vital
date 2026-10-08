param(
    [string]$Documento = (Join-Path $PSScriptRoot '../docs/traducao-c-java.md'),
    [string]$Manifesto = (Join-Path $PSScriptRoot 'citacoes-c-java.json')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$raiz = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$expectativas = Get-Content -LiteralPath $Manifesto -Raw -Encoding UTF8 | ConvertFrom-Json
$texto = Get-Content -LiteralPath $Documento -Raw -Encoding UTF8
$citacoes = [regex]::Matches($texto, '\x60(?<arquivo>[^\x60 \r\n]+\.(?:java|c|h)):(?<linha>\d+)\x60')
if ($citacoes.Count -eq 0) { throw 'No citations found.' }

$fontesJava = @(Get-ChildItem (Join-Path $raiz 'backend/rotavital/src/main/java') -Recurse -File -Filter '*.java')
$visitadas = @{}
$conteudos = @{}
$falhas = @()
foreach ($citacao in $citacoes) {
    $arquivo = $citacao.Groups['arquivo'].Value
    $numero = [int]$citacao.Groups['linha'].Value
    $chave = '{0}:{1}' -f $arquivo, $numero
    $visitadas[$chave] = $true
    $esperado = $expectativas.PSObject.Properties[$chave]
    if ($null -eq $esperado -or [string]::IsNullOrWhiteSpace($esperado.Value)) {
        $falhas += "$chave - missing expected excerpt."
        continue
    }

    if ($arquivo.Contains('/')) {
        $caminho = [IO.Path]::GetFullPath((Join-Path $raiz $arquivo))
        if (-not $caminho.StartsWith($raiz + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
            $falhas += "$chave - path outside the repository."
            continue
        }
    } else {
        $candidatos = @($fontesJava | Where-Object { $_.Name -ceq $arquivo })
        if ($candidatos.Count -ne 1) {
            $falhas += "$chave - missing or ambiguous file."
            continue
        }
        $caminho = $candidatos[0].FullName
    }

    if (-not (Test-Path -LiteralPath $caminho -PathType Leaf)) {
        $falhas += "$chave - file does not exist."
        continue
    }
    if (-not $conteudos.ContainsKey($caminho)) {
        $conteudos[$caminho] = [IO.File]::ReadAllLines($caminho)
    }
    $linhas = $conteudos[$caminho]
    if ($numero -lt 1 -or $numero -gt $linhas.Length) {
        $falhas += "$chave - line outside the file."
    } elseif ($linhas[$numero - 1].Trim() -cne $esperado.Value) {
        $falhas += "$chave - expected '$($esperado.Value)', found '$($linhas[$numero - 1].Trim())'."
    }
}

foreach ($entrada in $expectativas.PSObject.Properties) {
    if (-not $visitadas.ContainsKey($entrada.Name)) {
        $falhas += "$($entrada.Name) - expectation has no citation in the document."
    }
}
if ($falhas.Count -gt 0) {
    $falhas | ForEach-Object { Write-Output $_ }
    throw ('Verification failed: {0} mismatches.' -f $falhas.Count)
}
Write-Output ('OK: {0} citations checked ({1} distinct references), no mismatches.' -f $citacoes.Count, $visitadas.Count)

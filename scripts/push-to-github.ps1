<#
PowerShell helper to safely push this project to a GitHub repository.
Designed for PowerShell 5.1 on Windows. This script will prompt before destructive actions.
Usage: Open PowerShell, run as your user (not admin), and:
    cd D:\yestanger\tbMini
    .\scripts\push-to-github.ps1
#>

param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Prompt-YesNo([string]$msg, [switch]$defaultYes) {
    $yn = if ($defaultYes) { "[Y/n]" } else { "[y/N]" }
    while ($true) {
        $r = Read-Host "$msg $yn"
        if ([string]::IsNullOrWhiteSpace($r)) { return $defaultYes.IsPresent }
        if ($r -match '^[yY](es)?$') { return $true }
        if ($r -match '^[nN](o)?$') { return $false }
    }
}

function Invoke-Git {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$Args
    )

    & git @Args
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Args -join ' ') failed with exit code $LASTEXITCODE."
    }
}

function Get-GitOutput {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$Args
    )

    $output = & git @Args 2>$null
    if ($LASTEXITCODE -ne 0) {
        return $null
    }
    return (($output | Out-String).Trim())
}

# Defaults
$repoPathDefault = "D:\yestanger\tbMini"
$remoteDefault = "https://github.com/Erica-233/tbMini.git"
$branchDefault = "main"

$repoPath = Read-Host "Path to local repo (press Enter for default: $repoPathDefault)"
if ([string]::IsNullOrWhiteSpace($repoPath)) { $repoPath = $repoPathDefault }

if (-not (Test-Path $repoPath)) {
    Write-Error "Path '$repoPath' does not exist. Please check and run again."; exit 1
}

Set-Location $repoPath

# Check git
& git --version | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "git is not installed or not in PATH. Install Git for Windows and try again."; exit 1
}

# Show current git user
$gitName = Get-GitOutput config --global user.name
$gitEmail = Get-GitOutput config --global user.email
if ($gitName -and $gitEmail) {
    Write-Host "Git global user.name=$gitName user.email=$gitEmail"
} else {
    Write-Warning "Git global identity is incomplete. Set user.name and user.email before creating commits."
}

# Check .git
$hasGit = Test-Path (Join-Path $repoPath ".git")
if (-not $hasGit) {
    if (Prompt-YesNo "No .git found in the folder. Initialize a new git repository?" -defaultYes) {
        Invoke-Git init
        Write-Host "Initialized new git repository."
    } else {
        Write-Error "Aborting. Please initialize git manually and re-run this script."; exit 1
    }
} else {
    Write-Host ".git directory exists. Using existing repository."
}

# Ensure required identity exists before any commit path.
$canCommit = [bool]($gitName -and $gitEmail)

# Ensure .gitignore exists
if (-not (Test-Path (Join-Path $repoPath ".gitignore"))) {
    if (Prompt-YesNo "No .gitignore found. Create a sensible default .gitignore?" -defaultYes) {
        @"
# Basic .gitignore created by push-to-github.ps1
/target/
/frontend/node_modules/
/backend/uploads/
/.idea/
.env
"@ | Out-File -FilePath .gitignore -Encoding UTF8
        if ($canCommit) {
            Invoke-Git add .gitignore
            Invoke-Git commit -m "Add .gitignore"
            Write-Host "Created and committed .gitignore"
        } else {
            Write-Warning "Created .gitignore, but skipped commit because git identity is not configured."
        }
    }
}

# Large files check (>100MB)
Write-Host "Scanning for files > 100MB (this may take a moment)..."
$bigFiles = Get-ChildItem -Path $repoPath -Recurse -File -Force -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -notmatch '\\(\.git|node_modules|target|dist)\\' -and $_.Length -gt 100MB } |
    Select-Object FullName,Length
if ($bigFiles) {
    Write-Host "Found large files:"; $bigFiles | ForEach-Object { Write-Host "  $($_.FullName) - $([math]::Round($_.Length/1MB,2)) MB" }
    Write-Host "If these files are not intended, remove them or use Git LFS before pushing."
    if (Prompt-YesNo "Run 'git lfs install' and track patterns? (You must have Git LFS installed)" ) {
        Invoke-Git lfs install
        Write-Host "Git LFS initialized. You'll need to 'git lfs track' specific patterns and re-commit large files manually."
    }
}

# Remote handling
$remotes = & git remote -v 2>$null
Write-Host "Existing remotes:"; if ($remotes) { Write-Host $remotes } else { Write-Host "<none>" }

$remoteUrl = Read-Host "Enter remote URL (press Enter for default: $remoteDefault)"
if ([string]::IsNullOrWhiteSpace($remoteUrl)) { $remoteUrl = $remoteDefault }

$originExists = ((& git remote) -contains 'origin')
if ($originExists) {
    Write-Host "Remote 'origin' already exists."
    $currentOrigin = Get-GitOutput remote get-url origin
    if ($currentOrigin -and ($currentOrigin -ne $remoteUrl)) {
        Write-Warning "Current origin is '$currentOrigin', which does not match the target '$remoteUrl'."
        if (Prompt-YesNo "Replace the existing origin URL with the target repository?" ) {
            Invoke-Git remote set-url origin $remoteUrl
            Write-Host "Updated origin URL."
        } else {
            Write-Error "Aborting to avoid pushing to the wrong repository."; exit 1
        }
    } else {
        Write-Host "origin already points to the target repository."
    }
} else {
    Invoke-Git remote add origin $remoteUrl
    Write-Host "Added remote origin -> $remoteUrl"
}

# Check for any commits
$hasCommits = $true
& git rev-parse --verify HEAD > $null 2>&1
if ($LASTEXITCODE -ne 0) {
    $hasCommits = $false
}

if (-not $hasCommits) {
    if (-not $canCommit) {
        Write-Error "This repository has no commits yet, but git user.name/user.email are not configured. Set them before rerunning this script."; exit 1
    }
    Write-Host "No commits yet. Creating initial commit."
    Invoke-Git add .
    Invoke-Git commit -m "Initial import from local project"
    Write-Host "Committed."
} else {
    Write-Host "Repository already has commits. You can still push existing history."
    if (Prompt-YesNo "Stage all changes and create a new commit?" ) {
        if (-not $canCommit) {
            Write-Error "Git user.name/user.email are not configured, so a new commit cannot be created."; exit 1
        }
        Invoke-Git add .
        $msg = Read-Host "Enter commit message (default: 'Update from local')"
        if ([string]::IsNullOrWhiteSpace($msg)) { $msg = "Update from local" }
        Invoke-Git commit -m $msg
        Write-Host "Committed changes."
    }
}

# Branch and push
$branch = Read-Host "Branch to push (default: $branchDefault)"
if ([string]::IsNullOrWhiteSpace($branch)) { $branch = $branchDefault }

# Confirm branch name locally only if needed.
$currentBranch = Get-GitOutput branch --show-current
if ($currentBranch -and ($currentBranch -ne $branch)) {
    if (Prompt-YesNo "Rename local branch '$currentBranch' to '$branch' before pushing?" ) {
        Invoke-Git branch -M $branch
    } else {
        Write-Warning "Keeping local branch '$currentBranch'. The push will still target remote branch '$branch'."
    }
}

if (Prompt-YesNo "Push to remote 'origin' branch '$branch' now?" -defaultYes) {
    Write-Host "About to push. If using HTTPS you will be prompted for credentials (username and PAT as password)."
    Invoke-Git push --set-upstream origin "HEAD:$branch"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Push successful. Repository available at: $remoteUrl"
    } else {
        Write-Error "Push failed. Inspect output above. Common issues: permissions, remote exists with different history (non-fast-forward), or network.";
    }
} else {
    Write-Host "Skipped push. You can push later with: git push --set-upstream origin $branch"
}

Write-Host "Done."


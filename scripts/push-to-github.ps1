<#
PowerShell helper to safely push this project to a GitHub repository.
Designed for PowerShell 5.1 on Windows. This script will prompt before destructive actions.
Usage: Open PowerShell, run as your user (not admin), and:
    cd D:\yestanger\tbMini
    .\scripts\push-to-github.ps1
#>

param()

function Prompt-YesNo([string]$msg, [switch]$defaultYes) {
    $yn = if ($defaultYes) { "[Y/n]" } else { "[y/N]" }
    while ($true) {
        $r = Read-Host "$msg $yn"
        if ([string]::IsNullOrWhiteSpace($r)) { return $defaultYes.IsPresent }
        if ($r -match '^[yY](es)?$') { return $true }
        if ($r -match '^[nN](o)?$') { return $false }
    }
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
try {
    git --version | Out-Null
} catch {
    Write-Error "git is not installed or not in PATH. Install Git for Windows and try again."; exit 1
}

# Show current git user
$gitName = git config --global user.name 2>$null
$gitEmail = git config --global user.email 2>$null
Write-Host "Git global user.name=$gitName user.email=$gitEmail"

# Check .git
$hasGit = Test-Path (Join-Path $repoPath ".git")
if (-not $hasGit) {
    if (Prompt-YesNo "No .git found in the folder. Initialize a new git repository?" -defaultYes) {
        git init
        Write-Host "Initialized new git repository."
    } else {
        Write-Error "Aborting. Please initialize git manually and re-run this script."; exit 1
    }
} else {
    Write-Host ".git directory exists. Using existing repository."
}

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
        git add .gitignore
        git commit -m "Add .gitignore" -q
        Write-Host "Created and committed .gitignore"
    }
}

# Large files check (>100MB)
Write-Host "Scanning for files > 100MB (this may take a moment)..."
$bigFiles = Get-ChildItem -Path $repoPath -Recurse -ErrorAction SilentlyContinue | Where-Object { -not $_.PSIsContainer -and $_.Length -gt 100MB } | Select-Object FullName,Length
if ($bigFiles) {
    Write-Host "Found large files:"; $bigFiles | ForEach-Object { Write-Host "  $($_.FullName) - $([math]::Round($_.Length/1MB,2)) MB" }
    Write-Host "If these files are not intended, remove them or use Git LFS before pushing."
    if (Prompt-YesNo "Run 'git lfs install' and track patterns? (You must have Git LFS installed)" ) {
        git lfs install
        Write-Host "Git LFS initialized. You'll need to 'git lfs track' specific patterns and re-commit large files manually."
    }
}

# Remote handling
$remotes = git remote -v 2>$null
Write-Host "Existing remotes:"; if ($remotes) { Write-Host $remotes } else { Write-Host "<none>" }

$remoteUrl = Read-Host "Enter remote URL (press Enter for default: $remoteDefault)"
if ([string]::IsNullOrWhiteSpace($remoteUrl)) { $remoteUrl = $remoteDefault }

$originExists = (git remote | Select-String -Pattern "^origin$" -Quiet)
if ($originExists) {
    Write-Host "Remote 'origin' already exists."
    if (Prompt-YesNo "Rename existing 'origin' to 'origin-backup' and set the new remote URL as 'origin'? (safe)" ) {
        git remote rename origin origin-backup
        git remote add origin $remoteUrl
        Write-Host "Renamed origin -> origin-backup and added new origin"
    } else {
        if (Prompt-YesNo "Replace URL of existing 'origin' with the new remote URL?" ) {
            git remote set-url origin $remoteUrl
            Write-Host "Updated origin URL."
        } else {
            Write-Host "Keeping existing origin. Will push to existing origin unless you change it later."
        }
    }
} else {
    git remote add origin $remoteUrl
    Write-Host "Added remote origin -> $remoteUrl"
}

# Check for any commits
$hasCommits = $true
try {
    git rev-parse --verify HEAD > $null 2>&1
} catch {
    $hasCommits = $false
}

if (-not $hasCommits) {
    Write-Host "No commits yet. Creating initial commit."
    git add .
    git commit -m "Initial import from local project" -q
    Write-Host "Committed."
} else {
    Write-Host "Repository already has commits. You can still push existing history."
    if (Prompt-YesNo "Stage all changes and create a new commit?" ) {
        git add .
        $msg = Read-Host "Enter commit message (default: 'Update from local')"
        if ([string]::IsNullOrWhiteSpace($msg)) { $msg = "Update from local" }
        git commit -m "$msg" -q
        Write-Host "Committed changes."
    }
}

# Branch and push
$branch = Read-Host "Branch to push (default: $branchDefault)"
if ([string]::IsNullOrWhiteSpace($branch)) { $branch = $branchDefault }

# Set branch name locally
try { git branch -M $branch } catch { }

if (Prompt-YesNo "Push to remote 'origin' branch '$branch' now?" -defaultYes) {
    Write-Host "About to push. If using HTTPS you will be prompted for credentials (username and PAT as password)."
    git push --set-upstream origin $branch
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Push successful. Repository available at: $remoteUrl"
    } else {
        Write-Error "Push failed. Inspect output above. Common issues: permissions, remote exists with different history (non-fast-forward), or network.";
    }
} else {
    Write-Host "Skipped push. You can push later with: git push --set-upstream origin $branch"
}

Write-Host "Done."


# Architecture

## Data Flow
CBSL Website (PDF)
↓
fetchPdfForDate() — HTTP client with retries
↓
parsePriceReport() — PDF parsing
↓
saveReport() — JSON to disk
↓
Ktor API — Serve JSON


## Components

| Component | Purpose |
|-----------|---------|
| `DailyScheduler` | Timer-based daily trigger (9 AM SL time) |
| `fetchPdfForDate()` | Downloads PDF from CBSL with retry logic |
| `parsePriceReport()` | Extracts data from PDF |
| `saveReport()` | Saves JSON to `data/` folder |
| `cleanupOldReports()` | Keeps only last 3 days |

## Hosting

| Service | Purpose |
|---------|---------|
| Render | Free web hosting |
| cron-job.org | Free cron scheduling |
| CBSL | Data source |



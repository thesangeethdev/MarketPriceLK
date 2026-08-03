# MarketPriceLK

Daily Sri Lankan market price reports from the Central Bank of Sri Lanka (CBSL).

## Overview

MarketPriceLK fetches daily price reports (PDF) from CBSL, parses them, and exposes the data via a REST API. The service runs on Render with scheduled daily updates at 9:00 AM Sri Lanka time.

## Features

- **Daily automatic fetching** — CBSL price reports fetched every day at 9:00 AM (Asia/Colombo)
- **REST API** — JSON endpoints for latest and historical price data
- **Backfill support** — Fetches last 3 available days on demand
- **Free hosting** — Runs on Render free tier with cron-job.org scheduling

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Health check / welcome message |
| `/latest` | GET | Latest available price report (JSON) |
| `/reports` | GET | List all available report dates |
| `/reports/{date}` | GET | Specific date's report (YYYYMMDD) |
| `/history` | GET | Last 3 reports combined (JSON) |
| `/run-now` | GET | Manually trigger fetch for today |
| `/backfill` | GET | Fetch last 3 available days |

## Tech Stack
- Kotlin + Ktor — Backend framework
- Render — Free cloud hosting
- cron-job.org — Free cron scheduling
- CBSL — Data source (Daily Price Report PDFs)

## Quick Start

```bash
# Get latest report
curl https://market-prices-lk.onrender.com/latest

# List available dates
curl https://market-prices-lk.onrender.com/reports

# Get specific date
curl https://market-prices-lk.onrender.com/reports/20260731

# Trigger manual fetch
curl https://market-prices-lk.onrender.com/run-now
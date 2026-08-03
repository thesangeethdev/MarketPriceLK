# API Endpoints

## Health Check

### `GET /`

Returns a welcome message.

**Response:**
```text
Hello market-price-lk!
```

## Latest Report
### `GET /latest`
```agsl
Returns the most recent available price report as JSON.
Response: 200 OK (JSON)
{
  "date": "2026-07-31",
  "items": [
    {
      "category": "Vegetables",
      "item": "Beans",
      "market": "Dambulla",
      "price": 335.00,
      "unit": "Rs/kg"
    }
  ]
}
```
```agsl
Error: 404 Not Found
No reports available
```

## List Reports
### `GET /reports`
```agsl
Lists all available report dates.
Response: 200 OK (text)
price_report_20260731.json
price_report_20260730.json
price_report_20260728.json

```


## Specific Date
### `GET /reports/{date}`
```agsl
Returns a specific date's report.
Parameter: date — YYYYMMDD format
Example: GET /reports/20260731
Response: 200 OK (JSON)
Error: 404 Not Found
Report not found for 20260731. Use /reports to see available dates
```

## History
### `GET /history`
```agsl
Returns the last 3 reports combined as a JSON array.
Response: 200 OK (JSON)
[
  {
    "date": "20260731",
    "data": { ... }
  },
  {
    "date": "20260730",
    "data": { ... }
  }
]
```
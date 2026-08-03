# Usage Examples

## cURL

### Get latest report
```
curl https://market-prices-lk.onrender.com/latest
```

### List all dates
```
curl https://market-prices-lk.onrender.com/reports
```
### Get specific date
```
curl https://market-prices-lk.onrender.com/reports/20260731
```

### JavaScript / Fetch
```
// Get latest report
const response = await fetch('https://market-prices-lk.onrender.com/latest');
const data = await response.json();
console.log(data);
```

### Python / Requests
import requests
```
# Get latest report
response = requests.get('https://market-prices-lk.onrender.com/latest')
data = response.json()
print(data)
```


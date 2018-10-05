# Monitoring Microservice

This is a simple REST microservice for basic monitoring of HTTP, HTTPS and TCP services.

## Add monitoring
`curl -X POST http://localhost:80/monitoring/ -H 'Content-Type: text/plain' -d tcp://www.google.de:12345`

## Delete monitoring
`curl -X DELETE http://localhost:80/monitoring/2`

## Get states as plaintext
`curl -X GET http://localhost:80/monitoring/ -H 'Accept: text/plain'`
```
[ UP ]	2018-10-06 00:30:49	http://www.google.de
[ UP ]	2018-10-06 00:30:49	https://www.google.de
[DOWN]	null	tcp://www.google.de:12345
[ UP ]	2018-10-06 00:30:49	tcp://www.google.de:80
```

## Get states in Browser
![Hello World](data:image/png;base64,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)

## Get states as JSON
`curl -X GET http://localhost:80/monitoring/ -H 'Accept: application/json'`
```
[
    {
        "serviceState": "Up",
        "lastSeen": {
            "date": {
                "year": 2018,
                "month": 10,
                "day": 6
            },
            "time": {
                "hour": 0,
                "minute": 32,
                "second": 10,
                "nano": 406000000
            }
        },
        "lastCheck": {
            "date": {
                "year": 2018,
                "month": 10,
                "day": 6
            },
            "time": {
                "hour": 0,
                "minute": 32,
                "second": 10,
                "nano": 406000000
            }
        },
        "id": 1,
        "url": "http://www.google.de"
    },
    {
        "serviceState": "Up",
        "lastSeen": {
            "date": {
                "year": 2018,
                "month": 10,
                "day": 6
            },
            "time": {
                "hour": 0,
                "minute": 32,
                "second": 10,
                "nano": 588000000
            }
        },
        "lastCheck": {
            "date": {
                "year": 2018,
                "month": 10,
                "day": 6
            },
            "time": {
                "hour": 0,
                "minute": 32,
                "second": 10,
                "nano": 588000000
            }
        },
        "id": 2,
        "url": "https://www.google.de"
    },
    {
        "serviceState": "Up",
        "lastSeen": {
            "date": {
                "year": 2018,
                "month": 10,
                "day": 6
            },
            "time": {
                "hour": 0,
                "minute": 32,
                "second": 10,
                "nano": 616000000
            }
        },
        "lastCheck": {
            "date": {
                "year": 2018,
                "month": 10,
                "day": 6
            },
            "time": {
                "hour": 0,
                "minute": 32,
                "second": 10,
                "nano": 616000000
            }
        },
        "id": 3,
        "url": "tcp://www.google.de:80"
    },
    {
        "serviceState": "Down",
        "lastCheck": {
            "date": {
                "year": 2018,
                "month": 10,
                "day": 6
            },
            "time": {
                "hour": 0,
                "minute": 32,
                "second": 31,
                "nano": 621000000
            }
        },
        "id": 4,
        "url": "tcp://www.google.de:12345"
    }
]
```

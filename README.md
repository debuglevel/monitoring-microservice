# Monitoring Microservice
This is a simple REST microservice for basic monitoring of HTTP, HTTPS, TCP and ICMP services.

# HTTP API
## Add monitoring
```
curl -X POST 'http://localhost:8080/monitorings/' -H 'Content-Type: application/json' \
 --data-raw '{
 	"url": "https://www.google.de",
 	"name": "Google"
 }'
```
```
{
  "id" : 5,
  "name" : "Google",
  "url" : "https://www.google.de",
  "serviceState" : "Unknown",
  "createdOn" : "2020-08-30T23:11:18.962",
  "lastModified" : "2020-08-30T23:11:18.962"
}
```

## Update monitoring
```
curl -X PUT 'http://localhost:8080/monitorings/1' -H 'Content-Type: application/json' \
 --data-raw '{
	"url": "https://www.yahoo.de",
	"name": "Yahoo"
 }'
```
```
{
  "id" : 1,
  "name" : "Yahoo",
  "url" : "https://www.yahoo.de",
  "serviceState" : "Unknown",
  "createdOn" : "2020-08-30T23:11:10.182",
  "lastModified" : "2020-08-30T23:12:14.729"
}
```

## Delete monitoring
`curl -X DELETE http://localhost:80/monitorings/2`

## Get states as JSON
`curl -X GET http://localhost:8080/monitorings/`
```
[
  {
    "id": 1,
    "url": "http://www.google.de",
    "ip": "172.217.23.3",
    "name": "Google HTTP",
    "createdOn": "2020-08-30T23:01:12.677",
    "lastCheck": "2020-08-30T23:01:19.607",
    "lastModified": "2020-08-30T23:01:42.173",
    "serviceState": "Up"
  },
  {
    "id": 2,
    "url": "https://www.google.de",
    "ip": "172.217.23.3",
    "name": "Google HTTPS",
    "createdOn": "2020-08-30T23:01:12.744",
    "lastCheck": "2020-08-30T23:01:21.098",
    "lastModified": "2020-08-30T23:01:42.191",
    "serviceState": "Up"
  },
  {
    "id": 3,
    "url": "tcp://www.google.de:80",
    "ip": "172.217.23.3",
    "name": "Google TCP 80",
    "createdOn": "2020-08-30T23:01:12.765",
    "lastCheck": "2020-08-30T23:01:21.123",
    "lastModified": "2020-08-30T23:01:42.197",
    "serviceState": "Up"
  },
  {
    "id": 4,
    "url": "tcp://www.google.de:12345",
    "ip": "172.217.23.3",
    "name": "Google TCP 12345",
    "createdOn": "2020-08-30T23:01:12.773",
    "lastCheck": "2020-08-30T23:01:42.128",
    "lastModified": "2020-08-30T23:01:42.204",
    "serviceState": "Down"
  }
]
```

## Get states as HTML
Point browser to `http://localhost:80/monitorings/html`.

## Get states as plaintext
`curl -X GET http://localhost:80/monitorings/plaintext`
```
[ UP ]	2018-10-06 00:30:49	http://www.google.de
[ UP ]	2018-10-06 00:30:49	https://www.google.de
[DOWN]	null	tcp://www.google.de:12345
[ UP ]	2018-10-06 00:30:49	tcp://www.google.de:80
```
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

# Migration from 0.0.x to 0.1.0
0.1.0 uses Hibernate to store data instead of MongoDB. If you already used version 0.0.x, you have to export all monitorings from 0.0.x and import them into 0.1.0. These two scripts should help you to do this:
```bash
#!/bin/bash

rm *.json

COUNT=$(curl --location --request GET 'http://HOST/monitorings/' --header 'Accept: application/json' | jq length)
echo "There are '$COUNT' objects"

for ((i=0; i<=COUNT-1; i++)); do
  echo Processing array entry $i...
  curl --location --request GET 'http://HOST/monitorings/' --header 'Accept: application/json' | jq ".[$i] | {name: .name, url: .url}" > $i.json
done
```

```bash
#!/bin/bash

for i in *.json; do
  echo Processing $i...
  curl --location --request POST 'http://HOST/monitorings/' \
    --header 'Content-Type: application/json' \
    --data @$i
done
```
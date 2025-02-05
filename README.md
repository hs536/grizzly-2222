Test app for https://github.com/eclipse-ee4j/grizzly/issues/2222

### Build & Run Server (on port 8080)
```
$ mvn clean install
$ java -jar target/grizzly-2222-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Setup Apache(httpd) as proxy
```
# cat > /etc/httpd/conf.d/grizzly.conf << EOF
LoadModule proxy_module modules/mod_proxy.so
LoadModule proxy_http_module modules/mod_proxy_http.so
Listen 8090
<VirtualHost *:8090>
  ProxyPreserveHost On
  ProxyPass / http://localhost:8080/
  ProxyPassReverse / http://localhost:8080/
</VirtualHost>
EOF
# systemctl restart httpd
```

### Reproduction
curl receives a 100-continue from Apache(httpd) and sends the body, but Apache(httpd) does not receive a 100-continue from Grizzly and times out.
```
$ curl -v -s -X POST -H "Expect: 100-continue" -H "Transfer-Encoding: chunked" --data-binary "chunk1\nchunk2\nchunk3\r\n\r\n" http://localhost:8090/grizzly
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8090 (#0)
> POST /grizzly HTTP/1.1
> Host: localhost:8090
> User-Agent: curl/7.61.1
> Accept: */*
> Expect: 100-continue
> Transfer-Encoding: chunked
> Content-Type: application/x-www-form-urlencoded
> 
* Done waiting for 100-continue
* Signaling end of chunked upload via terminating chunk.
< HTTP/1.1 408 Request Timeout
< Date: Wed, 05 Feb 2025 08:27:24 GMT
< Server: Apache/2.4.37 (Red Hat Enterprise Linux) OpenSSL/1.1.1k
< Connection: close
< Content-Type: text/html; charset=iso-8859-1
< 
<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
<html><head>
<title>408 Request Timeout</title>
</head><body>
<h1>Request Timeout</h1>
<p>Server timeout waiting for the HTTP request from the client.</p>
<p>Additionally, a 502 Bad Gateway
error was encountered while trying to use an ErrorDocument to handle the request.</p>
</body></html>
* Closing connection 0
```

### versions
```
$ java -version
openjdk version "11.0.2" 2019-01-15
OpenJDK Runtime Environment 18.9 (build 11.0.2+9)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)

$ mvn -version
Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
Maven home: /usr/share/maven
Java version: 11.0.2, vendor: Oracle Corporation, runtime: /usr/lib/jvm/jdk11
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "4.18.0-553.el8_10.x86_64", arch: "amd64", family: "unix"

$ httpd -v
Server version: Apache/2.4.37 (Red Hat Enterprise Linux)
Server built:   Feb 16 2024 04:23:20

$ curl -V
curl 7.61.1 (x86_64-redhat-linux-gnu) libcurl/7.61.1 OpenSSL/1.1.1k zlib/1.2.11 brotli/1.0.6 libidn2/2.2.0 libpsl/0.20.2 (+libidn2/2.2.0) libssh/0.9.6/openssl/zlib nghttp2/1.33.0
Release-Date: 2018-09-05
Protocols: dict file ftp ftps gopher http https imap imaps ldap ldaps pop3 pop3s rtsp scp sftp smb smbs smtp smtps telnet tftp 
Features: AsynchDNS IDN IPv6 Largefile GSS-API Kerberos SPNEGO NTLM NTLM_WB SSL libz brotli TLS-SRP HTTP2 UnixSockets HTTPS-proxy PSL 
```


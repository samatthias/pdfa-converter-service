# pdfa-converter-service
This is a very lightweight spring boot microservice which converts scanned pdf documents (jpg and ocr text) into valid PDF A-1 or PDF B-1 compliant archive PDF format.
It has two rest endpoints:

* /health [HTTP GET]
* /convertpdf [HTTP POST]

You can convert pdf files with curl in a one liner: 
```
curl -v -F "file=@\"foobar.pdf\"" http://localhost:8081/convertpdf > foobar_pdf_a-1.pdf
```


# Installation

1) Download newest release.
2) Run it with 
```
java -jar pdfaconverterservice-x.x.x.jar
```

# pdfa-converter-service

## Description
This is a very lightweight spring boot microservice which converts scanned pdf documents (jpg and ocr text) into valid PDF A-1 or PDF B-1 compliant archive PDF format.
It has two rest endpoints:

* /health [HTTP GET]
* /convertpdf [HTTP POST]

You can convert pdf files with curl in a one liner: 
```
curl -v -F "file=@\"foobar.pdf\"" http://localhost:8081/convertpdf > foobar_pdf_a-1.pdf
```

## PDF Archive Validation
You can do pdf archive validation with vera pdf gui.

## Missing features

* true type fonts other than "arial" not supported yet
* rgp icc profile other than the one inlcuded are not supported yet
* pdf documents mixed with text and pictures are not supported yet
* XMP Metadate can not be set correctly yet
* PDF A-2 and PDF B-2 format are not supported yet
* PDF A-3 and PDF B-3 format are not supported yet
* Error handling not stable yet
* Switch between PDF A-1 and PDF B-1 not supported yet

## Installation

Needs Java 17 or higher

1) Download newest release from release page

2) Run it with 
```
java -jar pdfaconverterservice-x.x.x.jar --spring.config.location=application-properties
```

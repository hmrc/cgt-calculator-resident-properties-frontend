# Frontend for the Capital Gains Tax Calculator for Resident Properties

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)[
![Build Status](https://travis-ci.org/hmrc/cgt-calculator-resident-properties-frontend.svg?branch=master)](https://travis-ci.org/hmrc/cgt-calculator-resident-properties-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/cgt-calculator-resident-properties-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/cgt-calculator-resident-properties-frontend/_latestVersion)

## Summary

This service provides end users with a mechanism for calculating their capital gains tax for properties owned by residents.

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

## Running the service

To run the microservices locally start the dependent microservices using service manager
<code>sm2 --start CGT_ALL</code> </br>
<code>sm2 --stop  CGT_CALC_RES_PROP_FRONTEND</code> </br>

Then from the root directory execute 
<code>sbt "run 9702"</code></br>

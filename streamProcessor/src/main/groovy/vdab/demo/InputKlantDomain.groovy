package vdab.demo

import groovy.transform.ToString

@ToString
class Klant {
    Integer ikl
    String naam
    String voornaam
String provincie
}

@ToString
class Provincie {
    Integer provincieid
    String naam
}

@ToString
class KlantProvincie
{
    Klant klant
    Provincie provincie
}


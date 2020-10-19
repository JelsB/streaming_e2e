package vdab.demo

import groovy.transform.ToString

class AggregateRecord {

    Integer MaxAantal=0
    Integer TotaalAantal=0
    Integer AantalVerzendingen=0
    Float GemiddeldAantal
    String kleur

    public doAggregation(Integer aantal){
        if(MaxAantal <aantal)
            MaxAantal=aantal

        TotaalAantal+=aantal
        AantalVerzendingen++

        if(AantalVerzendingen>0)
            GemiddeldAantal= TotaalAantal/AantalVerzendingen
    }
}
@ToString
class VMAS_KansOpWerk{
    KansOpWerk kansOpWerk
    VMASEvent rawEvent
}

@ToString
class VMAS_KOW_Provincie  {
    String kleur
    String ikl
    String provincie
}



package vdab.demo

import groovy.transform.ToString
import org.apache.kafka.streams.processor.To


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
    RawEvent rawEvent
}

package vdab.demo


class AggregateRecord {

    Integer MaxAantal=0
    Integer TotaalAantal=0
    Integer AantalVerzendingen=0

    public DoAggregation(RawEvent event){
        Integer aantal =event.payload.aantalVacatures()
        if(MaxAantal <aantal)
            MaxAantal=aantal

        TotaalAantal+=aantal
        AantalVerzendingen++
    }
}

package vdab.demo

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.time.LocalDate
import java.time.LocalDateTime
@ToString
class VMASEvent {

    String eventIdentifier
    String eventtype
    LocalDateTime timestamp
    VerzendingGestart payload
}

@CompileStatic
class VerzendingGestart {

    String ikl
    String bundelId
    String verzendingType
    VerrijkteVacatureData verrijkteVacatureData

    VerzendingGestart() {

    }

    Integer aantalVacatures(){
//        println "printout aantal vacatures"
        return this.verrijkteVacatureData.zoekopdrachten.geselecteerd[0].geselecteerd.size() +
                this.verrijkteVacatureData.zoekopdrachten.geselecteerd[1].geselecteerd.size()
    }

}
class VerrijkteVacatureData{
    Klantinformatie klantinformatie;
// Suggesties suggesties;
// Matchings matchings;
    Zoekopdrachten zoekopdrachten;
}



class Zoekopdrachten{
    List<Geselecteerd> geselecteerd;
    List<Genegeerd> genegeerd;
    boolean meer;
}

class Adres{
    Adres(){}

    String straat;
    String huisnummer;
    String gemeente;
    String postcode;
    String landCode;
}
//

class Klantinformatie{
    String ikl;
    String naam;
    String voornaam;
    String email;
    String gsm;
    Adres adres;
    boolean verwijderd;
    String catwzCode;
}
//
// class Suggesties{
//     List<Object> geselecteerd;
//     List<Object> genegeerd;
//     boolean meer;
//}
//
// class Matchings{
//     List<Object> geselecteerd;
//     List<Object> genegeerd;
//     boolean meer;
//}
//
class VacatureInformatie{
    int vacatureId;
    String functienaam;
    String leverancier;
    String tewerkstellingslocatie;
    boolean gesloten;
}
//
class Geselecteerd{
    List<GeselecteerdNested> geselecteerd
}

class GeselecteerdNested{

    VacatureInformatie vacatureInformatie;
    String uuid;
}
//
class Genegeerd{
    int vacatureId;
    String uuid;
    String reden;
}
//
// class Jobselectie{
// String id;
// String url;
// String naam;
// Date datumCreatie;
//}
//
// class Geselecteerd{
//
// List<Geselecteerd2> geselecteerd;
// List<Genegeerd> genegeerd;
// boolean meer;
// Jobselectie jobselectie;
//}
//
// class Genegeerd3{
// String class;
// int vacatureId;
// String uuid;
// String reden;
//}
//
// class Jobselectie2{
// String id;
// String url;
// String naam;
// Date datumCreatie;
//}
//
// class Genegeerd2{
//@JsonProperty("@class")
// String class;
// List<Object> geselecteerd;
// List<Genegeerd3> genegeerd;
// boolean meer;
// Jobselectie2 jobselectie;
//}
//

//
//class VerrijkteVacatureData{
// Klantinformatie klantinformatie;
// Suggesties suggesties;
// Matchings matchings;
// Zoekopdrachten zoekopdrachten;
//}
//class VerzendingGestart
// {
// String ikl
// String bundelId
// String verzendingType
// VerrijkteVacatureData verrijkteVacatureData
//}

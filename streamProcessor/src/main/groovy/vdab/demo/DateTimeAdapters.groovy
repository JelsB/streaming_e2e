package vdab.demo

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
@CompileStatic
class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime ) throws IOException {
        jsonWriter.value(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
    }


    @Override
    public LocalDateTime read(final JsonReader jsonReader ) throws IOException {
        return LocalDateTime.parse(jsonReader.nextString(), DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss[.SSS]X"))
    }
}

@CompileStatic
class LocalDateAdapter extends TypeAdapter<LocalDate> {
    @Override
    public void write(final JsonWriter jsonWriter, final LocalDate localDateTime ) throws IOException {
        jsonWriter.value(localDateTime.format(DateTimeFormatter.ofPattern( 'yyyy-MM-dd')))
    }

    @Override
    public LocalDate read(final JsonReader jsonReader ) throws IOException {
        return LocalDate.parse(jsonReader.nextString(),DateTimeFormatter.ofPattern( 'yyyy-MM-dd'))
    }
}
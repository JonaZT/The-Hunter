package com.example.demo.filter;

import com.example.demo.scraper.ScraperService.VacanteRaw;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilterEngine {

    // Palabras que DEBEN aparecer en el título
    private static final List<String> KEYWORDS_REQUERIDAS = List.of(
            "software", "backend", "frontend", "fullstack", "java",
            "developer", "desarrollador", "programador", "sistemas", "web"
    );

    // Ubicaciones aceptadas (flexible — acepta Tijuana o remoto)
    private static final List<String> UBICACIONES_ACEPTADAS = List.of(
            "tijuana", "baja california", "méxico", "mexico", "remoto", "remote", "híbrido"
    );

    // Palabras que descartan inmediatamente aunque pasen lo demás
    private static final List<String> KEYWORDS_EXCLUIDAS = List.of(
            "arquitecto", "diseñador industrial", "firmware", "mecánico",
            "obra", "construcción", "mep", "tooling", "gerente sr"
    );

    public List<VacanteRaw> filtrar(List<VacanteRaw> vacantes) {
        return vacantes.stream()
                .filter(this::tituloRelevante)
                .filter(this::noExcluida)
                .filter(this::ubicacionNoExcluida)  // ← primero excluir CDMX/MTY
                .filter(this::ubicacionAceptada)    // ← luego aceptar Tijuana/remoto
                .collect(Collectors.toList());
    }

    public boolean ubicacionNoExcluida(VacanteRaw v) {
        String ubicacion = limpiar(v.ubicacion()).toLowerCase();
        return !(ubicacion.contains("ciudad de méxico") || ubicacion.contains("cdmx")
                || ubicacion.contains("monterrey") || ubicacion.contains("mty"));
    }
    private boolean tituloRelevante(VacanteRaw v) {
        String titulo = v.titulo().toLowerCase();
        return KEYWORDS_REQUERIDAS.stream().anyMatch(titulo::contains);
    }

    private boolean ubicacionAceptada(VacanteRaw v) {
        String ubicacion = limpiar(v.ubicacion()).toLowerCase();

        // "México" solo pasa si es exactamente eso — no "Ciudad de México"
        boolean esMexicoGenerico = ubicacion.trim().equals("méxico")
                || ubicacion.trim().equals("mexico");

        return UBICACIONES_ACEPTADAS.stream().anyMatch(ubicacion::contains)
                || esMexicoGenerico;
    }

    private boolean noExcluida(VacanteRaw v) {
        String titulo = v.titulo().toLowerCase();
        return KEYWORDS_EXCLUIDAS.stream().noneMatch(titulo::contains);
    }

    public static String limpiar(String texto) {
        return texto == null ? "" : texto.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚüÜ\\s]", " ").replaceAll("\\s+", " ").trim();
    }
}
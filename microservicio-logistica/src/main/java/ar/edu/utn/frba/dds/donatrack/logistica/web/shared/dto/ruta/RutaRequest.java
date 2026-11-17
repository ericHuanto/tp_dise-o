package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.ruta;

import java.time.LocalDate;

public record RutaRequest(
    String patenteCamion,
    LocalDate fecha
) {}

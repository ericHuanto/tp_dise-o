package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.registro;

import java.time.LocalDate;

public record BienRequest(String tipo, String categoria, String subcategoria, String descripcion,
                          Float cantidad, String unidadMedida, LocalDate fechaVencimiento,
                          Boolean usado) {}
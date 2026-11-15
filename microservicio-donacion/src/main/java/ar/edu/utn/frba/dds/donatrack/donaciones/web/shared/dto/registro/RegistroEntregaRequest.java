package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.registro;

import java.time.LocalDate;
import java.util.List;

public record RegistroEntregaRequest(Long donanteId, LocalDate fechaRecepcion,
                                     String observaciones, List<BienRequest> bienes) {}


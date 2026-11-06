package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.beneficiario;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.donacion.DonacionMapaResponse;

import java.util.List;

public record BeneficiarioMapaResponse(
    Long id,
    String razonSocial,
    String direccion,
    String latitud,
    String longitud,
    List<DonacionMapaResponse> donaciones
) { }

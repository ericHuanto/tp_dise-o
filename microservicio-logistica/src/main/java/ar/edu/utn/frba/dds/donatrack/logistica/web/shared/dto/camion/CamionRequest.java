package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.camion;

public record CamionRequest(
    String patente,
    Float capacidadVolumen,
    Float altura,
    Float capacidadCarga
) { }
package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.camion;

public record CamionResponse(
    String patente,
    float capacidadVolumen,
    float altura,
    float capacidadCarga,
    String imeiGps
) { }
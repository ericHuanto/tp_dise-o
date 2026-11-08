package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.camion;

public record ActualizarCamionRequest(
    Float capacidadVolumen,
    Float altura,
    Float capacidadCarga,
    String gpsImei
) { }
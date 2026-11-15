package ar.edu.utn.frba.dds.donatrack.donacion.builder;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.Documento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.TipoDocumento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.CorreoDeContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.entrega.RegistroEntrega;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class RegistroEntregaBuilder {
  private LocalDate fechaRecepcion = LocalDate.now();
  private String observaciones;
  private List<Bien> bienes;
  private Donante donantePrueba = new PersonaHumanaBuilder()
      .conNombre("Juan")
      .conApellido("Pérez")
      .conDocumento(new Documento(TipoDocumento.DNI, "12345678"))
      .conAgregarContacto(new CorreoDeContacto("juan@prueba.com", true))
      .conDireccion("alguna dirección")
      .build();

  public RegistroEntregaBuilder() {
    this.bienes = new ArrayList<>();
  }

  public RegistroEntregaBuilder conFechaRecepcion(LocalDate fechaRecepcion) {
    this.fechaRecepcion = fechaRecepcion;
    return this;
  }

  public RegistroEntregaBuilder conObservaciones(String observaciones) {
    this.observaciones = observaciones;
    return this;
  }

  public RegistroEntregaBuilder conBien(Bien bien) {
    this.bienes.add(bien);
    return this;
  }

  public RegistroEntregaBuilder conBienes(List<Bien> bienes) {
    this.bienes.addAll(bienes);
    return this;
  }

  public RegistroEntrega build() {
    return new RegistroEntrega(fechaRecepcion, observaciones, bienes, donantePrueba);
  }
}
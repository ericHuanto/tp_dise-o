package ar.edu.utn.frba.dds.donatrack.usuario.web.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class PerfilVista {
  private String nombre;
  private String iniciales = "";
  private final List<String> etiquetas = new ArrayList<>();
  private final List<Resumen> resumenes = new ArrayList<>();
  private final List<Seccion> secciones = new ArrayList<>();
  private final List<Destacado> destacados = new ArrayList<>();
  private final List<String> avisos = new ArrayList<>();

  public void definirNombre(String candidato) {      // gana el primero que llega
    if (nombre == null && candidato != null && !candidato.isBlank()) nombre = candidato.trim();
  }
  public void agregarEtiqueta(String e) { etiquetas.add(e); }
  public void agregarResumen(Resumen r) { resumenes.add(r); }
  public void agregarSeccion(Seccion s) { secciones.add(s); }
  public void agregarDestacado(Destacado d) { destacados.add(d); }
  public void agregarAviso(String a) { avisos.add(a); }

  public void cerrar(String nombreUsuario) {
    if (nombre == null) nombre = nombreUsuario;
    String[] p = nombre.trim().split("\\s+");
    iniciales = (p.length > 1 ? p[0].substring(0, 1) + p[1].substring(0, 1)
        : p[0].substring(0, Math.min(2, p[0].length()))).toUpperCase();
  }

  public boolean isTieneDatos() { return !resumenes.isEmpty() || !secciones.isEmpty(); }

  @Getter @AllArgsConstructor
  public static class Dato {
    private final String valor;
    private final String texto;
  }

  @Getter
  public static class Resumen {
    private final String titulo;
    private final List<Dato> items;
    private final String enlaceTexto;
    private final String enlaceUrl;

    public Resumen(String titulo, List<Dato> items) {
      this(titulo, items, null, null);
    }

    public Resumen(String titulo, List<Dato> items, String enlaceTexto, String enlaceUrl) {
      this.titulo = titulo;
      this.items = items;
      this.enlaceTexto = enlaceTexto;
      this.enlaceUrl = enlaceUrl;
    }
  }

  @Getter @AllArgsConstructor
  public static class Campo {
    private final String etiqueta;
    private final String valor;
  }

  @Getter @AllArgsConstructor
  public static class Seccion {
    private final String titulo;
    private final List<Campo> campos;
  }

  @Getter @AllArgsConstructor
  public static class Destacado {
    private final String titulo;
    private final String fecha;
    private final String descripcion;
    private final String pie;
  }
}
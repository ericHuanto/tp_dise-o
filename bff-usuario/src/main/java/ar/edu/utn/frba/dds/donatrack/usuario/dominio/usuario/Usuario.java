package ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.generadorhash.Integridad;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_usuario")
  private Long id;
  @Column(name = "nombre_usuario", unique = true)
  private String nombreUsuario;
  @Column(name = "contraseña_hasheada")
  private String contraseña;
  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "usuario_permiso", joinColumns = @JoinColumn(name = "id_usuario"))
  @Column(name = "permiso")
  private List<Permiso> permisos;
  @Column(name = "id_donante")
  private Long idDonante;
  @Column(name = "id_beneficiario")
  private Long idBeneficiario;

  public Usuario(String nombreUsuario, String contraseña) {
    this.nombreUsuario = nombreUsuario;
    this.contraseña = hashear(contraseña);
    this.permisos = new ArrayList<>();
    this.idDonante = null;
    this.idBeneficiario = null;
  }

  public void vincularDonante(Long idDonante) {
    this.idDonante = idDonante;
    this.otorgarPermiso(Permiso.VER_DONACIONES);
  }

  public void vincularBeneficiario(Long idBeneficiario) {
    this.idBeneficiario = idBeneficiario;
    this.otorgarPermiso(Permiso.SOLICITAR_AYUDA);
  }

  public void otorgarPermiso(Permiso nuevoPermiso) {
    if (!this.permisos.contains(nuevoPermiso)) {
      this.permisos.add(nuevoPermiso);
    }
  }

  public boolean tienePermiso(Permiso permiso) {
    if (this.permisos.isEmpty() || permiso == null) {
      return false;
    }
    return this.permisos.contains(Permiso.ADMINISTRADOR) || this.permisos.contains(permiso);
  }

  public boolean isDonante() {
    return tieneRol(Permiso.VER_DONACIONES);
  }

  public boolean isBeneficiario() {
    return tieneRol(Permiso.SOLICITAR_AYUDA);
  }

  public boolean isAdmin() {
    return tieneRol(Permiso.ADMINISTRADOR);
  }

  private boolean tieneRol(Permiso permisoDelRol) {
    return this.permisos != null && this.permisos.contains(permisoDelRol);
  }

  public void cambiarContrasena(String nueva) {
    this.contraseña = hashear(nueva);
  }

  public void actualizarContraseña(String contraseña) {
    this.contraseña = hashear(contraseña);
  }

  //================== FUNCIONES AUXILIARES ===================
  private String hashear(String contraseña) {
    return Integridad.hashear(contraseña);
  }

}
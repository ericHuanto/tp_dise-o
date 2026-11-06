package ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.utils.Integridad;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "usuario", unique = true)
  private String nombreUsuario;
  @Column(name = "contraseña")
  private String contraseña;
  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "usuario_permiso", joinColumns = @JoinColumn(name = "id_usuario"))
  @Column(name = "permiso")
  private List<Permiso> permisos;

  public Usuario(String nombreUsuario, String contraseña) {
    this.nombreUsuario = nombreUsuario;
    this.contraseña = hashear(contraseña);
    this.permisos = new ArrayList<>();
  }

  public void otorgarPermiso(Permiso nuevoPermiso) {
    if (!this.permisos.contains(nuevoPermiso)) {
      this.permisos.add(nuevoPermiso);
    }
  }

  //================== FUNCIONES AUXILIARES ===================
  private String hashear(String contraseña){
    return Integridad.hashear(contraseña);
  }

}

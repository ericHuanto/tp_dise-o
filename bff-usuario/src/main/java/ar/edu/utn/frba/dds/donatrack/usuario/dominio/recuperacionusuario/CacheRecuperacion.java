package ar.edu.utn.frba.dds.donatrack.usuario.dominio.recuperacionusuario;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

public class CacheRecuperacion {
  private static final Cache<String, String> cachePines = Caffeine.newBuilder()
      .expireAfterWrite(15, TimeUnit.MINUTES)
      .build();

  public static void guardarPin(String nombreUsuario, String pin) {
    cachePines.put(nombreUsuario, pin);
  }

  public static String obtenerPin(String nombreUsuario) {
    return cachePines.getIfPresent(nombreUsuario);
  }

  public static void borrarPin(String nombreUsuario) {
    cachePines.invalidate(nombreUsuario);
  }

}

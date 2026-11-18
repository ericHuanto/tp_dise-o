package ar.edu.utn.frba.dds.donatrack.shared;

import java.util.HashMap;
import java.util.Map;

//Arma las propiedades de conexión a la base a partir de las variables de entorno.
public final class ConfiguracionPersistencia {
  private ConfiguracionPersistencia() {}

  public static Map<String, String> propiedadesBaseDeDatos(String url) {
    ConfiguracionEntorno config = ConfiguracionEntorno.getInstance();
    Map<String, String> propiedades = new HashMap<>();

    propiedades.put("hibernate.connection.url", url);
    propiedades.put("hibernate.connection.username", config.getDbUser());
    propiedades.put("hibernate.connection.password", config.getDbPassword());

    if (url.startsWith("jdbc:mysql")) {
      propiedades.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
      propiedades.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
    }
    return propiedades;
  }
}
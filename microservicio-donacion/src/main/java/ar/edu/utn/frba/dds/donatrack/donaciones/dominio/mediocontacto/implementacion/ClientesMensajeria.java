package ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.correo.ClienteCorreo;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.correo.ClienteCorreoMock;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.correo.ClienteCorreoReal;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.sms.ClienteSms;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.sms.ClienteSmsMock;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.sms.ClienteSmsRealTwilio;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.whatsapp.ClienteWhatsapp;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.whatsapp.ClienteWhatsappMock;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.implementacion.whatsapp.ClienteWhatsappRealTwilio;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;

//Elige el cliente real si están las credenciales. Si no, el Mock.
public final class ClientesMensajeria {
  private ClientesMensajeria() { }

  public static ClienteCorreo correo() {
    return configuradas("EMAIL_USER", "EMAIL_PASSWORD")
        ? new ClienteCorreoReal() : new ClienteCorreoMock();
  }

  public static ClienteSms sms() {
    return configuradas("TWILIO_ACCOUNT_SID", "TWILIO_AUTH_TOKEN", "TWILIO_SMS_NUMBER")
        ? new ClienteSmsRealTwilio() : new ClienteSmsMock();
  }

  public static ClienteWhatsapp whatsapp() {
    return configuradas("TWILIO_ACCOUNT_SID", "TWILIO_AUTH_TOKEN", "TWILIO_WHATSAPP_NUMBER")
        ? new ClienteWhatsappRealTwilio() : new ClienteWhatsappMock();
  }

  private static boolean configuradas(String... variables) {
    ConfiguracionEntorno cfg = ConfiguracionEntorno.getInstance();
    for (String variable : variables) {
      if (cfg.obtenerOpcional(variable) == null) return false;
    }
    return true;
  }
}
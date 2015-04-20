package ordenesDeCompra.dominio;

import java.io.Serializable;

public class Correo{

   
    private String asunto;//titulo del mensaje
    private String mensaje;//contenido del mensaje
    private String para;

    public Correo() {
        this.asunto = "";
        this.mensaje = "";
        this.para = "";
    }
    
    

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPara() {
        return para;
    }

    public void setPara(String para) {
        this.para = para;
    }

    
    

    
   

}

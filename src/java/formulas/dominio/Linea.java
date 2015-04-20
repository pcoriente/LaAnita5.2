package formulas.dominio;

import java.io.Serializable;

/**
 *
 * @author jesc
 */
public class Linea implements Serializable {
    String linea;
    
    public Linea() {
        this.linea="";
    }
    
    public Linea(String linea) {
        this.linea=linea;
    }

    public String getLinea() {
        return linea;
    }

    public void setLinea(String linea) {
        this.linea = linea;
    }
}

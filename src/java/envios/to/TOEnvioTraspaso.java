package envios.to;

import java.util.Date;
import traspasos.to.TOTraspaso;

/**
 *
 * @author jesc
 */
public class TOEnvioTraspaso extends TOTraspaso {
    private int idEnvio;
    private int diasInventario;
    private Date fechaProduccion;
    private int directo;
    
    public TOEnvioTraspaso() {
        super();
        this.fechaProduccion = new Date();
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getDiasInventario() {
        return diasInventario;
    }

    public void setDiasInventario(int diasInventario) {
        this.diasInventario = diasInventario;
    }

    public Date getFechaProduccion() {
        return fechaProduccion;
    }

    public void setFechaProduccion(Date fechaProduccion) {
        this.fechaProduccion = fechaProduccion;
    }

    public int getDirecto() {
        return directo;
    }

    public void setDirecto(int directo) {
        this.directo = directo;
    }
}

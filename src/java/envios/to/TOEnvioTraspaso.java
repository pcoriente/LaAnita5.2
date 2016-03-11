package envios.to;

import java.util.Date;
import traspasos.to.TOTraspaso;

/**
 *
 * @author jesc
 */
public class TOEnvioTraspaso extends TOTraspaso {
    private int diasInventario;
    private Date fechaProduccion;
    
    public TOEnvioTraspaso() {
        super();
        this.fechaProduccion = new Date();
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
}

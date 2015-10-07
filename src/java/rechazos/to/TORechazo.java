package rechazos.to;

import java.util.Date;
import movimientos.to.TOMovimientoOficina;

/**
 *
 * @author jesc
 */
public class TORechazo extends TOMovimientoOficina {
    private int recepcionFolio;
    private Date recepcionFecha;
    private int traspasoFolio;
    private Date traspasoFecha;
    
    public TORechazo() {
        super();
        this.recepcionFecha = new Date();
        this.traspasoFecha = new Date();
    }
    
    public TORechazo(int idTipo) {
        super(idTipo);
        this.recepcionFecha = new Date();
        this.traspasoFecha = new Date();
    }

    public int getRecepcionFolio() {
        return recepcionFolio;
    }

    public void setRecepcionFolio(int recepcionFolio) {
        this.recepcionFolio = recepcionFolio;
    }

    public Date getRecepcionFecha() {
        return recepcionFecha;
    }

    public void setRecepcionFecha(Date recepcionFecha) {
        this.recepcionFecha = recepcionFecha;
    }

    public int getTraspasoFolio() {
        return traspasoFolio;
    }

    public void setTraspasoFolio(int traspasoFolio) {
        this.traspasoFolio = traspasoFolio;
    }

    public Date getTraspasoFecha() {
        return traspasoFecha;
    }

    public void setTraspasoFecha(Date traspasoFecha) {
        this.traspasoFecha = traspasoFecha;
    }
}

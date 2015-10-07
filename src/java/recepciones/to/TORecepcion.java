package recepciones.to;

import java.util.Date;
import movimientos.to.TOMovimientoOficina;

/**
 *
 * @author jesc
 */
public class TORecepcion extends TOMovimientoOficina {
    private int idSolicitud;
    private int solicitudFolio;
    private Date solicitudFecha;
    private int traspasoFolio;
    private Date traspasoFecha;
    
    public TORecepcion() {
        super();
        solicitudFecha=new Date();
        traspasoFecha=new Date();
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public int getSolicitudFolio() {
        return solicitudFolio;
    }

    public void setSolicitudFolio(int solicitudFolio) {
        this.solicitudFolio = solicitudFolio;
    }

    public Date getSolicitudFecha() {
        return solicitudFecha;
    }

    public void setSolicitudFecha(Date solicitudFecha) {
        this.solicitudFecha = solicitudFecha;
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

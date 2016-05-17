package recepciones.to;

import java.util.Date;
import movimientos.to.TOMovimientoOficina;

/**
 *
 * @author jesc
 */
public class TORecepcion extends TOMovimientoOficina {
    private int idEnvio;
    private int envioFolio;
    private int idSolicitud;
    private int solicitudFolio;
    private Date solicitudFecha;
    private int traspasoFolio;
    private Date traspasoFecha;
    private int pedidoFolio;
    private Date pedidoFecha;
    
    public TORecepcion() {
        super();
        solicitudFecha=new Date();
        traspasoFecha=new Date();
        pedidoFecha=new Date();
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getEnvioFolio() {
        return envioFolio;
    }

    public void setEnvioFolio(int envioFolio) {
        this.envioFolio = envioFolio;
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

    public int getPedidoFolio() {
        return pedidoFolio;
    }

    public void setPedidoFolio(int pedidoFolio) {
        this.pedidoFolio = pedidoFolio;
    }

    public Date getPedidoFecha() {
        return pedidoFecha;
    }

    public void setPedidoFecha(Date pedidoFecha) {
        this.pedidoFecha = pedidoFecha;
    }
}

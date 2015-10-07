package traspasos.to;

import java.util.Date;
import movimientos.to.TOMovimientoOficina;

/**
 *
 * @author jesc
 */
public class TOTraspaso extends TOMovimientoOficina {
    private int solicitudFolio;
    private Date solicitudFecha;
    private int solicitudIdUsuario;
    private int solicitudProietario;
    private int solicitudEstatus;
    
    public TOTraspaso() {
        super();
        this.solicitudFecha=new Date();
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

    public int getSolicitudIdUsuario() {
        return solicitudIdUsuario;
    }

    public void setSolicitudIdUsuario(int solicitudIdUsuario) {
        this.solicitudIdUsuario = solicitudIdUsuario;
    }

    public int getSolicitudProietario() {
        return solicitudProietario;
    }

    public void setSolicitudProietario(int solicitudProietario) {
        this.solicitudProietario = solicitudProietario;
    }

    public int getSolicitudEstatus() {
        return solicitudEstatus;
    }

    public void setSolicitudEstatus(int solicitudEstatus) {
        this.solicitudEstatus = solicitudEstatus;
    }
}

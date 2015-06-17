package movimientos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class Solicitud {
    private int idMovto;
    private TOAlmacenJS almacen;
    private int folio;
    private Date fecha;
    private int idUsuario;
    private TOAlmacenJS almacenOrigen;
    private int idMovtoAlmacen;
    private int folioAlmacen;
    
    public Solicitud() {
        this.almacen=new TOAlmacenJS();
        this.fecha=new Date();
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public TOAlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(TOAlmacenJS almacen) {
        this.almacen = almacen;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public TOAlmacenJS getAlmacenOrigen() {
        return almacenOrigen;
    }

    public void setAlmacenOrigen(TOAlmacenJS almacenOrigen) {
        this.almacenOrigen = almacenOrigen;
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

    public int getFolioAlmacen() {
        return folioAlmacen;
    }

    public void setFolioAlmacen(int folioAlmacen) {
        this.folioAlmacen = folioAlmacen;
    }
}

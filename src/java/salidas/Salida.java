package salidas;

import almacenes.dominio.AlmacenJS;
import almacenes.to.TOAlmacenJS;
import java.util.Date;
import movimientos.dominio.MovimientoTipo;

/**
 *
 * @author jesc
 */
public class Salida {
    private int idMovto;
    private TOAlmacenJS almacen;
    private MovimientoTipo tipo;
    private int folio;
    private Date fecha;
    private int idUsuario;
    
    public Salida() {
        this.almacen=new TOAlmacenJS();
        this.tipo=new MovimientoTipo();
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

    public MovimientoTipo getTipo() {
        return tipo;
    }

    public void setTipo(MovimientoTipo tipo) {
        this.tipo = tipo;
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
}

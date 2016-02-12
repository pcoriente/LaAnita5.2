package mvEntradas.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;

/**
 *
 * @author jesc
 */
public class EntradaProduccion extends MovimientoOficina {
    private Date fechaReporte;
    
    public EntradaProduccion() {
        super();
        this.fechaReporte = new GregorianCalendar(1900,1,1).getTime();
    }
    
//    public EntradaProduccion(MovimientoTipo tipo) {
//        super(tipo);
//        this.fechaReporte = new GregorianCalendar(1900,1,1).getTime();
//    }
    
    public EntradaProduccion(TOAlmacenJS almacen, MovimientoTipo tipo, Date fechaReporte) {
        super(tipo, almacen);
        this.fechaReporte = fechaReporte;
    }

    public Date getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(Date fechaReporte) {
        this.fechaReporte = fechaReporte;
    }
}

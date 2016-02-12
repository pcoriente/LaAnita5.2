package mvEntradas.to;

import java.util.Date;
import movimientos.to.TOMovimientoOficina;

/**
 *
 * @author jesc
 */
public class TOEntradaProduccion extends TOMovimientoOficina {
    private Date fechaReporte;
    
    public TOEntradaProduccion() {
        super();
        this.fechaReporte = new Date();
    }
    
    public TOEntradaProduccion(int idTipo) {
        super(idTipo);
        this.fechaReporte = new Date();
    }

    public Date getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(Date fechaReporte) {
        this.fechaReporte = fechaReporte;
    }
}

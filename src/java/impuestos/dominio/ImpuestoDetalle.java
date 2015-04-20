package impuestos.dominio;

import java.util.Date;
import utilerias.Utilerias;

/**
 *
 * @author JULIOS
 */
public class ImpuestoDetalle {
    //private ImpuestoZona zona;
    //private ImpuestoGrupo grupo;
    private Impuesto impuesto;

    private Date fechaInicial;
    private Date fechaFinal;
    //private Date finFecha;
    private double valor=0.00;
    //private Date fechaInicialSiguiente;
    
    public ImpuestoDetalle() {
        this.impuesto=new Impuesto(0, "", false, 1, false, false);
        //this.fechaInicialSiguiente=new Date();
    }
    
    @Override
    public String toString() {
        String tmp=Utilerias.formatoNumero("######.###", this.valor);
        return this.impuesto.getImpuesto()+(this.impuesto.isAplicable()?" "+(this.impuesto.getModo()==1?tmp+"%":"$"+tmp):"");
    }
    /*
    public ImpuestoZona getZona() {
        return zona;
    }

    public void setZona(ImpuestoZona zona) {
        this.zona = zona;
    }

    public ImpuestoGrupo getGrupo() {
        return grupo;
    }

    public void setGrupo(ImpuestoGrupo grupo) {
        this.grupo = grupo;
    }
    * */
    public Impuesto getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(Impuesto impuesto) {
        this.impuesto = impuesto;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }
    
    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }
    
    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
    /*
    public Date getFechaInicialSiguiente() {
        return fechaInicialSiguiente;
    }

    public void setFechaInicialSiguiente(Date fechaInicialSiguiente) {
        this.fechaInicialSiguiente = fechaInicialSiguiente;
    }
    
    public Date getFinFecha() {
        return finFecha;
    }

    public void setFinFecha(Date finFecha) {
        this.finFecha = finFecha;
    }
    * */
}

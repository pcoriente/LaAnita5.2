package impuestos.dominio;

/**
 *
 * @author jsolis
 */
public class ImpuestosProducto {
    private int idImpuesto;
    private String impuesto;
    private double valor;
    private boolean aplicable;
    private int modo;
    private boolean acreditable;
    private double importe;
    private boolean acumulable;
    
    public ImpuestosProducto() {
        this.idImpuesto=0;
        this.impuesto="";
        this.valor=0.00;
        this.aplicable=true;
        this.modo=0;
        this.acreditable=true;
        this.acumulable=false;
        this.importe=0.00;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.idImpuesto;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.valor) ^ (Double.doubleToLongBits(this.valor) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImpuestosProducto other = (ImpuestosProducto) obj;
        if (this.idImpuesto != other.idImpuesto) {
            return false;
        }
        if (Double.doubleToLongBits(this.valor) != Double.doubleToLongBits(other.valor)) {
            return false;
        }
        return true;
    }

    public int getIdImpuesto() {
        return idImpuesto;
    }

    public void setIdImpuesto(int idImpuesto) {
        this.idImpuesto = idImpuesto;
    }

    public String getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(String impuesto) {
        this.impuesto = impuesto;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isAplicable() {
        return aplicable;
    }

    public void setAplicable(boolean aplicable) {
        this.aplicable = aplicable;
    }

    public int getModo() {
        return modo;
    }

    public void setModo(int modo) {
        this.modo = modo;
    }

    public boolean isAcreditable() {
        return acreditable;
    }

    public void setAcreditable(boolean acreditable) {
        this.acreditable = acreditable;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }

    public boolean isAcumulable() {
        return acumulable;
    }

    public void setAcumulable(boolean acumulable) {
        this.acumulable = acumulable;
    }
}

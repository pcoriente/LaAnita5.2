package movimientos.to;

/**
 *
 * @author jesc
 */
public class TOProductoOficina {
    private int idMovto;
    private int idProducto;
    private double cantFacturada;
    private double cantSinCargo;
    private double costoPromedio;
    private double costo;
    private double desctoProducto1;
    private double desctoProducto2;
    private double desctoConfidencial;
    private double unitario;
    private int idImpuestoGrupo;
    
    public TOProductoOficina() {}

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.idProducto;
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
        final TOProductoOficina other = (TOProductoOficina) obj;
        if (this.idProducto != other.idProducto) {
            return false;
        }
        return true;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public double getCantFacturada() {
        return cantFacturada;
    }

    public void setCantFacturada(double cantFacturada) {
        this.cantFacturada = cantFacturada;
    }

    public double getCantSinCargo() {
        return cantSinCargo;
    }

    public void setCantSinCargo(double cantSinCargo) {
        this.cantSinCargo = cantSinCargo;
    }

    public double getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(double costoPromedio) {
        this.costoPromedio = costoPromedio;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public double getDesctoProducto1() {
        return desctoProducto1;
    }

    public void setDesctoProducto1(double desctoProducto1) {
        this.desctoProducto1 = desctoProducto1;
    }

    public double getDesctoProducto2() {
        return desctoProducto2;
    }

    public void setDesctoProducto2(double desctoProducto2) {
        this.desctoProducto2 = desctoProducto2;
    }

    public double getDesctoConfidencial() {
        return desctoConfidencial;
    }

    public void setDesctoConfidencial(double desctoConfidencial) {
        this.desctoConfidencial = desctoConfidencial;
    }

    public double getUnitario() {
        return unitario;
    }

    public void setUnitario(double unitario) {
        this.unitario = unitario;
    }

    public int getIdImpuestoGrupo() {
        return idImpuestoGrupo;
    }

    public void setIdImpuestoGrupo(int idImpuestoGrupo) {
        this.idImpuestoGrupo = idImpuestoGrupo;
    }
}

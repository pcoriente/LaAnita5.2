package proveedores.dominio;

import ordenesDeCompra.dominio.OrdenCompraEncabezado;

/**
 *
 * @author jsolis
 */
public class MiniProveedor {
    private int idProveedor;
    private String proveedor;
    private String rfc;
    private double desctoComercial;
    private double desctoProntoPago;
    private int idImpuestoZona;
    private OrdenCompraEncabezado ordenCompraEncabezado;

    public MiniProveedor() {
        this.idProveedor=0;
        this.proveedor="";
        this.rfc="";
        this.idImpuestoZona=0;
    }

    public MiniProveedor(int idProveedor, String proveedor, String rfc) {
        this.idProveedor = idProveedor;
        this.proveedor = proveedor;
        this.rfc = rfc;
    }

    @Override
    public String toString() {
        return this.proveedor;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.idProveedor;
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
        final MiniProveedor other = (MiniProveedor) obj;
        if (this.idProveedor != other.idProveedor) {
            return false;
        }
        return true;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public double getDesctoComercial() {
        return desctoComercial;
    }

    public void setDesctoComercial(double desctoComercial) {
        this.desctoComercial = desctoComercial;
    }

    public double getDesctoProntoPago() {
        return desctoProntoPago;
    }

    public void setDesctoProntoPago(double desctoProntoPago) {
        this.desctoProntoPago = desctoProntoPago;
    }

    public int getIdImpuestoZona() {
        return idImpuestoZona;
    }

    public void setIdImpuestoZona(int idImpuestoZona) {
        this.idImpuestoZona = idImpuestoZona;
    }

    public OrdenCompraEncabezado getOrdenCompraEncabezado() {
        return ordenCompraEncabezado;
    }

    public void setOrdenCompraEncabezado(OrdenCompraEncabezado ordenCompraEncabezado) {
        this.ordenCompraEncabezado = ordenCompraEncabezado;
    }

   
}

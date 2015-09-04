package movimientos.dominio;

import impuestos.dominio.ImpuestosProducto;
import java.util.ArrayList;
import java.util.Objects;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class ProductoOficina {
    private int idMovto;
    private Producto producto;
    private double cantFacturada;
    private double cantSinCargo;
    private double separados;
    private double costoPromedio;
    private double costo;
    private double desctoProducto1;
    private double desctoProducto2;
    private double desctoConfidencial;
    private double unitario;
    private ArrayList<ImpuestosProducto> impuestos;
    private double neto;
    private double importe;
    
    public ProductoOficina() {
        this.producto = new Producto();
        this.impuestos = new ArrayList<>();
    }
    
    public ProductoOficina(Producto producto) {
        this.producto = producto;
        this.impuestos = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.producto.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.producto);
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
        final ProductoOficina other = (ProductoOficina) obj;
        if (!Objects.equals(this.producto, other.producto)) {
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

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
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

    public ArrayList<ImpuestosProducto> getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(ArrayList<ImpuestosProducto> impuestos) {
        this.impuestos = impuestos;
    }

    public double getNeto() {
        return neto;
    }

    public void setNeto(double neto) {
        this.neto = neto;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }
}

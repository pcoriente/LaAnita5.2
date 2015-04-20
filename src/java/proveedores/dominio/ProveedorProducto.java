package proveedores.dominio;

import impuestos.dominio.ImpuestoGrupo;
import java.util.Date;
import producto2.dominio.Empaque;
import producto2.dominio.Marca;
import producto2.dominio.Presentacion;
import producto2.dominio.Producto;
//import productos.dominio.Empaque;
//import productos.dominio.Marca;
//import productos.dominio.Presentacion;
//import productos.dominio.UnidadEmpaque;
import unidadesMedida.UnidadMedida;

/**
 *
 * @author jsolis
 */
public class ProveedorProducto {
    private int idProducto;
    private String sku;
    private int diasEntrega;
    private Empaque empaque;
    private int piezas;
    private Marca marca;
    private String producto;
    private Presentacion presentacion;
    private double contenido;
    private UnidadMedida unidadMedida;
    private UnidadMedida unidadMedida2;
    private ImpuestoGrupo impuestoGrupo;
    private int idFactura;
    private Date ultimaCompraFecha;
    private double ultimaCompraPrecio;
    private Producto equivalencia;

    public ProveedorProducto() {
        this.idProducto=0;
        this.sku="";
        this.diasEntrega=0;
        this.empaque=new Empaque();
        this.piezas=0;
        this.marca=new Marca();
        this.producto="";
        this.presentacion=new Presentacion();
        this.contenido=0;
        this.unidadMedida=new UnidadMedida(0, "", "");
        this.unidadMedida2=new UnidadMedida(0, "", "");
        this.impuestoGrupo=new ImpuestoGrupo(0, "");
        this.idFactura=0;
        this.ultimaCompraFecha=null;
        this.ultimaCompraPrecio=0.00;
        this.equivalencia=new Producto();
    }

    @Override
    public String toString() {
        return (this.marca.getIdMarca()==0 ? "" : this.marca.toString()+" ") 
                + this.producto + " " + this.presentacion.getAbreviatura()
                + (this.contenido == 0 ? "("+this.unidadMedida.getAbreviatura()+")" : " " + Double.toString(this.contenido))
                + (this.empaque.getIdEmpaque()==1 ? "" : " " + this.empaque.getAbreviatura() + "x" + Integer.toString(this.piezas));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.idProducto;
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
        final ProveedorProducto other = (ProveedorProducto) obj;
        if (this.idProducto != other.idProducto) {
            return false;
        }
        return true;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Empaque getEmpaque() {
        return empaque;
    }

    public void setEmpaque(Empaque empaque) {
        this.empaque = empaque;
    }

    public int getPiezas() {
        return piezas;
    }

    public void setPiezas(int piezas) {
        this.piezas = piezas;
    }

    public double getContenido() {
        return contenido;
    }

    public void setContenido(double contenido) {
        this.contenido = contenido;
    }

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(UnidadMedida unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public UnidadMedida getUnidadMedida2() {
        return unidadMedida2;
    }

    public void setUnidadMedida2(UnidadMedida unidadMedida2) {
        this.unidadMedida2 = unidadMedida2;
    }

    public ImpuestoGrupo getImpuestoGrupo() {
        return impuestoGrupo;
    }

    public void setImpuestoGrupo(ImpuestoGrupo impuestoGrupo) {
        this.impuestoGrupo = impuestoGrupo;
    }

    public Presentacion getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(Presentacion presentacion) {
        this.presentacion = presentacion;
    }

    public int getDiasEntrega() {
        return diasEntrega;
    }

    public void setDiasEntrega(int diasEntrega) {
        this.diasEntrega = diasEntrega;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public Date getUltimaCompraFecha() {
        return ultimaCompraFecha;
    }

    public void setUltimaCompraFecha(Date ultimaCompraFecha) {
        this.ultimaCompraFecha = ultimaCompraFecha;
    }

    public double getUltimaCompraPrecio() {
        return ultimaCompraPrecio;
    }

    public void setUltimaCompraPrecio(double ultimaCompraPrecio) {
        this.ultimaCompraPrecio = ultimaCompraPrecio;
    }

    public Producto getEquivalencia() {
        return equivalencia;
    }

    public void setEquivalencia(Producto equivalencia) {
        this.equivalencia = equivalencia;
    }
}

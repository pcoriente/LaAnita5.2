package proveedores.dominio;

import impuestos.dominio.ImpuestoGrupo;
import java.util.Date;
import java.util.Objects;
import producto2.dominio.Empaque;
import producto2.dominio.Marca;
import producto2.dominio.Parte;
import producto2.dominio.Presentacion;
import producto2.dominio.Producto;
import unidadesMedida.UnidadMedida;

/**
 *
 * @author jsolis
 */
public class ProveedorProducto {
    private boolean nuevo;
    private Producto equivalencia;
    private String sku;
    private Empaque empaque;
    private int piezas;
    private Marca marca;
    private Parte parte;
    private String descripcion;
    private Presentacion presentacion;
    private double contenido;
    private UnidadMedida unidadMedida;
    private UnidadMedida unidadMedida2;
    private ImpuestoGrupo impuestoGrupo;
    private int diasEntrega;
    private Date ultimaCompraFecha;
    private double ultimaCompraPrecio;

    public ProveedorProducto() {
        this.equivalencia=new Producto();
        this.sku="";
        this.empaque=new Empaque();
        this.marca=new Marca();
        this.parte = new Parte(0, "");
        this.descripcion = "";
        this.presentacion=new Presentacion();
        this.unidadMedida=new UnidadMedida(0, "", "");
        this.unidadMedida2=new UnidadMedida(0, "", "");
        this.impuestoGrupo=new ImpuestoGrupo(0, "");
        this.ultimaCompraFecha=new Date();
    }

    @Override
    public String toString() {
        return (this.marca.getIdMarca()==0 ? "" : this.marca.toString()+" ")
                + this.parte + (this.descripcion.equals("") ? "" : " "+this.descripcion)
                + (this.presentacion.getIdPresentacion()==1 ? "" : " " + this.presentacion.getAbreviatura()) 
                + (this.contenido == 0 ? "" : " " + Double.toString(this.contenido))
                + (this.contenido == 0 ? " ("+this.unidadMedida.getAbreviatura()+")" : " " + this.unidadMedida.getAbreviatura());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.equivalencia);
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
        if (!Objects.equals(this.equivalencia, other.equivalencia)) {
            return false;
        }
        return true;
    }

    public boolean isNuevo() {
        return nuevo;
    }

    public void setNuevo(boolean nuevo) {
        this.nuevo = nuevo;
    }

    public Producto getEquivalencia() {
        return equivalencia;
    }

    public void setEquivalencia(Producto equivalencia) {
        this.equivalencia = equivalencia;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Parte getParte() {
        return parte;
    }

    public void setParte(Parte parte) {
        this.parte = parte;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Presentacion getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(Presentacion presentacion) {
        this.presentacion = presentacion;
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

    public int getDiasEntrega() {
        return diasEntrega;
    }

    public void setDiasEntrega(int diasEntrega) {
        this.diasEntrega = diasEntrega;
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
}

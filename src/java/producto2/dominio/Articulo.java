package producto2.dominio;

import impuestos.dominio.ImpuestoGrupo;
import java.util.ArrayList;
import unidadesMedida.UnidadMedida;

/**
 *
 * @author jesc
 */
public class Articulo {
    private int idArticulo;
    private Parte parte;
    private String descripcion;
    private ArrayList<Upc> upcs;
    private Tipo tipo;
    private Grupo grupo;
    private SubGrupo subGrupo;
    private Marca marca;
    private Presentacion presentacion;
    private double contenido;
    private UnidadMedida unidadMedida;
    private ImpuestoGrupo impuestoGrupo;
    
    public Articulo() {
        this.parte=new Parte(0, "");
        this.descripcion="";
        this.upcs=new ArrayList<Upc>();
        this.tipo=new Tipo(0, "");
        this.grupo=new Grupo(0, 0, "");
        this.subGrupo=new SubGrupo(0, "");
        this.marca=new Marca(0, "", false);
        this.presentacion=new Presentacion(0, "", "");
        this.contenido=0;
        this.unidadMedida=new UnidadMedida(0, "", "");
        this.impuestoGrupo=new ImpuestoGrupo(0, "");
    }
    
//    @Override
//    public String toString() {
//        return (this.marca.getIdMarca()==0 ? "" : this.marca.toString()+" ")
//                + this.parte + (this.descripcion.equals("") ? "" : " "+this.descripcion) 
//                + (this.presentacion.getIdPresentacion()==1 ? "" : " " + this.presentacion.getAbreviatura()) 
//                + (this.contenido == 0 ? "" : " " + Double.toString(this.contenido))
//                + (this.contenido == 0 ? " ("+this.unidadMedida.getAbreviatura()+")" : " " + this.unidadMedida.getAbreviatura());
//    }
    @Override
    public String toString() {
        return (this.marca.getIdMarca()==0 ? "" : this.marca.toString()+" ")
                + this.parte + (this.descripcion.equals("") ? "" : " "+this.descripcion) 
                + (this.presentacion.getIdPresentacion()==1 ? "" : " " + this.presentacion.getAbreviatura()) 
                + (this.contenido == 0 ? "" : " " + Double.toString(this.contenido))
                + (this.contenido == 0 ? " ("+this.unidadMedida.getAbreviatura()+")" : " " + this.unidadMedida.getAbreviatura());
    }

    public int getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
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

    public ArrayList<Upc> getUpcs() {
        return upcs;
    }

    public void setUpcs(ArrayList<Upc> upcs) {
        this.upcs = upcs;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public SubGrupo getSubGrupo() {
        return subGrupo;
    }

    public void setSubGrupo(SubGrupo subGrupo) {
        this.subGrupo = subGrupo;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
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

    public ImpuestoGrupo getImpuestoGrupo() {
        return impuestoGrupo;
    }

    public void setImpuestoGrupo(ImpuestoGrupo impuestoGrupo) {
        this.impuestoGrupo = impuestoGrupo;
    }
}

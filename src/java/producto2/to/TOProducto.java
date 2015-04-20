package producto2.to;

import producto2.dominio.Empaque;
import producto2.dominio.SubProducto;

/**
 *
 * @author jesc
 */
public class TOProducto {
    private int idProducto;
    private String cod_pro;
    private int idArticulo;
    private int piezas;
    private Empaque empaque;
    private SubProducto subProducto;
    private String dun14;
    private double peso;
    private double volumen;
    
    public TOProducto() {
        this.cod_pro="";
        this.empaque=new Empaque();
        this.subProducto=new SubProducto();
        this.dun14="";
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCod_pro() {
        return cod_pro;
    }

    public void setCod_pro(String cod_pro) {
        this.cod_pro = cod_pro;
    }

    public int getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
    }

    public int getPiezas() {
        return piezas;
    }

    public void setPiezas(int piezas) {
        this.piezas = piezas;
    }

    public Empaque getEmpaque() {
        return empaque;
    }

    public void setEmpaque(Empaque empaque) {
        this.empaque = empaque;
    }

    public SubProducto getSubProducto() {
        return subProducto;
    }

    public void setSubProducto(SubProducto subProducto) {
        this.subProducto = subProducto;
    }

    public String getDun14() {
        return dun14;
    }

    public void setDun14(String dun14) {
        this.dun14 = dun14;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getVolumen() {
        return volumen;
    }

    public void setVolumen(double volumen) {
        this.volumen = volumen;
    }
}

package formulas.dominio;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author jesc
 */
public class Formula implements Serializable {
    private int idFormula;
    private int idEmpresa;
    private int idEmpaque;
//    private int tipo;
    private String cod_pro;
    private String empaque;
    private int idTipo;
    private double merma;
    private double mermaCant;
    private double manoDeObra;
    private double piezas;
    private double costoPromedio;
    private double costoPrimo;
    private String observaciones;
    private ArrayList<Insumo> insumos;
    private double sumaCantidad;
    private double sumaCosto;
    
    public Formula() {
        this.cod_pro="";
        this.empaque="";
        this.observaciones="";
        this.insumos=new ArrayList<Insumo>();
    }

    public int getIdFormula() {
        return idFormula;
    }

    public void setIdFormula(int idFormula) {
        this.idFormula = idFormula;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public String getCod_pro() {
        return cod_pro;
    }

    public void setCod_pro(String cod_pro) {
        this.cod_pro = cod_pro;
    }

    public String getEmpaque() {
        return empaque;
    }

    public void setEmpaque(String empaque) {
        this.empaque = empaque;
    }

    public double getMerma() {
        return merma;
    }

    public void setMerma(double merma) {
        this.merma = merma;
    }

    public double getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(double costoPromedio) {
        this.costoPromedio = costoPromedio;
    }

    public double getCostoPrimo() {
        return costoPrimo;
    }

    public void setCostoPrimo(double costoPrimo) {
        this.costoPrimo = costoPrimo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public ArrayList<Insumo> getInsumos() {
        return insumos;
    }

    public void setInsumos(ArrayList<Insumo> insumos) {
        this.insumos = insumos;
    }

    public double getSumaCantidad() {
        return sumaCantidad;
    }

    public void setSumaCantidad(double sumaCantidad) {
        this.sumaCantidad = sumaCantidad;
    }

    public double getSumaCosto() {
        return sumaCosto;
    }

    public void setSumaCosto(double sumaCosto) {
        this.sumaCosto = sumaCosto;
    }

//    public int getTipo() {
//        return tipo;
//    }
//
//    public void setTipo(int tipo) {
//        this.tipo = tipo;
//    }

    public double getMermaCant() {
        return mermaCant;
    }

    public void setMermaCant(double mermaCant) {
        this.mermaCant = mermaCant;
    }

    public double getPiezas() {
        return piezas;
    }

    public void setPiezas(double piezas) {
        this.piezas = piezas;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public double getManoDeObra() {
        return manoDeObra;
    }

    public void setManoDeObra(double manoDeObra) {
        this.manoDeObra = manoDeObra;
    }
}

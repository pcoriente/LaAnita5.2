package impuestos.dominio;

import java.io.Serializable;

/**
 *
 * @author Usuario
 */
public class Impuesto implements Serializable {

    private int idImpuesto;
    private String impuesto;
    private boolean acumulable;
    private boolean aplicable;
    private int modo; // 1. Aplica % sobre la base; 2. Aplica importe por pieza
    private boolean acreditable;
    
    public Impuesto() {
        this.impuesto="";
    }

    public Impuesto(int idImpuesto, String impuesto, boolean aplicable, int modo, boolean acreditable, boolean acumulable) {
        this.idImpuesto = idImpuesto;
        this.impuesto = impuesto;
        this.acumulable=acumulable;
        this.aplicable = aplicable;
        this.modo = modo;
        this.acreditable = acreditable;
    }

    public String modoAplicacion() {
        String strModo = "";
        if (this.aplicable) {
            if (this.modo == 1) {
                strModo = "PCTJE. SOBRE BASE";
            } else if (this.modo == 2) {
                strModo = "IMPORTE POR PIEZA";
            }
        }
        return strModo;
    }

    public String aplicacion() {
        return (this.aplicable?(this.acreditable?"ACREDITABLE":"RETENIDO")+", "+(this.modo==1?"PCTJE. SOBRE BASE":"IMPORTE POR PIEZA")+(this.acumulable?", ACUMULABLE":"") : "NO APLICA");
    }

    @Override
    public String toString() {
        return this.impuesto+(this.aplicable?(this.acreditable?", ACREDITABLE":", RETENIDO")+", "+(this.modo==1?"PCTJE. SOBRE BASE":"IMPORTE POR PIEZA")+(this.acumulable?", ACUMULABLE":"") : "");
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.idImpuesto;
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
        final Impuesto other = (Impuesto) obj;
        if (this.idImpuesto != other.idImpuesto) {
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
        return this.impuesto;
    }

    public void setImpuesto(String impuesto) {
        this.impuesto = impuesto;
    }

    public boolean isAplicable() {
        return aplicable;
    }

    public void setAplicable(boolean aplicable) {
        this.aplicable = aplicable;
    }

    public int getModo() {
        return this.modo;
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

    public boolean isAcumulable() {
        return acumulable;
    }

    public void setAcumulable(boolean acumulable) {
        this.acumulable = acumulable;
    }
}

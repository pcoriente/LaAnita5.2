package monedas;

import java.io.Serializable;

/**
 *
 * @author Comodoro
 */
public class Moneda implements Serializable {

    private int idMoneda;
    private String moneda;
    private String codigoIso;
    private String prefijoUnidad;
    private String prefijo;
    private String sufijo;
    private String simbolo;

    public Moneda() {
        this.idMoneda=1;
        this.moneda="";
        this.codigoIso="";
        this.prefijoUnidad="";
        this.prefijo="";
        this.sufijo="";
        this.simbolo="";
    }
    
    public Moneda(int idMoneda) {
        this.idMoneda=idMoneda;
        this.moneda="";
        this.codigoIso="";
        this.prefijoUnidad="";
        this.prefijo="";
        this.sufijo="";
        this.simbolo="";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.idMoneda;
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
        final Moneda other = (Moneda) obj;
        if (this.idMoneda != other.idMoneda) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return moneda;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getCodigoIso() {
        return codigoIso;
    }

    public void setCodigoIso(String codigoIso) {
        this.codigoIso = codigoIso;
    }

    public String getPrefijoUnidad() {
        return prefijoUnidad;
    }

    public void setPrefijoUnidad(String prefijoUnidad) {
        this.prefijoUnidad = prefijoUnidad;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public String getSufijo() {
        return sufijo;
    }

    public void setSufijo(String sufijo) {
        this.sufijo = sufijo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }
}

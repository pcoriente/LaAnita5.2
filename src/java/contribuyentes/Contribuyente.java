package contribuyentes;

import direccion.dominio.Direccion;
import java.io.Serializable;

/**
 *
 * @author jsolis
 */
public class Contribuyente implements Serializable {
    private int idContribuyente;
    private String contribuyente;
    private int idRfc;
    private String rfc;
    private Direccion direccion = new Direccion();
    private String curp;
    
    public Contribuyente(int idRfc, String rfc) {
        this.contribuyente="";
        this.idRfc=idRfc;
        this.rfc=rfc;
        this.curp="";
        this.direccion=new Direccion();
    }

    public Contribuyente() {
        this.idContribuyente = 0;
        this.contribuyente = "";
        this.idRfc = 0;
        this.rfc = "";
        this.curp = "";
        this.direccion = new Direccion();
    }

    @Override
    public String toString() {
        return this.contribuyente;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.idContribuyente;
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
        final Contribuyente other = (Contribuyente) obj;
        if (this.idContribuyente != other.idContribuyente) {
            return false;
        }
        return true;
    }

    public int getIdContribuyente() {
        return idContribuyente;
    }

    public void setIdContribuyente(int idContribuyente) {
        this.idContribuyente = idContribuyente;
    }

    public String getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(String contribuyente) {
        this.contribuyente = contribuyente;
    }

    public int getIdRfc() {
        return idRfc;
    }

    public void setIdRfc(int idRfc) {
        this.idRfc = idRfc;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }
}

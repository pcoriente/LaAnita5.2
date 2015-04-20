package direccion.dominio;

import java.io.Serializable;

/**
 *
 * @author Julio
 */
public class Asentamiento implements Serializable{
    private String codAsentamiento;
    private String codigoPostal;
    private String cTipo;
    private String tipo;
    private String asentamiento;
    private String codEstado;
    private String estado;
    private String codMunicipio;
    private String municipio;
    private String ciudad;
    
    public Asentamiento() {
        this.codAsentamiento="";
        this.codigoPostal="";
        this.cTipo="";
        this.tipo="";
        this.asentamiento="";
        this.codEstado="";
        this.estado="";
        this.codMunicipio="";
        this.municipio="";
        this.ciudad="";
    }

    @Override
    public String toString() {
        return tipo + " " + asentamiento;
//        String str="";
//        if(!this.asentamiento.isEmpty()) str=this.tipo+" "+this.asentamiento;
//        return str;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Asentamiento other = (Asentamiento) obj;
        if ((this.codAsentamiento == null) ? (other.codAsentamiento != null) : !this.codAsentamiento.equals(other.codAsentamiento)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.codAsentamiento != null ? this.codAsentamiento.hashCode() : 0);
        return hash;
    }

    public String getAsentamiento() {
        return asentamiento;
    }

    public void setAsentamiento(String asentamiento) {
        this.asentamiento = asentamiento;
    }

    public String getcTipo() {
        return cTipo;
    }

    public void setcTipo(String cTipo) {
        this.cTipo = cTipo;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodAsentamiento() {
        return codAsentamiento;
    }

    public void setCodAsentamiento(String codAsentamiento) {
        this.codAsentamiento = codAsentamiento;
    }

    public String getCodEstado() {
        return codEstado;
    }

    public void setCodEstado(String codEstado) {
        this.codEstado = codEstado;
    }

    public String getCodMunicipio() {
        return codMunicipio;
    }

    public void setCodMunicipio(String codMunicipio) {
        this.codMunicipio = codMunicipio;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}

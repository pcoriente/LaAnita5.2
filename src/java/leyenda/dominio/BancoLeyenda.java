package leyenda.dominio;

import java.io.Serializable;

public class BancoLeyenda implements Serializable {

    public int idBanco;
    public String rfc;
    public int codigoBanco;
    public String razonSocial;
    public String nombreCorto;
    private String codigoB;

    public String getCodigoB() {
        return codigoB;
    }

    public void setCodigoB(String codigoB) {
        this.codigoB = codigoB;
    }

    public BancoLeyenda() {
    }

    public int getCodigoBanco() {
        return codigoBanco;
    }

    public void setCodigoBanco(int codigoBanco) {
        this.codigoBanco = codigoBanco;
    }

    public int getIdBanco() {
        return idBanco;
    }

    public void setIdBanco(int idBanco) {
        this.idBanco = idBanco;
    }

    public String getNombreCorto() {
        return nombreCorto;
    }

    public void setNombreCorto(String nombreCorto) {
        this.nombreCorto = nombreCorto;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    @Override
    public String toString() {
        return nombreCorto;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.idBanco;
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
        final BancoLeyenda other = (BancoLeyenda) obj;
        if (this.idBanco != other.idBanco) {
            return false;
        }
        return true;
    }
}
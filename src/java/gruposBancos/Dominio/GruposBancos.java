/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruposBancos.Dominio;

/**
 *
 * @author Usuario
 */
public class GruposBancos {

    private int idGrupoBanco;
    private int idGrupoCte;
    private int idBanco;
    private String numCtaPago;
    private String medioPago;

    public int getIdGrupoBanco() {
        return idGrupoBanco;
    }

    public void setIdGrupoBanco(int idGrupoBanco) {
        this.idGrupoBanco = idGrupoBanco;
    }

    public int getIdGrupoCte() {
        return idGrupoCte;
    }

    public void setIdGrupoCte(int idGrupoCte) {
        this.idGrupoCte = idGrupoCte;
    }

    public int getIdBanco() {
        return idBanco;
    }

    public void setIdBanco(int idBanco) {
        this.idBanco = idBanco;
    }

    public String getNumCtaPago() {
        return numCtaPago;
    }

    public void setNumCtaPago(String numCtaPago) {
        this.numCtaPago = numCtaPago;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.idGrupoBanco;
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
        final GruposBancos other = (GruposBancos) obj;
        if (this.idGrupoBanco != other.idGrupoBanco) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return numCtaPago + ", " + medioPago ;
    }

}

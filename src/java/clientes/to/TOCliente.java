package clientes.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOCliente {

    private int idCliente;
    private int idGrupoCte;
    private String grupoCte;
    private String grupoClienteCodigo;
//    private int idFormato;
//    private String formato;
    private int idEsquema;
    private int idContribuyente;
    private String contribuyente;
    private int idDireccionFiscal;
    private int idRfc;
    private String rfc;
    private String curp;
    private int idDireccion;
    private Date fechaAlta;
    private int diasCredito;
    private double limiteCredito;
    private double desctoComercial;
    private int diasBloqueo;
    private String esquema;

    public TOCliente() {
//        this.formato="";
        this.contribuyente = "";
        this.rfc = "";
        this.curp = "";
        this.fechaAlta = new Date();
    }

    @Override
    public String toString() {
        return this.contribuyente;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.idCliente;
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
        final TOCliente other = (TOCliente) obj;
        if (this.idCliente != other.idCliente) {
            return false;
        }
        return true;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdGrupoCte() {
        return idGrupoCte;
    }

    public void setIdGrupoCte(int idGrupoCte) {
        this.idGrupoCte = idGrupoCte;
    }

    public String getGrupoCte() {
        return grupoCte;
    }

    public void setGrupoCte(String grupoCte) {
        this.grupoCte = grupoCte;
    }

    public String getGrupoClienteCodigo() {
        return grupoClienteCodigo;
    }

    public void setGrupoClienteCodigo(String grupoClienteCodigo) {
        this.grupoClienteCodigo = grupoClienteCodigo;
    }

//    public int getIdFormato() {
//        return idFormato;
//    }
//
//    public void setIdFormato(int idFormato) {
//        this.idFormato = idFormato;
//    }
//
//    public String getFormato() {
//        return formato;
//    }
//
//    public void setFormato(String formato) {
//        this.formato = formato;
//    }
    public int getIdEsquema() {
        return idEsquema;
    }

    public void setIdEsquema(int idEsquema) {
        this.idEsquema = idEsquema;
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

    public int getIdDireccionFiscal() {
        return idDireccionFiscal;
    }

    public void setIdDireccionFiscal(int idDireccionFiscal) {
        this.idDireccionFiscal = idDireccionFiscal;
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

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public int getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(int idDireccion) {
        this.idDireccion = idDireccion;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public int getDiasCredito() {
        return diasCredito;
    }

    public void setDiasCredito(int diasCredito) {
        this.diasCredito = diasCredito;
    }

    public double getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(double limiteCredito) {
        this.limiteCredito = limiteCredito;
    }

    public double getDesctoComercial() {
        return desctoComercial;
    }

    public void setDesctoComercial(double desctoComercial) {
        this.desctoComercial = desctoComercial;
    }

    public int getDiasBloqueo() {
        return diasBloqueo;
    }

    public void setDiasBloqueo(int diasBloqueo) {
        this.diasBloqueo = diasBloqueo;
    }

    public String getEsquema() {
        return esquema;
    }

    public void setEsquema(String esquema) {
        this.esquema = esquema;
    }

}

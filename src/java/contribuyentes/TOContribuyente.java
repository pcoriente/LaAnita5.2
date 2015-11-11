package contribuyentes;

/**
 *
 * @author jsolis
 */
public class TOContribuyente {
    private int idContribuyente;
    private String contribuyente;
    private int idRfc;
    private String rfc;
    private int idDireccion;
    private String curp;

    public TOContribuyente() {
        this.contribuyente="";
        this.rfc="";
        this.curp="";
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

    public int getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(int idDireccion) {
        this.idDireccion = idDireccion;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }
}

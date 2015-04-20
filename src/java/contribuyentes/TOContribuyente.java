package contribuyentes;

/**
 *
 * @author jsolis
 */
public class TOContribuyente {
    private int idContribuyente;
    private String contribuyente;
    private String rfc;
    private int idDireccion;

    public TOContribuyente() {
        this.idContribuyente=0;
        this.contribuyente="";
        this.rfc="";
        this.idDireccion=0;
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
}

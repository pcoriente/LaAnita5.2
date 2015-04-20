package clientes.dominio;

/**
 *
 * @author jesc
 */
public class MiniCliente {
    private int idCliente;
    private int idContribuyente;
    private String contribuyente;
//    private int idFormato;
//    private String formato;
    
    public MiniCliente() {
        this.contribuyente="";
//        this.formato="";
    }

    @Override
    public String toString() {
//        return this.contribuyente+(this.idFormato==0?"":" ("+this.formato+")");
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
        final MiniCliente other = (MiniCliente) obj;
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
}

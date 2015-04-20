package contactos.dominio;
import java.io.Serializable;

/**
 *
 * @author jsolis
 */
public class Telefono implements Serializable {
    private int idTelefono;
    private String lada;
    private String telefono;
    private String extension;
    private TelefonoTipo tipo = new TelefonoTipo(false);

    public Telefono() {
        this.idTelefono=0;
        this.lada="";
        this.telefono="";
        this.extension="";
        this.tipo=new TelefonoTipo(false);
    }

    @Override
    public String toString() {
        return this.tipo.toString()
                +(this.telefono.isEmpty() ? "" : (this.lada.isEmpty() ? "" : "("+this.lada+")")+this.telefono+(this.extension.isEmpty() ? "" : "Ext."+this.extension));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.idTelefono;
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
        final Telefono other = (Telefono) obj;
        if (this.idTelefono != other.idTelefono) {
            return false;
        }
        return true;
    }

    public int getIdTelefono() {
        return idTelefono;
    }

    public void setIdTelefono(int idTelefono) {
        this.idTelefono = idTelefono;
    }

    public String getLada() {
        return lada;
    }

    public void setLada(String lada) {
        this.lada = lada;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public TelefonoTipo getTipo() {
        return tipo;
    }

    public void setTipo(TelefonoTipo tipo) {
        this.tipo = tipo;
    }
}

package producto2.dominio;

/**
 *
 * @author jesc
 */
public class Parte {
    private int idParte;
    private String parte;
    
    public Parte() {
        this.parte="";
    }
    
    public Parte(int idParte, String parte) {
        this.idParte=idParte;
        this.parte=parte;
    }
    
    @Override
    public String toString() {
        return parte;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.idParte;
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
        final Parte other = (Parte) obj;
        if (this.idParte != other.idParte) {
            return false;
        }
        return true;
    }

    public int getIdParte() {
        return idParte;
    }

    public void setIdParte(int idParte) {
        this.idParte = idParte;
    }

    public String getParte() {
        return parte;
    }

    public void setParte(String parte) {
        this.parte = parte;
    }
}

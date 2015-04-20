package dbs.dominio;

/**
 *
 * @author david
 */
public class Dbs {
    private int idDbs;
    private String nombreBds;
    private String jndiDbs;

    public int getIdDbs() {
        return idDbs;
    }

    public void setIdDbs(int idDbs) {
        this.idDbs = idDbs;
    }

    public String getJndiDbs() {
        return jndiDbs;
    }

    public void setJndiDbs(String jndiDbs) {
        this.jndiDbs = jndiDbs;
    }

    public String getNombreBds() {
        return nombreBds;
    }

    public void setNombreBds(String nombreBds) {
        this.nombreBds = nombreBds;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Dbs other = (Dbs) obj;
        if (this.idDbs != other.idDbs) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.idDbs;
        return hash;
    }
}

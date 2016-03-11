package envios.dominio;

/**
 *
 * @author jesc
 */
public class EnvioTraspasoPojo {
    private int idSolicitud;
//    private int idEnvio;
    private String almacen;

    public EnvioTraspasoPojo() {
        this.almacen = "";
    }

    @Override
    public String toString() {
        return this.almacen;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.idSolicitud;
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
        final EnvioTraspasoPojo other = (EnvioTraspasoPojo) obj;
        if (this.idSolicitud != other.idSolicitud) {
            return false;
        }
        return true;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

//    public int getIdEnvio() {
//        return idEnvio;
//    }
//
//    public void setIdEnvio(int idEnvio) {
//        this.idEnvio = idEnvio;
//    }
//
    public String getAlmacen() {
        return almacen;
    }

    public void setAlmacen(String almacen) {
        this.almacen = almacen;
    }
}

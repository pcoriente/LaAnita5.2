package almacenes.dominio;
import direccion.dominio.Direccion;
import java.io.Serializable;

/**
 *
 * @author julios
 */
public class Almacen implements Serializable{
    private int idAlmacen;
    private String almacen;
    private int idCedis;
    private int idEmpresa;
    private Direccion direccion;
    
    public Almacen() {
        this.idAlmacen=0;
        this.almacen="";
        this.idCedis=0;
        this.idEmpresa=0;
        this.direccion=new Direccion();
    }
    
    public Almacen(int idCedis, int idEmpresa) {
        this.idAlmacen = 0;
        this.almacen = "";
        this.idCedis = idCedis;
        this.idEmpresa = idEmpresa;
        this.direccion = new Direccion();
    }

    @Override
    public String toString() {
        return this.almacen;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.idAlmacen;
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
        final Almacen other = (Almacen) obj;
        if (this.idAlmacen != other.idAlmacen) {
            return false;
        }
        return true;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public String getAlmacen() {
        return almacen;
    }

    public void setAlmacen(String almacen) {
        this.almacen = almacen;
    }

    public int getIdCedis() {
        return idCedis;
    }

    public void setIdCedis(int idCedis) {
        this.idCedis = idCedis;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }
}

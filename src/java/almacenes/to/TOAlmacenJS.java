package almacenes.to;

/**
 *
 * @author jesc
 */
public class TOAlmacenJS {
    private int idAlmacen;
    private String almacen;
    private int idCedis;
    private int idEmpresa;
    private String empresa;
    private int idDireccion;
    
    public TOAlmacenJS() {
        this.idAlmacen=0;
        this.almacen="";
        this.idCedis=0;
        this.idEmpresa=0;
        this.empresa="";
        this.idDireccion=0;
    }
    
    @Override
    public String toString() {
        return (this.idEmpresa==0?"":this.empresa+" - ")+this.almacen;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.idAlmacen;
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
        final TOAlmacenJS other = (TOAlmacenJS) obj;
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

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public int getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(int idDireccion) {
        this.idDireccion = idDireccion;
    }
}

package almacenes.to;

/**
 *
 * @author jesc
 */
public class TOAlmacenJS {
    private int idAlmacen;
    private String almacen;
    private int idCedis;
    private String cedis;
    private int idEmpresa;
    private String nombreComercial;
    private String empresa;
    private int idDireccion;
    
    public TOAlmacenJS() {
        this.almacen="";
        this.cedis="";
        this.nombreComercial = "";
        this.empresa="";
    }
    
    @Override
    public String toString() {
        return (this.idEmpresa==0?"":this.nombreComercial+" - ")+this.almacen+" - "+ this.cedis;
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

    public String getCedis() {
        return cedis;
    }

    public void setCedis(String cedis) {
        this.cedis = cedis;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
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

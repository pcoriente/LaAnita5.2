package almacenes.dominio;

/**
 *
 * @author jesc
 */
public class MiniAlmacen {
    private int idAlmacen;
    private String almacen;
//    private int idEmpresa;
//    private int idCedis;
    private int idEmpresa;
    private String empresa;

    public MiniAlmacen() {
//        this.idAlmacen=0;
        this.almacen="";
//        this.idEmpresa=0;
//        this.idCedis=0;
        this.empresa="";
    }

    @Override
    public String toString() {
        return this.empresa+ " " + this.almacen;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.idAlmacen;
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
        final MiniAlmacen other = (MiniAlmacen) obj;
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

//    }

    //    public int getIdEmpresa() {
    //        return idEmpresa;
    //    }
    //
    //    public void setIdEmpresa(int idEmpresa) {
    //        this.idEmpresa = idEmpresa;
    //    }
    //
    //    public int getIdCedis() {
    //        return idCedis;
    //    }
    //
    //    public void setIdCedis(int idCedis) {
    //        this.idCedis = idCedis;
    //    }
    
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
}

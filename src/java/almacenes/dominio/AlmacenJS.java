package almacenes.dominio;

import cedis.dominio.MiniCedis;
import direccion.dominio.Direccion;
import empresas.dominio.MiniEmpresa;

/**
 *
 * @author jesc
 */
public class AlmacenJS {
    private int idAlmacen;
    private String almacen;
    private MiniCedis cedis;
    private MiniEmpresa empresa;
    private Direccion direccion;

    public AlmacenJS() {
        this.idAlmacen=0;
        this.almacen="";
        this.cedis=new MiniCedis();
        this.empresa=new MiniEmpresa();
        this.direccion=new Direccion();
    }

    @Override
    public String toString() {
        return (this.empresa.getIdEmpresa()==0?"":this.empresa.getNombreComercial()+" - ")+this.almacen;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.idAlmacen;
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
        final AlmacenJS other = (AlmacenJS) obj;
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

    public MiniCedis getCedis() {
        return cedis;
    }

    public void setCedis(MiniCedis cedis) {
        this.cedis = cedis;
    }

    public MiniEmpresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(MiniEmpresa empresa) {
        this.empresa = empresa;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }
}

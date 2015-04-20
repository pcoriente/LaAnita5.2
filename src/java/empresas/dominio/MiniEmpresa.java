package empresas.dominio;

/**
 *
 * @author JULIOS
 */
public class MiniEmpresa {
    private int idEmpresa;
    private String codigoEmpresa;
    private String nombreComercial;
    //private Direccion direccion;

    public MiniEmpresa() {
        this.idEmpresa=0;
        this.codigoEmpresa="0";
        this.nombreComercial="";
    }

    @Override
    public String toString() {
        return nombreComercial;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.idEmpresa;
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
        final MiniEmpresa other = (MiniEmpresa) obj;
        if (this.idEmpresa != other.idEmpresa) {
            return false;
        }
        return true;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getCodigoEmpresa() {
        return codigoEmpresa;
    }

    public void setCodigoEmpresa(String codigoEmpresa) {
        this.codigoEmpresa = codigoEmpresa;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

//    public Direccion getDireccion() {
//        return direccion;
//    }
//
//    public void setDireccion(Direccion direccion) {
//        this.direccion = direccion;
//    }
    
    
}

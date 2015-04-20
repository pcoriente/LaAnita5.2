package cedis.dominio;

import direccion.dominio.Direccion;

/**
 *
 * @author Julio
 */
public class Cedis {
    private int idCedis;
    private int codigo;
    private String cedis;
    private Direccion direccion;
    private String telefono;
    private String fax;
    private String correo;
    private String representante;
    //private int idDireccion;

    public String getCedis() {
        return cedis;
    }

    public void setCedis(String cedis) {
        this.cedis = cedis;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public int getIdCedis() {
        return idCedis;
    }

    public void setIdCedis(int idCedis) {
        this.idCedis = idCedis;
    }

    public String getRepresentante() {
        return representante;
    }

    public void setRepresentante(String representante) {
        this.representante = representante;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

//    public int getIdDireccion() {
//        return idDireccion;
//    }
//
//    public void setIdDireccion(int idDireccion) {
//        this.idDireccion = idDireccion;
//    }
    
    
}

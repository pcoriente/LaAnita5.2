package usuarios.dominio;

/**
 *
 * @author JULIOS
 */
public class Accion {
    private int idAccion;
    private String accion;
    private String idBoton;

    public Accion(int idAccion, String accion, String idBoton) {
        this.idAccion = idAccion;
        this.accion = accion;
        this.idBoton = idBoton;
    }

    public int getIdAccion() {
        return idAccion;
    }

    public void setIdAccion(int idAccion) {
        this.idAccion = idAccion;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getIdBoton() {
        return idBoton;
    }

    public void setIdBoton(String idBoton) {
        this.idBoton = idBoton;
    }
}

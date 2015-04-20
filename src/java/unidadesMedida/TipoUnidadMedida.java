package unidadesMedida;

/**
 *
 * @author JULIOS
 */
public enum TipoUnidadMedida {
    CAPACIDAD (1, "Capacidad"),
    PESO (2, "Peso"),
    AREA (3, "Area"),
    LONGITUD (4, "Longitud"),
    UNIDAD (5, "Unidad");
    
    private int idTipo;
    private String tipo;
    
    TipoUnidadMedida(int idTipo, String tipo) {
        this.idTipo=idTipo;
        this.tipo=tipo;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}

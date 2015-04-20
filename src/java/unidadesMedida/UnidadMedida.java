package unidadesMedida;

/**
 *
 * @author JULIOS
 */
public class UnidadMedida {
    private int idUnidadMedida;
    private String unidadMedida;
    private String abreviatura;
    private int idTipo; // Para cuando se requierea inplementar los tipod de unidad de Medida: Longitud, volumen, peso
                        // Tambien abria que pensar en las conversiones de metrico decimal a ingles.

    public UnidadMedida(int idUnidadMedida, String unidadMedida, String abreviatura) {
        this.idUnidadMedida = idUnidadMedida;
        this.unidadMedida = unidadMedida;
        this.abreviatura = abreviatura;
        this.idTipo = 0;
    }

    UnidadMedida() {
        
    }

    @Override
    public String toString() {
        return unidadMedida;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.idUnidadMedida;
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
        final UnidadMedida other = (UnidadMedida) obj;
        if (this.idUnidadMedida != other.idUnidadMedida) {
            return false;
        }
        return true;
    }

    public int getIdUnidadMedida() {
        return idUnidadMedida;
    }

    public void setIdUnidadMedida(int idUnidadMedida) {
        this.idUnidadMedida = idUnidadMedida;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }
}

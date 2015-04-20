package clientes.dominio;

public class ClienteSEA {

    private int cod_cli;
    private String cod_gru;
    private String cod_age;
    private String nombre;
    

    public String getCod_age() {
        return cod_age;
    }

    public void setCod_age(String cod_age) {
        this.cod_age = cod_age;
    }

    public int getCod_cli() {
        return cod_cli;
    }

    public void setCod_cli(int cod_cli) {
        this.cod_cli = cod_cli;
    }

    public String getCod_gru() {
        return cod_gru;
    }

    public void setCod_gru(String cod_gru) {
        this.cod_gru = cod_gru;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

   
    public String obtenCodigoString(int cod_cli) {
         String  sig = String.valueOf(cod_cli);
        while (sig.length()<5) sig="0"+sig;
        return sig;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClienteSEA other = (ClienteSEA) obj;
        if (this.cod_cli != other.cod_cli) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + this.cod_cli;
        return hash;
    }

    @Override
    public String toString() {
        return obtenCodigoString(cod_cli)+"-"+ nombre ;
    }

   
    
    
}

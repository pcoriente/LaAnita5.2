package ventas.dominio;

import clientes.dominio.MiniCliente;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class VentaComprobante {
//    private Contribuyente cliente;
//    private ClientesFormatos formato;
    private MiniCliente cliente;
    private String remision;
    private Date fecha;
    
    public VentaComprobante() {
//        this.cliente=new Contribuyente();
//        this.formato=new ClientesFormatos();
        this.cliente=new MiniCliente();
        this.remision="";
        this.fecha=new Date();
    }

//    public Contribuyente getCliente() {
//        return cliente;
//    }
//
//    public void setCliente(Contribuyente cliente) {
//        this.cliente = cliente;

//    public ClientesFormatos getFormato() {
//        return formato;
//    }
//
//    public void setFormato(ClientesFormatos formato) {
//        this.formato = formato;
//    }

//    public MiniTienda getTienda() {
//        return tienda;
//    }
//
//    public void setTienda(MiniTienda tienda) {
//        this.tienda = tienda;
//    }

    //    }
//        return tienda;
//    }
//
//    public void setTienda(MiniTienda tienda) {
//        this.tienda = tienda;
//    }
    
    public MiniCliente getCliente() {
        return cliente;
    }

    public void setCliente(MiniCliente cliente) {
        this.cliente = cliente;
    }

    public String getRemision() {
        return remision;
    }

    public void setRemision(String remision) {
        this.remision = remision;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}

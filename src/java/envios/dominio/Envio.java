package envios.dominio;

import almacenes.dominio.MiniAlmacen;
import cedis.dominio.MiniCedis;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class Envio {
    private int idEnvio;
    private MiniCedis cedis;
    private MiniAlmacen almacen;
    private Date generado;
    private Date enviado;
    private double peso;
    private int status;
    private int prioridad;
    private int idChofer;
    private int idCamion;
    private ArrayList<EnvioPedido> pedidos;
    
    public Envio() {
        this.cedis=new MiniCedis();
        this.almacen=new MiniAlmacen();
        this.generado=new Date();
        this.enviado=new Date();
        this.peso=0;
        this.pedidos=new ArrayList<EnvioPedido>();
    }
    
    public Envio(MiniCedis cedis, MiniAlmacen almacen) {
        this.cedis=cedis;
        this.almacen=almacen;
        this.generado=new Date();
        this.enviado=new Date();
        this.peso=0;
        this.pedidos=new ArrayList<EnvioPedido>();
    }

    @Override
    public String toString() {
        return (this.idEnvio==0?"Nuevo Envio":String.format("%06d", this.idEnvio));
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public MiniCedis getCedis() {
        return cedis;
    }

    public void setCedis(MiniCedis cedis) {
        this.cedis = cedis;
    }

    public MiniAlmacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(MiniAlmacen almacen) {
        this.almacen = almacen;
    }

    public Date getGenerado() {
        return generado;
    }

    public void setGenerado(Date generado) {
        this.generado = generado;
    }

    public Date getEnviado() {
        return enviado;
    }

    public void setEnviado(Date enviado) {
        this.enviado = enviado;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
    
    public int getStatus() {
        return status;
    }

    public String getEstatus() {
        String msg="";
        if(this.status==0) {
            msg="Abierto";
        } else if(this.status==1) {
            msg="Cerrado";
        } else {
            msg="Enviado";
        }
        return msg;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getIdChofer() {
        return idChofer;
    }

    public void setIdChofer(int idChofer) {
        this.idChofer = idChofer;
    }

    public int getIdCamion() {
        return idCamion;
    }

    public void setIdCamion(int idCamion) {
        this.idCamion = idCamion;
    }

    public ArrayList<EnvioPedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(ArrayList<EnvioPedido> pedidos) {
        this.pedidos = pedidos;
    }
}

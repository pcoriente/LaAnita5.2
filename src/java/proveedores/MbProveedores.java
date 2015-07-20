package proveedores;

import contactos.MbContactos;
import contactos.dominio.Contacto;
import contactos.dominio.Telefono;
import contactos.dominio.TelefonoTipo;
import Message.Mensajes;
import contribuyentes.Contribuyente;
import contribuyentes.MbBuscarContribuyente;
import contribuyentes.MbContribuyentesJS;
import direccion.MbDireccion;
import direccion.dominio.Direccion;
import impuestos.MbZonas;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import proveedores.dao.DAOProveedores;
import proveedores.dominio.Clasificacion;
import proveedores.dominio.Proveedor;
import proveedores.dominio.SubClasificacion;
import proveedores.dominio.TipoOperacion;
import proveedores.dominio.TipoTercero;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import utilerias.Utilerias;

/**
 *
 * @author Julio
 */
@ManagedBean(name = "mbProveedores")
@SessionScoped
public class MbProveedores implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion;
    @ManagedProperty(value = "#{mbContribuyentesJS}")
    private MbContribuyentesJS mbContribuyentes;
    private ArrayList<SelectItem> listaContribuyentes;
    @ManagedProperty(value="#{mbBuscarContribuyente}")
    private MbBuscarContribuyente mbBuscarContribuyente;
    @ManagedProperty(value="#{mbClasificaciones}")
    private MbClasificaciones mbClasificaciones;
    @ManagedProperty(value="#{mbTipoTerceros}")
    private MbTipoTerceros mbTipoTerceros;
    @ManagedProperty(value="#{mbTipoOperacion}")
    private MbTipoOperacion mbTipoOperacion;
    @ManagedProperty(value="#{mbContactos}")
    private MbContactos mbContactos;
    @ManagedProperty(value="#{mbZonas}")
    private MbZonas mbImpuestoZona;

    private int idTipo=1;
    private Clasificacion clasificacion;
    private ArrayList<SelectItem> listaClasificaciones;
    private ArrayList<SelectItem> listaSubClasificaciones;
    private ArrayList<SelectItem> listaTipoTerceros;
    private ArrayList<SelectItem> listaTipoOperaciones;
    private Contacto contacto;  // Contacto Seleccionado del SelectOne
    private Telefono telefono;  // Telefono Seleccionado del SelectOne
    private Proveedor proveedor;
    private ArrayList<Proveedor> listaProveedores;
    private ArrayList<Proveedor> listaFiltrados;
    private DAOProveedores dao;

    public MbProveedores() {
        this.mbBuscarContribuyente=new MbBuscarContribuyente();
        this.mbContribuyentes = new MbContribuyentesJS();
        this.mbDireccion = new MbDireccion();
        this.mbAcciones = new MbAcciones();
        this.mbClasificaciones=new MbClasificaciones();
        this.mbTipoTerceros=new MbTipoTerceros();
        this.mbTipoOperacion=new MbTipoOperacion();
        this.mbContactos=new MbContactos();
        this.mbImpuestoZona=new MbZonas();
        
        this.obtenerListaTipoTerceros();
//        this.obtenerListaTipoOperaciones();
//        this.cargaClasificaciones();
        
        this.listaProveedores=new ArrayList<>();
        this.clasificacion=new Clasificacion();
        this.listaContribuyentes = new ArrayList<>();
        this.listaFiltrados = new ArrayList<>();
    }

    public void eliminarTelefonoTipo() {
        if(this.mbContactos.getMbTelefonos().eliminarTipo(this.mbContactos.getMbTelefonos().getTipo().getIdTipo())) {
            this.mbContactos.getMbTelefonos().getTelefono().setTipo(new TelefonoTipo(this.mbContactos.getMbTelefonos().isCelular()));
            this.mbContactos.getMbTelefonos().cargaTipos();
        }
    }
    
    public void grabarTelefonoTipo() {
        if(this.mbContactos.getMbTelefonos().grabarTipo()) {
            this.mbContactos.getMbTelefonos().getTelefono().setTipo(this.mbContactos.getMbTelefonos().getTipo());
            this.mbContactos.getMbTelefonos().cargaTipos();
        }
    }
    
    public void mttoTelefonoTipo() {
        if(this.mbContactos.getMbTelefonos().getTelefono().getTipo().getIdTipo()==0) {
            this.mbContactos.getMbTelefonos().setTipo(new TelefonoTipo(this.mbContactos.getMbTelefonos().isCelular()));
        } else {
            this.mbContactos.getMbTelefonos().setTipo(this.mbContactos.getMbTelefonos().copiaTipo(this.mbContactos.getMbTelefonos().getTelefono().getTipo()));
        }
    }
    
    public void eliminarTelefono() {
        if(this.mbContactos.getMbTelefonos().eliminar(this.mbContactos.getMbTelefonos().getTelefono().getIdTelefono())) {
            this.telefono=new Telefono();
            this.mbContactos.getMbTelefonos().cargaTelefonos(this.contacto.getIdContacto());
        }
    }
    
    public void grabarTelefono() {
        if(this.mbContactos.getMbTelefonos().grabar(this.contacto.getIdContacto())) {
            this.telefono=this.mbContactos.getMbTelefonos().getTelefono();
            this.mbContactos.getMbTelefonos().cargaTelefonos(this.contacto.getIdContacto());
        }
    }
    
    public void mttoTelefono() {
        if(this.telefono.getIdTelefono()==0) {
            this.mbContactos.getMbTelefonos().setTelefono(new Telefono());
        } else {
            this.mbContactos.getMbTelefonos().setTelefono(this.mbContactos.getMbTelefonos().copia(this.telefono));
        }
        this.mbContactos.getMbTelefonos().setCelular(this.mbContactos.getMbTelefonos().getTelefono().getTipo().isCelular());
        this.mbContactos.getMbTelefonos().cargaTipos();
    }
    
    public void cargarTelefonos() {
        this.mbContactos.getMbTelefonos().cargaTelefonos(this.contacto.getIdContacto());
    }
    
    public void eliminarContacto() {
        if(this.mbContactos.eliminar(this.contacto.getIdContacto())) {
            this.contacto=new Contacto();
            this.mbContactos.cargaContactos(this.idTipo, this.proveedor.getIdProveedor());
        }
    }
    
    public void grabarContacto() {
        if(this.mbContactos.grabar(this.idTipo, this.proveedor.getIdProveedor())) {
            this.contacto=this.mbContactos.getContacto();
            this.mbContactos.cargaContactos(this.idTipo, this.proveedor.getIdProveedor());
        }
    }
    
    public void mttoContacto() {
        if(this.contacto.getIdContacto()==0) {
            this.mbContactos.setContacto(new Contacto());
        } else {
            this.mbContactos.setContacto(this.mbContactos.copia(this.contacto));
        }
    }
    
    public void obtenerDireccionContribuyente() {
        int idDireccion = this.proveedor.getContribuyente().getDireccion().getIdDireccion();
        this.proveedor.getContribuyente().setDireccion(this.mbDireccion.obtener(idDireccion));
    }

    public void grabarContribuyente() {
        if (!this.mbDireccion.validarDireccion(this.mbContribuyentes.getContribuyente().getDireccion())) {
            Mensajes.mensajeAlert("Direccion no valida !!!");
        } else {
            boolean agregar = false;
            if (this.mbContribuyentes.getContribuyente().getIdContribuyente() == 0) {
                agregar = true;
            }
            if (this.mbContribuyentes.grabar()) {
                this.proveedor.setContribuyente(this.mbContribuyentes.getContribuyente());
                if (agregar) {
                    this.listaContribuyentes.add(new SelectItem(this.proveedor.getContribuyente(), this.proveedor.getContribuyente().getContribuyente()));
                }
            }
            this.listaContribuyentes=new ArrayList<>();
            for(Contribuyente c: this.mbContribuyentes.obtenerContribuyentesRfc(this.proveedor.getContribuyente().getRfc())) {
                this.listaContribuyentes.add(new SelectItem(c, c.getContribuyente()));
            }
        }
    }

    public void actualizarContribuyente() {
        if (this.mbContribuyentes.valida()) {
            this.proveedor.setContribuyente(this.mbContribuyentes.getContribuyente());
        }
    }

    public void agregarContribuyente() {
        this.listaContribuyentes.clear();
        this.mbContribuyentes.getContribuyentes().clear();
        int idRfc = this.mbContribuyentes.getContribuyente().getIdRfc();
        String rfc = this.mbContribuyentes.getContribuyente().getRfc();

        Contribuyente c = new Contribuyente();
        c.setIdRfc(idRfc);
        c.setRfc(rfc);
        this.mbContribuyentes.setContribuyente(c);
    }

    public void aceptarContribuyente() {
        this.proveedor.setContribuyente(this.mbContribuyentes.getContribuyente());
    }

//    public void obtenerRfc() {
//        this.mbContribuyentes.obtenerContribuyentesRFC();
//        if(!this.mbContribuyentes.getContribuyentes().isEmpty()) {
//            this.mbContribuyentes.setContribuyente(this.mbContribuyentes.getContribuyentes().get(0));
//        }
//        this.listaContribuyentes=new ArrayList<>();
//        for(Contribuyente c: this.mbContribuyentes.getContribuyentes()) {
//            this.listaContribuyentes.add(new SelectItem(c, c.toString()));
//        }
//    }
    
    public void mttoDireccionFisica() {
//        Direccion d=this.mbDireccion.obtener(this.proveedor.getDireccionEntrega().getIdDireccion());
        this.mbDireccion.setDireccion(this.proveedor.getDireccionEntrega());
//        this.proveedor.setDireccionEntrega(d);
        this.mbDireccion.setActualiza(":main:txtAreaDirFisica");
    }
    
    public void mttoDireccionFiscal() {
        Direccion d=this.mbDireccion.obtener(this.proveedor.getContribuyente().getDireccion().getIdDireccion());
        this.mbDireccion.setDireccion(d);
        this.proveedor.getContribuyente().setDireccion(d);
        this.mbDireccion.setActualiza(":main:direccionFiscal");
    }

    public void mttoDireccion() {
        this.mbDireccion.setDireccion(this.mbContribuyentes.getContribuyente().getDireccion());
        this.mbDireccion.setActualiza("txtDireccion");
    }

    public void nuevoContribuyente() {
//        this.listaContribuyentes.clear();
//        this.mbContribuyentes.getContribuyentes().clear();
        if (this.listaContribuyentes.isEmpty()) {
            //this.mbContribuyentes.setContribuyente(new Contribuyente());
            this.mbContribuyentes.nuevoContribuyente();
        } else {
            Contribuyente c = (Contribuyente) this.listaContribuyentes.get(0).getValue();
            this.mbContribuyentes.setContribuyente(new Contribuyente(c.getIdRfc(), c.getRfc()));
        }
    }

    public void mttoContribuyente() {
//        this.listaContribuyentes.clear();
//        this.mbContribuyentes.getContribuyentes().clear();
//        Contribuyente c = this.mbContribuyentes.copia(this.proveedor.getContribuyente());
//        this.mbContribuyentes.setContribuyente(c);
        Contribuyente c = this.proveedor.getContribuyente();
        c.setDireccion(this.mbDireccion.obtener(c.getDireccion().getIdDireccion()));
        this.mbContribuyentes.copiaContribuyente(c);
    }

    public void eliminarTipoOperacion() {
        if(this.mbTipoOperacion.eliminar()) {
            this.proveedor.setTipoOperacion(new TipoOperacion());
            this.listaTipoOperaciones.clear();
            for(TipoOperacion t: this.mbTipoOperacion.getTipoOperaciones()) {
                this.listaTipoOperaciones.add(new SelectItem(t, t.toString()));
            }
        }
    }
    
    public void grabarTipoOperacion() {
        if(this.mbTipoOperacion.grabar()) {
            this.proveedor.setTipoOperacion(this.mbTipoOperacion.getTipoOperacion());
            this.listaTipoOperaciones.clear();
            for(TipoOperacion t: this.mbTipoOperacion.getTipoOperaciones()) {
                this.listaTipoOperaciones.add(new SelectItem(t, t.toString()));
            }
        }
    }
    
    public void mttoTipoOperacion() {
        if(this.proveedor.getTipoOperacion().getIdTipoOperacion()==0) {
            this.mbTipoOperacion.setTipoOperacion(new TipoOperacion());
        } else {
            this.mbTipoOperacion.setTipoOperacion(this.mbTipoOperacion.copia(this.proveedor.getTipoOperacion()));
        }
    }
    
    private void obtenerListaTipoOperaciones() {
        this.listaTipoOperaciones=new ArrayList<>();
        
        TipoOperacion tt=new TipoOperacion(0, "00", "Nuevo Tipo de Operación");
        this.listaTipoOperaciones.add(new SelectItem(tt, tt.toString()));
        
        ArrayList<TipoOperacion> ts=this.mbTipoOperacion.obtenerTipoOperaciones();
        for(TipoOperacion t: ts) {
            this.listaTipoOperaciones.add(new SelectItem(t, t.toString()));
        }
    }
    
    public void eliminarTipoTercero() {
        if(this.mbTipoTerceros.eliminar()) {
            this.proveedor.setTipoTercero(new TipoTercero());
            this.listaTipoTerceros.clear();
            for(TipoTercero t: this.mbTipoTerceros.getTipoTerceros()) {
                this.listaTipoTerceros.add(new SelectItem(t, t.toString()));
            }
        }
    }
    
    public void grabarTipoTercero() {
        if(this.mbTipoTerceros.grabar()) {
            this.proveedor.setTipoTercero(this.mbTipoTerceros.getTipoTercero());
            this.listaTipoTerceros.clear();
            for(TipoTercero t: this.mbTipoTerceros.getTipoTerceros()) {
                this.listaTipoTerceros.add(new SelectItem(t, t.toString()));
            }
        }
    }
    
    public void mttoTipoTercero() {
        if(this.proveedor.getTipoTercero().getIdTipoTercero()==0) {
            this.mbTipoTerceros.setTipoTercero(new TipoTercero());
        } else {
            this.mbTipoTerceros.setTipoTercero(this.mbTipoTerceros.copia(this.proveedor.getTipoTercero()));
        }
    }
    
    private void obtenerListaTipoTerceros() {
        this.listaTipoTerceros=new ArrayList<>();
        
        TipoTercero tt=new TipoTercero(0, "00", "Nuevo Tipo de Tercero");
        this.listaTipoTerceros.add(new SelectItem(tt, tt.toString()));
        
        ArrayList<TipoTercero> ts=this.mbTipoTerceros.obtenerTipoTerceros();
        for(TipoTercero t: ts) {
            this.listaTipoTerceros.add(new SelectItem(t, t.toString()));
        }
    }
    
    public void cargaSubClasificaciones() {
        this.listaSubClasificaciones=new ArrayList<>();
        
        SubClasificacion sc=new SubClasificacion(0, "Nueva SubClasificacion");
        this.listaSubClasificaciones.add(new SelectItem(sc, sc.toString()));
        
        ArrayList<SubClasificacion> scs=this.mbClasificaciones.obtenerSubClasificaciones(this.proveedor.getClasificacion().getIdClasificacion());
        for(SubClasificacion s: scs) {
            this.listaSubClasificaciones.add(new SelectItem(s, s.toString()));
        }
    }
    
    public void eliminarSubClasificacion() {
        if(this.mbClasificaciones.eliminarSubClasificacion(this.proveedor.getClasificacion().getIdClasificacion())) {
            this.proveedor.setSubClasificacion(new SubClasificacion());
            this.cargaSubClasificaciones();
        }
    }
    
    public void grabarSubClasificacion() {
        if(this.mbClasificaciones.grabarSubClasificacion(this.proveedor.getClasificacion().getIdClasificacion())) {
            this.proveedor.setSubClasificacion(this.mbClasificaciones.getSubClasificacion());
            this.cargaSubClasificaciones();
        }
    }
    
    public void mttoSubClasificacion() {
        if(this.proveedor.getSubClasificacion().getIdSubClasificacion()==0) {
            this.mbClasificaciones.setSubClasificacion(new SubClasificacion());
        } else {
            this.mbClasificaciones.setSubClasificacion(this.mbClasificaciones.copia(this.proveedor.getSubClasificacion()));
        }
    }
    
    public void eliminarClasificacion() {
        if(this.mbClasificaciones.eliminar()) {
            this.proveedor.setClasificacion(new Clasificacion());
            this.cargaClasificaciones();
            this.cargaSubClasificaciones();
        }
    }
    
    public void grabarClasificacion() {
        if(this.mbClasificaciones.grabar()) {
            this.proveedor.setClasificacion(this.mbClasificaciones.getClasificacion());
            this.cargaClasificaciones();
            this.cargaSubClasificaciones();
        }
    }
    
    public void mttoClasificacion() {
        if(this.proveedor.getClasificacion().getIdClasificacion()==0) {
            this.mbClasificaciones.setClasificacion(new Clasificacion());
        } else {
            this.mbClasificaciones.setClasificacion(this.mbClasificaciones.copia(this.proveedor.getClasificacion()));
        }
    }
    
    private void cargaClasificaciones() {
        this.listaClasificaciones=new ArrayList<>();
        
        Clasificacion cl=new Clasificacion(0, "Nueva Clasificación");
        this.listaClasificaciones.add(new SelectItem(cl, cl.toString()));
        
        ArrayList<Clasificacion> cs=this.mbClasificaciones.obtenerClasificaciones();
        for(Clasificacion c: cs) {
            this.listaClasificaciones.add(new SelectItem(c, c.toString()));
        }
    }
    
    public void seleccionaContribuyente() {
        this.proveedor.setContribuyente(this.mbBuscarContribuyente.getContribuyente());
        int idDireccion=this.proveedor.getContribuyente().getDireccion().getIdDireccion();
        this.proveedor.getContribuyente().setDireccion(this.mbDireccion.obtener(idDireccion));
        this.listaContribuyentes=new ArrayList<>();
        for(Contribuyente c: this.mbContribuyentes.obtenerContribuyentesRfc(this.proveedor.getContribuyente().getRfc())) {
            this.listaContribuyentes.add(new SelectItem(c, c.getContribuyente()));
        }
    }
    
    public void buscar() {
        this.mbBuscarContribuyente.buscar();
        if(this.mbBuscarContribuyente.getContribuyente()!=null) {
            this.proveedor.setContribuyente(this.mbBuscarContribuyente.getContribuyente());
        }
    }
    
    public void eliminar() {
        // Probar buscar contribuyente cuando solo se encuentra un contribuyente por rfc 
        // y tambien cuando se encuentran varios y se selecciona de la lista
    }

    public void grabar() {
//        String strContribuyente = Utilerias.Acentos(this.proveedor.getContribuyente().getContribuyente());
//        this.proveedor.getContribuyente().setContribuyente(strContribuyente);
        this.proveedor.getContribuyente().setDireccion(this.mbDireccion.obtener(this.proveedor.getContribuyente().getDireccion().getIdDireccion()));
        if (this.proveedor.getContribuyente().getRfc().equals("")) {
            Mensajes.mensajeAlert("Se requiere el RFC del contribuyente !!");
        } else if (!this.mbContribuyentes.valida(this.proveedor.getContribuyente())) {
            Mensajes.mensajeAlert("Se requiere el nombre del proveedor !!");
        } else if (!mbDireccion.validarDireccion(this.proveedor.getContribuyente().getDireccion())) {
            Mensajes.mensajeAlert("La dirección fiscal del contribuyente no es valida !!");
        } else if (this.proveedor.getClasificacion().getIdClasificacion() == 0) {
            Mensajes.mensajeAlert("Se requiere la clasificacion del proveedor !!");
        } else if (this.proveedor.getImpuestoZona().getIdZona() == 0) {
            Mensajes.mensajeAlert("Se requiere la zona de impuestos del proveedor !!");
        } else if (this.proveedor.getDiasCredito() < 0) {
            Mensajes.mensajeAlert("Los dias de crédito no debe ser menores que cero !!");
        } else if (this.proveedor.getDesctoComercial() < 0) {
            Mensajes.mensajeAlert("El descuento comercial no debe ser menor que cero !!");
        } else if (this.proveedor.getDesctoProntoPago() < 0) {
            Mensajes.mensajeAlert("El descuento por pronto pago no debe ser menor que cero !!");
        } else if (this.proveedor.getLimiteCredito() < 0.00) {
            Mensajes.mensajeAlert("El límite de crédito no debe ser menor que cero !!");
        } else {
            try {
                this.dao = new DAOProveedores();
                if (this.proveedor.getIdProveedor() == 0) {
                    this.proveedor.setIdProveedor(this.dao.agregar(this.proveedor));
                } else {
                    this.dao.modificar(this.proveedor);
                }
//                this.proveedor = this.obtenerProveedor(idProveedor);
//                this.listaProveedores = null;
                Mensajes.mensajeSucces("El proveedor se grabó correctamente !!");
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public String salir() {
        if (this.proveedor.getIdProveedor() == 0 && this.proveedor.getDireccionEntrega().getIdDireccion() > 0) {
            mbDireccion.eliminar(this.proveedor.getDireccionEntrega().getIdDireccion());
        }
        return "proveedor.salir";
    }

    public String terminar() {
        this.listaProveedores = null;
        this.listaClasificaciones=null;
        this.listaTipoTerceros=null;
        this.listaTipoOperaciones=null;
        this.acciones = null;
        return "menuProveedores.terminar";
    }

    private Proveedor obtenerProveedor(int idProveedor) throws NamingException, SQLException {
        this.dao = new DAOProveedores();
        return this.convertir(this.dao.obtenerProveedor(idProveedor));
    }

    public String mantenimiento(int idProveedor) {
        String destino = null;
        this.setListaContribuyentes(new ArrayList<SelectItem>());
        try {

            if (idProveedor == 0) {
                this.proveedor = new Proveedor();
                this.proveedor.getContribuyente().setDireccion(this.mbDireccion.nuevaDireccion());
                this.proveedor.setDireccionEntrega(this.mbDireccion.nuevaDireccion());
            } else {
                int idDireccion;
                this.proveedor = this.obtenerProveedor(idProveedor);
                for (Contribuyente c : this.mbContribuyentes.obtenerContribuyentesRfc(this.proveedor.getContribuyente().getRfc())) {
                    idDireccion = c.getDireccion().getIdDireccion();
                    c.setDireccion(this.mbDireccion.obtener(idDireccion));
                    this.listaContribuyentes.add(new SelectItem(c, c.getContribuyente()));
                }
                this.cargaSubClasificaciones();
            }
            this.contacto=new Contacto();
            this.mbContactos.cargaContactos(this.idTipo, this.proveedor.getIdProveedor());
            this.cargarTelefonos();
            destino = "proveedor.mantenimiento";
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return destino;
    }

    public ArrayList<Proveedor> getListaProveedores() {
        if (listaProveedores == null) {
            this.cargaProveedores(this.clasificacion.getIdClasificacion());
        }
        return listaProveedores;
    }

    public void cargaProveedores(int idClasificacion) {
        this.listaProveedores = new ArrayList<>();
        try {
            this.dao = new DAOProveedores();
            for (Proveedor p : this.dao.obtenerProveedores(idClasificacion)) {
                this.listaProveedores.add(convertir(p));
            }
//            this.listaFiltrados = this.listaProveedores;
            this.listaFiltrados = null;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private Proveedor convertir(Proveedor proveedor) throws NamingException, SQLException {
        int idDireccion;
        idDireccion = proveedor.getContribuyente().getDireccion().getIdDireccion();
        proveedor.getContribuyente().setDireccion(this.mbDireccion.obtener(idDireccion));
        idDireccion = proveedor.getDireccionEntrega().getIdDireccion();
        proveedor.setDireccionEntrega(this.mbDireccion.obtener(idDireccion));
        proveedor.setContactos(this.mbContactos.obtenerContactos(1, proveedor.getIdProveedor()));
        return proveedor;
    }

    public void setListaProveedores(ArrayList<Proveedor> listaProveedores) {
        this.listaProveedores = listaProveedores;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public MbDireccion getMbDireccion() {
        return mbDireccion;
    }

    public void setMbDireccion(MbDireccion mbDireccion) {
        this.mbDireccion = mbDireccion;
    }

    public MbBuscarContribuyente getMbBuscarContribuyente() {
        return mbBuscarContribuyente;
    }

    public void setMbBuscarContribuyente(MbBuscarContribuyente mbBuscarContribuyente) {
        this.mbBuscarContribuyente = mbBuscarContribuyente;
    }
    
    public ArrayList<SelectItem> getListaClasificaciones() {
        if(this.listaClasificaciones==null) {
            this.cargaClasificaciones();
        }
        return listaClasificaciones;
    }

    public void setListaClasificaciones(ArrayList<SelectItem> listaClasificaciones) {
        this.listaClasificaciones = listaClasificaciones;
    }

    public MbClasificaciones getMbClasificaciones() {
        return mbClasificaciones;
    }

    public void setMbClasificaciones(MbClasificaciones mbClasificaciones) {
        this.mbClasificaciones = mbClasificaciones;
    }
    public ArrayList<SelectItem> getListaTipoTerceros() {
        if(this.listaTipoTerceros==null) {
            this.obtenerListaTipoTerceros();
        }
        return listaTipoTerceros;
    }

    public void setListaTipoTerceros(ArrayList<SelectItem> listaTipoTerceros) {
        this.listaTipoTerceros = listaTipoTerceros;
    }

    public MbTipoTerceros getMbTipoTerceros() {
        return mbTipoTerceros;
    }

    public void setMbTipoTerceros(MbTipoTerceros mbTipoTerceros) {
        this.mbTipoTerceros = mbTipoTerceros;
    }
    
    public MbTipoOperacion getMbTipoOperacion() {
        return mbTipoOperacion;
    }

    public void setMbTipoOperacion(MbTipoOperacion mbTipoOperacion) {
        this.mbTipoOperacion = mbTipoOperacion;
    }

    public ArrayList<SelectItem> getListaTipoOperaciones() {
        if(this.listaTipoOperaciones==null) {
            this.obtenerListaTipoOperaciones();
        }
        return listaTipoOperaciones;
    }

    public void setListaTipoOperaciones(ArrayList<SelectItem> listaTipoOperaciones) {
        this.listaTipoOperaciones = listaTipoOperaciones;
    }
    
    public MbZonas getMbImpuestoZona() {
        return mbImpuestoZona;
    }

    public void setMbImpuestoZona(MbZonas mbImpuestoZona) {
        this.mbImpuestoZona = mbImpuestoZona;
    }
    
    public MbContribuyentesJS getMbContribuyentes() {
        return mbContribuyentes;
    }

    public void setMbContribuyentes(MbContribuyentesJS mbContribuyentes) {
        this.mbContribuyentes = mbContribuyentes;
    }

    public ArrayList<SelectItem> getListaContribuyentes() {
        return listaContribuyentes;
    }

    public void setListaContribuyentes(ArrayList<SelectItem> listaContribuyentes) {
        this.listaContribuyentes = listaContribuyentes;
    }

    public ArrayList<SelectItem> getListaSubClasificaciones() {
        return listaSubClasificaciones;
    }

    public void setListaSubClasificaciones(ArrayList<SelectItem> listaSubClasificaciones) {
        this.listaSubClasificaciones = listaSubClasificaciones;
    }
    
    public Contacto getContacto() {
        return contacto;
    }

    public void setContacto(Contacto contacto) {
        this.contacto = contacto;
    }

    public Telefono getTelefono() {
        return telefono;
    }

    public void setTelefono(Telefono telefono) {
        this.telefono = telefono;
    }
    
    public MbContactos getMbContactos() {
        return mbContactos;
    }

    public void setMbContactos(MbContactos mbContactos) {
        this.mbContactos = mbContactos;
    }
    
    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(5);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public ArrayList<Proveedor> getListaFiltrados() {
        return listaFiltrados;
    }

    public void setListaFiltrados(ArrayList<Proveedor> listaFiltrados) {
        this.listaFiltrados = listaFiltrados;
    }
    public Clasificacion getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(Clasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }
}
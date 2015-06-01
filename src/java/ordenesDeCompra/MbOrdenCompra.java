package ordenesDeCompra;

import Message.Mensajes;
import cedis.MbMiniCedis;
import cedis.dominio.Cedis;
import contactos.dominio.Contacto;
import cotizaciones.MbCotizaciones;
import direccion.dao.DAODirecciones;
import empresas.MbEmpresas;
import empresas.dominio.Empresa;
import java.io.File;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;
import monedas.MbMonedas;
import net.sf.jasperreports.engine.JRException;
import ordenDeCompra.Report.OrdenCompraReporte;
import ordenesDeCompra.Reporte.Reportes;
import ordenesDeCompra.dao.DAOOrdenDeCompra;
import ordenesDeCompra.dominio.Correo;
import ordenesDeCompra.dominio.OrdenCompraEncabezado;
import ordenesDeCompra.dominio.OrdenCompraDetalle;
import ordenesDeCompra.dominio.TotalesOrdenCompra;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import proveedores.MbMiniProveedor;
import proveedores.dao.DAOProveedores;
import proveedores.dominio.Proveedor;

@Named(value = "mbOrdenCompra")
@SessionScoped
public class MbOrdenCompra implements Serializable {
    @ManagedProperty(value = "#{mbCotizaciones}")
    private MbCotizaciones mbCotizaciones;
    
    private OrdenCompraEncabezado ordenCompraEncabezado;
    private ArrayList<OrdenCompraEncabezado> listaOrdenesEncabezado;
    private OrdenCompraEncabezado ordenElegida;
    private ArrayList<OrdenCompraDetalle> listaOrdenDetalle;
    private OrdenCompraDetalle ordenCompraDetalle;
    private double subtotalGeneral;
    private double sumaDescuentosProductos;
    private double sumaDescuentosGenerales;
    private double subtotalBruto;
    private double impuesto;
    private double total;
    private String subtotF;
    private String descF;
    private String subtotalBrutoF;
    private String impF;
    private String totalF;
    private String sumaDescuentosProductosF;
    private String sumaDescuentosGeneralesF;
    private double iva = 0.16;
    private double sumaDescuentoTotales;
    private String sumaDescuentosTotalesF;
    private transient Correo correo;
    private ArrayList<Contacto> listaContactos;
    private transient Contacto contactoElegido;
    private String cadena;
    private transient OrdenCompraReporte ocr;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private ArrayList<Producto> listaEmpaque = new ArrayList<Producto>();
    private Producto empaque;
    private Empresa empresa = new Empresa();
    private Cedis cedis = new Cedis();
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbEmpresas}")
    private MbEmpresas mbEmpresas;
    @ManagedProperty(value = "#{mbMiniProveedor}")
    private MbMiniProveedor mbProveedores;
    private Proveedor provee;
    //---------------------DIRECTAS
    private OrdenCompraEncabezado ordenCompraEncabezadoDirecta;
    private Empresa empre;
    @ManagedProperty(value = "#{mbMonedas}")
    private MbMonedas mbMonedas;

    public MbOrdenCompra() throws NamingException {
        this.ordenCompraEncabezado = new OrdenCompraEncabezado();
        this.mbCotizaciones = new MbCotizaciones();
        this.ordenCompraDetalle = new OrdenCompraDetalle();
        this.ordenElegida = new OrdenCompraEncabezado();
        this.correo = new Correo();
        this.contactoElegido = new Contacto();
        this.listaContactos = new ArrayList<Contacto>();
        this.ocr = new OrdenCompraReporte();
        this.mbBuscar = new MbProductosBuscar();
        this.empaque = new Producto();
        this.mbProveedores = new MbMiniProveedor();
        this.mbEmpresas = new MbEmpresas();
        this.provee = new Proveedor();
        //-------DIRECTAS
        this.mbMonedas = new MbMonedas();

    }

    //M E T O D O S  ////////////////////////////////////////////////////////////////////////////////////////////
    public void cargaOrdenesEncabezado() throws NamingException, SQLException {
        listaOrdenesEncabezado = new ArrayList<OrdenCompraEncabezado>();
        DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
        ArrayList<OrdenCompraEncabezado> lista = daoOC.listaOrdenes();
        for (OrdenCompraEncabezado d : lista) {
            listaOrdenesEncabezado.add(d);
        }
    }

    public void cargaOrdenesEncabezadoAlmacen(int idProveedor, int status) throws NamingException, SQLException {
        this.listaOrdenesEncabezado = new ArrayList<OrdenCompraEncabezado>();
//        try {
            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
            for (OrdenCompraEncabezado d : daoOC.listaOrdenesAlmacen(idProveedor, status)) {
                listaOrdenesEncabezado.add(d);
            }
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        }
    }
    
    public void cargaOrdenesEncabezado(int idProveedor, int status) throws NamingException, SQLException {
        this.listaOrdenesEncabezado = new ArrayList<OrdenCompraEncabezado>();
//        try {
            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
            for (OrdenCompraEncabezado d : daoOC.listaOrdenes(idProveedor, status)) {
                listaOrdenesEncabezado.add(d);
            }
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        }
    }

    public void dameEmpaqueSeleccionado() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "dameEmpaqueSeleccionado");
        boolean verifi = verificacion(mbBuscar.getProducto());
        if (verifi == false) {
            listaEmpaque.add(mbBuscar.getProducto());
            mbBuscar.getProductos().remove(mbBuscar.getProducto());
            ok = true;
        } else {
            fMsg.setDetail("Este Empaque ya esta en la lista");
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }

    }

    public void eliminarEmpaqueSeleccionado() {
        listaEmpaque.remove(empaque);
    }

    public boolean verificacion(Producto e) {
        boolean verificar = false;

        for (Producto em : listaEmpaque) {
            if (em.equals(e)) {
                verificar = true;
                break;
            } else {
                verificar = false;
            }
        }
        return verificar;
    }

    public void limpiarCamposBusqueda() {
        mbBuscar.getMbParte().setParte(null);
        mbBuscar.setStrBuscar("");
        mbBuscar.setProductos(null);
    }
    
//    public boolean aseguraOrdenCompra(int idOrdenCompra) {
//        int propietario;
//        boolean ok = false;
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "aseguraOrdenCompra");
//        try {
//            DAOOrdenDeCompra dao=new DAOOrdenDeCompra();
//            propietario=dao.aseguraOrdenCompra(idOrdenCompra);
//            if(propietario==dao.obtenerIdUsuario()) {
//                ok=true;
//            } else if(propietario==0) {
//                fMsg.setDetail("No se encontro la orden de compra");
//            } else {
//                fMsg.setDetail("La orden de compra esta siendo procesada por otro usuario("+propietario+")");
//            }
//        } catch (SQLException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//        } catch (NamingException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getMessage());
//        }
//        if (!ok) {
//            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
//        return ok;
//    }

    public void obtenerDetalleOrdenCompra() {
        listaOrdenDetalle = new ArrayList<OrdenCompraDetalle>();
        this.subtotalGeneral = 0;
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerDetalleOrdenCompra");
        try {
            int idOC = ordenElegida.getIdOrdenCompra();
            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
            for (OrdenCompraDetalle d : daoOC.consultaOrdenCompra(idOC)) {
                d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
                if(d.getCotizacionDetalle().getProducto()!=null) {
                    d.getCotizacionDetalle().setProducto(this.mbBuscar.obtenerProducto(d.getCotizacionDetalle().getProducto().getIdProducto()));
                }
                listaOrdenDetalle.add(d);
                this.calculosOrdenCompra(d.getProducto().getIdProducto());
            }
            ok=true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void dameOrdenCompra(SelectEvent event) {
        this.ordenElegida = (OrdenCompraEncabezado) event.getObject();

        listaOrdenDetalle = new ArrayList<OrdenCompraDetalle>();
        this.subtotalGeneral = 0;

        try {
            int idOC = ordenElegida.getIdOrdenCompra();


            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();

            ArrayList<OrdenCompraDetalle> lista = daoOC.consultaOrdenCompra(idOC);
            for (OrdenCompraDetalle d : lista) {
                d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
                d.getCotizacionDetalle().setProducto(this.mbBuscar.obtenerProducto(d.getCotizacionDetalle().getProducto().getIdProducto()));
                listaOrdenDetalle.add(d);
                this.calculosOrdenCompra(d.getProducto().getIdProducto());
                d.setNombreProducto(d.getProducto().toString());
            }
        } catch (NamingException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void calculosOrdenCompra(int idProd) throws NamingException {
        DAOOrdenDeCompra daoO = new DAOOrdenDeCompra();
        try {

            for (OrdenCompraDetalle e : listaOrdenDetalle) {
                int idProducto = e.getProducto().getIdProducto();
                if (idProd == idProducto) {
                    double neto = e.getCostoOrdenado() - (e.getCostoOrdenado() * (e.getDescuentoProducto() / 100));
                    double neto2 = neto - neto * (e.getDescuentoProducto2() / 100);
                    e.setNeto(neto2);
//                    e.setSubtotal(e.getNeto() * e.getCantidadSolicitada());
                    e.setSubtotal(e.getCantidadSolicitada() * e.getCostoOrdenado());
                    daoO.actualizarCantidadOrdenada(e.getIdOrdenCompra(), idProd, e.getCantidadSolicitada());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void calculoSubtotalGeneral() {
        subtotalGeneral = 0;
        sumaDescuentosProductos = 0;
        sumaDescuentosGenerales = 0;
        double descProds = 0;
        double descuentoC;
        double descuentoPP;
        double subt;

        if (this.listaOrdenDetalle != null) {
            for (OrdenCompraDetalle oc : listaOrdenDetalle) {
                subtotalGeneral += oc.getSubtotal();
                descProds += (oc.getCantidadSolicitada() * (oc.getCostoOrdenado() - oc.getNeto()));
            }
            setSumaDescuentosProductos(descProds);
            double dc = this.ordenElegida.getDesctoComercial();
            double dpp = this.ordenElegida.getDesctoProntoPago();
            subt = subtotalGeneral;
            descuentoC = subt * (dc / 100);

            subt = subt - descuentoC;

            descuentoPP = subt * (dpp / 100);
            double descuentosGenerales = descuentoC + descuentoPP;

            setSumaDescuentosGenerales(descuentosGenerales);

            //    setSumaDescuentoTotales(sumaDescuentosProductos + sumaDescuentosGenerales);

        } else {
            System.out.println("No hay valores en el arraylist");
        }
    }

    public void calcularSumaDescuentosTotales() {
        sumaDescuentoTotales = 0;
        setSumaDescuentoTotales(sumaDescuentosProductos + sumaDescuentosGenerales);
    }

    public void calcularSubtotalBruto() {
        subtotalBruto = 0;
        //  subtotalBruto = this.subtotalGeneral - this.sumaDescuentosGenerales;
        subtotalBruto = this.subtotalGeneral - this.sumaDescuentoTotales;
    }

    public void calculoIVA() {
        impuesto = 0;
        double desc = this.subtotalBruto;
        if (desc > 0) {
            impuesto = (desc) * iva;
        } else {
            impuesto = 0;
        }
    }

    public void calculoTotal() {
        total = 0;
        double desc = subtotalBruto;
        if (desc > 0) {
            total = (desc) + impuesto;
        } else {
            total = 0;
        }
    }

    public void guardarOrden(int idOrden, int estado) throws NamingException {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", "guardarOrden");
        DAOOrdenDeCompra daoO = new DAOOrdenDeCompra();
        try {
            if (estado == 1) {
//                Ima=this.mbMonedas.getMoneda().getIdMoneda();
                daoO.procesarOrdenCompra(idOrden);
                this.setListaOrdenesEncabezado(null);
                this.cargaOrdenesEncabezado();

                fMsg.setDetail("Se ha guardado con satisfactoriamente...");
            } else if (estado == 2) {
                fMsg.setDetail("La orden se ha registrado con anterioridad");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
            fMsg.setDetail("No se realizó el registro de la orden de compra..");
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }

    public void cancelarOrden(int idOrden, int estado) throws NamingException {
        Boolean correcto = false;
        //    FacesMessage msg = null;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cancelarOrden");
        DAOOrdenDeCompra daoO = new DAOOrdenDeCompra();
        try {
            if (estado == 0) {
                daoO.cancelarOrdenCompra(idOrden);
                this.setListaOrdenesEncabezado(null);
                this.cargaOrdenesEncabezado();

                fMsg.setDetail("Se ha CANCELADO");
                correcto = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
            fMsg.setDetail("No se realizó la cancelación de la orden de compra.." + ex.getMessage());
        }

        if (!correcto) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public String irMenu() throws NamingException {
        // this.limpiaRequisicion();
        String navega = "menuOrdenesDeCompra.xhtml";
        return navega;
    }

    public void generarReporte() {
        try {
            TotalesOrdenCompra totalOrdenCompra = new TotalesOrdenCompra();
            totalOrdenCompra.setImpF(impF);
            totalOrdenCompra.setSubTotalBrutoF(subtotalBrutoF);
            totalOrdenCompra.setSubtoF(subtotF);
            totalOrdenCompra.setSumaDescuentosGeneralesF(sumaDescuentosGeneralesF);
            totalOrdenCompra.setSumaDescuentosTotalesF(sumaDescuentosTotalesF);
            totalOrdenCompra.setSumaDescuentsoProductosF(sumaDescuentosProductosF);
            totalOrdenCompra.setTotalF(totalF);
            ocr.generarReporte(listaOrdenDetalle, ordenElegida, totalOrdenCompra, 0);
        } catch (JRException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void enviarCorreo(String emails) {
        String asunto = correo.getAsunto();
        String contenido = correo.getMensaje();
        String ruta = "";
        Boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "enviarCorreo");
        if (emails.equals("")) {
            fMsg.setDetail("Se requiere un correo !!");
        } else if (asunto.equals("")) {
            fMsg.setDetail("Se requiere un ASUNTO !!");
        } else if (contenido.equals("")) {
            fMsg.setDetail("Se requiere un MENSAJE !!");
        } else {
            String verifica = this.validarCadenaCorreos(emails);
            if (verifica.equals("")) {
                fMsg.setDetail("Es incorrecto el correo " + verifica);
            } else {
                try {
                    TotalesOrdenCompra totalOrdenCompra = new TotalesOrdenCompra();
                    totalOrdenCompra.setImpF(impF);
                    totalOrdenCompra.setSubTotalBrutoF(subtotalBrutoF);
                    totalOrdenCompra.setSubtoF(subtotF);
                    totalOrdenCompra.setSumaDescuentosGeneralesF(sumaDescuentosGeneralesF);
                    totalOrdenCompra.setSumaDescuentosTotalesF(sumaDescuentosTotalesF);
                    totalOrdenCompra.setSumaDescuentsoProductosF(sumaDescuentosProductosF);
                    totalOrdenCompra.setTotalF(totalF);
                    //DATOS FIJOS
                    String servidorCorreos = "mail.laanita.com";
                    String user = "carlos.pat";
                    String remitente = "carlos.pat@laanita.com";
                    String passRemitente = "Mildred1";
                    int puerto = 587;
                    String protocolo = "smtp";
                    Properties props = new Properties();
                    props.setProperty("mail.transport.protocol", protocolo);
                    props.setProperty("mail.smtp.host", servidorCorreos);
                    props.setProperty("mail.smtp.auth", "true");
                    props.setProperty("mail.smtp.user", user);
                    props.setProperty("mail.smtp.pass", passRemitente);
                    Session mailSession;
                    mailSession = Session.getDefaultInstance(props);
                    MimeMessage mensaje = new MimeMessage(mailSession);
                    try {
                        mensaje.setFrom(new InternetAddress(remitente));
                        mensaje.setSender(new InternetAddress(remitente));
                        BodyPart texto = new MimeBodyPart();
                        texto.setText(contenido);
                        MimeMultipart multiparte = new MimeMultipart();
                        multiparte.addBodyPart(texto);
                        BodyPart adjunto = new MimeBodyPart();
                        Reportes reportes = new Reportes();
                        ruta = reportes.generarReporteCorreo(listaOrdenDetalle, ordenCompraEncabezado, totalOrdenCompra);
//                      ruta = ocr.generarReporte(listaOrdenDetalle, ordenCompraEncabezado, totalOrdenCompra, 1);
                        adjunto.setDataHandler(new DataHandler(new FileDataSource(ruta)));
                        int idOC = ordenElegida.getIdOrdenCompra();
                        adjunto.setFileName("OrdendeCompra" + idOC + ".pdf");
                        multiparte.addBodyPart(adjunto);
                        mensaje.setSubject(asunto);
                        mensaje.setContent(multiparte);
                        if (emails.indexOf(',') > 0) {
                            mensaje.addRecipients(Message.RecipientType.TO, InternetAddress.parse(emails));
                        } else {
                            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(emails));
                        }
                    } catch (MessagingException e) {
                    }

                    Transport transport;
                    try {
                        transport = mailSession.getTransport(protocolo);
                        transport.connect(servidorCorreos, puerto, user, passRemitente);
                        transport.sendMessage(mensaje, mensaje.getRecipients(Message.RecipientType.TO));
                        transport.close();
                        ok = true;
                        File file = new File(ruta);
                        file.delete();
                        fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                        fMsg.setDetail("El correo se envió correctamente !!");
                        limpiarFormulario();
                        FacesContext.getCurrentInstance().addMessage(null, fMsg);
                    } catch (NoSuchProviderException e) {
                        fMsg.setDetail(e.getMessage() + "Error capturado1");
                    } catch (MessagingException e) {
                        fMsg.setDetail("Aviso: Corrige el correo..");
                    }
                } catch (Exception ex) {
                    fMsg.setDetail("Aviso: Acompleta el correo...");
                }
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }        // TODO

    public void cargaContactos() {
        try {
            this.listaContactos = new ArrayList<Contacto>();
            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
            try {
                listaContactos = daoOC.obtenerContactos(ordenElegida.getIdOrdenCompra());
                cadena = "";


            } catch (SQLException ex) {
                Logger.getLogger(MbOrdenCompra.class
                        .getName()).log(Level.SEVERE, null, ex);
            }


        } catch (NamingException ex) {
            Logger.getLogger(MbOrdenCompra.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void contactoSeleccion() {
        String elegido = contactoElegido.getCorreo();
        cadena = cadena + elegido + ",";
        correo.setPara(cadena);
    }

    public void limpiarFormulario() {
        for (Contacto e : listaContactos) {
            e.setCorreo("");
        }
        this.correo.setPara("");
        this.correo.setAsunto("");
        this.correo.setMensaje("");

        cargaContactos();
    }

    public String validarCadenaCorreos(String cadena) {
        String cad = "";
        cadena += ",";
        char arregloCadena[] = cadena.trim().toCharArray();
        for (int x = 0; x < cadena.length(); x++) {
            if (arregloCadena[x] != ',') {
                cad += arregloCadena[x];
            } else {
                boolean paso = validarEmail(cad);
                if (paso == false) {
                    break;
                } else {
                    cad = "";
                }
            }
        }
        return cad;
    }

    public boolean validarEmail(String email) {
        email=email.trim();
        boolean validar;
        Pattern pattern = Pattern.compile("[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]");
        Matcher matcher = pattern.matcher(email);
        validar = matcher.matches();
        return validar;
    }

    public void buscar() throws NamingException, SQLException {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.dameEmpaqueSeleccionado();
        }
    }

    public void cargaDatosProveedor() throws NamingException {

        int idProve = this.mbProveedores.getMiniProveedor().getIdProveedor();
        DAODirecciones daoD = new DAODirecciones();
        DAOProveedores daoProv = new DAOProveedores();
        try {
            provee = daoProv.obtenerProveedor(idProve);
            int idDirProv = provee.getContribuyente().getDireccion().getIdDireccion(); //CAMBIAR CUANDO TENGAMOS LA DIRECCION DE ENTREGA
            this.provee.setDireccionEntrega(daoD.obtener(idDirProv));
        } catch (SQLException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void validarRangoFechas() {

        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "validarRangoFechas");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        this.ordenCompraEncabezado.setFechaCreacion(sdf.format(ordenCompraEncabezado.getFechaEmisionDirectas()));
        this.ordenCompraEncabezado.setFechaFinalizacion(sdf.format(ordenCompraEncabezado.getFechaEntregaDirectas()));
        if (this.ordenCompraEncabezado.getFechaCreacion() != null && ordenCompraEncabezado.getFechaFinalizacion() != null) {
            if (this.ordenCompraEncabezado.getFechaCreacion().compareTo(ordenCompraEncabezado.getFechaFinalizacion()) <= 0) {
                ok = true;
            } else {
                fMsg.setDetail("La fecha de emision  debe ser menor o igual a la fecha de entrega... ");
            }
        } else {
            fMsg.setDetail("Las fechas no deben ser vacías... ");
        }

        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void handleClose() throws NamingException {
        this.mbEmpresas = new MbEmpresas();
        this.mbProveedores = new MbMiniProveedor();
        this.provee = new Proveedor();
        this.ordenCompraEncabezado = new OrdenCompraEncabezado();
        this.mbMonedas.getMoneda().setIdMoneda(0);
    }
    

    
    // GET Y SETS ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public MbMiniProveedor getMbProveedores() {
        return mbProveedores;
    }

    public void setMbProveedores(MbMiniProveedor mbProveedores) {
        this.mbProveedores = mbProveedores;
    }

    public MbCotizaciones getMbCotizaciones() {
        return mbCotizaciones;
    }

    public void setMbCotizaciones(MbCotizaciones mbCotizaciones) {
        this.mbCotizaciones = mbCotizaciones;
    }

    public OrdenCompraEncabezado getOrdenCompraEncabezado() {
        return ordenCompraEncabezado;
    }

    public void setOrdenCompraEncabezado(OrdenCompraEncabezado ordenCompraEncabezado) {
        this.ordenCompraEncabezado = ordenCompraEncabezado;
    }

    public ArrayList<OrdenCompraEncabezado> getListaOrdenesEncabezado() throws NamingException {
        try {
            if (listaOrdenesEncabezado == null) {
                this.cargaOrdenesEncabezado();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaOrdenesEncabezado;
    }

    public void setListaOrdenesEncabezado(ArrayList<OrdenCompraEncabezado> listaOrdenesEncabezado) {
        this.listaOrdenesEncabezado = listaOrdenesEncabezado;
    }

    public OrdenCompraEncabezado getOrdenElegida() {
        return ordenElegida;
    }

    public void setOrdenElegida(OrdenCompraEncabezado ordenElegida) {
        this.ordenElegida = ordenElegida;
    }

    public ArrayList<OrdenCompraDetalle> getListaOrdenDetalle() {
        return listaOrdenDetalle;
    }

    public void setListaOrdenDetalle(ArrayList<OrdenCompraDetalle> listaOrdenDetalle) {
        this.listaOrdenDetalle = listaOrdenDetalle;
    }

    public OrdenCompraDetalle getOrdenCompraDetalle() {
        return ordenCompraDetalle;
    }

    public void setOrdenCompraDetalle(OrdenCompraDetalle ordenCompraDetalle) {
        this.ordenCompraDetalle = ordenCompraDetalle;
    }

    public double getSubtotalGeneral() {
        calculoSubtotalGeneral();
        return subtotalGeneral;
    }

    public void setSubtotalGeneral(double subtotalGeneral) {
        this.subtotalGeneral = subtotalGeneral;
    }

    public double getSumaDescuentosProductos() {
        return sumaDescuentosProductos;
    }

    public void setSumaDescuentosProductos(double sumaDescuentosProductos) {
        this.sumaDescuentosProductos = sumaDescuentosProductos;
    }

    public double getImpuesto() {
        calculoIVA();
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getTotal() {
        calculoTotal();
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getSubtotF() {
        subtotF = utilerias.Utilerias.formatoMonedas(this.getSubtotalGeneral());
        return subtotF;
    }

    public String getDescF() {
        descF = utilerias.Utilerias.formatoMonedas(this.getSumaDescuentosProductos());
        return descF;
    }

    public String getImpF() { //IVA
        impF = utilerias.Utilerias.formatoMonedas(this.getImpuesto());
        return impF;
    }

    public String getTotalF() {
        totalF = utilerias.Utilerias.formatoMonedas(this.getTotal());
        return totalF;
    }

    public double getSumaDescuentosGenerales() {
        return sumaDescuentosGenerales;
    }

    public void setSumaDescuentosGenerales(double sumaDescuentosGenerales) {
        this.sumaDescuentosGenerales = sumaDescuentosGenerales;
    }

    public String getSumaDescuentosProductosF() {
        sumaDescuentosProductosF = utilerias.Utilerias.formatoMonedas(this.getSumaDescuentosProductos());
        return sumaDescuentosProductosF;
    }

    public String getSumaDescuentosGeneralesF() {
        sumaDescuentosGeneralesF = utilerias.Utilerias.formatoMonedas(this.getSumaDescuentosGenerales());
        return sumaDescuentosGeneralesF;
    }

    public double getSubtotalBruto() {
        calcularSubtotalBruto();
        return subtotalBruto;
    }

    public void setSubtotalBruto(double subtotalBruto) {
        this.subtotalBruto = subtotalBruto;
    }

    public String getSubtotalBrutoF() {
        subtotalBrutoF = utilerias.Utilerias.formatoMonedas(this.getSubtotalBruto());
        return subtotalBrutoF;
    }

    public double getSumaDescuentoTotales() {
        calcularSumaDescuentosTotales();
        return sumaDescuentoTotales;
    }

    public void setSumaDescuentoTotales(double sumaDescuentoTotales) {
        this.sumaDescuentoTotales = sumaDescuentoTotales;
    }

    public String getSumaDescuentosTotalesF() {
        sumaDescuentosTotalesF = utilerias.Utilerias.formatoMonedas(this.getSumaDescuentoTotales());

        return sumaDescuentosTotalesF;
    }

    public Correo getCorreo() {
        return correo;
    }

    public void setCorreo(Correo correo) {
        this.correo = correo;
    }

    public ArrayList<Contacto> getListaContactos() {
        return listaContactos;
    }

    public void setListaContactos(ArrayList<Contacto> listaContactos) {
        this.listaContactos = listaContactos;
    }

    public Contacto getContactoElegido() {
        return contactoElegido;
    }

    public void setContactoElegido(Contacto contactoElegido) {
        this.contactoElegido = contactoElegido;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public ArrayList<Producto> getListaEmpaque() {
        return listaEmpaque;
    }

    public void setListaEmpaque(ArrayList<Producto> listaEmpaque) {
        this.listaEmpaque = listaEmpaque;
    }

    public Producto getEmpaque() {
        return empaque;
    }

    public void setEmpaque(Producto empaque) {
        this.empaque = empaque;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Cedis getCedis() {
        return cedis;
    }

    public void setCedis(Cedis cedis) {
        this.cedis = cedis;
    }

    public MbMiniCedis getMbCedis() {
        return mbCedis;
    }

    public void setMbCedis(MbMiniCedis mbCedis) {
        this.mbCedis = mbCedis;
    }

    public Proveedor getProvee() {
        return provee;
    }

    public void setProvee(Proveedor provee) {
        this.provee = provee;
    }

    //-----------DIRECTAS
    public OrdenCompraEncabezado getOrdenCompraEncabezadoDirecta() {
        return ordenCompraEncabezadoDirecta;
    }

    public void setOrdenCompraEncabezadoDirecta(OrdenCompraEncabezado ordenCompraEncabezadoDirecta) {
        this.ordenCompraEncabezadoDirecta = ordenCompraEncabezadoDirecta;
    }

    public MbEmpresas getMbEmpresas() {
        return mbEmpresas;
    }

    public void setMbEmpresas(MbEmpresas mbEmpresas) {
        this.mbEmpresas = mbEmpresas;
    }

    public Empresa getEmpre() {
        return empre;
    }

    public void setEmpre(Empresa empre) {
        this.empre = empre;
    }

    public MbMonedas getMbMonedas() {
        return mbMonedas;
    }

    public void setMbMonedas(MbMonedas mbMonedas) {
        this.mbMonedas = mbMonedas;
    }
}

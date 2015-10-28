package ordenesDeCompra;

import Message.Mensajes;
import cedis.MbMiniCedis;
import cedis.dominio.Cedis;
import contactos.dominio.Contacto;
import cotizaciones.MbCotizaciones;
import direccion.dao.DAODirecciones;
import empresas.MbEmpresas;
import empresas.dominio.Empresa;
import impuestos.CalculoDeImpuestos;
import impuestos.dominio.ImpuestosProducto;
import java.io.File;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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
import movimientos.dao.DAOMovimientosOld;
import net.sf.jasperreports.engine.JRException;
import ordenDeCompra.Report.OrdenCompraReporte;
import ordenesDeCompra.Reporte.Reportes;
import ordenesDeCompra.dao.DAOOrdenDeCompra;
import ordenesDeCompra.dominio.Correo;
import ordenesDeCompra.dominio.OrdenCompraEncabezado;
import ordenesDeCompra.dominio.OrdenCompraDetalle;
import ordenesDeCompra.dominio.TotalesOrdenCompra;
import org.primefaces.context.RequestContext;
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
    private ArrayList<OrdenCompraEncabezado> listaOrdenesEncabezadoD;
    private OrdenCompraEncabezado ordenElegida;
    private ArrayList<OrdenCompraDetalle> listaOrdenDetalle;
    private OrdenCompraDetalle ordenCompraDetalle;
    private double subtotalGeneral;
    private transient Correo correo;
    private ArrayList<Contacto> listaContactos;
    private transient Contacto contactoElegido;
    private String cadena;
    private transient OrdenCompraReporte ocr;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private ArrayList<Producto> listaEmpaque = new ArrayList<>();
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
    private OrdenCompraEncabezado ordenCompraEncabezadoDirecta = new OrdenCompraEncabezado();
    private ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas = new ArrayList<>();
    private Empresa empre;
    @ManagedProperty(value = "#{mbMonedas}")
    private MbMonedas mbMonedas;
    private double descuentoGeneralAplicado;
    private TotalesOrdenCompra totales = new TotalesOrdenCompra();
    double sumaDescuentosProductos = 0.0;
    private OrdenCompraDetalle ordenCompraDetalleSeleccionado; //para eliminar producto en el formulario
    private OrdenCompraEncabezado ordenElegidaD;
    private ArrayList<OrdenCompraDetalle> listaOrdenDetalleD;
    private OrdenCompraDetalle ordenCompraDetalleD;

    public MbOrdenCompra() throws NamingException {
        this.ordenCompraEncabezado = new OrdenCompraEncabezado();
        this.mbCotizaciones = new MbCotizaciones();
        this.ordenCompraDetalle = new OrdenCompraDetalle();
        this.ordenElegida = new OrdenCompraEncabezado();
        this.correo = new Correo();
        this.contactoElegido = new Contacto();
        this.listaContactos = new ArrayList<>();
        this.ocr = new OrdenCompraReporte();
        this.mbBuscar = new MbProductosBuscar();
        this.empaque = new Producto();
        this.mbProveedores = new MbMiniProveedor();
        this.mbEmpresas = new MbEmpresas();
        this.provee = new Proveedor();
        //-------DIRECTAS
        this.mbMonedas = new MbMonedas();
        this.ordenCompraDetalleSeleccionado = new OrdenCompraDetalle();
        this.ordenElegidaD = new OrdenCompraEncabezado();
        this.ordenCompraDetalleD = new OrdenCompraDetalle();
        this.ordenCompraEncabezadoDirecta = new OrdenCompraEncabezado();

    }

    //M E T O D O S  ////////////////////////////////////////////////////////////////////////////////////////////
    public void cargaOrdenesEncabezado() throws NamingException, SQLException {

        listaOrdenesEncabezado = new ArrayList<>();
        DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
        ArrayList<OrdenCompraEncabezado> lista = daoOC.listaOrdenes();
        for (OrdenCompraEncabezado d : lista) {
            listaOrdenesEncabezado.add(d);
        }
    }

    public void cargaOrdenesEncabezadoAlmacen(int idProveedor, int status) throws NamingException, SQLException {
        this.listaOrdenesEncabezado = new ArrayList<>();
        DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
        for (OrdenCompraEncabezado d : daoOC.listaOrdenesAlmacen(idProveedor, status)) {
            listaOrdenesEncabezado.add(d);
        }
    }

    public void cargaOrdenesEncabezado(int idProveedor, int status) throws NamingException, SQLException {
        this.listaOrdenesEncabezado = new ArrayList<>();
//        try {
        DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
        for (OrdenCompraEncabezado d : daoOC.listaOrdenes(idProveedor, status)) {
            listaOrdenesEncabezado.add(d);
        }
    }

    // ............ DAVID 3JUNIO2015
    public void actualizaProductosSeleccionados() {
        for (Producto e : this.mbBuscar.getSeleccionados()) {
            OrdenCompraDetalle ocd = new OrdenCompraDetalle();
            ocd.setProducto(e);
            ordenCompraDetallesDirectas.add(ocd);
        }
        HashSet hs = new HashSet();
        hs.addAll(ordenCompraDetallesDirectas);
        ordenCompraDetallesDirectas.removeAll(ordenCompraDetallesDirectas);
        ordenCompraDetallesDirectas.addAll(hs);
    }

    //.................
    public void dameEmpaqueSeleccionado() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "dameEmpaqueSeleccionado");
        boolean verifi = verificacion(mbBuscar.getProducto());

        if (verifi == false) {
            ordenCompraDetallesDirectas.add(ordenCompraDetalle);

            mbBuscar.getProductos().remove(mbBuscar.getProducto());
            ok = true;
        } else {
            fMsg.setDetail("Este Empaque ya esta en la lista");
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }

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
        mbBuscar.setProductos(new ArrayList<Producto>());
    }

    public void obtenerDetalleOrdenCompra() {
        listaOrdenDetalle = new ArrayList<>();
        this.subtotalGeneral = 0;
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerDetalleOrdenCompra");
        try {
            int idOC = ordenElegida.getIdOrdenCompra();
            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
            for (OrdenCompraDetalle d : daoOC.consultaOrdenCompra(idOC)) {
                d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
                if (d.getCotizacionDetalle().getProducto() != null) {
                    d.getCotizacionDetalle().setProducto(this.mbBuscar.obtenerProducto(d.getCotizacionDetalle().getProducto().getIdProducto()));
                }
                listaOrdenDetalle.add(d);
                this.calculosOrdenCompra(d.getProducto().getIdProducto());
            }
            ok = true;
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
    
    public void dameOrdenCompra() {
        listaOrdenDetalle = new ArrayList<>();
        this.subtotalGeneral = 0;
        try {
            int idOC = ordenElegida.getIdOrdenCompra();

            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();

            ArrayList<OrdenCompraDetalle> lista = daoOC.consultaOrdenCompra(idOC);
            for (OrdenCompraDetalle d : lista) {
                d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
//                d.getCotizacionDetalle().setProducto(this.mbBuscar.obtenerProducto(d.getCotizacionDetalle().getProducto().getIdProducto()));
                d.getCotizacionDetalle().setProducto(d.getProducto());
                listaOrdenDetalle.add(d);
                this.calculosOrdenCompra(d.getProducto().getIdProducto());
                d.setNombreProducto(d.getProducto().toString());
            }
        } catch (NamingException | SQLException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void dameOrdenCompra(SelectEvent event) {
        this.ordenElegida = (OrdenCompraEncabezado) event.getObject();
        this.dameOrdenCompra();
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
                    e.setSubtotal(e.getCantidadSolicitada() * e.getCostoOrdenado());
                    daoO.actualizarCantidadOrdenada(e.getIdOrdenCompra(), idProd, e.getCantidadSolicitada());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void guardarOrden(int idOrden, int estado) throws NamingException {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", "guardarOrden");
        DAOOrdenDeCompra daoO = new DAOOrdenDeCompra();
        try {
            if (estado == 1) {
                daoO.procesarOrdenCompra(idOrden);

                this.cargaOrdenesEncabezado();

                fMsg.setDetail("Se ha guardado con satisfactoriamente...");
            } else if (estado == 5) {
                fMsg.setDetail("La orden se ha registrado con anterioridad");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
            fMsg.setDetail("No se realizó el registro de la orden de compra..");
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }

    public String irMenu() throws NamingException {
        String navega = "menuOrdenesDeCompra.xhtml";
        return navega;
    }

    public void generarReporteD() throws NamingException, SQLException {
        try {
            ocr.generarReporte(listaOrdenDetalleD, ordenElegidaD, totales, 0);
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
//                    totalOrdenCompra.setImpF(impF);
//                    totalOrdenCompra.setSubTotalBrutoF(subtotalBrutoF);
//                //    totalOrdenCompra.setSubtoF(subtotF);
//                    totalOrdenCompra.setSumaDescuentosGeneralesF(sumaDescuentosGeneralesF);
//                    totalOrdenCompra.setSumaDescuentosTotalesF(sumaDescuentosTotalesF);
//                    totalOrdenCompra.setSumaDescuentsoProductosF(sumaDescuentosProductosF);
//                    totalOrdenCompra.setTotalF(totalF);
                    //DATOS FIJOS
//                    String servidorCorreos = "mail.laanita.com";
//                    String user = "carlos.pat";
//                    String remitente = "carlos.pat@laanita.com";
//                    String passRemitente = "Mildred1";
                    String servidorCorreos = "p3plcpnl0527.prod.phx3.secureserver.net";
                    String user = "david.arias";
                    String remitente = "david.arias@pcoriente.com.mx";
                    String passRemitente = "Darias660922";
                    int puerto = 465;
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
            this.listaContactos = new ArrayList<>();
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
        // cadena += ",";
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
        email = email.trim();
        boolean validar;
        Pattern pattern = Pattern.compile("[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]");
        Matcher matcher = pattern.matcher(email);
        validar = matcher.matches();
        return validar;
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
            Logger.getLogger(MbOrdenCompra.class
                    .getName()).log(Level.SEVERE, null, ex);
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

    public double getSumaDescuentosProductos() {
        return sumaDescuentosProductos;
    }

    public void setSumaDescuentosProductos(double sumaDescuentosProductos) {
        this.sumaDescuentosProductos = sumaDescuentosProductos;
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

    public ArrayList<OrdenCompraDetalle> getOrdenCompraDetallesDirectas() {

        return ordenCompraDetallesDirectas;
    }

    public void setOrdenCompraDetallesDirectas(ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas) {
        this.ordenCompraDetallesDirectas = ordenCompraDetallesDirectas;
    }

    public double getDescuentoGeneralAplicado() {
        return descuentoGeneralAplicado;
    }

    public void setDescuentoGeneralAplicado(double descuentoGeneralAplicado) {
        this.descuentoGeneralAplicado = descuentoGeneralAplicado;
    }

    public TotalesOrdenCompra getTotales() {
        return totales;
    }

    public void setTotales(TotalesOrdenCompra totales) {
        this.totales = totales;
    }

    public OrdenCompraDetalle getOrdenCompraDetalleSeleccionado() {
        return ordenCompraDetalleSeleccionado;
    }

    public void setOrdenCompraDetalleSeleccionado(OrdenCompraDetalle ordenCompraDetalleSeleccionado) {
        this.ordenCompraDetalleSeleccionado = ordenCompraDetalleSeleccionado;
    }

    public ArrayList<OrdenCompraEncabezado> getListaOrdenesEncabezadoD() throws NamingException {
        try {
            if (listaOrdenesEncabezadoD == null) {
                this.cargaOrdenesEncabezadoD();
            }
        } catch (SQLException ex) {
            Mensajes.MensajeAlertP(ex.getMessage());
        }
        return listaOrdenesEncabezadoD;
    }

    public void setListaOrdenesEncabezadoD(ArrayList<OrdenCompraEncabezado> listaOrdenesEncabezadoD) {
        this.listaOrdenesEncabezadoD = listaOrdenesEncabezadoD;
    }

    public OrdenCompraEncabezado getOrdenElegidaD() {
        return ordenElegidaD;
    }

    public void setOrdenElegidaD(OrdenCompraEncabezado ordenElegidaD) {
        this.ordenElegidaD = ordenElegidaD;
    }

    public ArrayList<OrdenCompraDetalle> getListaOrdenDetalleD() {
        return listaOrdenDetalleD;
    }

    public void setListaOrdenDetalleD(ArrayList<OrdenCompraDetalle> listaOrdenDetalleD) {
        this.listaOrdenDetalleD = listaOrdenDetalleD;
    }

    public OrdenCompraDetalle getOrdenCompraDetalleD() {
        return ordenCompraDetalleD;
    }

    public void setOrdenCompraDetalleD(OrdenCompraDetalle ordenCompraDetalleD) {
        this.ordenCompraDetalleD = ordenCompraDetalleD;
    }

    //MENU SEGUNDA TABLA DE ORDENES DIRECTAS
    public void cargaOrdenesEncabezadoD() throws NamingException, SQLException {
        listaOrdenesEncabezadoD = new ArrayList<>();
        DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
        ArrayList<OrdenCompraEncabezado> listaD = daoOC.listaOrdenesD();
        for (OrdenCompraEncabezado oced : listaD) {
            //oced.setImporteTotal(dameImporteOrdenCompra(oced.getIdOrdenCompra(), oced.getProveedor().getImpuestoZona().getIdZona(), oced));
            listaOrdenesEncabezadoD.add(oced);
            totales = new TotalesOrdenCompra();
            subtotalGeneral = 0.00;
            sumaDescuentosProductos = 0.00;
        }
    }

    public void limpiar() {
        System.out.println("Entro  a limpiar");
//        ordenElegidaD = null;
//        System.out.println("entro a limpiar");
    }

    public void guardarOrdenCompraDirecta() {
        if (validar() == true) {
            ordenCompraEncabezadoDirecta.setEmpresa(empresa);
            boolean control = false;
            for (OrdenCompraDetalle p : this.ordenCompraDetallesDirectas) {
                if (p.getCantOrdenada() <= 0 || p.getCostoOrdenado() == 0.00) {
                    Mensajes.MensajeAlertP("Aviso: Para el producto " + p.getProducto().toString() + " Capture una cantidad o un costo. ");
                    control = true;
                    break;
                }
            }
            if (control == false) {
                try {
                    DAOOrdenDeCompra dao = new DAOOrdenDeCompra();
                    dao.guardarOrdenCompraDirecta(mbProveedores.getMiniProveedor(), ordenCompraEncabezadoDirecta, ordenCompraDetallesDirectas);
                    Mensajes.MensajeSuccesP("Nueva orden de compra disponible");
                    this.listaOrdenesEncabezado = null;
                    this.listaOrdenesEncabezadoD = null;
                    this.getListaOrdenesEncabezado();
                    this.getListaOrdenesEncabezadoD();
                    RequestContext primeContext = RequestContext.getCurrentInstance();
                    primeContext.execute("PF('odec').hide()");
                    ordenCompraEncabezadoDirecta = new OrdenCompraEncabezado();
                    ordenCompraDetallesDirectas = new ArrayList<>();
                } catch (NamingException | SQLException ex) {
                    Mensajes.MensajeErrorP(ex.getMessage());
                }
            }

        }
    }

    public boolean validar() {
        boolean ok = false;
        if (empresa.getIdEmpresa() == 0) {
            Mensajes.MensajeAlertP("Se requiere una empresa");
        } else if (ordenCompraEncabezadoDirecta.getFechaEntregaDirectas() == null) {
            Mensajes.MensajeAlertP("Se requiere una fecha de entrega");
        } else if (ordenCompraEncabezadoDirecta.getMoneda().getIdMoneda() == 0) {
            Mensajes.MensajeAlertP("Se requiere una moneda");
        } else if (ordenCompraDetallesDirectas.isEmpty()) {
            Mensajes.MensajeAlertP("Se requiere por lo menos un producto");
        } else {
            ok = true;
        }
        return ok;
    }

    public double truncarNumeros(double dato) {
        DecimalFormat df = new DecimalFormat("##.##");
        return Double.parseDouble(df.format(dato));
    }

    public double Redondear(double numero) {
        return Math.rint(numero * 100) / 100;
    }

    public void buscarSku() {
        try {
            this.mbBuscar.buscarLista();
            if (this.mbBuscar.getProducto() != null) {
                DAOOrdenDeCompra dao = new DAOOrdenDeCompra();
                OrdenCompraDetalle req = new OrdenCompraDetalle();
                req.setProducto(mbBuscar.getProducto());
                Double ultimoCosto = dao.ObtenerUltimoCosto(mbBuscar.getProducto().getIdProducto(), empresa.getCodigoEmpresa(), mbProveedores.getMiniProveedor().getIdProveedor());
                req.setCostoOrdenado(ultimoCosto);
                ordenCompraDetallesDirectas.add(req);
                mbBuscar.setProducto(new Producto());
            }
        } catch (NullPointerException e) {
            Mensajes.MensajeAlertP("Error de excepcion");
        } catch (NamingException | SQLException ex) {
            Mensajes.MensajeAlertP(ex.getMessage());
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            Logger.getLogger(MbOrdenCompra.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleClose() throws NamingException {

        // ACTUALIZAR EL DATATABLE DOBLE DE ORDENES DE COMPRA
        System.out.println("Entro  a limpiar");
        this.mbEmpresas = new MbEmpresas();
//        this.mbProveedores = new MbMiniProveedor();
        this.provee = new Proveedor();
        this.ordenCompraEncabezado = new OrdenCompraEncabezado();
        //   this.ordenCompraEncabezadoDirecta = new OrdenCompraEncabezado();
        this.mbMonedas.getMoneda().setIdMoneda(0);
        ordenCompraDetallesDirectas = new ArrayList<>();
        totales = new TotalesOrdenCompra();
    }

    public void eliminaProducto() throws NamingException {
        ordenCompraDetallesDirectas.remove(ordenCompraDetalleSeleccionado);
        ordenCompraDetalleSeleccionado = null;
        calcularImporte();
    }

    public void limpiaOrdenCompraDirecta() throws NamingException {
        this.setListaOrdenesEncabezado(null);
        ordenCompraDetallesDirectas.clear();
        this.totales = new TotalesOrdenCompra();
    }

    public void calcularImporte() throws NamingException {
        // ordenCompraDetallesDirectas
        this.totales = new TotalesOrdenCompra();
        double sumaCostoCotizado = 0;
        double descuentoC;
        double descuentoPP;
        double desctoProd1;
        double desctoProd2;
        sumaDescuentosProductos = 0;
        subtotalGeneral = 0;
        descuentoGeneralAplicado = 0;
        DAOMovimientosOld dao = new DAOMovimientosOld();
        Double impuestos = 0.00;

        //CALCULOS
        for (OrdenCompraDetalle e : ordenCompraDetallesDirectas) {
            try {
                sumaCostoCotizado += (e.getCantOrdenada() * e.getCostoOrdenado());
                descuentoC = (e.getCantOrdenada() * e.getCostoOrdenado()) * (mbProveedores.getMiniProveedor().getDesctoComercial() / 100);
                descuentoPP = ((e.getCantOrdenada() * e.getCostoOrdenado()) - descuentoC) * (mbProveedores.getMiniProveedor().getDesctoProntoPago() / 100);
                desctoProd1 = ((e.getCantOrdenada() * e.getCostoOrdenado()) - (descuentoC + descuentoPP)) * (e.getDescuentoProducto() / 100);
                desctoProd2 = ((e.getCantOrdenada() * e.getCostoOrdenado()) - (descuentoC + descuentoPP + desctoProd1)) * (e.getDescuentoProducto2() / 100);

                double neto2 = sumaCostoCotizado - (descuentoC + descuentoPP + desctoProd1 + desctoProd2);
                e.setNeto(neto2);
                e.setSubtotal(e.getCostoOrdenado() * e.getCantOrdenada());
                subtotalGeneral += e.getSubtotal();
                descuentoGeneralAplicado += descuentoC + descuentoPP;
                sumaDescuentosProductos += desctoProd1 + desctoProd2;

                ArrayList<ImpuestosProducto> lst = dao.generarImpuestosProducto(e.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo(), mbProveedores.getMiniProveedor().getIdImpuestoZona());
                CalculoDeImpuestos calculo = new CalculoDeImpuestos();
                if (e.getCostoOrdenado() > 0 && e.getCantOrdenada() > 0) {
                    double costoReal;
                    //carlos pat descuentos globales
                    double descuentoComercial = Redondear((e.getCostoOrdenado() * this.mbProveedores.getMiniProveedor().getDesctoComercial()) / 100);
                    costoReal = e.getCostoOrdenado() - descuentoComercial;
                    double descuentoProntoPago = Redondear(costoReal * this.mbProveedores.getMiniProveedor().getDesctoProntoPago() / 100);
                    costoReal = costoReal - descuentoProntoPago;
                    double descuento = Redondear((costoReal * e.getDescuentoProducto()) / 100);
                    costoReal = costoReal - descuento;
                    double descuento2 = Redondear((costoReal * e.getDescuentoProducto2() / 100));
                    costoReal = costoReal - descuento2;

                    impuestos += (calculo.calculaImpuestos(costoReal, e.getCantOrdenada(), e.getProducto(), lst) * e.getCantOrdenada());
                }

            } catch (SQLException ex) {
                Mensajes.MensajeErrorP(ex.getMessage());
                Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        totales.setSubtotalGeneral(Redondear(subtotalGeneral));
        totales.setDescuentoGeneralAplicado(Redondear(descuentoGeneralAplicado));
        totales.setSumaDescuentosProductos(Redondear(sumaDescuentosProductos));
        totales.setSumaDescuentoTotales(Redondear(totales.getDescuentoGeneralAplicado() + totales.getSumaDescuentosProductos()));
        totales.setSubtotalBruto(Redondear(totales.getSubtotalGeneral() - totales.getSumaDescuentoTotales()));
        totales.setImpuesto(Redondear(impuestos));
        totales.setTotal(Redondear(totales.getImpuesto() + totales.getSubtotalBruto()));
    }

    public void dameOrdenCompraDirectaV(SelectEvent event) {
        this.ordenElegidaD = (OrdenCompraEncabezado) event.getObject();
        double sumaCostoCotizado = 0;
        double descuentoC;
        double descuentoPP;
        double desctoProd1;
        double desctoProd2;
        sumaDescuentosProductos = 0;
        subtotalGeneral = 0;
        descuentoGeneralAplicado = 0;
        double costoConDescuentos;
        
        listaOrdenDetalleD = new ArrayList<>();
        try {
            int idOC = ordenElegidaD.getIdOrdenCompra();
            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
            ArrayList<OrdenCompraDetalle> lista = daoOC.consultaOrdenCompra(idOC);
            DAOMovimientosOld dao = new DAOMovimientosOld();
            Double impuestos = 0.00;
            //calculos
            for (OrdenCompraDetalle d : lista) {
                sumaCostoCotizado += (d.getCantOrdenada() * d.getCostoOrdenado());
                descuentoC = (d.getCantOrdenada() * d.getCostoOrdenado()) * (ordenElegidaD.getDesctoComercial() / 100);
                descuentoPP = ((d.getCantOrdenada() * d.getCostoOrdenado()) - descuentoC) * (ordenElegidaD.getDesctoProntoPago() / 100);
                desctoProd1 = ((d.getCantOrdenada() * d.getCostoOrdenado()) - (descuentoC + descuentoPP)) * (d.getDescuentoProducto() / 100);
                desctoProd2 = ((d.getCantOrdenada() * d.getCostoOrdenado()) - (descuentoC + descuentoPP + desctoProd1)) * (d.getDescuentoProducto2() / 100);
                
                costoConDescuentos = d.getCostoOrdenado() * ((1 - (ordenElegidaD.getDesctoComercial() / 100)) * (1 - (ordenElegidaD.getDesctoProntoPago() / 100)) * (1 - (d.getDescuentoProducto() / 100)) * (1 - (d.getDescuentoProducto2() / 100)));

                double neto2 = sumaCostoCotizado - (descuentoC + descuentoPP + desctoProd1 + desctoProd2);
                d.setNeto(neto2);
                d.setSubtotal(d.getCostoOrdenado() * d.getCantOrdenada());
                subtotalGeneral += d.getSubtotal();
                descuentoGeneralAplicado += descuentoC + descuentoPP;
                sumaDescuentosProductos += desctoProd1 + desctoProd2;

                d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
                CalculoDeImpuestos calculo = new CalculoDeImpuestos();
                ArrayList<ImpuestosProducto> lst = dao.generarImpuestosProducto(d.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo(), ordenElegidaD.getProveedor().getImpuestoZona().getIdZona());
                if (d.getCostoOrdenado() > 0 && d.getCantOrdenada() > 0) {
                    impuestos += (calculo.calculaImpuestos(costoConDescuentos, d.getCantOrdenada(), d.getProducto(), lst) * d.getCantOrdenada());
                }
                listaOrdenDetalleD.add(d);
            }
            totales.setSubtotalGeneral(Redondear(subtotalGeneral));
            totales.setDescuentoGeneralAplicado(Redondear(descuentoGeneralAplicado));
            totales.setSumaDescuentosProductos(Redondear(sumaDescuentosProductos));
            totales.setSumaDescuentoTotales(Redondear(totales.getDescuentoGeneralAplicado() + totales.getSumaDescuentosProductos()));
            totales.setSubtotalBruto(Redondear(totales.getSubtotalGeneral() - totales.getSumaDescuentoTotales()));
            totales.setImpuesto(Redondear(impuestos));
            totales.setTotal(Redondear(totales.getImpuesto() + totales.getSubtotalBruto()));
        } catch (NamingException | SQLException ex) {
            Mensajes.MensajeErrorP(ex.getMessage());
        }
    }

    public double dameImporteOrdenCompra(int idOrdenCompra, int idZona, OrdenCompraEncabezado ordenElegidaD) {
        try {
            double sumaCostoCotizado = 0;
            double descuentoC;
            double descuentoPP;
            double desctoProd1;
            double desctoProd2;
            sumaDescuentosProductos = 0;
            subtotalGeneral = 0;
            descuentoGeneralAplicado = 0;
            double costoConDescuentos;
            DAOOrdenDeCompra daoTotOc = new DAOOrdenDeCompra();//para calculo de totales
            DAOOrdenDeCompra daoOC = new DAOOrdenDeCompra();
            ArrayList<OrdenCompraDetalle> lista = daoOC.consultaOrdenCompra(idOrdenCompra);
            DAOMovimientosOld dao = new DAOMovimientosOld();
            Double impuestos = 0.00;
            //calculos            
            for (OrdenCompraDetalle d : lista) {
                sumaCostoCotizado += (d.getCantOrdenada() * d.getCostoOrdenado());
                descuentoC = (d.getCantOrdenada() * d.getCostoOrdenado()) * (ordenElegidaD.getDesctoComercial() / 100);
                descuentoPP = ((d.getCantOrdenada() * d.getCostoOrdenado()) - descuentoC) * (ordenElegidaD.getDesctoProntoPago() / 100);
                desctoProd1 = ((d.getCantOrdenada() * d.getCostoOrdenado()) - (descuentoC + descuentoPP)) * (d.getDescuentoProducto() / 100);
                desctoProd2 = ((d.getCantOrdenada() * d.getCostoOrdenado()) - (descuentoC + descuentoPP + desctoProd1)) * (d.getDescuentoProducto2() / 100);

                costoConDescuentos = d.getCostoOrdenado() * ((1 - (ordenElegidaD.getDesctoComercial() / 100)) * (1 - (ordenElegidaD.getDesctoProntoPago() / 100)) * (1 - (d.getDescuentoProducto() / 100)) * (1 - (d.getDescuentoProducto2() / 100)));

                double neto2 = sumaCostoCotizado - (descuentoC + descuentoPP + desctoProd1 + desctoProd2);
                d.setNeto(neto2);
                d.setSubtotal(d.getCostoOrdenado() * d.getCantOrdenada());
                subtotalGeneral += d.getSubtotal();
                descuentoGeneralAplicado += descuentoC + descuentoPP;
                sumaDescuentosProductos += desctoProd1 + desctoProd2;

                d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));

                CalculoDeImpuestos calculo = new CalculoDeImpuestos();
                ArrayList<ImpuestosProducto> lst = dao.generarImpuestosProducto(d.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo(), idZona);
                if (d.getCostoOrdenado() > 0 && d.getCantOrdenada() > 0) {
                    impuestos += (calculo.calculaImpuestos(costoConDescuentos, d.getCantOrdenada(), d.getProducto(), lst) * d.getCantOrdenada());
                }
            }
            totales.setSubtotalGeneral(Redondear(subtotalGeneral));
            totales.setDescuentoGeneralAplicado(Redondear(descuentoGeneralAplicado));
            totales.setSumaDescuentosProductos(Redondear(sumaDescuentosProductos));
            totales.setSumaDescuentoTotales(Redondear(totales.getDescuentoGeneralAplicado() + totales.getSumaDescuentosProductos()));
            totales.setSubtotalBruto(Redondear(totales.getSubtotalGeneral() - totales.getSumaDescuentoTotales()));
            totales.setImpuesto(Redondear(impuestos));
            totales.setTotal(Redondear(totales.getImpuesto() + totales.getSubtotalBruto()));
            //aqui actualizo
            //daoTotOc.actualizaTotal(idOrdenCompra, totales.getTotal());
        } catch (NamingException ex) {
            Mensajes.MensajeErrorP(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.MensajeAlertP(ex.getMessage());
        }
        return totales.getTotal();
    }

    public boolean validarRangoFechas() {

        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "validarRangoFechas");

        Date fechaActual = new Date();
        DateFormat formatoFecha = new SimpleDateFormat("yyyy/MM/dd");
        String hoy = formatoFecha.format(fechaActual);
        Date entrega = this.ordenCompraEncabezadoDirecta.getFechaEntregaDirectas();
        String fE = formatoFecha.format(entrega);

        if (hoy != null && entrega != null) {
            if (hoy.compareTo(fE) <= 0) {
                ok = true;

            } else {
                fMsg.setDetail("La fecha de emision  debe ser menor o igual a la fecha de entrega... ");
                this.ordenCompraEncabezadoDirecta.setFechaEntregaDirectas(null);
            }
        } else {
            fMsg.setDetail("Las fechas no deben ser vacías... ");
            this.ordenCompraEncabezadoDirecta.setFechaEntregaDirectas(null);
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }

        return ok;
    }

    public void cancelarOrden() throws NamingException {
        int idOrden = this.ordenElegidaD.getIdOrdenCompra();
        Boolean correcto = false;
        //    FacesMessage msg = null;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cancelarOrden");
        DAOOrdenDeCompra daoO = new DAOOrdenDeCompra();
        try {
            if (this.ordenElegidaD.getEstado() == 5) {
                daoO.cancelarOrdenCompra(idOrden);
                this.setListaOrdenesEncabezadoD(null);
                this.cargaOrdenesEncabezadoD();

                fMsg.setDetail("Se ha CANCELADO");
                correcto = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbOrdenCompra.class.getName()).log(Level.SEVERE, null, ex);
            fMsg.setDetail("No se realizÃ³ la cancelaciÃ³n de la orden de compra.." + ex.getMessage());
        }

        if (!correcto) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

}

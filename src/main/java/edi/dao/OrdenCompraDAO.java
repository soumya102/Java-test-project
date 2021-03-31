/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import mars.comercial.dao.hibernate.ListaPrecio;
import mars.web.common.Utilidades;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.dao.support.DataAccessUtils;

/**
 *
 * @author alvarjor
 */
public class OrdenCompraDAO implements IOrdenCompraDAO {
    ParametroDAO parametroDAO;
    LogDAO logDAO;
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public LogDAO getLogDAO() {
        return logDAO;
    }


    public void setLogDAO(LogDAO logDAO) {
        this.logDAO = logDAO;
    }


    public ParametroDAO getParametroDAO() {
        return parametroDAO;
    }


    public void setParametroDAO(ParametroDAO parametroDAO) {
        this.parametroDAO = parametroDAO;
    }


    public MaestroOrden obtenerOrdenCompra(String codMaestroOrden) throws Exception {
        try {
            sessionFactory.getCurrentSession().clear();
            return (MaestroOrden)sessionFactory.getCurrentSession().get(MaestroOrden.class, codMaestroOrden);
        } catch (Exception ex) {
            throw new Exception("Error al obtener la orden de compra->" + Utilidades.obtenerMsgError(ex));
        }
    }


    public List listarOrdenesLog(int codLog) {
        String sentencia;

        //Se consultan todas las ordenes que pertenezcan al log especificado
        sentencia="SELECT m " +
                  "FROM MaestroOrden m " +
                  "WHERE m.log.codLog=" + codLog;

        return sessionFactory.getCurrentSession().createQuery(sentencia).list();
    }


    public List listarClientesPorFecha(String codTerritorio, Date fecha) throws Exception {
        List resultado;
        String sentencia;
        Query query;
        
        try {
            resultado=new ArrayList();

            //Se listan solo los errores a nivel de producto
            sentencia="SELECT DISTINCT mo.codBillTo, mo.nombreBillTo " +
                      "FROM MaestroOrden mo " +
                      "WHERE mo.codTerritorio='" + codTerritorio + "' " +
                      "AND TRUNC(mo.fechaActual)=? " +
                      "AND mo.nombreBillTo IS NOT NULL " +
                      "ORDER BY mo.nombreBillTo";
            query = sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, fecha);
            
            resultado=query.list();
        } catch (Exception ex) {
            throw new Exception("Error consultando la lista de clientes por fecha->" + Utilidades.obtenerMsgError(ex));
        }

        return resultado;
    }


    //En la primera posición manda el total de ordenes que estan en ese estado para ese cliente
    //En la segunda posición manda el total en dinero de las ordenes que estan en ese estado para ese cliente
    public double[] obtenerTotalOrdenesPorClienteEstado(Date fecha, String codBillTo, String codEstado, String tipo) throws Exception {
        double[] resultado;
        Object[] valorObjTemp;
        String sentencia;
        Query query;
        
        try {
            resultado=new double[2];

            if (tipo.equals("GENERAL")) {
                sentencia="SELECT " +
                          "COUNT(*) AS Cant_Ordenes," +
                          "NVL(SUM(mo.totalMars),0) AS Total_Dinero " +
                          "FROM MaestroOrden mo " +
                          "WHERE TRUNC(mo.fechaActual)=? " +
                          "AND mo.codBillTo='" + codBillTo + "' " +
                          "AND mo.estado.codigo='" + codEstado + "'";
            } else if (tipo.equals("DETALLE")) {
                sentencia="SELECT " +
                          "COUNT(*) AS Cant_Ordenes," +
                          "NVL(SUM(mo.totalMars),0) AS Total_Dinero " +
                          "FROM MaestroOrden mo " +
                          "WHERE TRUNC(mo.fechaActual)=? " +
                          "AND mo.codBillTo='" + codBillTo + "' " +
                          "AND mo.estado.codigo='EOR004' " +
                          "AND EXISTS (SELECT 1 " +
                                      "FROM DetalleOrden do " +
                                      "WHERE do.id.maestroOrden.codMaestroOrden=mo.codMaestroOrden " +
                                      "AND do.estadoNuevo.codigo='" + codEstado + "' " +
                                      ")";
            }  else if (tipo.equals("CLIENTE")) {
                sentencia="SELECT " +
                          "COUNT(*) AS Cant_Ordenes," +
                          "NVL(SUM(mo.totalMars),0) AS Total_Dinero " +
                          "FROM MaestroOrden mo " +
                          "WHERE TRUNC(mo.fechaActual)=? " +
                          "AND mo.codBillTo='" + codBillTo + "'";
            } else {
                sentencia="SELECT " +
                          "COUNT(*) AS Cant_Ordenes," +
                          "NVL(SUM(mo.totalMars),0) AS Total_Dinero " +
                          "FROM MaestroOrden mo " +
                          "WHERE TRUNC(mo.fechaActual)=? " +
                          "AND mo.codBillTo='" + codBillTo + "' " +
                          "AND mo.estado.codigo='EOR004'";
            }

            query=sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, fecha);
            valorObjTemp=(Object[])query.uniqueResult();

            resultado[0]=0;
            resultado[1]=0;
            if (valorObjTemp!=null) {
                resultado[0]=Double.parseDouble(valorObjTemp[0].toString());
                resultado[1]=Double.parseDouble(valorObjTemp[1].toString());
            }

        } catch (Exception ex) {
            throw new Exception("Error obteniendo el total de ordenes por fecha, cliente y estado->" + Utilidades.obtenerMsgError(ex));
        }

        return resultado;
    }


    public int obtenerEstadoAvance(String codTerritorio, Date fecha) throws Exception {
        double resultado, totalOrdenes, procOrdenes;
        String sentencia;
        Query query;
        
        try {
            resultado=0;

            //Total de las ordenes de la fecha
            sentencia="SELECT COUNT(*) " +                      
                      "FROM MaestroOrden m " +
                      "WHERE m.codTerritorio='" + codTerritorio + "' " +
                      "AND TRUNC(m.fechaActual)=TRUNC(?)";
            query = sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, fecha);
            
            totalOrdenes=Double.parseDouble(query.uniqueResult().toString());

            if (totalOrdenes>0) {
                //Total de las ordenes con errores
                sentencia+=" AND m.estado.codigo!='EOR004'";
                query = sessionFactory.getCurrentSession().createQuery(sentencia);
                query.setParameter(0, fecha);
                procOrdenes=Double.parseDouble(query.uniqueResult().toString());

                resultado=(procOrdenes/totalOrdenes);
                resultado=resultado*100;

                if (resultado>99.5 && resultado<100) {
                    resultado=99;
                } else {
                    resultado=Math.rint(resultado);
                }
            }
        } catch (Exception ex) {
            throw new Exception("Error obteniendo el estado de avance->" + Utilidades.obtenerMsgError(ex));
        }

        return (int)resultado;
    }


    public List consultarOrdenes(String codTerritorio, String operadorFecha, Date fechaReporte, String numOrden, String codCliente, String codError, String tipoError) throws Exception {
        String sentencia, filtro;
        Query query;

        try {

            filtro="";
            if (fechaReporte!=null) {
                filtro+="TRUNC(m.fechaActual)" + operadorFecha + ":pFchActual ";
            }

            if (!numOrden.isEmpty()) {
                filtro+=(filtro.isEmpty() ? "" : "AND ") + "m.numOrden LIKE '" + numOrden + "' ";
            }

            if (!codCliente.isEmpty()) {
                filtro+=(filtro.isEmpty() ? "" : "AND ") + "m.codBillTo='" + codCliente + "' ";
            }

            if (!tipoError.isEmpty()) {
                if (tipoError.equals("GENERAL")) {
                    filtro+=(filtro.isEmpty() ? "" : "AND ") + "m.estado.codigo='" + codError + "' ";
                } else {
                    filtro+=(filtro.isEmpty() ? "" : "AND ") + "m.estado.codigo='EOR004' " +
                                                               "AND EXISTS (SELECT 1 " +
                                                                           "FROM DetalleOrden d " +
                                                                           "WHERE d.id.maestroOrden.codMaestroOrden=m.codMaestroOrden " +
                                                                           "AND d.estadoNuevo='" + codError + "'" +
                                                                           ")";
                }
            }

            sentencia="SELECT m " +
                      "FROM MaestroOrden m JOIN FETCH m.estado " +
                      "WHERE m.codTerritorio='" + codTerritorio + "' " +
                      "AND " + filtro +
                      " ORDER BY m.nombreSoldTo, m.numOrden";

            //Se limpia los objetos
            sessionFactory.getCurrentSession().clear();
            
            query = sessionFactory.getCurrentSession().createQuery(sentencia);
            if (fechaReporte!=null) {
                query.setParameter("pFchActual", fechaReporte);
            }
            
            return query.list();
        } catch (Exception ex) {
            throw new Exception("Error consultando las ordenes->" + Utilidades.obtenerMsgError(ex));
        }
    }


    public String cancelarOrdenes(MaestroOrden[] listadoOrdenes) {
        String resultado="OK", sentencia, codMaestroOrden;

        try {
            for (int i=0; i<listadoOrdenes.length; i++) {
                codMaestroOrden=listadoOrdenes[i].getCodMaestroOrden();
                //Se actualiza el estado de la cabecera de la orden
                sentencia="UPDATE MaestroOrden m SET " +
                          "m.estado.codigo='EOR003' " +
                          "WHERE m.codMaestroOrden='" + codMaestroOrden + "'";
                sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();

                //Se actualiza el estado de los productos de la orden
                sentencia="UPDATE DetalleOrden d SET " +
                          "d.estadoNuevo.codigo='EPR002' " +
                          "WHERE d.id.maestroOrden.codMaestroOrden='" + codMaestroOrden + "'";
                sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();
            }
        } catch (Exception ex) {
            resultado=Utilidades.obtenerMsgError(ex);
        }

        return resultado;
    }

   
    public boolean puedeforzarPosteo(String codMaestroOrden) {
        boolean resultado=false;
        int cantidad;
        String sentencia;

        sentencia="SELECT COUNT(*) " +
                  "FROM DetalleOrden d " +
                  "WHERE d.id.maestroOrden.codMaestroOrden='" + codMaestroOrden + "' " +
                  "AND d.estadoNuevo.enviar='SI'";

        cantidad=DataAccessUtils.intResult(sessionFactory.getCurrentSession().createQuery(sentencia).list());

        if (cantidad>0) {
            resultado=true;
        }

        return resultado;
    }


    public String forzarPosteo(MaestroOrden[] listadoOrdenes) {
        String resultado="OK";
        String sentencia;
        
        try {
            for (int i=0; i<listadoOrdenes.length; i++) {
                sentencia = "UPDATE MaestroOrden m SET " +
                            "m.estado.codigo='EOR005' " +
                            "WHERE m.codMaestroOrden='" + listadoOrdenes[i].getCodMaestroOrden() + "'";
                sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();
            }
        } catch (Exception ex) {
            resultado=Utilidades.obtenerMsgError(ex);
        }

        return resultado;
    }


    public String resetearEstadoItem(DetalleOrden[] listadoItems) {
        String resultado="OK";
        DetalleOrden detalleOrden;
        String sentencia;
        
        try {
            for (int i=0; i<listadoItems.length; i++) {
                detalleOrden=listadoItems[i];
                sentencia="UPDATE DetalleOrden d SET " +
                          "d.estadoNuevo.codigo=d.estadoOriginal.codigo " +
                          "WHERE d.id.maestroOrden.codMaestroOrden='" + detalleOrden.getId().getMaestroOrden().getCodMaestroOrden() + "' " +
                          "AND d.id.eanProducto='" + detalleOrden.getId().getEanProducto() + "' " +
                          "AND d.id.eanShipToSec='" + detalleOrden.getId().getEanShipToSec() + "' " +
                          "AND d.id.numLinea=" + detalleOrden.getId().getNumLinea();
                sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();
            }
        } catch (Exception ex) {
            resultado=Utilidades.obtenerMsgError(ex);
        }
        return resultado;
    }


    public String cancelarItem(DetalleOrden[] listadoItems) {
        String resultado="OK";
        DetalleOrden detalleOrden;
        String sentencia;
        
        try {
            for (int i=0; i<listadoItems.length; i++) {
                detalleOrden=listadoItems[i];
                sentencia="UPDATE DetalleOrden d SET " +
                          "d.estadoOriginal.codigo=d.estadoNuevo.codigo, " +
                          "d.estadoNuevo.codigo='EPR002' " +
                          "WHERE d.id.maestroOrden.codMaestroOrden='" + detalleOrden.getId().getMaestroOrden().getCodMaestroOrden() + "' " +
                          "AND d.id.eanProducto='" + detalleOrden.getId().getEanProducto() + "' " +
                          "AND d.id.eanShipToSec='" + detalleOrden.getId().getEanShipToSec() + "' " +
                          "AND d.id.numLinea=" + detalleOrden.getId().getNumLinea();
                sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();
            }
        } catch (Exception ex) {
            resultado=Utilidades.obtenerMsgError(ex);
        }
        return resultado;
    }


    public List consultarCaseFill(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado) {
        String sentencia;

        sentencia="SELECT " +
                  "m.fechaActual, " +
                  "m.codBillTo, " +
                  "m.nombreBillTo, " +
                  "m.codSoldTo, " +
                  "m.nombreSoldTo, " +
                  "m.numOrden, " +
                  "m.fechaOrden, " +
                  "m.fechaEntrega, " +
                  "d.codProducto, " +
                  "d.id.eanProducto, " +
                  "d.descripcionProducto, " +
                  "d.cantPLU, " +
                  "d.precioNetoCliente, " +
                  "d.precioNetoCliente*d.cantUndVenta, " +
                  "d.cantFacturada*d.cantUndInternas, " +
                  "d.precioNetoMars, " +
                  "d.precioNetoMars*d.cantFacturada, " +
                  "d.indicadorLlenado, " +
                  "d.estadoNuevo.descripcion AS Estado " +
                  "FROM MaestroOrden m JOIN m.detalleOrden d JOIN d.estadoOriginal " +
                  "WHERE m.codTerritorio='" + codTerritorio + "' " +
                  "AND TRUNC(m.fechaActual) BETWEEN TRUNC(?) AND TRUNC(?) ";

        if (!codBillTo.equals("TODO")) {
            sentencia+="AND m.codBillTo='" + codBillTo + "' ";
        }

        if (!listadoEstado.isEmpty()) {
            sentencia+="AND d.estadoOriginal.codigo IN (" + listadoEstado + ") ";
        }
        sentencia+="ORDER BY m.nombreBillTo";

        Query query=sessionFactory.getCurrentSession().createQuery(sentencia);
        query.setParameter(0, fechaInicial);
        query.setParameter(1, fechaFinal);
        return query.list();
    }


    public List consultarGeneralOrdRecibidas(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado) {
        String sentencia;

        sentencia="SELECT " +
                  "m.Cod_BillTo, " +
                  "m.Nombre_BillTo, " +
                  "m.Cod_SoldTo, " +
                  "m.Nombre_SoldTo, " +
                  "COUNT(DISTINCT m.Cod_Maestro_Orden) AS Cant_Ordenes, " +
                  "SUM(d.Precio_Neto_Mars*d.Cant_Und_Venta) AS Valor_Ordenes, " +
                  "SUM(DECODE(d.Estado_Nuevo, 'EPR009', d.Precio_Neto_Mars*d.Cant_Und_Venta, 0)) AS OrdOK, " +
                  "SUM(DECODE(d.Estado_Nuevo, 'EPR009', 0, d.Precio_Neto_Mars*d.Cant_Und_Venta)) AS OrdErro " +
                  "FROM EDI_Maestro_Orden m, EDI_Detalle_Orden d " +
                  "WHERE m.Cod_Territorio='" + codTerritorio + "' " +
                  "AND m.Cod_Maestro_Orden=d.Cod_Maestro_Orden ";

        if (!codBillTo.equals("TODO")) {
            sentencia+="AND m.Cod_BillTo='" + codBillTo + "' ";
        }

        if (fechaInicial!=null && fechaFinal!=null) {
            sentencia+="AND TRUNC(m.Fecha_Actual) BETWEEN TRUNC(:fechaIni) AND TRUNC(:fechaFin) ";
        
            if (!listadoEstado.isEmpty()) {
                sentencia += "AND d.Estado_Original IN (" + listadoEstado + ") ";
            }
        } else {
            sentencia+="AND m.Envio_Fuerza_Venta='PT'";
        }
        sentencia+="GROUP BY m.Cod_BillTo, m.Nombre_BillTo, m.Cod_SoldTo, m.Nombre_SoldTo " +
                   "ORDER BY m.Nombre_BillTo";

        return ejecutarSQLQuery(sentencia, fechaInicial, fechaFinal);
    }


    public List consultarDetalleOrdRecibidas(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado) {
        String sentencia;

        sentencia="SELECT " +
                  "m.fechaActual, " +
                  "m.codBillTo, " +
                  "m.nombreBillTo, " +
                  "m.codSoldTo, " +
                  "m.nombreSoldTo, " +
                  "m.numOrden, " +
                  "m.fechaOrden, " +
                  "m.fechaEntrega, " +
                  "m.fechaEntregaNoAntes, " +
                  "d.codProducto, " +
                  "d.id.eanProducto, " +
                  "d.descripcionProducto, " +
                  "d.cantPLU, " +
                  "d.cantUndVenta, " +
                  "d.precioNetoCliente, " +
                  "d.precioNetoMars, " +
                  "d.precioNetoCliente*d.cantUndVenta, " +
                  "d.precioNetoMars*d.cantUndVenta, " +
                  "d.estadoOriginal.descripcion " +
                  
                  "FROM MaestroOrden m JOIN m.detalleOrden d " +
                  "WHERE m.codTerritorio='" + codTerritorio + "' ";

        if (!codBillTo.equals("TODO")) {
            sentencia+="AND m.codBillTo='" + codBillTo + "' ";
        }

        if (fechaInicial!=null && fechaFinal!=null) {
            sentencia+="AND TRUNC(m.fechaActual) BETWEEN TRUNC(?) AND TRUNC(?) ";
        
            if (!listadoEstado.isEmpty()) {
                sentencia+="AND d.estadoOriginal.codigo IN (" + listadoEstado + ")";
            }            
            Query query=sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, fechaInicial);
            query.setParameter(1, fechaFinal);
            return query.list();
        } else {
            sentencia+="AND m.envioFuerzaVenta='PT'";
            return sessionFactory.getCurrentSession().createQuery(sentencia).list();
        }

        
    }


    public int obtenerCantOrdenesRetenidas(String codTerritorio)  throws Exception {
        String sentencia;
        Calendar fechaActual;
        Query query;
        
         try {

             fechaActual=Calendar.getInstance();
             fechaActual.add(Calendar.MONTH, -2); //Dos meses hacia atras

            //Total de las ordenes con error en la fecha de entrega
            sentencia="SELECT " +
                              "COUNT(*) AS Cant_Ordenes " +
                              "FROM MaestroOrden mo " +
                              "WHERE mo.codTerritorio='" + codTerritorio + "' " +
                              "AND TRUNC(mo.fechaActual)>=? " +
                              "AND mo.estado.codigo='EOR004' " +
                              "AND EXISTS (SELECT 1 " +
                                          "FROM DetalleOrden do " +
                                          "WHERE do.id.maestroOrden.codMaestroOrden=mo.codMaestroOrden " +
                                          "AND do.estadoNuevo.codigo='EPR003'" +
                                          ")";

            query=sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, fechaActual.getTime());
            
            return Integer.parseInt(query.uniqueResult().toString());
        } catch (Exception ex) {
            throw new Exception("Error obteniendo el estado de avance->" + Utilidades.obtenerMsgError(ex));
        }
    }


    public void actualizarEstadoEnvioMail(String codTerritorio, String estadoActual, String estadoNuevo) throws Exception {
        String sentencia;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");

        try {
            //Se actualiza el campo que indica que el registro fue enviado por correo
            sentencia="UPDATE MaestroOrden m SET " +
                      "m.envioFuerzaVenta=" + estadoNuevo + " " +
                      "WHERE m.codTerritorio='" + codTerritorio + "' " +
                      "AND m.envioFuerzaVenta IN (" + estadoActual + ")";

            sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();

            //Si se trata de un cierre final se actualiza el parametro de cierre para parar los procesos batch
            //edi.dao.hibernate.
            if (estadoNuevo.equals("'SI'")) {
                sentencia="UPDATE Parametro p SET " +
                          "p.valorAlfa1='SI', " +
                          "p.valorAlfa2='" + sdf.format(new Date()) + "' " +
                          "WHERE p.codigo='CERRADO_" + codTerritorio + "'";

                sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();
            }
        } catch (Exception ex) {
           throw new Exception("Error actualizando estado envio->" + Utilidades.obtenerMsgError(ex));
        }
    }



    public List ejecutarSQLQuery(final String sentencia, final Date fechaInicial, final Date fechaFinal) {
        Query query;
        
        query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
        if (fechaInicial!=null && fechaFinal!=null) {
            query.setParameter("fechaIni", fechaInicial);
            query.setParameter("fechaFin", fechaFinal);
        }
        return query.list();
    }


    public String reprocesarOrden(String codMaestroOrden) throws Exception {
        boolean existeCliente, ordenProcesadaOK, tieneErrorCantDiasSolEnt, tieneSegmentoPAC;
        String sentencia, segmentoSoldTo, codSoldTo, codBillTo, codTerritorio, EANTemp,
               metodo, prefijoNumOrden, codSalesOrg, nombreSoldTo, nombreBillTo, 
               estadoFinalOrden, estadoFinalItem, resultado, nomArchivo, replicarDsctoCliente,
               esBloqueadoVenta;
        int codOrden, valorIntTemp, cantMaxDiasEntregaMin, cantMaxDiasEntregaMax, codLogOrden, secOrden, cocienteCercano,
            codLogOriginal;
        double valorDblTemp, difMaximaPrecio;
        Object valorObjTemp;
        Calendar fechaActual, fechaCarga;
        double[] totalOrden;
        String[] vectorCadena;
        Object[] registro;
        Field propiedad;
        DecimalFormat formateadoDecimal;
        Parametro parametro;
        MaestroOrden maestroOrden=null;
        DetalleOrden detalleOrden=null;
        DetalleOrdenId detalleOrdenId=null;
        Query query;
        ListaPrecio oPrecio;
        
        try {
            resultado="";
            fechaActual = Calendar.getInstance();
            fechaCarga=Calendar.getInstance();
            formateadoDecimal = new DecimalFormat("#.##");

            //Se descuenta la orden de la cantidad de ordenes asociadas al Log
            sentencia="UPDATE Log l SET " +
                      "l.totalOrdenes=l.totalOrdenes-1 " +
                      "WHERE l.codLog<>0 " +
                      "AND l.codLog=(SELECT m.log.codLog " +
                                    "FROM MaestroOrden m " +
                                    "WHERE m.codMaestroOrden='" + codMaestroOrden + "'" +
                                    ")";
            sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();

            //Se obtiene el nombre del archivo y secuencial de la orden para volver a cargarla
            maestroOrden=obtenerOrdenCompra(codMaestroOrden);

            nomArchivo=maestroOrden.getNomArchivo();
            secOrden=maestroOrden.getSecOrden();
            codTerritorio=maestroOrden.getCodTerritorio();
            codLogOriginal=maestroOrden.getLog().getCodLog();
            fechaCarga.setTime(maestroOrden.getFechaActual());
            fechaActual.setTime(maestroOrden.getFechaActual());
            maestroOrden=null;

            //Se borra la orden de las tablas principales
            sentencia="DELETE FROM DetalleOrden d " +
                      "WHERE d.id.maestroOrden.codMaestroOrden='" + codMaestroOrden + "'";
            sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();
            
            sentencia="DELETE FROM MaestroOrden m " +
                      "WHERE m.codMaestroOrden='" + codMaestroOrden + "'";
            sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();

            //Se extrae los tipos de segmento NAD que son SoldTo o ShipTo
            parametro = parametroDAO.obtener("SEGMENTOS_SOLDTO");
            segmentoSoldTo = parametro.getValorAlfa1();

            parametro = parametroDAO.obtener("DIAS_ENTREGA_MAX");
            cantMaxDiasEntregaMin=(int)parametro.getValorNum1();
            cantMaxDiasEntregaMax=(int)parametro.getValorNum2();


            parametro = parametroDAO.obtener("DIF_PRECIO_MAX_" + codTerritorio);
            difMaximaPrecio = parametro.getValorNum1();

            parametro=parametroDAO.obtener("REPLICAR_DESCUENTO_CLIENTE");
            replicarDsctoCliente="-" + parametro.getValorAlfa1() + "-";

            //Se borran los mensajes de información OK que esten registrados en la fecha actual
            sentencia = "DELETE FROM Log l " +
                        "WHERE TRUNC(l.fechaActual)=TRUNC(?) " +
                        "AND l.referencia='OK' " +
                        "AND l.codTerritorio='" + codTerritorio + "'";

            query=sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, fechaCarga.getTime());
            query.executeUpdate();

            //Se extrae el máximo código de referencia para asignar a la orden
            prefijoNumOrden = "A" + String.valueOf(fechaActual.get(Calendar.YEAR)).substring(2);
            codOrden = this.obtenerSecuenciaOrdenEDI();

            sessionFactory.getCurrentSession().clear(); //Se limpian los objetos que se encuentren en memoria
                        
            //Se asume que la orden esta correcta
            ordenProcesadaOK = true;

            //Se busca el territorio al que pertenece la orden con el EAN de MARS enviado por el proveedor
            sentencia = "SELECT p.valorNum1, p.valorAlfa2 " +
                        "FROM OrdenRecibida r, Parametro p " +
                        "WHERE r.id.nomArchivo='" + nomArchivo + "' " +
                        "AND r.id.secOrden=" + secOrden + " " +
                        "AND r.id.segmento='NAD' " +
                        "AND r.id.tipoSegmento='SU' " +
                        "AND r.id.secCampo=1 " +
                        "AND r.id.nivel='CABECERA' " +
                        "AND p.codigo LIKE 'EAN_MARS%' " +
                        "AND p.valorAlfa1=r.valorCampo";

            valorObjTemp = sessionFactory.getCurrentSession().createQuery(sentencia).uniqueResult();

            if (valorObjTemp == null) {
                //Se guarda el error como crítico
                ordenProcesadaOK = false;
                logDAO.guardar(new Log(-1, "SEG_NAD-SU", "ERROR", "-", fechaCarga.getTime(), "Archivo: " + nomArchivo + " / Sec_Orden: " + secOrden + " -> no contiene segmento NAD-SU", 1, "NO", "NO"), false);
                resultado="Archivo: " + nomArchivo + " / Sec_Orden: " + secOrden + " -> no contiene segmento NAD-SU";
            } else {
                codSalesOrg = String.valueOf(Math.round(Double.parseDouble(((Object[]) valorObjTemp)[0].toString())));
                codTerritorio = String.valueOf(((Object[]) valorObjTemp)[1]);

                //Se busca el SoldTo de ATLAS con el EAN de la orden
                sentencia = "SELECT r.valorCampo " +
                            "FROM OrdenRecibida r " +
                            "WHERE r.id.nomArchivo='" + nomArchivo + "' " +
                            "AND r.id.secOrden=" + secOrden + " " +
                            "AND r.id.segmento='NAD' " +
                            "AND r.id.secCampo=1 " +
                            "AND r.id.nivel='CABECERA' " +
                            "AND '-" + segmentoSoldTo + "-' LIKE '%-' || r.id.tipoSegmento || '-%'";

                existeCliente = false;
                codSoldTo = "";
                nombreSoldTo = "";
                codBillTo = "";
                nombreBillTo = "";
                EANTemp = "";

                for (Iterator<String> clienteSoldTo = sessionFactory.getCurrentSession().createQuery(sentencia).list().iterator(); clienteSoldTo.hasNext();) {
                    EANTemp = clienteSoldTo.next();

                    vectorCadena = obtenerInfoCliente(EANTemp);
                    if (vectorCadena != null) {
                        existeCliente = true;
                        codSoldTo = vectorCadena[0];
                        nombreSoldTo = vectorCadena[1];
                        codBillTo = vectorCadena[2];
                        nombreBillTo = vectorCadena[3];
                    }
                }

                //Se crea el objeto de la orden de compra
                maestroOrden = new MaestroOrden();
                codOrden++; //Se incrementa el codigo de maestro orden que se va a asignar
                maestroOrden.setCodMaestroOrden(prefijoNumOrden + Utilidades.izquierdaRellenar(String.valueOf(codOrden), "0", 6));
                maestroOrden.setNomArchivo(nomArchivo);
                maestroOrden.setSecOrden(secOrden);
                maestroOrden.setCodTerritorio(codTerritorio);
                maestroOrden.setEanSoldTo(EANTemp);
                maestroOrden.setFechaActual(fechaCarga.getTime());
                codLogOrden = 0; //El codigo de log 0 indica que no tiene ningún error
                estadoFinalOrden = "EOR005"; //Se asume que la orden se encuentra correcta y esta pendiente por enviar a ATLAS

                if (!existeCliente) {
                    //No se encontró el SoldTo
                    codLogOrden = logDAO.guardar(new Log(-1, "EANSOLDTO_" + EANTemp, "ERROR", codTerritorio, fechaCarga.getTime(), "No se pudo encontrar el SoldTo relacionado al EAN " + EANTemp, 1, "SI", "NO"), true);
                    valorObjTemp = "0"; //Se toma la plantilla general ya que no se encontró el BillTo
                    estadoFinalOrden = "EOR004"; //Se marca la orden con error
                } else {
                    maestroOrden.setCodBillTo(codBillTo);
                    maestroOrden.setNombreBillTo(nombreBillTo);
                    maestroOrden.setCodSoldTo(codSoldTo);
                    maestroOrden.setNombreSoldTo(nombreSoldTo);

                    sentencia = "SELECT pcl.id.codPlantilla " +
                                "FROM PlantillaCliente pcl " +
                                "WHERE pcl.id.codCliente='" + codBillTo + "'";

                    valorObjTemp = sessionFactory.getCurrentSession().createQuery(sentencia).uniqueResult();
                    //Sino tiene asociado una plantilla se toma la universal (Cod=0)
                    if (valorObjTemp == null) {
                        valorObjTemp = "0";
                    }
                }


                //Se consulta con el BillTo que mapeo tiene asociado
                sentencia = "SELECT " +
                            "r.id.nomArchivo, " +
                            "pc.id.segmento, " +
                            "pc.id.tipoSegmento, " +
                            "pc.id.nivel, " +
                            "pc.operacion, " +
                            "r.valorCampo, " +
                            "pc.nomClase, " +
                            "pc.propiedadClase, " +
                            "pc.tipoDato," +
                            "',' || pc.noIncluir || ',' " +
                            "FROM PlantillaCampo pc, OrdenRecibida r " +
                            "WHERE pc.id.codPlantilla=" + valorObjTemp + " " +
                            "AND r.id.nomArchivo='" + nomArchivo + "' " +
                            "AND r.id.secOrden=" + secOrden + " " +
                            "AND pc.id.segmento=r.id.segmento " +
                            "AND pc.id.tipoSegmento=r.id.tipoSegmento " +
                            "AND r.id.nivel LIKE pc.id.nivel || '%' " +
                            "AND pc.id.secCampo=r.id.secCampo " +
                            "ORDER BY r.id.nivel, pc.prioridad";

                detalleOrden = null;
                detalleOrdenId = null;
                for (Iterator<Object[]> mapeoOrden = sessionFactory.getCurrentSession().createQuery(sentencia).list().iterator(); mapeoOrden.hasNext();) {
                    registro = mapeoOrden.next();

                    //se construye el metodo SET de esa propiedad
                    if (registro[6].toString().equals("MaestroOrden")) {
                        propiedad = MaestroOrden.class.getDeclaredField(registro[7].toString());
                        metodo = "set" + propiedad.getName().substring(0, 1).toUpperCase() + propiedad.getName().substring(1);
                        MaestroOrden.class.getDeclaredMethod(metodo, propiedad.getType()).invoke(maestroOrden, Utilidades.convertirTipoDato(registro[5].toString(), registro[8].toString()));

                    } else if (registro[9].toString().indexOf("," + maestroOrden.getTipoOrden() + ",") < 0) { //Si el segmento aplica para el tipo de orden que se está procesando
                        if (registro[6].toString().equals("DetalleOrdenId")) {
                            if (maestroOrden.getTipoOrden().equals("YB1")) { //Si es una orden CROSS_DOCKING
                                if (registro[7].toString().equals("eanProducto")) { //Si es una nueva linea
                                    detalleOrden = null;
                                }
                            } else { //Si es una orden NORMAL
                                if (detalleOrden != null) {
                                    maestroOrden.adicionarDetalle(detalleOrden);
                                    detalleOrden = null;
                                }
                            }

                            if (detalleOrdenId == null) {
                                detalleOrdenId = new DetalleOrdenId();
                            }


                            propiedad = DetalleOrdenId.class.getDeclaredField(registro[7].toString());
                            metodo = "set" + propiedad.getName().substring(0, 1).toUpperCase() + propiedad.getName().substring(1);
                            DetalleOrdenId.class.getDeclaredMethod(metodo, propiedad.getType()).invoke(detalleOrdenId, Utilidades.convertirTipoDato(registro[5].toString(), registro[8].toString()));

                            //Si es CROSS_DOCKING y estamos en el segmento del ShipToSec duplicamos el objeto
                            if (maestroOrden.getTipoOrden().equals("YB1") && registro[1].toString().equals("LOC")) {
                                //Se coloca la unidad de venta en Cero para borrar lo especificado en la cabecera
                                //Luego mas adelante se calcula con base en el divisor asociado al EAN del producto
                                detalleOrden.setCantUndVenta(0);
                                //Se crea una copia del objeto solo cambiando el EAN del Ship To Secundario
                                maestroOrden.adicionarDetalle(detalleOrden.copiaInstancia(detalleOrdenId.getEanShipToSec()));
                            }
                        } else if (registro[6].toString().equals("DetalleOrden")) {
                            if (detalleOrden == null) {
                                detalleOrden = new DetalleOrden();
                                detalleOrden.setId(detalleOrdenId);
                                detalleOrdenId = null;
                            }

                            propiedad = DetalleOrden.class.getDeclaredField(registro[7].toString());
                            metodo = propiedad.getName().substring(0, 1).toUpperCase() + propiedad.getName().substring(1);

                            //Se verifica si tiene algún operador para determinar que hacer
                            if (registro[4].equals("+")) {
                                valorObjTemp = DetalleOrden.class.getDeclaredMethod("get" + metodo).invoke(detalleOrden);
                                if (valorObjTemp != null) {
                                    registro[5] = valorObjTemp + ". " + registro[5];
                                }
                            }
                            DetalleOrden.class.getDeclaredMethod("set" + metodo, propiedad.getType()).invoke(detalleOrden, Utilidades.convertirTipoDato(registro[5].toString(), registro[8].toString()));
                        }
                    }
                }

                //Se verifica que no se haya cargado ese numero de orden para ese Sold_To
                sentencia = "SELECT COUNT(*) " +
                            "FROM MaestroOrden m " +
                            "WHERE m.numOrden='" + maestroOrden.getNumOrden() + "' " +
                            "AND m.eanSoldTo='" + maestroOrden.getEanSoldTo() + "'";

                if (Integer.parseInt(sessionFactory.getCurrentSession().createQuery(sentencia).uniqueResult().toString()) > 0) {
                    codOrden--;
                } else {
                    //Se adiciona el último producto de la orden si es una orden NORMAL
                    if (detalleOrden != null && !maestroOrden.getTipoOrden().equals("YB1")) {
                        maestroOrden.adicionarDetalle(detalleOrden);
                    }

                    //Si el tipo de orden=220 es normal de lo contrario es CrossDocking
                    if (maestroOrden.getTipoOrden().equals("YB1")) {
                        maestroOrden.setTipoOrden("CROSS_DOCKING");
                    } else {
                        maestroOrden.setTipoOrden("NORMAL");
                    }

                    //Se verifica que tenga la fecha de entrega y la max fecha de entrega
                    if (maestroOrden.getFechaEntregaNoAntes() == null) {
                        maestroOrden.setFechaEntregaNoAntes(maestroOrden.getFechaEntrega());
                    }

                    //Se verifica si la diferencia entre la fecha de entrega y del pedido supera lo maximo permitido
                    tieneErrorCantDiasSolEnt = false;
                    if (Utilidades.restarFechas(maestroOrden.getFechaOrden(), maestroOrden.getFechaEntrega()) > cantMaxDiasEntregaMax) {
                        tieneErrorCantDiasSolEnt = true;
                    }

                    //Se verifica si la diferencia entre la fecha actual y la fecha minima de entrega supera lo minimo permitido
                    if (Utilidades.restarFechas(maestroOrden.getFechaActual(), maestroOrden.getFechaEntregaNoAntes()) > cantMaxDiasEntregaMin) {
                        tieneErrorCantDiasSolEnt = true;
                    }

                    //Se busca el código del producto ATLAS relacionado al EAN
                    for (Iterator<DetalleOrden> iterDetalleOrden = maestroOrden.getDetalleOrden().iterator(); iterDetalleOrden.hasNext();) {
                        detalleOrden = iterDetalleOrden.next();
                        
                        //Se busca el código del producto asociado a ese EAN. La busqueda se inicia con los productos normales
                        //ordenados por la cant_unidades_interna y luego se procede con los master_cases
                        sentencia = "SELECT DISTINCT p.materialNumber, p.description, pd.cantUndInterna " +
                                    "FROM ProductoDisplay pd, Producto p " +
                                    "WHERE (pd.id.ean='" + detalleOrden.getId().getEanProducto() + "' " +
                                            "OR " +
                                            "LTRIM(pd.id.ean,'0')='" + Utilidades.IzqTrim(detalleOrden.getId().getEanProducto(), '0') + "' " +
                                            ") " +
                                    "AND pd.id.codProducto=p.materialNumber " +  
                                    "AND EXISTS (SELECT 1 " +
                                                "FROM ProductoSalesOrg ps " +
                                                "WHERE ps.id.codProducto=p.materialNumber " +
                                                "AND ps.id.codSalesOrg='" + codSalesOrg + "' " +
                                                "AND ps.id.codCanalDist='10' " +
                                                "AND ps.estado='20' " +
                                                ") " +
                                    "ORDER BY REPLACE(p.materialNumber, 'M', '0') DESC, pd.cantUndInterna DESC";

                        valorObjTemp = null;
                        cocienteCercano = 999999;
                        registro = null;
                        esBloqueadoVenta = "NO";
                        for (Iterator<Object[]> iterProducto = sessionFactory.getCurrentSession().createQuery(sentencia).list().iterator(); iterProducto.hasNext();) {
                            registro = iterProducto.next();

                            valorIntTemp = detalleOrden.getCantPLU() % Integer.parseInt(registro[2].toString());
                            if ((valorIntTemp) < cocienteCercano) {
                                valorObjTemp = registro;
                                cocienteCercano = valorIntTemp;
                            }
                        }

                        //Si no se encontró se realiza el proceso para los que están inactivos
                        if (valorObjTemp == null) {
                            sentencia = "SELECT DISTINCT p.materialNumber, p.description, pd.cantUndInterna " +
                                        "FROM ProductoDisplay pd, Producto p " +
                                        "WHERE (pd.id.ean='" + detalleOrden.getId().getEanProducto() + "' " +
                                            "OR " +
                                            "LTRIM(pd.id.ean,'0')='" + Utilidades.IzqTrim(detalleOrden.getId().getEanProducto(), '0') + "' " +
                                            ") " +
                                        "AND pd.id.codProducto=p.materialNumber " +  
                                        "AND EXISTS (SELECT 1 " +
                                                    "FROM ProductoSalesOrg ps " +
                                                    "WHERE ps.id.codProducto=p.materialNumber " +
                                                    "AND ps.id.codSalesOrg='" + codSalesOrg + "' " +
                                                    "AND ps.id.codCanalDist='10' " +
                                                    ") " +
                                        "ORDER BY REPLACE(p.materialNumber, 'M', '0') DESC, pd.cantUndInterna DESC";

                            esBloqueadoVenta = "SI";
                            for (Iterator<Object[]> iterProducto=sessionFactory.getCurrentSession().createQuery(sentencia).iterate(); iterProducto.hasNext();) {
                                registro=iterProducto.next();

                                valorIntTemp=detalleOrden.getCantPLU() % Integer.parseInt(registro[2].toString());
                                if (valorIntTemp<cocienteCercano) {
                                    valorObjTemp=registro;
                                    cocienteCercano=valorIntTemp;                                        
                                }
                            }
                            
                            //Si no se encuentra el codigo del producto se busca en la tabla de mantenimiento manual
                            if (valorObjTemp == null) {    
                                esBloqueadoVenta = "NO";
                                //Si no se encuentra el codigo del producto se busca en la tabla de mantenimiento
                                sentencia = "SELECT DISTINCT pe.id.codProducto, pe.descripcion, pe.unidadesInternas " +
                                            "FROM ProductoEAN pe " +
                                            "WHERE pe.ean='" + detalleOrden.getId().getEanProducto() + "'";

                                valorObjTemp = sessionFactory.getCurrentSession().createQuery(sentencia).uniqueResult();
                            }                             
                        }
                        
                        //Si se encontró el producto se asigna al objeto
                        if (valorObjTemp != null) {
                            registro = (Object[]) valorObjTemp;
                            detalleOrden.setCodProducto(registro[0].toString());
                            detalleOrden.setDescripcionProducto(registro[1].toString());
                            detalleOrden.setCantUndInternas(Integer.parseInt(registro[2].toString()));

                            if (esBloqueadoVenta.equals("SI")) { //Si esta bloqueado para la venta se marca el error
                                detalleOrden.setEstadoOriginal(new Estado("EPR011", ""));
                            }
                        } else {
                            detalleOrden.setCodProducto("");
                            detalleOrden.setCantUndInternas(1);
                        }

                        //Se busca el código del ShipTo Secundario si la orden es de tipo CROSS_DOCKING
                        if (maestroOrden.getTipoOrden().equals("CROSS_DOCKING")) {
                            vectorCadena = obtenerInfoCliente(detalleOrden.getId().getEanShipToSec());
                            if (vectorCadena != null) {
                                detalleOrden.setCodShipToSec(vectorCadena[0]);
                                detalleOrden.setNombreShipToSec(vectorCadena[1]);
                            } else {
                                detalleOrden.setCodShipToSec("");
                            }
                        } else {
                            detalleOrden.setCodShipToSec("0");
                            detalleOrden.getId().setEanShipToSec("-");
                        }
                    }

                    //Se actualizan los precios y descuentos calculados anteriormente
                    totalOrden = new double[]{0, 0, 0, 0, 0, 0};
                    for (Iterator<DetalleOrden> iterDetalleOrden = maestroOrden.getDetalleOrden().iterator(); iterDetalleOrden.hasNext();) {
                        detalleOrden = iterDetalleOrden.next();
                        detalleOrden.setPrecioBrutoMars(0);
                        detalleOrden.setPorcDescMars(0);
                        detalleOrden.setPrecioNetoMars(0);

                        if (!maestroOrden.getCodSoldTo().isEmpty() && !detalleOrden.getCodProducto().isEmpty()) {
                            //Se busca el precio del producto
                            sentencia = "SELECT p " +
                                        "FROM ListaPrecio p " + 
                                        "WHERE p.id.codSoldTo = '" + maestroOrden.getCodSoldTo() + "' " +
                                        "AND p.id.codProducto = '" + detalleOrden.getCodProducto() + "'";

                            oPrecio = (ListaPrecio)sessionFactory.getCurrentSession().createQuery(sentencia).uniqueResult();

                            //Se asignan los valores del precio                                
                            if (oPrecio!=null) {
                                detalleOrden.setPrecioBrutoMars(oPrecio.getPrecioBruto());
                                detalleOrden.setPorcDescMars(oPrecio.getPorcentajeDescuento());
                                valorDblTemp = detalleOrden.getPrecioBrutoMars() * detalleOrden.getPorcDescMars() / 100;
                                detalleOrden.setPrecioNetoMars(Double.valueOf(formateadoDecimal.format(detalleOrden.getPrecioBrutoMars() - valorDblTemp)));
                            }
                        }
                                
                        estadoFinalItem = "";
                        //Se verifica si no encontró el codigo del producto asociado a ese EAN
                        if (detalleOrden.getCodProducto().isEmpty()) {
                            estadoFinalItem = "EPR005";
                            if (codLogOrden == 0) {
                                //Se guardar en el log el error encontrado si no se ha encontrado uno previamente
                                codLogOrden = logDAO.guardar(new Log(-1, "EANPROD_" + detalleOrden.getId().getEanProducto(), "ERROR", codTerritorio, fechaCarga.getTime(), "No se pudo encontrar el Producto relacionado al EAN " + detalleOrden.getId().getEanProducto(), 1, "SI", "NO"), true);
                            }
                        } else {
                            tieneSegmentoPAC = detalleOrden.getCantUndVenta() > 0;
                            //Se actualiza la información de unidades PLU y unidades de venta asi como el precio enviado por el cliente
                            if (detalleOrden.getCantPLU() == 0 && detalleOrden.getCantUndVenta() > 0) {
                                detalleOrden.setCantPLU((int) Math.rint(detalleOrden.getCantUndVenta() * detalleOrden.getCantUndInternas()));
                            } else if (detalleOrden.getCantUndVenta() == 0 && detalleOrden.getCantPLU() > 0) {
                                detalleOrden.setCantUndVenta((int) Math.rint(detalleOrden.getCantPLU() / detalleOrden.getCantUndInternas()));
                            }

                            //Se actualiza el precio final porque viene en PLU y se debe pasar a unidad de venta
                            detalleOrden.setPrecioBrutoCliente(detalleOrden.getPrecioBrutoCliente() * detalleOrden.getCantUndInternas());
                            detalleOrden.setPrecioNetoCliente(detalleOrden.getPrecioNetoCliente() * detalleOrden.getCantUndInternas());
                            DecimalFormat decf = new DecimalFormat("#####0.00");
                            if (detalleOrden.getPrecioBrutoCliente() > 0) {
                                detalleOrden.setPorcDescCliente(decf.parse(decf.format((detalleOrden.getPrecioBrutoCliente() - detalleOrden.getPrecioNetoCliente()) * 100 / detalleOrden.getPrecioBrutoCliente())).doubleValue());
                            }

                            //Si está parametrizado que ese BillTo se le debe aceptar cualquier descuento que el envíe
                            //se reemplazará el descuento definido por Mars por el que envía el cliente y se vuelve a calcular
                            //el precio neto con base al nuevo descuento
                            if (replicarDsctoCliente.indexOf("-" + maestroOrden.getCodBillTo() + "-") >= 0) {
                                detalleOrden.setPorcDescMars(detalleOrden.getPorcDescCliente());
                                valorDblTemp = detalleOrden.getPrecioBrutoMars() * detalleOrden.getPorcDescMars() / 100;
                                detalleOrden.setPrecioNetoMars(Double.valueOf(formateadoDecimal.format(detalleOrden.getPrecioBrutoMars() - valorDblTemp)));
                            }

                            //Se verifica si hay un estado previamente detectado
                            if (detalleOrden.getEstadoOriginal()!= null) {
                                //Se asigna ese error por prioridad
                                estadoFinalItem = detalleOrden.getEstadoOriginal().getCodigo();
                            } else if (detalleOrden.getCantPLU() < detalleOrden.getCantUndInternas()) {
                                //La cantidad de PLU solicitados es menor a la cantidad de productos internos
                                //que trae una unidad de venta
                                estadoFinalItem = "EPR006";
                            } else if (detalleOrden.getCantUndInternasCliente() != detalleOrden.getCantUndInternas() && tieneSegmentoPAC) {
                                //Si la cantidad de unidades internas que trae el producto es diferente al que
                                //especifica el ciente
                                estadoFinalItem = "EPR007";
                            } else if (Math.abs(detalleOrden.getPrecioBrutoCliente() - detalleOrden.getPrecioBrutoMars()) <= difMaximaPrecio &&
                                    !decf.format(detalleOrden.getPorcDescCliente()).equals(decf.format(detalleOrden.getPorcDescMars()))) {
                                //Se verifica si los precios brutos son iguales pero el descuento es diferente
                                estadoFinalItem = "EPR004";
                            } else if (Math.abs(detalleOrden.getPrecioNetoCliente() - detalleOrden.getPrecioNetoMars()) > difMaximaPrecio) {
                                //Se verifica si la diferencia entre los precios neto supera el maximo permitido
                                estadoFinalItem = "EPR008";
                            } else if (maestroOrden.getTipoOrden().equals("CROSS_DOCKING") && detalleOrden.getCodShipToSec().isEmpty()) {
                                //Se verifica si se encontró el EAN asociado al ShipToSec de la orden cross
                                estadoFinalItem = "EPR010";
                                if (codLogOrden == 0) {
                                    //Se guardar en el log el error encontrado si no se ha encontrado uno previamente
                                    codLogOrden = logDAO.guardar(new Log(-1, "EANSHIPTO_" + detalleOrden.getId().getEanShipToSec(), "ERROR", codTerritorio, fechaCarga.getTime(), "No se pudo encontrar el ShipTo secundario relacionado al EAN " + detalleOrden.getId().getEanShipToSec(), 1, "SI", "NO"), true);
                                }
                            } else if (tieneErrorCantDiasSolEnt) {
                                //Si el producto no tiene ningun error mas critico y hay diferencia entre la fecha de la orden
                                //y la fecha de entrega se marca el error
                                estadoFinalItem = "EPR003";
                            } else {
                                //No se encontró ningún error
                                estadoFinalItem = "EPR001";
                            }
                        }

                        //Se actualiza el estado del item
                        detalleOrden.setEstadoOriginal(new Estado(estadoFinalItem, ""));
                        if (!estadoFinalItem.equals("EPR001")) { //Se encontró algun error
                            estadoFinalOrden = "EOR004"; //Error
                        }

                        //Se suma al total de la orden los precios del cliente y de Mars
                        totalOrden[0] += detalleOrden.getCantUndVenta() * detalleOrden.getPrecioBrutoCliente();
                        totalOrden[1] += detalleOrden.getPorcDescCliente();
                        totalOrden[2] += detalleOrden.getCantUndVenta() * detalleOrden.getPrecioNetoCliente();

                        totalOrden[3] += detalleOrden.getCantUndVenta() * detalleOrden.getPrecioBrutoMars();
                        totalOrden[4] += detalleOrden.getPorcDescMars();
                        totalOrden[5] += detalleOrden.getCantUndVenta() * detalleOrden.getPrecioNetoMars();
                    }

                    //Se actualiza los totales a nivel de la orden
                    maestroOrden.setSubTotalCliente(totalOrden[0]);
                    maestroOrden.setPorcDescCliente(totalOrden[1] / maestroOrden.getDetalleOrden().size());
                    maestroOrden.setTotalCliente(totalOrden[2]);

                    maestroOrden.setSubTotalMars(totalOrden[3]);
                    maestroOrden.setPorcDescMars(totalOrden[4] / maestroOrden.getDetalleOrden().size());
                    maestroOrden.setTotalMars(totalOrden[5]);

                    //Se guarda la orden
                    maestroOrden.setEstado(new Estado(estadoFinalOrden, ""));
                    maestroOrden.setLog(new Log(codLogOrden));
                    sessionFactory.getCurrentSession().save(maestroOrden);
                }
            }

            //Se marcan los registros como actualizados
            sentencia = "UPDATE OrdenRecibida r SET " +
                        "r.procesado='" + (ordenProcesadaOK ? "SI" : "ER") + "' " +
                        "WHERE r.id.nomArchivo='" + nomArchivo + "' " +
                        "AND r.id.secOrden=" + secOrden;
            sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();

            //Se borra el log si la cantidad de ordenes afectadas es igual a cero
            if (codLogOriginal>0) {
                sentencia="DELETE FROM Log l " +
                          "WHERE l.codLog=" + codLogOriginal +
                          " AND l.totalOrdenes=0";
                sessionFactory.getCurrentSession().createQuery(sentencia).executeUpdate();
            }

            //Se verifica si no existe ningún mensaje de error en la fecha actual y que no se haya insertado
            //ningún mensaje de información de ordenes procesadas satisfactoriamente
            sentencia = "SELECT COUNT(*) " +
                        "FROM Log l " +
                        "WHERE l.codTerritorio='" + codTerritorio + "' " +
                        "AND TRUNC(l.fechaActual)=TRUNC(?)";

            query=sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, fechaCarga.getTime());            
            valorIntTemp=Utilidades.obtenerEntero(query.uniqueResult());

            if (valorIntTemp==0) {
                logDAO.guardar(new Log(-1, "OK", "INFO", codTerritorio, fechaCarga.getTime(), "Todas las ordenes fueron procesadas sin ningún inconveniente", 0, "NO", "SI"), false);
            }
        } catch (Exception ex) {
            throw new Exception("Error reprocesando orden->" + Utilidades.obtenerMsgError(ex));
        }
       
        return resultado;
    }



    //Devuelve la info del cliente relacionada con el EAN pasado por parametro
    //Pos 0: Cod_SoldTo
    //Pos 1: Nombre del SoldTo
    //Pos 3: Cod_BillTo
    //Pos 4: Nombre del BillTo
    private String[] obtenerInfoCliente(String ean) {
        String sentencia, codSoldTo = "", codBillTo = "", nombreSoldTo = "", nombreBillTo = "";
        Object valorObjTemp;
        Object[] registro;


        //Se verifica si aparece como SoldTo(Rol=AG) en ATLAS
        sentencia = "SELECT cr.codCliFuncion " +
                    "FROM ClienteRolFuncional cr " +
                    "WHERE LTRIM(cr.ean, '0')='" + Utilidades.IzqTrim(ean, '0') + "' " +
                    "AND cr.id.rol='AG'";


        codSoldTo = Utilidades.obtenerTexto(DataAccessUtils.uniqueResult(sessionFactory.getCurrentSession().createQuery(sentencia).list()));

        if (!codSoldTo.trim().isEmpty()) {
            //Se busca el nombre del cliente
            sentencia = "SELECT c.nombreSoldTo, c.codBillTo, c.nombreBillTo  " +
                        "FROM Cliente c " +
                        "WHERE c.codSoldTo='" + codSoldTo + "'";

            valorObjTemp = DataAccessUtils.uniqueResult(Utilidades.obtenerPrimerRegistro(sessionFactory.getCurrentSession().createQuery(sentencia).list()));
            if (valorObjTemp != null) {
                registro = (Object[]) valorObjTemp;
                nombreSoldTo = registro[0].toString();
                codBillTo = registro[1].toString();
                nombreBillTo = registro[2].toString();
            }
        } else {
            //Se verifica si aparece ese EAN en la tabla manual
            sentencia = "SELECT ce.codSoldTo, ce.nombreSoldTo, ce.codBillTo, ce.nombreBillTo " +
                        "FROM ClienteEAN ce " +
                        "WHERE ce.ean='" + ean + "'";

            valorObjTemp = DataAccessUtils.uniqueResult(sessionFactory.getCurrentSession().createQuery(sentencia).list());

            if (valorObjTemp != null) {
                registro = (Object[]) valorObjTemp;
                codSoldTo = registro[0].toString();
                nombreSoldTo = registro[1].toString();
                codBillTo = registro[2].toString();
                nombreBillTo = registro[3].toString();
            }
        }

        if (codSoldTo.isEmpty() || nombreSoldTo.isEmpty() || codBillTo.isEmpty() || nombreBillTo.isEmpty()) {
            return null;
        } else {
            return new String[]{codSoldTo, nombreSoldTo, codBillTo, nombreBillTo};
        }
    }


    public List listarBillTo(String codTerritorio) throws Exception {
        List resultado;
        String sentencia;

        try {
            sentencia="SELECT b " +
                      "FROM BillToEDI b " +
                      "WHERE b.codTerritorio='" + codTerritorio + "' " +
                      "ORDER BY b.nombreBillTo";

            resultado=sessionFactory.getCurrentSession().createQuery(sentencia).list();
        } catch (Exception ex) {
            throw new Exception("Error consultando los BillTo->" + Utilidades.obtenerMsgError(ex));
        }

        return resultado;
    }



    public int obtenerSecuenciaOrdenEDI() throws Exception {
        Query query;
        List arrayResultado;
        
        try {            
            //Se obtiene el texto de la interfaz generado
            query=sessionFactory.getCurrentSession().getNamedQuery("SP_ObtenerSecuenciaOrdenEDI");
            arrayResultado = query.list();
            return Integer.parseInt(arrayResultado.get(0).toString());
        } catch (Exception ex) {
            throw new Exception("Error obteniendo el próximo secuencial de ordenes->" + Utilidades.obtenerMsgError(ex));
        }
    }
}

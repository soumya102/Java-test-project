/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.IEstadoDAO;
import edi.dao.IOrdenCompraDAO;
import edi.dao.IParametroDAO;
import edi.dao.hibernate.DetalleOrden;
import edi.dao.hibernate.Estado;
import edi.dao.hibernate.MaestroOrden;
import edi.dao.hibernate.Parametro;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import mars.web.common.Utilidades;

/**
 *
 * @author alvarjor
 */
public class OrdenCompraServicio implements IOrdenCompraServicio { 

    private IEstadoDAO estadoDAO;
    private IOrdenCompraDAO ordenCompraDAO;
    private IParametroDAO parametroDAO;


    public void setEstadoDAO(IEstadoDAO estadoDAO) {
        this.estadoDAO = estadoDAO;
    }


    public void setOrdenCompraDAO(IOrdenCompraDAO ordenCompraDAO) {
        this.ordenCompraDAO = ordenCompraDAO;
    }


    public void setParametroDAO(IParametroDAO parametroDAO) {
        this.parametroDAO = parametroDAO;
    }
    

    
    
    
    
    
    public MaestroOrden obtenerOrdenCompra(String codMaestroOrden) throws Exception {
        return this.ordenCompraDAO.obtenerOrdenCompra(codMaestroOrden);
    }


    public ArrayList consultarEstadoOrden(String codTerritorio, Date fechaReporte) throws Exception {
        ArrayList resultado = new ArrayList();
        Object[] infoCliente;
        double infoOrdenes[];
        Estado estado;

        //Se extra el listado de clientes que enviaron ordenes en la fecha pasada por parámetro
        for (Iterator<Object[]> iterInfoCliente = ordenCompraDAO.listarClientesPorFecha(codTerritorio, fechaReporte).iterator(); iterInfoCliente.hasNext();)  {
            infoCliente=iterInfoCliente.next();

            //Se recorren todos los estados generales (Aplican a nivel de Orden)
            for (Iterator<Estado> iterEstado = estadoDAO.listar("EOR", true).iterator(); iterEstado.hasNext();)  {
                estado=iterEstado.next();
                infoOrdenes=ordenCompraDAO.obtenerTotalOrdenesPorClienteEstado(fechaReporte, infoCliente[0].toString(), estado.getCodigo(), "GENERAL");
                resultado.add(new String[]{infoCliente[0].toString(), infoCliente[1].toString(), estado.getCodigo(), String.valueOf(Math.round(infoOrdenes[0])), String.valueOf(infoOrdenes[1]), "GENERAL"});
            }

            //Se recorren todos los estados a nivel de detalle de la orden (Errores)
            for (Iterator<Estado> iterEstado = estadoDAO.listar("EPR", true).iterator(); iterEstado.hasNext();)  {
                estado=iterEstado.next();
                infoOrdenes=ordenCompraDAO.obtenerTotalOrdenesPorClienteEstado(fechaReporte, infoCliente[0].toString(), estado.getCodigo(), "DETALLE");
                resultado.add(new String[]{infoCliente[0].toString(), infoCliente[1].toString(), estado.getCodigo(), String.valueOf(Math.round(infoOrdenes[0])), String.valueOf(infoOrdenes[1]), "DETALLE"});
            }

            //Se adiciona el total de todo el cliente
            infoOrdenes=ordenCompraDAO.obtenerTotalOrdenesPorClienteEstado(fechaReporte, infoCliente[0].toString(), "", "CLIENTE");
            resultado.add(new String[]{infoCliente[0].toString(), infoCliente[1].toString(), "", String.valueOf(Math.round(infoOrdenes[0])), String.valueOf(infoOrdenes[1]), "CLIENTE"});
        }

        return resultado;
    }


    public ArrayList consultarResumenEstadoOrden(String codTerritorio, Date fechaReporte) throws Exception {
        ArrayList resultado = new ArrayList();
        Object[] infoCliente;
        double infoOrdenes[];
        Estado estado;

        //Se extra el listado de clientes que enviaron ordenes en la fecha pasada por parámetro
        for (Iterator<Object[]> iterInfoCliente = ordenCompraDAO.listarClientesPorFecha(codTerritorio, fechaReporte).iterator(); iterInfoCliente.hasNext();)  {
            infoCliente=iterInfoCliente.next();

            //Se recorren todos los estados a nivel de detalle de la orden (Errores)
            for (Iterator<Estado> iterEstado = estadoDAO.listar("EPR", true).iterator(); iterEstado.hasNext();)  {
                estado=iterEstado.next();
                infoOrdenes=ordenCompraDAO.obtenerTotalOrdenesPorClienteEstado(fechaReporte, infoCliente[0].toString(), estado.getCodigo(), "DETALLE");
                resultado.add(new String[]{infoCliente[0].toString(), infoCliente[1].toString(), estado.getCodigo(), String.valueOf(Math.round(infoOrdenes[0])), String.valueOf(infoOrdenes[1]), "DETALLE"});
            }

            //Se adiciona el total de todo el cliente
            infoOrdenes=ordenCompraDAO.obtenerTotalOrdenesPorClienteEstado(fechaReporte, infoCliente[0].toString(), "", "TOTAL_ERROR");
            resultado.add(new String[]{infoCliente[0].toString(), infoCliente[1].toString(), "", String.valueOf(Math.round(infoOrdenes[0])), String.valueOf(infoOrdenes[1]), "CLIENTE"});
        }

        return resultado;
    }


    public List consultarOrdenes(String codTerritorio, String operadorFecha, Date fechaReporte, String numOrden, String codCliente, String codError, String tipoError) throws Exception {
        return this.ordenCompraDAO.consultarOrdenes(codTerritorio, operadorFecha, fechaReporte, numOrden.replace("*", "%"), codCliente, codError, tipoError);
    }


    public int obtenerEstadoAvance(String codTerritorio, Date fechaReporte) throws Exception {
        return this.ordenCompraDAO.obtenerEstadoAvance(codTerritorio, fechaReporte);
    }


    public String cancelarOrdenes(MaestroOrden[] listadoOrdenes) {
        String resultado;
        MaestroOrden maestroOrden;

        resultado="";
        for (int i=0; i<listadoOrdenes.length; i++) {
            maestroOrden=listadoOrdenes[i];

            if (!maestroOrden.getEstado().getCodigo().equals("EOR004")) {
                resultado+=maestroOrden.getNumOrden() + ", ";
            }
        }

        if (resultado.isEmpty()) {
            resultado=ordenCompraDAO.cancelarOrdenes(listadoOrdenes);
        } else {
            resultado=resultado.substring(0, resultado.lastIndexOf(","));
            resultado="Las siguientes ordenes de compra no se pueden cancelar porque no contienen ningún error: " + resultado;
        }

        return resultado;
    }
    
    
    /*public String actualizarBanderaProcesamiento(boolean bloquear) {
        String resultado;
        Parametro parametro;
        
        try {
            resultado="";
            if (bloquear) {
                //Se verifica si el proceso se está ejecutando actualmente
                parametro=parametroDAO.obtener("EJECUTANDO_PROC_ORDEN");

                if (parametro.getValorAlfa1().equals("SI")) {
                    resultado="Actualmente se están procesando unas ordenes por favor espere...<br>Hora Inicio Proceso:" + parametro.getValorAlfa2();
                }
                
                //Se cambia el valor del parametro a SI
                parametro.setValorAlfa1("SI"); parametro.setValorAlfa2(new Date().toString());
            } else {
                //Se cambia el valor del parametro a NO
                parametro=new Parametro("EJECUTANDO_PROC_ORDEN", 0, 0, "NO", "");
            }
            
            //Se guarda el parametro
            parametroDAO.guardar(parametro);
        } catch (Exception ex) {
            resultado = "Error en el servicio de bloquear proceso->" + Utilidades.obtenerMsgError(ex);
        }

        return resultado;
    }*/


    public String reprocesarOrdenes(MaestroOrden[] listadoOrdenes) throws Exception {
        String resultado, resultadoTmp;
        MaestroOrden maestroOrden;

        try {
            resultado="";
            for (int i=0; i<listadoOrdenes.length; i++) {
                maestroOrden=listadoOrdenes[i];

                if (!maestroOrden.getEstado().getCodigo().equals("EOR003") && !maestroOrden.getEstado().getCodigo().equals("EOR004")) {
                    resultado+=maestroOrden.getNumOrden() + ", ";
                }
            }

            if (resultado.isEmpty()) {
                for (int i = 0; i < listadoOrdenes.length; i++) {
                    maestroOrden = (MaestroOrden) listadoOrdenes[i];
                    resultadoTmp = ordenCompraDAO.reprocesarOrden(maestroOrden.getCodMaestroOrden());

                    if (!resultadoTmp.isEmpty()) {
                        resultado += "- No Orden " + maestroOrden.getNumOrden() + ": " + resultadoTmp + "<br>";
                    }
                }

                if (!resultado.isEmpty()) {                    
                    resultado = "Se presentaron problemas al reprocesar las ordenes: <br>" + resultado;
                }
            } else {
                resultado = resultado.substring(0, resultado.lastIndexOf(","));
                resultado = "Las siguientes ordenes de compra no se pueden reprocesar porque no contienen errores: " + resultado;
            }
        } catch (Exception ex) {
            throw ex;
        }

        return resultado;
    }


    public String reprocesarOrdLog(int codLog) throws Exception {
        ArrayList listadoOrdenes;

        listadoOrdenes=new ArrayList();
        for (Iterator<MaestroOrden> iterMstOrden=ordenCompraDAO.listarOrdenesLog(codLog).iterator(); iterMstOrden.hasNext();) {
            listadoOrdenes.add(iterMstOrden.next());
        }

        return this.reprocesarOrdenes((MaestroOrden[])listadoOrdenes.toArray(new MaestroOrden[0]));
    }


    public String forzarPosteo(MaestroOrden[] listadoOrdenes) {
        String resultOrden, resultDetalle, resultado;
        MaestroOrden maestroOrden;

        resultOrden="";
        resultDetalle="";
        resultado="OK";
        for (int i=0; i<listadoOrdenes.length; i++) {
            maestroOrden=listadoOrdenes[i];

            if (!maestroOrden.getEstado().getCodigo().equals("EOR004")) {
                resultOrden+=maestroOrden.getNumOrden() + ", ";
            } else {
                if (!ordenCompraDAO.puedeforzarPosteo(maestroOrden.getCodMaestroOrden())) {
                    resultDetalle+=maestroOrden.getNumOrden() + ", ";
                }
            }
        }

        if (resultOrden.isEmpty() && resultDetalle.isEmpty()) {
            resultOrden=ordenCompraDAO.forzarPosteo(listadoOrdenes);
        } else {
            resultado="";
            if (!resultOrden.isEmpty()) {
                resultOrden=resultOrden.substring(0, resultOrden.lastIndexOf(","));
                resultado+="Las siguientes ordenes de compra no se pueden enviar porque ya fueron procesadas: " + resultOrden + ".";
            }

            if (!resultDetalle.isEmpty()) {
                resultDetalle=resultDetalle.substring(0, resultDetalle.lastIndexOf(","));
                resultado+="<br>Las siguientes ordenes de compra no se pueden enviar porque contienen errores que no se pueden obviar: " + resultDetalle + ".";
            }
        }

        return resultado;
    }


    public String resetearEstadoItem(DetalleOrden[] listadoItems) {
        String resultado;
        DetalleOrden detalleOrden;
        
        resultado="";
        for (int i=0; i<listadoItems.length; i++) {
            detalleOrden=listadoItems[i];

            if (!detalleOrden.getEstadoNuevo().getCodigo().equals("EPR002")) {
                resultado+=detalleOrden.getId().getNumLinea() + ", ";
            }
        }

        if (resultado.isEmpty()) {
            resultado=ordenCompraDAO.resetearEstadoItem(listadoItems);
        } else {
            resultado=resultado.substring(0, resultado.lastIndexOf(","));
            resultado="Los siguientes items no se pueden resetear porque no tienen un estado anterior: " + resultado;
        }

        return resultado;
    }


     public String cancelarItem(DetalleOrden[] listadoItems) {
         String resultado;
         DetalleOrden detalleOrden;

         resultado="";
         for (int i=0; i<listadoItems.length; i++) {
             detalleOrden=listadoItems[i];

             if (detalleOrden.getEstadoNuevo().getCodigo().equals("EPR001") || detalleOrden.getEstadoNuevo().getCodigo().equals("EPR002") ||
                 detalleOrden.getEstadoNuevo().getCodigo().equals("EPR009")) {
                 resultado+=detalleOrden.getId().getNumLinea() + ", ";
             }
         }

         if (resultado.isEmpty()) {
             resultado=ordenCompraDAO.cancelarItem(listadoItems);
         } else {
             resultado=resultado.substring(0, resultado.lastIndexOf(","));
             resultado="Los siguientes items no se pueden cancelar porque no tienen ningún error: " + resultado;
         }

         return resultado;
     }


     public List consultarCaseFill(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado) {
         return this.ordenCompraDAO.consultarCaseFill(codTerritorio, codBillTo, fechaInicial, fechaFinal, listadoEstado);
     }


     public List consultarGeneralOrdRecibidas(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado) {
         return this.ordenCompraDAO.consultarGeneralOrdRecibidas(codTerritorio, codBillTo, fechaInicial, fechaFinal, listadoEstado);
     }


     public List consultarDetalleOrdRecibidas(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado) {
         return this.ordenCompraDAO.consultarDetalleOrdRecibidas(codTerritorio, codBillTo, fechaInicial, fechaFinal, listadoEstado);
     }


     public int obtenerCantOrdenesRetenidas(String codTerritorio) throws Exception {
         return this.ordenCompraDAO.obtenerCantOrdenesRetenidas(codTerritorio);
     }


     public void actualizarEstadoEnvioMail(String codTerritorio, String estadoActual, String estadoNuevo) throws Exception {
         this.ordenCompraDAO.actualizarEstadoEnvioMail(codTerritorio, estadoActual, estadoNuevo);
     }


     public List listarBillTo(String codTerritorio) throws Exception {
         return this.ordenCompraDAO.listarBillTo(codTerritorio);
     }
}

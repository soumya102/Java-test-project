/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.Log;
import java.util.Date;
import java.util.List;
import mars.web.common.Utilidades;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.dao.support.DataAccessUtils;
/**
 *
 * @author alvarjor
 */
public class LogDAO implements ILogDAO {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public int guardar(Log log, boolean verificarExiste) {
        String sentencia;
        Log logTmp=null;

        if (verificarExiste && !log.getReferencia().isEmpty()) {
            sentencia = "SELECT lg " +
                        "FROM Log lg " +
                        "WHERE lg.codTerritorio='" + log.getCodTerritorio() + "' " +
                        "AND TRUNC(lg.fechaActual)=TRUNC(?)" +
                        "AND lg.tipo='" + log.getTipo() + "' " +
                        "AND lg.referencia='" + log.getReferencia() + "' " +
                        "AND lg.corregido='NO'";

            Query query=sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, log.getFechaActual());            
            logTmp = (Log)query.uniqueResult();
        }

        if (logTmp == null) {
            logTmp=log;
            logTmp.setMensaje(log.getMensaje().length() > 1000 ? log.getMensaje().substring(0, 1000) : log.getMensaje());
        } else {
            logTmp.setTotalOrdenes(logTmp.getTotalOrdenes() + 1);
        }

        sessionFactory.getCurrentSession().saveOrUpdate(logTmp);

        return logTmp.getCodLog();
    }

    
    public List listar(String codTerritorio, Date fechaReporte) throws Exception {
        List resultado;
        String sentencia;

        try {
            sentencia="SELECT l " +
                      "FROM Log l " +
                      "WHERE l.codTerritorio='" + codTerritorio + "' " +
                      "AND TRUNC(l.fechaActual)=? " +
                      "ORDER BY l.tipo";

            Query query=sessionFactory.getCurrentSession().createQuery(sentencia);
            query.setParameter(0, fechaReporte);
            resultado=query.list();
        } catch (Exception ex) {
            throw new Exception("Error consultando el log por fecha->" + Utilidades.obtenerMsgError(ex));
        }

        return resultado;
    }
}

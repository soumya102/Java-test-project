/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.Estado;
import java.util.List;
import org.hibernate.SessionFactory;
/**
 *
 * @author alvarjor
 */
public class EstadoDAO implements IEstadoDAO {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public String guardar(Estado estado) {
        String resultado="OK";

        sessionFactory.getCurrentSession().save(estado);

        return resultado;
    }


    public Estado obtener(String codEstado) {
        return (Estado)sessionFactory.getCurrentSession().get(Estado.class, codEstado);
    }


    public List<Estado> listar(String prefijo, boolean incluirSoloOrdenados) {
        String sentencia;

        sentencia="FROM Estado e " +
                  "WHERE e.codigo LIKE '" + prefijo + "%'";

        if (incluirSoloOrdenados) {
            sentencia+=" AND e.orden>0 " +
                       "ORDER BY e.orden";
        } else {
            sentencia+="ORDER BY e.descripcionCorta";
        }

        return sessionFactory.getCurrentSession().createQuery(sentencia).list();
    }



}
